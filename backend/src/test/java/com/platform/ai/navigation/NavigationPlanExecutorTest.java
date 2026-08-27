package com.platform.ai.navigation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import com.platform.ai.navigation.application.NavigationPlanExecutor;
import com.platform.ai.navigation.domain.NavigationContext;
import com.platform.ai.navigation.domain.AiInteractionMode;
import com.platform.ai.navigation.domain.NavigationPlan;
import com.platform.ai.navigation.domain.NavigationPlanExecutionException;
import com.platform.ai.navigation.domain.NavigationPlanMatch;
import com.platform.ai.navigation.domain.NavigationPlanMatch.MatchOperator;
import com.platform.ai.navigation.domain.NavigationPlanStep;
import com.platform.ai.navigation.domain.NavigationResult;
import com.platform.ai.navigation.domain.ReadOnlyApiGateway;
import com.platform.ai.navigation.infrastructure.NavigationRouteValidator;
import com.platform.identityaccess.domain.AccountRoleType;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class NavigationPlanExecutorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void executesAChainedFanOutPlanAndBuildsTheExactRoute() throws Exception {
        UUID establishmentId = UUID.randomUUID();
        UUID departmentId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        UUID yearId = UUID.randomUUID();
        UUID levelId = UUID.randomUUID();
        UUID semesterId = UUID.randomUUID();

        ReadOnlyApiGateway gateway = (path, query, authorization) -> {
            String body;
            if (path.endsWith("/departments")) {
                body = "[{\"id\":\"" + departmentId + "\",\"name\":\"Informatique\"}]";
            } else if (path.endsWith("/program-filieres")) {
                body = "[{\"id\":\"" + programId + "\",\"code\":\"IL\"}]";
            } else if (path.endsWith("/academic-years")) {
                body = "[{\"id\":\"" + yearId + "\",\"status\":\"ACTIVE\"}]";
            } else if (path.endsWith("/academic-levels")) {
                body = "[{\"id\":\"" + levelId + "\",\"name\":\"M1\"}]";
            } else if (path.endsWith("/semesters")) {
                body = "[{\"id\":\"" + semesterId + "\",\"name\":\"S1\"}]";
            } else {
                throw new AssertionError("Unexpected path " + path);
            }
            return new ReadOnlyApiGateway.ApiReadResult(200, body);
        };
        NavigationPlanExecutor executor = new NavigationPlanExecutor(
            objectMapper,
            gateway,
            new NavigationRouteValidator(),
            12,
            20
        );
        NavigationContext context = context(establishmentId);
        NavigationPlan plan = new NavigationPlan(
            List.of(
                step("departments", "/api/v1/establishments/{{caller.establishmentId}}/departments", "", List.of()),
                step("program", "/api/v1/departments/{{item.id}}/program-filieres", "departments", List.of(
                    match("code", "IL")
                )),
                step("currentYear", "/api/v1/establishments/{{caller.establishmentId}}/academic-years", "", List.of(
                    match("status", "ACTIVE")
                )),
                step("level", "/api/v1/program-filieres/{{program.id}}/academic-levels", "", List.of(
                    match("name", "M1")
                )),
                new NavigationPlanStep(
                    "semester",
                    "/api/v1/academic-levels/{{level.id}}/semesters",
                    objectMapper.readTree("{\"academicYearId\":\"{{currentYear.id}}\"}"),
                    "",
                    List.of(match("name", "S1"))
                )
            ),
            "/management/programs/{{program.id}}?academicYearId={{currentYear.id}}&academicLevelId={{level.id}}&semesterId={{semester.id}}&section=schedule",
            "Opening the requested schedule."
        );

        NavigationResult result = executor.execute(plan, context, "Bearer token");

        assertThat(result.route()).isEqualTo(
            "/management/programs/" + programId
                + "?academicYearId=" + yearId
                + "&academicLevelId=" + levelId
                + "&semesterId=" + semesterId
                + "&section=schedule"
        );
        assertThat(result.apiCallCount()).isEqualTo(5);
    }

    @Test
    void preservesTheCompleteEvidenceChainForDirectAnswerGeneration() {
        UUID establishmentId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        ReadOnlyApiGateway gateway = (path, query, authorization) -> {
            if (path.endsWith("/students")) {
                return new ReadOnlyApiGateway.ApiReadResult(
                    200,
                    "[{\"studentId\":\"" + studentId
                        + "\",\"firstName\":\"Lina\",\"lastName\":\"Idrissi\"}]"
                );
            }
            return new ReadOnlyApiGateway.ApiReadResult(
                200,
                "{\"semesterName\":\"S3\",\"semesterAverage\":11.43,\"resultStatus\":\"VALIDATED\"}"
            );
        };
        NavigationPlanExecutor executor = new NavigationPlanExecutor(
            objectMapper,
            gateway,
            new NavigationRouteValidator(),
            12,
            20
        );
        NavigationPlan plan = new NavigationPlan(
            AiInteractionMode.ANSWER,
            List.of(
                step(
                    "student",
                    "/api/v1/establishments/{{caller.establishmentId}}/students",
                    "",
                    List.of(match("studentId", studentId.toString()))
                ),
                step(
                    "semesterResult",
                    "/api/v1/students/{{student.studentId}}/semester-result",
                    "",
                    List.of()
                )
            ),
            "",
            ""
        );

        NavigationResult result = executor.execute(plan, context(establishmentId), "Bearer token");

        assertThat(result.mode()).isEqualTo(AiInteractionMode.ANSWER);
        assertThat(result.route()).isBlank();
        assertThat(result.answerContext())
            .contains("\"student\"")
            .contains("Lina", "Idrissi", studentId.toString())
            .contains("\"semesterResult\"")
            .contains("S3", "11.43", "VALIDATED")
            .doesNotContain("authorization");
    }

    @Test
    void recordsTheExactRejectedPathAndQueryBeforeExecutionFails() {
        ReadOnlyApiGateway gateway = (path, query, authorization) -> {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "AI requested an invalid read-only API path: " + path
            );
        };
        NavigationPlanExecutor executor = new NavigationPlanExecutor(
            objectMapper,
            gateway,
            new NavigationRouteValidator(),
            12,
            20
        );
        NavigationPlan plan = new NavigationPlan(
            List.of(step("professors", "/api/v1/professors", "", List.of())),
            "/management/professors",
            "Opening professors."
        );

        NavigationPlanExecutionException failure = catchThrowableOfType(
            () -> executor.execute(plan, context(UUID.randomUUID()), "Bearer token"),
            NavigationPlanExecutionException.class
        );

        assertThat(failure.apiCalls()).singleElement().satisfies(call -> {
            assertThat(call.path()).isEqualTo("/api/v1/professors");
            assertThat(call.queryParameters()).isEqualTo("{}");
            assertThat(call.status()).isEqualTo(400);
            assertThat(call.responsePreview()).contains("invalid read-only API path");
        });
    }

    private NavigationPlanStep step(
        String id,
        String path,
        String forEach,
        List<NavigationPlanMatch> matches
    ) {
        return new NavigationPlanStep(id, path, objectMapper.createObjectNode(), forEach, matches);
    }

    private NavigationPlanMatch match(String field, String value) {
        return new NavigationPlanMatch(field, MatchOperator.EQUALS_IGNORE_CASE, value);
    }

    private NavigationContext context(UUID establishmentId) {
        return new NavigationContext(
            "Open the IL M1 S1 schedule",
            "Open the IL M1 S1 schedule",
            "knowledge",
            "/management",
            "none",
            new AuthenticatedUserPrincipal(
                UUID.randomUUID(),
                AccountRoleType.ADMIN,
                UUID.randomUUID(),
                establishmentId,
                "admin@uiz.ac.ma"
            )
        );
    }
}
