package com.platform.ai.navigation;

import static org.assertj.core.api.Assertions.assertThat;

import com.platform.ai.navigation.application.AiNavigationService;
import com.platform.ai.navigation.domain.NavigationContext;
import com.platform.ai.navigation.domain.NavigationAnswerResult;
import com.platform.ai.navigation.domain.AiInteractionMode;
import com.platform.ai.navigation.domain.NavigationApiCall;
import com.platform.ai.navigation.domain.NavigationLanguageModel;
import com.platform.ai.navigation.domain.NavigationModelResult;
import com.platform.ai.navigation.domain.NavigationPlan;
import com.platform.ai.navigation.domain.NavigationPlanExecutionException;
import com.platform.ai.navigation.domain.NavigationResult;
import com.platform.ai.retrieval.application.KnowledgeRetrievalService;
import com.platform.ai.retrieval.domain.KnowledgeChunk;
import com.platform.ai.retrieval.domain.KnowledgeMatch;
import com.platform.ai.retrieval.domain.KnowledgeSource;
import com.platform.identityaccess.domain.AccountRoleType;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AiNavigationServiceTest {

    @Test
    void retrievesWithTheOriginalQuestionAndExecutesTheGeneratedPlan() {
        KnowledgeRetrievalService retrievalService = new KnowledgeRetrievalService(
            (query, source, limit) -> List.of(match(source, query))
        );
        RecordingLanguageModel languageModel = new RecordingLanguageModel();
        RecordingPlanExecutor planExecutor = new RecordingPlanExecutor();
        AiNavigationService service = new AiNavigationService(
            retrievalService,
            languageModel,
            planExecutor,
            3
        );
        AuthenticatedUserPrincipal principal = new AuthenticatedUserPrincipal(
            UUID.randomUUID(),
            AccountRoleType.ADMIN,
            UUID.randomUUID(),
            UUID.randomUUID(),
            "admin@uiz.ac.ma"
        );

        NavigationResult result = service.navigate(
            principal,
            "Bearer token",
            "Open Zakaria's grades",
            "/management/students",
            "none"
        );

        assertThat(result.route()).isEqualTo("/management/students/zakaria");
        assertThat(languageModel.context.retrievalQuery()).isEqualTo("Open Zakaria's grades");
        assertThat(languageModel.context.knowledgeContext()).contains("[API]", "[UI]");
        assertThat(planExecutor.authorizationHeader).isEqualTo("Bearer token");
        assertThat(result.diagnostics().retrievals()).hasSize(1);
        assertThat(result.diagnostics().retrievals().get(0).matches()).hasSize(2);
        assertThat(result.diagnostics().modelCalls()).extracting("label").containsExactly("Initial plan");
        assertThat(result.diagnostics().executions()).extracting("label").containsExactly("Initial execution");
    }

    @Test
    void repairsAPlanAfterAResponseDependentLookupFails() {
        KnowledgeRetrievalService retrievalService = new KnowledgeRetrievalService(
            (query, source, limit) -> List.of(match(source, query))
        );
        NavigationPlan initialPlan = new NavigationPlan(
            List.of(),
            "/management/students/{studentId}?tab=grades",
            "Opening Lina Idrissi's grades."
        );
        NavigationPlan repairedPlan = new NavigationPlan(
            List.of(),
            "/management/students/lina-id?tab=grades",
            "Opening Lina Idrissi's grades."
        );
        RepairingLanguageModel languageModel = new RepairingLanguageModel(
            initialPlan,
            repairedPlan
        );
        RepairingPlanExecutor planExecutor = new RepairingPlanExecutor();
        AiNavigationService service = new AiNavigationService(
            retrievalService,
            languageModel,
            planExecutor,
            3
        );

        NavigationResult result = service.navigate(
            principal(),
            "Bearer token",
            "open the grades of the student lina idrissi of the current academic year",
            "/management",
            "none"
        );

        assertThat(result.route()).isEqualTo("/management/students/lina-id?tab=grades");
        assertThat(planExecutor.attempts).isEqualTo(2);
        assertThat(result.apiCalls()).hasSize(1);
        assertThat(result.apiCalls().get(0).responsePreview()).contains("Lina", "Idrissi");
        assertThat(languageModel.failure.apiCalls()).hasSize(1);
        assertThat(result.diagnostics().modelCalls()).extracting("label")
            .containsExactly("Initial plan", "Repair plan");
        assertThat(result.diagnostics().executions()).extracting("label")
            .containsExactly("Initial execution", "Repair execution");
    }

    @Test
    void answersAQuestionFromVerifiedPlanResults() {
        KnowledgeRetrievalService retrievalService = new KnowledgeRetrievalService(
            (query, source, limit) -> List.of(match(source, query))
        );
        AnsweringLanguageModel languageModel = new AnsweringLanguageModel();
        AiNavigationService service = new AiNavigationService(
            retrievalService,
            languageModel,
            new AnswerPlanExecutor(),
            3
        );

        NavigationResult result = service.navigate(
            principal(),
            "Bearer token",
            "When does Yassine teach?",
            "/management",
            "none"
        );

        assertThat(result.mode()).isEqualTo(AiInteractionMode.ANSWER);
        assertThat(result.route()).isBlank();
        assertThat(result.message()).isEqualTo("Yassine teaches on Tuesday.");
        assertThat(languageModel.verifiedData).contains("Tuesday");
    }

    private KnowledgeMatch match(KnowledgeSource source, String query) {
        return new KnowledgeMatch(
            new KnowledgeChunk(source.name(), source, source + " knowledge", query),
            1.0
        );
    }

    private AuthenticatedUserPrincipal principal() {
        return new AuthenticatedUserPrincipal(
            UUID.randomUUID(),
            AccountRoleType.ADMIN,
            UUID.randomUUID(),
            UUID.randomUUID(),
            "admin@uiz.ac.ma"
        );
    }

    private static final class RecordingLanguageModel implements NavigationLanguageModel {

        private NavigationContext context;
        @Override
        public NavigationModelResult createPlan(NavigationContext context) {
            this.context = context;
            return modelResult(new NavigationPlan(
                List.of(),
                "/management/students/zakaria",
                "Opening grades."
            ));
        }

        @Override
        public NavigationModelResult repairPlan(
            NavigationContext context,
            NavigationPlan failedPlan,
            NavigationPlanExecutionException failure
        ) {
            throw new AssertionError("Repair should not be needed");
        }

        @Override
        public NavigationAnswerResult createAnswer(String question, String verifiedData) {
            throw new AssertionError("Answer should not be needed");
        }
    }

    private static final class RepairingLanguageModel implements NavigationLanguageModel {

        private final NavigationPlan initialPlan;
        private final NavigationPlan repairedPlan;
        private NavigationPlanExecutionException failure;

        private RepairingLanguageModel(NavigationPlan initialPlan, NavigationPlan repairedPlan) {
            this.initialPlan = initialPlan;
            this.repairedPlan = repairedPlan;
        }

        @Override
        public NavigationModelResult createPlan(NavigationContext context) {
            return modelResult(initialPlan);
        }

        @Override
        public NavigationModelResult repairPlan(
            NavigationContext context,
            NavigationPlan failedPlan,
            NavigationPlanExecutionException failure
        ) {
            this.failure = failure;
            return modelResult(repairedPlan);
        }

        @Override
        public NavigationAnswerResult createAnswer(String question, String verifiedData) {
            throw new AssertionError("Answer should not be needed");
        }
    }

    private static final class AnsweringLanguageModel implements NavigationLanguageModel {

        private String verifiedData;

        @Override
        public NavigationModelResult createPlan(NavigationContext context) {
            return modelResult(new NavigationPlan(
                AiInteractionMode.ANSWER,
                List.of(new com.platform.ai.navigation.domain.NavigationPlanStep(
                    "schedule",
                    "/api/v1/professors/me/schedule",
                    tools.jackson.databind.json.JsonMapper.builder().build().createObjectNode(),
                    "",
                    List.of()
                )),
                "",
                ""
            ));
        }

        @Override
        public NavigationModelResult repairPlan(
            NavigationContext context,
            NavigationPlan failedPlan,
            NavigationPlanExecutionException failure
        ) {
            throw new AssertionError("Repair should not be needed");
        }

        @Override
        public NavigationAnswerResult createAnswer(String question, String verifiedData) {
            this.verifiedData = verifiedData;
            return new NavigationAnswerResult("Yassine teaches on Tuesday.");
        }
    }

    private static final class AnswerPlanExecutor
        extends com.platform.ai.navigation.application.NavigationPlanExecutor {

        private AnswerPlanExecutor() {
            super(null, null, null, 12, 20);
        }

        @Override
        public NavigationResult execute(
            NavigationPlan plan,
            NavigationContext context,
            String authorizationHeader
        ) {
            return new NavigationResult(
                AiInteractionMode.ANSWER,
                "",
                "",
                "{\"schedule\":[{\"day\":\"Tuesday\"}]}",
                List.of()
            );
        }
    }

    private static final class RepairingPlanExecutor
        extends com.platform.ai.navigation.application.NavigationPlanExecutor {

        private int attempts;

        private RepairingPlanExecutor() {
            super(null, null, null, 12, 20);
        }

        @Override
        public NavigationResult execute(
            NavigationPlan plan,
            NavigationContext context,
            String authorizationHeader
        ) {
            attempts++;
            NavigationApiCall call = new NavigationApiCall(
                "/api/v1/establishments/establishment-id/students",
                "{\"query\":\"Lina Idrissi\"}",
                200,
                "[{\"id\":\"lina-id\",\"firstName\":\"Lina\",\"lastName\":\"Idrissi\"}]"
            );
            if (attempts == 1) {
                throw new NavigationPlanExecutionException(
                    404,
                    "The requested record could not be found",
                    List.of(call)
                );
            }
            return new NavigationResult(plan.route(), plan.message(), List.of(call));
        }
    }

    private static final class RecordingPlanExecutor
        extends com.platform.ai.navigation.application.NavigationPlanExecutor {

        private String authorizationHeader;

        private RecordingPlanExecutor() {
            super(null, null, null, 12, 20);
        }

        @Override
        public NavigationResult execute(
            NavigationPlan plan,
            NavigationContext context,
            String authorizationHeader
        ) {
            this.authorizationHeader = authorizationHeader;
            return new NavigationResult(plan.route(), plan.message(), 0);
        }
    }

    private static NavigationModelResult modelResult(NavigationPlan plan) {
        return new NavigationModelResult(plan);
    }
}
