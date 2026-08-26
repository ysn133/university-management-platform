package com.platform.ai.navigation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.platform.ai.navigation.infrastructure.NavigationRouteValidator;
import com.platform.identityaccess.domain.AccountRoleType;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class NavigationRouteValidatorTest {

    private final NavigationRouteValidator validator = new NavigationRouteValidator();

    @Test
    void acceptsTheWorkspaceOwnedByTheCallerRole() {
        assertThat(validator.validate("/management/establishments/123", AccountRoleType.ADMIN))
            .isEqualTo("/management/establishments/123");
        assertThat(validator.validate("/management/students?tab=grades", AccountRoleType.SUPER_ADMIN))
            .isEqualTo("/management/students?tab=grades");
    }

    @Test
    void rejectsExternalAndCrossRoleRoutes() {
        assertThatThrownBy(() -> validator.validate("https://example.com", AccountRoleType.ADMIN))
            .isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> validator.validate("/management/students", AccountRoleType.STUDENT))
            .isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> validator.validate("/studentevil", AccountRoleType.STUDENT))
            .isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> validator.validate("/management", AccountRoleType.ROOT_SUPER_ADMIN))
            .isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> validator.validate("/professor/schedule", AccountRoleType.PROFESSOR))
            .isInstanceOf(ResponseStatusException.class);
    }
}
