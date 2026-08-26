package com.platform.ai.navigation.application;

import com.platform.ai.navigation.domain.AiInteractionMode;
import com.platform.ai.navigation.domain.AiNavigationFailureException;
import com.platform.ai.navigation.domain.NavigationContext;
import com.platform.ai.navigation.domain.NavigationDebugExecution;
import com.platform.ai.navigation.domain.NavigationDebugKnowledgeMatch;
import com.platform.ai.navigation.domain.NavigationDebugModelCall;
import com.platform.ai.navigation.domain.NavigationDebugRetrieval;
import com.platform.ai.navigation.domain.NavigationDebugTrace;
import com.platform.ai.navigation.domain.NavigationLanguageModel;
import com.platform.ai.navigation.domain.NavigationPlan;
import com.platform.ai.navigation.domain.NavigationPlanExecutionException;
import com.platform.ai.navigation.domain.NavigationResult;
import com.platform.ai.retrieval.application.KnowledgeRetrievalService;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AiNavigationService {

    private final KnowledgeRetrievalService retrievalService;
    private final NavigationLanguageModel languageModel;
    private final NavigationPlanExecutor planExecutor;
    private final int retrievalLimit;

    public AiNavigationService(
        KnowledgeRetrievalService retrievalService,
        NavigationLanguageModel languageModel,
        NavigationPlanExecutor planExecutor,
        @Value("${app.ai.agent.retrieval-limit:5}") int retrievalLimit
    ) {
        this.retrievalService = retrievalService;
        this.languageModel = languageModel;
        this.planExecutor = planExecutor;
        this.retrievalLimit = retrievalLimit;
    }

    public NavigationResult navigate(
        AuthenticatedUserPrincipal principal,
        String authorizationHeader,
        String userQuery,
        String currentRoute,
        String conversationContext
    ) {
        String normalizedHistory = conversationContext == null ? "none" : conversationContext.trim();
        String retrievalQuery = normalizedHistory.equals("none") || normalizedHistory.isBlank()
            ? userQuery.trim()
            : userQuery.trim() + "\n\nRecent conversation:\n" + normalizedHistory;
        DebugSession debug = new DebugSession(
            retrievalQuery,
            currentRoute == null ? "" : currentRoute.trim()
        );
        KnowledgeRetrievalService.RetrievalResult knowledge = retrieve(retrievalQuery, debug);
        NavigationContext context = new NavigationContext(
            retrievalQuery,
            retrievalQuery,
            knowledge.context(),
            currentRoute == null ? "" : currentRoute.trim(),
            normalizedHistory.isBlank() ? "none" : normalizedHistory,
            principal
        );

        NavigationContext initialContext = context;
        NavigationPlan plan = modelCall(
            "Initial plan",
            debug,
            () -> languageModel.createPlan(initialContext)
        ).plan();
        if (plan.requiresMoreKnowledge()) {
            KnowledgeRetrievalService.RetrievalResult additionalKnowledge = retrieve(
                plan.additionalKnowledgeQuery(),
                debug
            );
            context = new NavigationContext(
                context.userQuery(),
                context.retrievalQuery(),
                context.knowledgeContext() + "\n\n--- ADDITIONAL KNOWLEDGE ---\n\n"
                    + additionalKnowledge.context(),
                context.currentRoute(),
                context.conversationContext(),
                context.principal()
            );
            NavigationContext enrichedContext = context;
            plan = modelCall(
                "Plan attempt 2",
                debug,
                () -> languageModel.createPlan(enrichedContext)
            ).plan();
            if (plan.requiresMoreKnowledge()) {
                throw failure(
                    422,
                    "AI could not resolve the required API or UI knowledge",
                    debug
                );
            }
        }

        NavigationResult result;
        try {
            result = execute("Initial execution", plan, context, authorizationHeader, debug);
        } catch (NavigationPlanExecutionException firstFailure) {
            NavigationPlan failedPlan = plan;
            NavigationContext repairContext = context;
            NavigationPlan repairedPlan = modelCall(
                "Repair plan",
                debug,
                () -> languageModel.repairPlan(repairContext, failedPlan, firstFailure)
            ).plan();
            if (repairedPlan.requiresMoreKnowledge()) throw failure(firstFailure, debug);
            try {
                result = execute(
                    "Repair execution",
                    repairedPlan,
                    context,
                    authorizationHeader,
                    debug
                );
            } catch (NavigationPlanExecutionException repairFailure) {
                throw failure(repairFailure, debug);
            }
        }
        result = complete(result, userQuery, debug);
        return new NavigationResult(
            result.mode(),
            result.route(),
            result.message(),
            result.answerContext(),
            result.apiCalls(),
            debug.snapshot()
        );
    }

    private KnowledgeRetrievalService.RetrievalResult retrieve(String query, DebugSession debug) {
        long started = System.nanoTime();
        KnowledgeRetrievalService.RetrievalResult result = retrievalService.retrieve(
            query,
            retrievalLimit
        );
        debug.retrievals.add(new NavigationDebugRetrieval(
            result.query(),
            elapsedMs(started),
            result.matches().size(),
            result.context().length(),
            result.matches().stream()
                .map(match -> new NavigationDebugKnowledgeMatch(
                    match.chunk().source().name(),
                    match.chunk().title(),
                    match.score()
                ))
                .toList()
        ));
        return result;
    }

    private com.platform.ai.navigation.domain.NavigationModelResult modelCall(
        String label,
        DebugSession debug,
        Supplier<com.platform.ai.navigation.domain.NavigationModelResult> call
    ) {
        long started = System.nanoTime();
        com.platform.ai.navigation.domain.NavigationModelResult result = call.get();
        debug.modelCalls.add(new NavigationDebugModelCall(label, elapsedMs(started), result.plan()));
        return result;
    }

    private NavigationResult execute(
        String label,
        NavigationPlan plan,
        NavigationContext context,
        String authorizationHeader,
        DebugSession debug
    ) {
        long started = System.nanoTime();
        try {
            NavigationResult result = planExecutor.execute(plan, context, authorizationHeader);
            debug.executions.add(new NavigationDebugExecution(
                label,
                elapsedMs(started),
                200,
                "Completed",
                result.apiCalls()
            ));
            return result;
        } catch (NavigationPlanExecutionException failure) {
            debug.executions.add(new NavigationDebugExecution(
                label,
                elapsedMs(started),
                failure.status(),
                failure.getMessage(),
                failure.apiCalls()
            ));
            throw failure;
        }
    }

    private NavigationResult complete(
        NavigationResult result,
        String userQuery,
        DebugSession debug
    ) {
        if (result.mode() != AiInteractionMode.ANSWER) return result;
        long started = System.nanoTime();
        String answer = languageModel.createAnswer(userQuery, result.answerContext()).answer();
        debug.modelCalls.add(new NavigationDebugModelCall(
            "Verified answer",
            elapsedMs(started),
            null
        ));
        return new NavigationResult(
            AiInteractionMode.ANSWER,
            "",
            answer,
            "",
            result.apiCalls(),
            null
        );
    }

    private AiNavigationFailureException failure(
        NavigationPlanExecutionException cause,
        DebugSession debug
    ) {
        return failure(cause.status(), cause.getMessage(), debug);
    }

    private AiNavigationFailureException failure(int status, String message, DebugSession debug) {
        return new AiNavigationFailureException(status, message, debug.snapshot());
    }

    private long elapsedMs(long startedNanos) {
        return (System.nanoTime() - startedNanos) / 1_000_000;
    }

    private static final class DebugSession {

        private final String query;
        private final String currentRoute;
        private final Instant startedAt = Instant.now();
        private final long startedNanos = System.nanoTime();
        private final List<NavigationDebugRetrieval> retrievals = new ArrayList<>();
        private final List<NavigationDebugModelCall> modelCalls = new ArrayList<>();
        private final List<NavigationDebugExecution> executions = new ArrayList<>();

        private DebugSession(String query, String currentRoute) {
            this.query = query;
            this.currentRoute = currentRoute;
        }

        private NavigationDebugTrace snapshot() {
            return new NavigationDebugTrace(
                query,
                currentRoute,
                startedAt.toString(),
                (System.nanoTime() - startedNanos) / 1_000_000,
                retrievals,
                modelCalls,
                executions
            );
        }
    }
}
