package com.platform.ai.navigation.infrastructure;

import com.platform.ai.navigation.domain.ReadOnlyApiGateway;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;

@Component
public class InternalReadOnlyApiGateway implements ReadOnlyApiGateway {

    private static final Logger log = LoggerFactory.getLogger(InternalReadOnlyApiGateway.class);

    private final RegisteredGetEndpointCatalog endpointCatalog;
    private final RestClient restClient;
    private final String baseUrl;
    private final int maximumResponseCharacters;

    public InternalReadOnlyApiGateway(
        RegisteredGetEndpointCatalog endpointCatalog,
        @Value("${app.ai.agent.internal-api-base-url:http://localhost:8080}") String baseUrl,
        @Value("${app.ai.agent.maximum-api-response-characters:50000}") int maximumResponseCharacters
    ) {
        this.endpointCatalog = endpointCatalog;
        this.restClient = RestClient.create();
        this.baseUrl = stripTrailingSlash(baseUrl);
        this.maximumResponseCharacters = maximumResponseCharacters;
    }

    @Override
    public ApiReadResult get(String path, JsonNode queryParameters, String authorizationHeader) {
        String validatedPath = validatePath(path);
        URI uri = buildUri(validatedPath, queryParameters);
        try {
            String body = restClient.get()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .retrieve()
                .body(String.class);
            return new ApiReadResult(HttpStatus.OK.value(), truncate(body));
        } catch (RestClientResponseException exception) {
            return new ApiReadResult(
                exception.getStatusCode().value(),
                truncate(exception.getResponseBodyAsString())
            );
        } catch (RuntimeException exception) {
            throw new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "Internal API request failed"
            );
        }
    }

    private String validatePath(String path) {
        if (path == null || path.isBlank() || path.length() > 1500) {
            throw invalidPath(path);
        }
        if (!path.startsWith("/api/v1/") || path.contains("?") || path.contains("#") || path.contains("%")
            || path.contains("\\") || path.contains("\r") || path.contains("\n")) {
            throw invalidPath(path);
        }

        URI uri;
        try {
            uri = URI.create(path);
        } catch (IllegalArgumentException exception) {
            throw invalidPath(path);
        }
        String normalized = uri.normalize().getPath();
        if (!path.equals(normalized) || normalized.contains("..") || !endpointCatalog.contains(normalized)) {
            throw invalidPath(path);
        }
        return normalized;
    }

    private URI buildUri(String path, JsonNode queryParameters) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl).path(path);
        if (queryParameters != null && queryParameters.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = queryParameters.properties().iterator();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                JsonNode value = field.getValue();
                if (value.isArray()) {
                    value.forEach(item -> builder.queryParam(field.getKey(), item.asText()));
                } else if (!value.isNull()) {
                    builder.queryParam(field.getKey(), value.asText());
                }
            }
        }
        return builder.encode(StandardCharsets.UTF_8).build().toUri();
    }

    private String truncate(String body) {
        if (body == null) {
            return "";
        }
        if (body.length() <= maximumResponseCharacters) {
            return body;
        }
        return body.substring(0, maximumResponseCharacters) + "\n[response truncated]";
    }

    private String stripTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private ResponseStatusException invalidPath(String path) {
        String diagnosticPath = diagnosticPath(path);
        log.warn("AI requested an invalid read-only API path: {}", diagnosticPath);
        return new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "AI requested an invalid read-only API path: " + diagnosticPath
        );
    }

    private String diagnosticPath(String path) {
        if (path == null) {
            return "<null>";
        }
        String sanitized = path
            .replace('\r', ' ')
            .replace('\n', ' ')
            .replace('\t', ' ');
        return sanitized.length() <= 300 ? sanitized : sanitized.substring(0, 300) + "...";
    }
}
