package com.platform.ai.navigation.infrastructure;

import com.platform.ai.navigation.domain.NavigationContext;
import com.platform.ai.navigation.domain.NavigationAnswerResult;
import com.platform.ai.navigation.domain.AiInteractionMode;
import com.platform.ai.navigation.domain.NavigationLanguageModel;
import com.platform.ai.navigation.domain.NavigationModelResult;
import com.platform.ai.navigation.domain.NavigationPlan;
import com.platform.ai.navigation.domain.NavigationPlanExecutionException;
import com.platform.ai.navigation.domain.NavigationPlanMatch;
import com.platform.ai.navigation.domain.NavigationPlanMatch.MatchOperator;
import com.platform.ai.navigation.domain.NavigationPlanStep;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

@Component
public class DeepSeekNavigationLanguageModel implements NavigationLanguageModel {

    private final ObjectMapper objectMapper;
    private final RestClient deepSeekClient;
    private final String apiKey;
    private final String model;

    public DeepSeekNavigationLanguageModel(
        ObjectMapper objectMapper,
        @Value("${app.ai.deepseek.api-key:}") String apiKey,
        @Value("${app.ai.deepseek.base-url:https://api.deepseek.com}") String baseUrl,
        @Value("${app.ai.deepseek.model:deepseek-v4-pro}") String model
    ) {
        this.objectMapper = objectMapper;
        this.deepSeekClient = RestClient.builder().baseUrl(stripTrailingSlash(baseUrl)).build();
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override
    public NavigationModelResult createPlan(NavigationContext context) {
        ArrayNode messages = objectMapper.createArrayNode();
        addMessage(messages, "system", AiNavigationPrompts.NAVIGATION_PLANNER);
        addMessage(messages, "user", buildPlannerInput(context));

        ObjectNode request = baseRequest(messages);
        request.putObject("response_format").put("type", "json_object");
        Completion completion = complete(request);
        String content = requiredText(completion.message(), "content", "AI planner returned no content");
        return new NavigationModelResult(parsePlan(content));
    }

    @Override
    public NavigationModelResult repairPlan(
        NavigationContext context,
        NavigationPlan failedPlan,
        NavigationPlanExecutionException failure
    ) {
        ArrayNode messages = objectMapper.createArrayNode();
        addMessage(messages, "system", AiNavigationPrompts.NAVIGATION_PLANNER);
        addMessage(messages, "user", buildPlannerInput(context));
        addMessage(messages, "assistant", writeJson(failedPlan));
        addMessage(messages, "user", """
            The plan failed during deterministic execution.

            FAILURE
            %s

            COMPLETED API READS
            %s

            Return one corrected complete plan. Reuse successful response fields and correct the failed
            endpoint, query, field match, dependency, or route. Do not repeat the same failed plan.
            """.formatted(failure.getMessage(), writeJson(failure.apiCalls())));

        ObjectNode request = baseRequest(messages);
        request.putObject("response_format").put("type", "json_object");
        Completion completion = complete(request);
        String content = requiredText(
            completion.message(),
            "content",
            "AI repair planner returned no content"
        );
        return new NavigationModelResult(parsePlan(content));
    }

    @Override
    public NavigationAnswerResult createAnswer(String question, String verifiedData) {
        ArrayNode messages = objectMapper.createArrayNode();
        addMessage(messages, "system", AiNavigationPrompts.VERIFIED_DATA_ANSWERER);
        addMessage(messages, "user", """
            QUESTION
            %s

            VERIFIED API DATA
            %s
            """.formatted(question.trim(), verifiedData));

        ObjectNode request = baseRequest(messages);
        request.putObject("response_format").put("type", "json_object");
        Completion completion = complete(request);
        String content = requiredText(completion.message(), "content", "AI answerer returned no content");
        try {
            String answer = requiredText(
                objectMapper.readTree(content),
                "answer",
                "AI answerer returned no answer"
            );
            return new NavigationAnswerResult(answer);
        } catch (JacksonException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI returned an invalid answer");
        }
    }

    private NavigationPlan parsePlan(String content) {
        try {
            JsonNode root = objectMapper.readTree(content);
            if (!root.isObject()) {
                throw invalidPlan();
            }
            String additionalKnowledgeQuery = root.path("additionalKnowledgeQuery").asText("").trim();
            if (!additionalKnowledgeQuery.isBlank()) {
                return new NavigationPlan(
                    AiInteractionMode.NAVIGATE,
                    List.of(),
                    "",
                    "",
                    additionalKnowledgeQuery
                );
            }
            if (!root.path("steps").isArray()) {
                throw invalidPlan();
            }
            List<NavigationPlanStep> steps = new ArrayList<>();
            for (JsonNode step : root.path("steps")) {
                List<NavigationPlanMatch> matches = new ArrayList<>();
                if (step.path("matches").isArray()) {
                    for (JsonNode match : step.path("matches")) {
                        matches.add(new NavigationPlanMatch(
                            requiredText(match, "field", "AI plan match has no field"),
                            parseOperator(requiredText(match, "operator", "AI plan match has no operator")),
                            requiredText(match, "value", "AI plan match has no value")
                        ));
                    }
                }
                steps.add(new NavigationPlanStep(
                    requiredText(step, "id", "AI plan step has no ID"),
                    requiredText(step, "path", "AI plan step has no path"),
                    step.path("queryParameters").isObject()
                        ? step.path("queryParameters").deepCopy()
                        : objectMapper.createObjectNode(),
                    step.path("forEach").asText(""),
                    matches
                ));
            }
            return new NavigationPlan(
                parseMode(root.path("mode").asText("NAVIGATE")),
                steps,
                root.path("route").asText(""),
                root.path("message").asText(""),
                ""
            );
        } catch (JacksonException | IllegalArgumentException exception) {
            throw invalidPlan();
        }
    }

    private AiInteractionMode parseMode(String value) {
        try {
            return AiInteractionMode.valueOf(value);
        } catch (IllegalArgumentException exception) {
            throw invalidPlan();
        }
    }

    private MatchOperator parseOperator(String value) {
        try {
            return MatchOperator.valueOf(value);
        } catch (IllegalArgumentException exception) {
            throw invalidPlan();
        }
    }

    private String buildPlannerInput(NavigationContext context) {
        String establishmentId = context.principal().establishmentId() == null
            ? "none"
            : context.principal().establishmentId().toString();
        return """
            USER REQUEST
            %s

            CALLER CONTEXT
            role: %s
            establishmentId: %s
            currentRoute: %s

            RECENT CONVERSATION
            Use this only to resolve references and follow-up questions. The latest USER REQUEST remains authoritative.
            %s

            RETRIEVED KNOWLEDGE
            %s
            """.formatted(
            context.userQuery(),
            context.principal().role().name(),
            establishmentId,
            context.currentRoute().isBlank() ? "none" : context.currentRoute(),
            context.conversationContext(),
            context.knowledgeContext()
        );
    }

    private ObjectNode baseRequest(ArrayNode messages) {
        ensureConfigured();
        ObjectNode request = objectMapper.createObjectNode();
        request.put("model", model);
        request.set("messages", messages);
        request.putObject("thinking").put("type", "disabled");
        request.put("temperature", 0);
        return request;
    }

    private Completion complete(ObjectNode request) {
        try {
            JsonNode response = deepSeekClient.post()
                .uri("/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .body(request)
                .retrieve()
                .body(JsonNode.class);
            JsonNode message = response == null
                ? null
                : response.path("choices").path(0).path("message");
            if (message == null || message.isMissingNode()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI provider returned no message");
            }
            return new Completion(message);
        } catch (RestClientResponseException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI provider request failed");
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI provider is unavailable");
        }
    }

    private record Completion(JsonNode message) {
    }

    private String requiredText(JsonNode node, String field, String errorMessage) {
        String value = node.path(field).asText().trim();
        if (value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, errorMessage);
        }
        return value;
    }

    private void addMessage(ArrayNode messages, String role, String content) {
        messages.addObject().put("role", role).put("content", content);
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JacksonException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to encode AI diagnostics");
        }
    }

    private void ensureConfigured() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "AI provider is not configured");
        }
    }

    private ResponseStatusException invalidPlan() {
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI returned an invalid navigation plan");
    }

    private String stripTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
