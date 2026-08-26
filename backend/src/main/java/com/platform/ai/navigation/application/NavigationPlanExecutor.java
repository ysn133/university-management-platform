package com.platform.ai.navigation.application;

import com.platform.ai.navigation.domain.NavigationApiCall;
import com.platform.ai.navigation.domain.AiInteractionMode;
import com.platform.ai.navigation.domain.NavigationContext;
import com.platform.ai.navigation.domain.NavigationPlan;
import com.platform.ai.navigation.domain.NavigationPlanMatch;
import com.platform.ai.navigation.domain.NavigationPlanExecutionException;
import com.platform.ai.navigation.domain.NavigationPlanStep;
import com.platform.ai.navigation.domain.NavigationResult;
import com.platform.ai.navigation.domain.ReadOnlyApiGateway;
import com.platform.ai.navigation.domain.ReadOnlyApiGateway.ApiReadResult;
import com.platform.ai.navigation.infrastructure.NavigationRouteValidator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

@Service
public class NavigationPlanExecutor {

    private static final Pattern VARIABLE = Pattern.compile("\\{\\{([a-zA-Z][a-zA-Z0-9_.]*)}}");
    private static final Pattern STEP_ID = Pattern.compile("[a-z][a-zA-Z0-9_]{0,39}");

    private final ObjectMapper objectMapper;
    private final ReadOnlyApiGateway apiGateway;
    private final NavigationRouteValidator routeValidator;
    private final int maximumApiCalls;
    private final int maximumFanOut;
    private final int maximumAnswerContextCharacters;

    @Autowired
    public NavigationPlanExecutor(
        ObjectMapper objectMapper,
        ReadOnlyApiGateway apiGateway,
        NavigationRouteValidator routeValidator,
        @Value("${app.ai.agent.maximum-api-calls:12}") int maximumApiCalls,
        @Value("${app.ai.agent.maximum-fan-out:20}") int maximumFanOut,
        @Value("${app.ai.agent.maximum-answer-context-characters:20000}") int maximumAnswerContextCharacters
    ) {
        this.objectMapper = objectMapper;
        this.apiGateway = apiGateway;
        this.routeValidator = routeValidator;
        this.maximumApiCalls = maximumApiCalls;
        this.maximumFanOut = maximumFanOut;
        this.maximumAnswerContextCharacters = maximumAnswerContextCharacters;
    }

    public NavigationPlanExecutor(
        ObjectMapper objectMapper,
        ReadOnlyApiGateway apiGateway,
        NavigationRouteValidator routeValidator,
        int maximumApiCalls,
        int maximumFanOut
    ) {
        this(objectMapper, apiGateway, routeValidator, maximumApiCalls, maximumFanOut, 20000);
    }

    public NavigationResult execute(
        NavigationPlan plan,
        NavigationContext context,
        String authorizationHeader
    ) {
        List<NavigationApiCall> traces = new ArrayList<>();
        try {
            return executePlan(plan, context, authorizationHeader, traces);
        } catch (NavigationPlanExecutionException exception) {
            throw exception;
        } catch (ResponseStatusException exception) {
            String message = exception.getReason() == null
                ? "The navigation plan could not be completed"
                : exception.getReason();
            throw new NavigationPlanExecutionException(
                exception.getStatusCode().value(),
                message,
                traces
            );
        }
    }

    private NavigationResult executePlan(
        NavigationPlan plan,
        NavigationContext context,
        String authorizationHeader,
        List<NavigationApiCall> traces
    ) {
        validatePlan(plan);
        Map<String, StepResult> results = new LinkedHashMap<>();

        for (NavigationPlanStep step : plan.steps()) {
            if (results.containsKey(step.id())) {
                throw invalidPlan("AI plan contains a duplicate step ID");
            }
            List<JsonNode> fanOutItems = fanOutItems(step, results);
            List<JsonNode> combinedItems = new ArrayList<>();
            JsonNode singleRoot = null;

            for (JsonNode item : fanOutItems) {
                if (traces.size() >= maximumApiCalls) {
                    throw new ResponseStatusException(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        "AI navigation plan exceeded the read limit"
                    );
                }
                String path = resolveTemplate(step.path(), context, results, item);
                JsonNode query = resolveJson(step.queryParameters(), context, results, item);
                ApiReadResult response;
                try {
                    response = apiGateway.get(path, query, authorizationHeader);
                } catch (ResponseStatusException exception) {
                    traces.add(new NavigationApiCall(
                        path,
                        query == null ? "{}" : query.toString(),
                        exception.getStatusCode().value(),
                        exception.getReason() == null ? "API path rejected" : exception.getReason()
                    ));
                    throw exception;
                }
                traces.add(new NavigationApiCall(
                    path,
                    query == null ? "{}" : query.toString(),
                    response.status(),
                    preview(response.body())
                ));
                if (response.status() < 200 || response.status() >= 300) {
                    throw new ResponseStatusException(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        "A planned read could not be completed"
                    );
                }
                JsonNode root = parseBody(response.body());
                singleRoot = root;
                combinedItems.addAll(collectionItems(root));
            }

            List<JsonNode> matched = applyMatches(combinedItems, step.matches(), context, results);
            JsonNode selected = select(step, matched);
            results.put(step.id(), new StepResult(singleRoot, List.copyOf(matched), selected));
        }

        if (plan.mode() == AiInteractionMode.ANSWER) {
            return new NavigationResult(
                AiInteractionMode.ANSWER,
                "",
                "",
                answerContext(results),
                List.copyOf(traces)
            );
        }

        String route = routeValidator.validate(
            resolveTemplate(plan.route(), context, results, null),
            context.principal().role()
        );
        return new NavigationResult(route, plan.message().trim(), List.copyOf(traces));
    }

    private List<JsonNode> fanOutItems(
        NavigationPlanStep step,
        Map<String, StepResult> results
    ) {
        if (step.forEach().isBlank()) {
            return java.util.Collections.singletonList(null);
        }
        StepResult source = results.get(step.forEach());
        if (source == null) {
            throw invalidPlan("AI plan fan-out references an unresolved step");
        }
        if (source.items().isEmpty()) {
            throw unresolved("The requested record could not be found");
        }
        if (source.items().size() > maximumFanOut) {
            throw unresolved("The requested search is too broad");
        }
        return source.items();
    }

    private List<JsonNode> collectionItems(JsonNode root) {
        if (root.isArray()) {
            return arrayValues(root);
        }
        JsonNode content = root.path("content");
        if (content.isArray()) {
            return arrayValues(content);
        }
        return root.isObject() ? List.of(root) : List.of();
    }

    private List<JsonNode> arrayValues(JsonNode array) {
        List<JsonNode> values = new ArrayList<>();
        array.forEach(values::add);
        return values;
    }

    private List<JsonNode> applyMatches(
        List<JsonNode> candidates,
        List<NavigationPlanMatch> matches,
        NavigationContext context,
        Map<String, StepResult> results
    ) {
        if (matches.isEmpty()) {
            return candidates;
        }
        return candidates.stream()
            .filter(candidate -> matches.stream().allMatch(match -> matches(
                candidate,
                match,
                resolveTemplate(match.value(), context, results, null)
            )))
            .toList();
    }

    private boolean matches(JsonNode candidate, NavigationPlanMatch match, String expected) {
        JsonNode value = field(candidate, match.field());
        if (value.isMissingNode() || value.isNull()) {
            return false;
        }
        String actual = value.asText();
        return switch (match.operator()) {
            case EQUALS -> actual.equals(expected);
            case EQUALS_IGNORE_CASE -> actual.equalsIgnoreCase(expected);
            case CONTAINS_IGNORE_CASE -> actual.toLowerCase().contains(expected.toLowerCase());
        };
    }

    private JsonNode select(NavigationPlanStep step, List<JsonNode> matched) {
        if (!step.matches().isEmpty()) {
            if (matched.isEmpty()) {
                throw unresolved("The requested record could not be found");
            }
            if (matched.size() > 1) {
                throw unresolved("Several records match the request");
            }
            return matched.get(0);
        }
        return matched.size() == 1 ? matched.get(0) : null;
    }

    private String resolveTemplate(
        String template,
        NavigationContext context,
        Map<String, StepResult> results,
        JsonNode item
    ) {
        if (template == null) {
            return "";
        }
        Matcher matcher = VARIABLE.matcher(template);
        StringBuilder resolved = new StringBuilder();
        while (matcher.find()) {
            String value = resolveVariable(matcher.group(1), context, results, item);
            matcher.appendReplacement(resolved, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(resolved);
        return resolved.toString();
    }

    private String resolveVariable(
        String expression,
        NavigationContext context,
        Map<String, StepResult> results,
        JsonNode item
    ) {
        if (expression.equals("caller.establishmentId")) {
            if (context.principal().establishmentId() == null) {
                throw unresolved("The caller has no establishment context");
            }
            return context.principal().establishmentId().toString();
        }
        if (expression.equals("caller.roleEntityId")) {
            return context.principal().roleEntityId().toString();
        }
        if (expression.startsWith("item.")) {
            return requiredScalar(field(item, expression.substring(5)), expression);
        }

        int separator = expression.indexOf('.');
        if (separator < 1) {
            throw invalidPlan("AI plan contains an invalid variable");
        }
        StepResult result = results.get(expression.substring(0, separator));
        if (result == null || result.selected() == null) {
            throw unresolved("A required API result was ambiguous");
        }
        return requiredScalar(field(result.selected(), expression.substring(separator + 1)), expression);
    }

    private JsonNode resolveJson(
        JsonNode value,
        NavigationContext context,
        Map<String, StepResult> results,
        JsonNode item
    ) {
        if (value == null || value.isNull() || value.isMissingNode()) {
            return objectMapper.createObjectNode();
        }
        if (value.isTextual()) {
            return objectMapper.getNodeFactory().textNode(resolveTemplate(value.asText(), context, results, item));
        }
        if (value.isArray()) {
            var output = objectMapper.createArrayNode();
            value.forEach(child -> output.add(resolveJson(child, context, results, item)));
            return output;
        }
        if (value.isObject()) {
            ObjectNode output = objectMapper.createObjectNode();
            Iterator<Map.Entry<String, JsonNode>> fields = value.properties().iterator();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                output.set(entry.getKey(), resolveJson(entry.getValue(), context, results, item));
            }
            return output;
        }
        return value.deepCopy();
    }

    private JsonNode field(JsonNode node, String path) {
        if (node == null || path == null || path.isBlank()) {
            return objectMapper.missingNode();
        }
        JsonNode current = node;
        for (String segment : path.split("\\.")) {
            current = current.path(segment);
        }
        return current;
    }

    private String requiredScalar(JsonNode node, String expression) {
        if (node.isMissingNode() || node.isNull() || node.isObject() || node.isArray()) {
            throw unresolved("The API response did not contain " + expression);
        }
        return node.asText();
    }

    private JsonNode parseBody(String body) {
        try {
            return objectMapper.readTree(body == null || body.isBlank() ? "{}" : body);
        } catch (JacksonException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Internal API returned invalid JSON");
        }
    }

    private void validatePlan(NavigationPlan plan) {
        if (plan == null) {
            throw invalidPlan("AI returned an incomplete navigation plan");
        }
        if (plan.mode() == AiInteractionMode.NAVIGATE
            && (plan.route().isBlank() || plan.message().isBlank())) {
            throw invalidPlan("AI returned an incomplete navigation plan");
        }
        if (plan.mode() == AiInteractionMode.ANSWER
            && (!plan.route().isBlank() || !plan.message().isBlank() || plan.steps().isEmpty())) {
            throw invalidPlan("AI returned an incomplete answer plan");
        }
        if (plan.steps().size() > maximumApiCalls) {
            throw invalidPlan("AI navigation plan is too large");
        }
        for (NavigationPlanStep step : plan.steps()) {
            if (step.id() == null || !STEP_ID.matcher(step.id()).matches()
                || step.path() == null || step.path().isBlank()) {
                throw invalidPlan("AI returned an invalid navigation step");
            }
        }
    }

    private String answerContext(Map<String, StepResult> results) {
        ObjectNode answerData = objectMapper.createObjectNode();
        for (Map.Entry<String, StepResult> entry : results.entrySet()) {
            String stepId = entry.getKey();
            StepResult result = entry.getValue();
            if (result.selected() != null) {
                answerData.set(stepId, result.selected());
            } else {
                var items = objectMapper.createArrayNode();
                result.items().forEach(items::add);
                answerData.set(stepId, items);
            }
        }
        String serialized = answerData.toString();
        if (serialized.length() > maximumAnswerContextCharacters) {
            throw unresolved("The requested answer contains too much data");
        }
        return serialized;
    }

    private String preview(String body) {
        if (body == null || body.isBlank()) {
            return "";
        }
        return body.length() <= 1200 ? body : body.substring(0, 1200) + "...[truncated]";
    }

    private ResponseStatusException invalidPlan(String message) {
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY, message);
    }

    private ResponseStatusException unresolved(String message) {
        return new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, message);
    }

    private record StepResult(JsonNode root, List<JsonNode> items, JsonNode selected) {
    }
}
