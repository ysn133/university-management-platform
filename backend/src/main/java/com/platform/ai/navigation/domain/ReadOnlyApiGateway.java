package com.platform.ai.navigation.domain;

import tools.jackson.databind.JsonNode;

public interface ReadOnlyApiGateway {

    ApiReadResult get(
        String path,
        JsonNode queryParameters,
        String authorizationHeader
    );

    record ApiReadResult(int status, String body) {
    }
}
