# Management UI Navigation Knowledge

## Scope

AI navigation is available only to establishment Super Admins and Admins. Every generated destination starts with `/management` and uses the establishment context already carried by the authenticated session.

Do not generate root routes under `/management/establishments/{establishmentId}`, professor routes, student portal routes, login routes, or mutation actions. The feature opens existing management pages; it does not perform the action found on those pages.

## Main Management Routes

| Destination | Route | Likely requests |
|---|---|---|
| Overview | `/management` | “Open the dashboard”, “go home” |
| Admin directory | `/management/admins` | “Show admins”, “find an administrator” |
| Student directory | `/management/students` | “Show students”, “find a student” |
| Professor directory | `/management/professors` | “Show professors”, “find a professor” |
| Departments | `/management/departments` | “Show departments” |
| Program paths | `/management/program-paths` | “Show Normal or Excellence paths” |
| Degree cycles | `/management/degree-cycles` | “Show Licence or Master cycles” |
| Programs | `/management/programs` | “Show programs or filieres” |
| Academic years | `/management/academic-years` | “Show academic years” |
| Academic settings | `/management/academic-rule-profiles` | “Open academic rules, ranks, or teaching preferences” |
| Academic domains | `/management/academic-domains` | “Show expertise or subject domains” |
| Facilities | `/management/facilities` | “Show blocks or rooms” |
| Account settings | `/management/account` | “Open my account”, “change my password” |

The current establishment ID comes from `GET /api/v1/auth/me`. Pass that ID to establishment-scoped API reads, but do not insert it into the frontend route.

## Managed User Records

| Destination | Route template | Required values | Useful reads |
|---|---|---|---|
| Admin record | `/management/admins/{adminId}` | `adminId` | Admin collection or `GET /api/v1/admins/{adminId}` |
| Admin permissions | `/management/admins/{adminId}?tab=permissions` | `adminId` | Admin record and permission grants |
| Student record | `/management/students/{studentId}` | `studentId` | Student directory or `GET /api/v1/students/{studentId}` |
| Student history | `/management/students/{studentId}?tab=academic-history` | `studentId` | Student academic registrations |
| Student academic record | `/management/students/{studentId}/academic-record/{academicRegistrationId}` | Both IDs | Student academic registrations |
| Professor record | `/management/professors/{professorId}` | `professorId` | Professor collection or professor record |
| Professor expertise | `/management/professors/{professorId}?tab=expertise` | `professorId` | Professor record and expertise |
| Professor teaching | `/management/professors/{professorId}?tab=teaching` | `professorId` | Professor teaching assignments |
| Professor schedule | `/management/professors/{professorId}?tab=schedule` | `professorId` | Professor schedule entries |

Supported admin record tabs: `overview`, `permissions`.

Supported student record tabs: `overview`, `academic-history`.

Supported professor record tabs: `profile`, `expertise`, `teaching`, `schedule`.

## Student Academic Record

Use this route for a specific student registration:

```text
/management/students/{studentId}/academic-record/{academicRegistrationId}
```

Supported query parameters:

| Parameter | Values | Meaning |
|---|---|---|
| `tab` | `overview`, `grades`, `attendance`, `schedule`, `decision` | Opens the exact academic-record section |
| `semesterId` | Semester UUID | Selects one semester inside that registration |

Examples:

- “Show Lina's grades for her 2026-2027 M1 registration” resolves the student and registration, then opens `?tab=grades`.
- “Show this student's attendance in S2” also resolves the matching semester and opens `?tab=attendance&semesterId={semesterId}`.
- “Open the progression decision for this registration” uses `?tab=decision`.

Resolve `academicRegistrationId` with the response field `id` from `GET /api/v1/students/{studentId}/academic-registrations`; `academicRegistrationId` is the UI placeholder name, not an API response field. Resolve a semester UUID through the selected academic level and academic year before adding `semesterId`.

## Academic Structure Entry Points

Programs can be opened from several directories. Keep the route that best matches the user's request.

| Context | Route template | Required values |
|---|---|---|
| Department programs | `/management/departments/{departmentId}/programs` | `departmentId` |
| Program-path programs | `/management/program-paths/{programPathId}/programs` | `programPathId` |
| Degree-cycle programs | `/management/degree-cycles/{degreeCycleId}/programs` | `degreeCycleId` |
| All programs | `/management/programs` | None |
| Academic-year paths | `/management/academic-years/{academicYearId}/program-paths` | `academicYearId` |
| Academic-year path programs | `/management/academic-years/{academicYearId}/program-paths/{programPathId}/programs` | Both IDs |

Resolve IDs through the corresponding establishment collections. Program IDs come from `GET /api/v1/departments/{departmentId}/program-filieres` or another implemented program collection read.

## Program Curriculum Workspace

Append `/{programFiliereId}` to the chosen program-list route. For the unfiltered directory, use:

```text
/management/programs/{programFiliereId}
```

Supported query parameters:

| Parameter | Meaning |
|---|---|
| `academicYearId` | Selects the academic year when it is not already part of the route |
| `academicLevelId` | Selects the level, such as M1 or M2 |
| `semesterId` | Selects the semester inside the chosen year and level |
| `section` | Opens one curriculum workspace section |

Supported `section` values:

| Value | Destination |
|---|---|
| Omitted or `curriculum` | Levels, semesters, modules, and configuration |
| `students` | Student cohort and registrations |
| `teaching-groups` | TD and TP groups |
| `teaching-plan` | Teaching requirements and assignments |
| `professors` | Professors teaching in the selected semester |
| `schedule` | Class timetable planning |
| `exam-planning` | Normal or rattrapage exam planning |
| `grades` | Grade review, publication, and result generation |
| `progression` | Academic-level progression decisions |
| `graduation` | Graduation decisions for a terminal level |

Examples:

- “Open IL M1 S1 students” resolves program, year, level, and semester, then uses `section=students`.
- “Show the exam plan for IL M2 S3” uses `section=exam-planning` with the matching academic context.
- “Open the grades for Software Engineering M1 S2 in 2026-2027” uses `section=grades` with all three academic IDs.
- “Show progression for 2026-2027 M1” uses `section=progression`; the semester is optional.

### Program Schedule Navigation Recipe

For “open the schedule/timetable/planning of Software Engineering or IL for the current
academic year, M1, S2”, resolve the active year, program, level, and semester, then navigate to:

```text
/management/programs/{programFiliereId}?academicYearId={academicYearId}&academicLevelId={academicLevelId}&semesterId={semesterId}&section=schedule
```

The complete identifier chain is:

```text
establishment departments
  -> programs/filieres for each department
  -> academic levels for the selected program
  -> semesters for the selected level

establishment academic years
  -> the year whose status is ACTIVE when the user says current
```

Do not open only `/management/programs/{programFiliereId}`. The `section=schedule` selector
and all four academic identifiers are required for this destination.

## Subject Module Workspace

Append `/modules/{subjectModuleId}` to the matching curriculum route. Preserve the academic context:

```text
?academicYearId={academicYearId}&academicLevelId={academicLevelId}&semesterId={semesterId}
```

If the academic year is already part of the route, omit `academicYearId`. Resolve the module ID with `GET /api/v1/semesters/{semesterId}/subject-modules`.

## Management Page Sections

| Page | Query parameter | Values |
|---|---|---|
| Academic settings | `section` | `rules`, `ranks`, `preferences` |
| Facilities | `section` | `blocks`, `rooms` |

Examples:

- “Open professor ranks” uses `/management/academic-rule-profiles?section=ranks`.
- “Show teaching preferences” uses `/management/academic-rule-profiles?section=preferences`.
- “Show rooms” uses `/management/facilities?section=rooms`.

## Navigation Assembly Rules

1. Resolve every name or label to its real UUID through an authorized `GET` endpoint.
2. Keep every generated route under `/management`.
3. Include all parent IDs required by the chosen route.
4. Use only the `tab`, `section`, and academic-context query parameters documented here.
5. If several records match, open the narrowest safe directory instead of guessing.
6. If an exact UI destination does not exist, open the closest real management page.
7. Never generate a mutation, modal action, root-governance route, professor route, or student portal route.
8. A requested supported destination is not optional. For example, “schedule” in a Program/Filière context requires `section=schedule`; opening only the curriculum parent is incomplete.
