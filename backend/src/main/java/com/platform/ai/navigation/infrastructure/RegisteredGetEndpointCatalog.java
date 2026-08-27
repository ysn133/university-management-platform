package com.platform.ai.navigation.infrastructure;

import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.PathContainer;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;

@Component
public class RegisteredGetEndpointCatalog {

    private final RequestMappingHandlerMapping handlerMapping;

    public RegisteredGetEndpointCatalog(
        @Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping handlerMapping
    ) {
        this.handlerMapping = handlerMapping;
    }

    public boolean contains(String path) {
        PathContainer candidate = PathContainer.parsePath(path);
        return getPatterns().stream().anyMatch(pattern -> pattern.matches(candidate));
    }

    private List<PathPattern> getPatterns() {
        return handlerMapping.getHandlerMethods().keySet().stream()
            .filter(this::supportsGet)
            .filter(mapping -> mapping.getPathPatternsCondition() != null)
            .flatMap(mapping -> mapping.getPathPatternsCondition().getPatterns().stream())
            .filter(pattern -> pattern.getPatternString().startsWith("/api/v1/"))
            .filter(pattern -> !pattern.getPatternString().startsWith("/api/v1/ai/"))
            .filter(pattern -> !pattern.getPatternString().startsWith("/api/v1/auth/"))
            .toList();
    }

    private boolean supportsGet(RequestMappingInfo mapping) {
        return mapping.getMethodsCondition().getMethods().stream()
            .anyMatch(method -> method.asHttpMethod() == HttpMethod.GET);
    }
}
