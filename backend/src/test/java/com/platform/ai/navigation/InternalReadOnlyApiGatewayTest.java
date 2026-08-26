package com.platform.ai.navigation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.platform.ai.navigation.infrastructure.InternalReadOnlyApiGateway;
import com.platform.ai.navigation.infrastructure.RegisteredGetEndpointCatalog;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

class InternalReadOnlyApiGatewayTest {

    @Test
    void reportsTheRejectedPathWhenTheModelRequestsAnInvalidEndpoint() {
        RegisteredGetEndpointCatalog endpointCatalog = new RegisteredGetEndpointCatalog(
            new RequestMappingHandlerMapping()
        );
        InternalReadOnlyApiGateway gateway = new InternalReadOnlyApiGateway(
            endpointCatalog,
            "http://localhost:8081",
            50_000
        );

        assertThatThrownBy(() -> gateway.get(
            "/api/v1/unsupported/student-grades",
            null,
            "Bearer token"
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("/api/v1/unsupported/student-grades");
    }
}
