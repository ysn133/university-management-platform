package com.platform.ai.retrieval;

import static org.assertj.core.api.Assertions.assertThat;

import com.platform.ai.retrieval.domain.KnowledgeCorpus;
import com.platform.ai.retrieval.domain.KnowledgeMatch;
import com.platform.ai.retrieval.domain.KnowledgeSource;
import com.platform.ai.retrieval.infrastructure.ClasspathKnowledgeCorpus;
import com.platform.ai.retrieval.infrastructure.HybridLexicalKnowledgeMatcher;
import com.platform.ai.retrieval.infrastructure.MarkdownKnowledgeChunker;
import com.platform.ai.retrieval.infrastructure.RetrievalTextAnalyzer;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

class KnowledgeCorpusRetrievalTest {

    private static final Pattern API_PATH = Pattern.compile("/api/v1/[A-Za-z0-9_{}./-]+");

    private KnowledgeCorpus corpus;
    private HybridLexicalKnowledgeMatcher matcher;

    @BeforeEach
    void setUp() {
        corpus = new ClasspathKnowledgeCorpus(
            new PathMatchingResourcePatternResolver(),
            new MarkdownKnowledgeChunker(),
            "classpath*:ai/knowledge/*.md",
            1800
        );
        matcher = new HybridLexicalKnowledgeMatcher(corpus, new RetrievalTextAnalyzer());
    }

    @Test
    void loadsThePackagedReadOnlyCorpus() {
        assertThat(corpus.chunks()).hasSizeGreaterThan(100);
        assertThat(corpus.chunks())
            .filteredOn(chunk -> chunk.source() == KnowledgeSource.API)
            .allMatch(chunk -> !chunk.content().contains("`POST /api/v1/"))
            .allMatch(chunk -> !chunk.content().contains("`PUT /api/v1/"))
            .allMatch(chunk -> !chunk.content().contains("`PATCH /api/v1/"))
            .allMatch(chunk -> !chunk.content().contains("`DELETE /api/v1/"));
    }

    @Test
    void documentsTheCompleteImplementedGetInventory() {
        Set<String> documentedPaths = corpus.chunks().stream()
            .filter(chunk -> chunk.source() == KnowledgeSource.API)
            .flatMap(chunk -> {
                Matcher matcher = API_PATH.matcher(chunk.content());
                return matcher.results().map(result -> result.group());
            })
            .collect(Collectors.toSet());

        assertThat(documentedPaths).hasSize(103);
    }

    @Test
    void retrievesStudentGradeApiForNaturalNoteQuestion() {
        List<KnowledgeMatch> matches = matcher.match(
            "show me the student notes for S1",
            KnowledgeSource.API,
            5
        );

        assertThat(matches).isNotEmpty();
        assertThat(matches.stream().limit(3).map(match -> match.chunk().content()).toList())
            .anyMatch(content -> content.contains("GET /api/v1/students/{studentId}/grades"));
    }

    @Test
    void retrievesStudentDirectoryDespiteTypingError() {
        List<KnowledgeMatch> matches = matcher.match(
            "find a studnet with apogee 2601008",
            KnowledgeSource.API,
            5
        );

        assertThat(matches.stream().limit(3).map(match -> match.chunk().content()).toList())
            .anyMatch(content -> content.contains("/students/directory"));
    }

    @Test
    void retrievesStudentGradeRecordRoute() {
        List<KnowledgeMatch> matches = matcher.match(
            "open Lina's grades for her M1 academic record",
            KnowledgeSource.UI,
            5
        );

        assertThat(matches).isNotEmpty();
        assertThat(matches.stream().limit(3).map(match -> match.chunk().content()).toList())
            .anyMatch(content -> content.contains("tab=grades"));
    }

    @Test
    void retrievesTheCompleteStudentSemesterGradeNavigationRecipe() {
        String query = "the grades of the student lina idrissi of this current academic year "
            + "academic level m2 semester s3";

        String apiContext = matcher.match(query, KnowledgeSource.API, 5).stream()
            .map(match -> match.chunk().content())
            .reduce("", (left, right) -> left + "\n" + right);
        String uiContext = matcher.match(query, KnowledgeSource.UI, 5).stream()
            .map(match -> match.chunk().content())
            .reduce("", (left, right) -> left + "\n" + right);

        assertThat(apiContext)
            .contains("/students/directory")
            .contains("selected registration field is id")
            .contains("/program-filieres/{programFiliereId}/academic-levels")
            .contains("/academic-levels/{academicLevelId}/semesters");
        assertThat(uiContext)
            .contains("/academic-record/{academicRegistrationId}")
            .contains("tab=grades")
            .contains("semesterId");
    }

    @Test
    void retrievesStudentSemesterValidationWorkflowForNamedLevel() {
        String context = matcher.match(
            "did lina idrissi validate s3 in academic level m2",
            KnowledgeSource.API,
            5
        ).stream().map(match -> match.chunk().content()).collect(Collectors.joining("\n"));

        assertThat(context)
            .contains("/students/{studentId}/academic-registrations")
            .contains("/academic-registrations/{academicRegistrationId}/semester-registrations")
            .contains("/semester-registrations/{semesterRegistrationId}/result")
            .contains("semesterName")
            .contains("semesterOrder=1");
    }

    @Test
    void retrievesBothSemesterValidationWorkflowWithoutConfusingCarriedSemester() {
        String context = matcher.match(
            "did lina idrissi validate s1 and s2",
            KnowledgeSource.API,
            5
        ).stream().map(match -> match.chunk().content()).collect(Collectors.joining("\n"));

        assertThat(context)
            .contains("Select S1 and S2 independently")
            .contains("semesterName")
            .contains("carried S2 registration")
            .contains("/semester-registrations/{semesterRegistrationId}/result");
    }

    @Test
    void retrievesTheCompleteProgramScheduleResolutionRecipe() {
        String query = "open the schedule of the program software engineer the current "
            + "academic year academic level m1 semester s2";

        String apiContext = matcher.match(query, KnowledgeSource.API, 5).stream()
            .map(match -> match.chunk().content())
            .reduce("", (left, right) -> left + "\n" + right);
        String uiContext = matcher.match(query, KnowledgeSource.UI, 5).stream()
            .map(match -> match.chunk().content())
            .reduce("", (left, right) -> left + "\n" + right);

        assertThat(apiContext)
            .contains("/departments/{departmentId}/program-filieres")
            .contains("/program-filieres/{programFiliereId}/academic-levels")
            .contains("/academic-levels/{academicLevelId}/semesters");
        assertThat(uiContext)
            .contains("section=schedule")
            .contains("academicYearId={academicYearId}")
            .contains("semesterId={semesterId}");
    }

    @Test
    void retrievesProgramAndYearResolutionForThePlannerFollowUpQuestion() {
        String query = "Which GET endpoints resolve the program filiere ID by name within "
            + "an establishment and the current academic year ID?";

        String context = matcher.match(query, KnowledgeSource.API, 5).stream()
            .map(match -> match.chunk().content())
            .reduce("", (left, right) -> left + "\n" + right);

        assertThat(context)
            .contains("/establishments/{establishmentId}/academic-years")
            .contains("/establishments/{establishmentId}/departments")
            .contains("/departments/{departmentId}/program-filieres");
    }

    @Test
    void sourceFilterDoesNotMixApiAndUiChunks() {
        assertThat(matcher.match("student grades", KnowledgeSource.UI, 10))
            .allMatch(match -> match.chunk().source() == KnowledgeSource.UI);
        assertThat(matcher.match("student grades", KnowledgeSource.API, 10))
            .allMatch(match -> match.chunk().source() == KnowledgeSource.API);
    }

    @Test
    void retrievesProfessorTeachingAndScheduleWorkflow() {
        String context = matcher.match(
            "open professor Yassine Chouikh teaching assignments and weekly schedule",
            KnowledgeSource.API,
            5
        ).stream().map(match -> match.chunk().title() + "\n" + match.chunk().content())
            .collect(Collectors.joining("\n"));

        assertThat(context)
            .contains("/professors?query={identity}")
            .contains("/teaching-assignments")
            .contains("/semester-schedules/{scheduleId}/entries");
    }

    @Test
    void retrievesTheExactManagementProfessorAvailabilityWorkflow() {
        String context = matcher.match(
            "is prof yassine chouikh available at monday morning",
            KnowledgeSource.API,
            5
        ).stream().map(match -> match.chunk().title() + "\n" + match.chunk().content())
            .collect(Collectors.joining("\n"));

        assertThat(context)
            .contains("Check Professor Availability, Free Time, or Scheduling Conflicts")
            .contains("/establishments/{establishmentId}/professors?query={identity}")
            .contains("/establishments/{establishmentId}/semester-schedules")
            .contains("/semester-schedules/{scheduleId}/entries")
            .contains("There is no implemented management endpoint")
            .contains("professors/{professorId}/schedule-entries");
    }

    @Test
    void retrievesRoomAvailabilityAndConflictWorkflow() {
        String context = matcher.match(
            "which computer room is free on monday from 10 to 12",
            KnowledgeSource.API,
            5
        ).stream().map(match -> match.chunk().title() + "\n" + match.chunk().content())
            .collect(Collectors.joining("\n"));

        assertThat(context)
            .contains("Check Room Availability or Room Conflicts")
            .contains("/establishments/{establishmentId}/rooms")
            .contains("entry.startTime < requestedEndTime");
    }

    @Test
    void retrievesProfessorWorkloadCalculationWorkflow() {
        String context = matcher.match(
            "how many hours does professor yassine teach this semester",
            KnowledgeSource.API,
            5
        ).stream().map(match -> match.chunk().title() + "\n" + match.chunk().content())
            .collect(Collectors.joining("\n"));

        assertThat(context)
            .contains("Calculate a Professor's Teaching Load")
            .contains("maximumWeeklyTeachingMinutes")
            .contains("endTime - startTime");
    }

    @Test
    void retrievesPaginatedStudentCountWorkflow() {
        String context = matcher.match(
            "how many students are registered in m1 this academic year",
            KnowledgeSource.API,
            5
        ).stream().map(match -> match.chunk().title() + "\n" + match.chunk().content())
            .collect(Collectors.joining("\n"));

        assertThat(context)
            .contains("How Many Students Are Registered in a Cohort")
            .contains("/students/directory")
            .contains("totalElements");
    }

    @Test
    void retrievesAttendanceJustificationWorkflow() {
        String context = matcher.match(
            "show the absence justification submitted by a student for a module",
            KnowledgeSource.API,
            5
        ).stream().map(match -> match.chunk().content()).collect(Collectors.joining("\n"));

        assertThat(context)
            .contains("/absences")
            .contains("/absence-justifications");
    }

    @Test
    void retrievesProgressionAndGraduationReads() {
        String context = matcher.match(
            "show progression decisions and graduation results for an academic level",
            KnowledgeSource.API,
            5
        ).stream().map(match -> match.chunk().content()).collect(Collectors.joining("\n"));

        assertThat(context)
            .contains("/progression-decisions")
            .contains("/graduation-decisions");
    }
}
