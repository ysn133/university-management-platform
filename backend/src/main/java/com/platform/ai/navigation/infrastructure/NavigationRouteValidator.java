package com.platform.ai.navigation.infrastructure;

import com.platform.identityaccess.domain.AccountRoleType;
import java.net.URI;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class NavigationRouteValidator {

    private static final Map<AccountRoleType, String> ROLE_PREFIXES = rolePrefixes();

    public String validate(String route, AccountRoleType role) {
        if (route == null || route.isBlank() || route.length() > 1500) {
            throw invalidRoute();
        }
        if (route.contains("\r") || route.contains("\n") || route.contains("\\")) {
            throw invalidRoute();
        }

        URI uri;
        try {
            uri = URI.create(route);
        } catch (IllegalArgumentException exception) {
            throw invalidRoute();
        }

        if (uri.isAbsolute() || uri.getHost() != null || uri.getFragment() != null) {
            throw invalidRoute();
        }
        String normalizedPath = URI.create(uri.getPath()).normalize().getPath();
        if (!normalizedPath.equals(uri.getPath()) || normalizedPath.contains("..")) {
            throw invalidRoute();
        }

        String requiredPrefix = ROLE_PREFIXES.get(role);
        boolean hasRequiredPrefix = requiredPrefix != null
            && (normalizedPath.equals(requiredPrefix) || normalizedPath.startsWith(requiredPrefix + "/"));
        if (!hasRequiredPrefix) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "AI navigation route is not available for this role"
            );
        }
        return route;
    }

    private ResponseStatusException invalidRoute() {
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI returned an invalid navigation route");
    }

    private static Map<AccountRoleType, String> rolePrefixes() {
        Map<AccountRoleType, String> prefixes = new EnumMap<>(AccountRoleType.class);
        prefixes.put(AccountRoleType.SUPER_ADMIN, "/management");
        prefixes.put(AccountRoleType.ADMIN, "/management");
        return Map.copyOf(prefixes);
    }
}
