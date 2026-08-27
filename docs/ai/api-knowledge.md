# Backend Read API Reference

## Overview

This document describes the implemented read side of the University Management Platform API.
It covers every `GET` operation exposed by the Spring Boot backend and explains the business
resource returned by each operation, its parameters, identifier semantics, and its place in a
larger lookup process. The generated OpenAPI contract remains authoritative for HTTP-level types;
this reference provides the domain meaning that cannot be derived from a path alone.

All operations require the normal authenticated request context unless the controller explicitly
states otherwise. Spring Security applies role, permission, ownership, and Establishment checks.
The existence of an operation in this reference does not imply that every authenticated role may
call it. A successful response only contains information visible to the caller.

This is a read-only reference. Account creation, editing, publication, generation, approval,
assignment, password management, and deletion use mutation operations and are outside this file.

## HTTP Conventions

- Path parameters ending in `Id` are UUIDs unless stated otherwise.
- Collection responses are either JSON arrays or a paged object with records in `content`.
- Detail responses are JSON objects.
- A detail operation returns `404 Not Found` when the identified resource does not exist or is not
  visible in the caller's permitted scope.
- A collection operation returns an empty array or an empty `content` page when no records match.
- `400 Bad Request` represents malformed UUIDs, invalid enum values, invalid date ranges, or missing
  required query parameters.
- `401 Unauthorized` represents a missing, expired, or invalid access token.
- `403 Forbidden` represents a valid identity that lacks the required role, permission, ownership,
  or Establishment access.
- Entity IDs are returned in `id` unless the response explicitly uses `studentId`, `professorId`,
  `adminId`, `academicRegistrationId`, or another documented field.
- A UUID identifies exactly one resource type. A `userAccountId`, `studentId`, `professorId`,
  `academicRegistrationId`, and `semesterRegistrationId` are not interchangeable.
- Query parameters not listed for an endpoint are unsupported. Identity search uses the documented
  `query` parameter rather than invented `firstName`, `lastName`, `name`, or `code` parameters.
- Dates use ISO `YYYY-MM-DD`. Date-time values use ISO-8601.
- Enum values use the uppercase values returned by the API.

## Academic Context Model

Academic information is historical and cannot be resolved from a Student or Semester label alone.
The following ownership chain defines a normal academic context:

```text
Establishment
  -> Academic Year
  -> Program / Filiere
  -> Academic Level
  -> Semester
  -> Subject Module
```

A Student participates in that structure through a separate registration chain:

```text
Student
  -> Academic Registration for one Program + Academic Level + Academic Year
  -> Semester Registration for each Semester included in that annual registration
  -> Module Registration for each regular or carried Subject Module
```

### Academic Registration

`AcademicRegistrationResponse` is the Student's annual placement. It establishes the Program,
Academic Level, and Academic Year in which the Student is registered. It does not itself identify
one Semester. A Student normally has at most one annual registration for an Academic Year, while
historical registrations remain available for closed years.

### Semester Registration

`SemesterRegistrationResponse` links an annual Academic Registration to one Semester. Its `id` is
the `semesterRegistrationId` required by Semester Result and Module Registration operations. The
response includes both `semesterName` and `semesterOrder`, which have different meanings:

- `semesterName` is the curriculum label, such as `S1`, `S2`, `S3`, or `S4`.
- `semesterOrder` is the Semester's position inside its Academic Level, normally `1` or `2`.

For example, M2 `S3` may have `semesterOrder=1`, and M2 `S4` may have `semesterOrder=2`.
Therefore `semesterOrder=3` does not mean S3. Questions naming S1, S2, S3, or S4 must match
`semesterName`, not `semesterOrder`.

### Second Inscription and Carried Modules

A current Academic Registration can contain an additional Semester Registration from an earlier
Academic Level when the Student carries a failed module. For example, an M2 registration can have
normal S3 and S4 registrations plus a carried S2 registration. This does not mean the Student is
currently placed in M1. The carried Semester Registration exists to host second-inscription Module
Registrations.

`ModuleRegistrationResponse.inscriptionNumber` distinguishes the first registration from a later
registration in the same module. `originAcademicLevelId` identifies the original Academic Level.
When a complete historical S1 or S2 Semester result is requested, select the original annual
Academic Registration and its original Semester Registration. Do not substitute a current carried
Semester Registration that may contain only one repeated module.

### Current and Historical Context

The current Academic Year is the record whose `status` is `ACTIVE`. A requested Semester label may
belong to a previous Academic Year. If the request specifies S1, S2, S3, S4, M1, M2, or a named
year, resolve the matching Academic Registration rather than assuming the active year. When the
year is omitted, inspect the Student's registration history and select the registration whose
Academic Level owns the requested Semester.

### Validation and Result Ownership

A `SemesterResultResponse` belongs to a `semesterRegistrationId`, not directly to a Student name.
The ownership chain proving a result is:

```text
Student.studentId
  -> AcademicRegistration.studentId
  -> SemesterRegistration.academicRegistrationId
  -> SemesterResult.semesterRegistrationId
```

A statement such as "Lina validated S3" is supported only when this complete chain identifies
Lina's S3 Semester Registration and its `resultStatus` is `VALIDATED`. Preserve the Student,
Academic Registration, Semester Registration, and Semester Result records together when presenting
the result.

## Common Query Parameters

### Identity Search

`query` is a general text search only on endpoints that explicitly declare it.

- Student `query` searches full name, first name, last name, university email, Apogee code,
  national Student code/CNE, and CIN.
- Professor `query` searches the Professor identifiers implemented by Professor Management,
  including name, university email, employee number, and CIN.
- Admin and Super Admin `query` searches their implemented identity fields such as name, email,
  and CIN.
- Establishment `query` searches Establishment name.

Do not translate a human name into `firstName` and `lastName` query parameters. Those parameters
do not exist. Use `query=Lina Idrissi`, then match `firstName` and `lastName` in the response when
needed.

### Account and Registration Status

- Account status: `ACTIVE`, `LOCKED`, `DEACTIVATED`, `ARCHIVED`.
- Academic Registration status: `ACTIVE`, `COMPLETED`, `CANCELLED`, `SUSPENDED`.
- Establishment status: `ACTIVE`, `INACTIVE`, `ARCHIVED`.
- Academic Year status includes `ACTIVE`; use the returned enum value for other lifecycle states.

### Pagination

The paged Student directory accepts `page` and `size`. `page` is zero-based and defaults to `0`.
`size` defaults to `25` and must be between `1` and `100`. Search by `query` before paging whenever
the user identifies one Student. Never assume the first unfiltered page contains the Student.

### Grade Result View

`resultView=EFFECTIVE` returns the current effective result and is the default. It incorporates a
later second-inscription result where applicable. `resultView=ORIGINAL` returns the preserved
historical result before that replacement.

## Core Response Fields

### Identity and Governance

| Response | Fields used for display and chaining |
|---|---|
| `CurrentUserResponse` | `userAccountId`, `roleEntityId`, `role`, `accountStatus`, `establishmentId`, `universityEmail`, `firstName`, `lastName` |
| `UniversityResponse` | `universityId`, `universityName` |
| `EstablishmentResponse` | `id`, `universityId`, `name`, `type`, `status` |
| `AdminProfileResponse` | `id`, `accountId`, `establishmentId`, `firstName`, `lastName`, `email`, `cin`, `phoneNumber`, `sex`, `role`, `status` |
| `SuperAdminProfileResponse` | `id`, `accountId`, `establishmentId`, `firstName`, `lastName`, `email`, `cin`, `phoneNumber`, `sex`, `role`, `status` |
| `PermissionResponse` | `id`, `code`, `name` |
| `AdminPermissionGrantsResponse` | `adminId`, `establishmentId`, `permissions` |

### People

| Response | Fields used for display and chaining |
|---|---|
| `StudentProfileResponse` | `studentId`, `userAccountId`, `establishmentId`, `apogeeCode`, `nationalStudentCode`, `universityEmail`, `accountStatus`, `firstName`, `lastName`, `birthDate`, `placeOfBirth`, `nationality`, `cin`, `sex`, `phoneNumber`, `initialEnrollmentDate`, `profilePicturePath` |
| `StudentDirectoryPageResponse` | `content`, `page`, `size`, `totalElements`, `totalPages`, `first`, `last`; each `content` item is a `StudentProfileResponse` |
| `ProfessorProfileResponse` | `professorId`, `userAccountId`, `establishmentId`, `employeeNumber`, `universityEmail`, `accountStatus`, `firstName`, `lastName`, `cin`, `sex`, `phoneNumber`, `hireDate`, `academicRankId`, `academicRank`, `maximumWeeklyTeachingMinutes` |
| `ProfessorExpertiseResponse` | `professorId`, `academicDomains`; each domain exposes `academicDomainId`, `code`, `name` |

### Academic Structure

| Response | Fields used for display and chaining |
|---|---|
| `DepartmentResponse` | `id`, `establishmentId`, `name` |
| `ProgramPathResponse` | `id`, `establishmentId`, `name` |
| `DegreeCycleResponse` | `id`, `establishmentId`, `name` |
| `ProgramFiliereResponse` | `id`, `establishmentId`, `departmentId`, `programPathId`, `degreeCycleId`, `code`, `name` |
| `AcademicLevelResponse` | `id`, `establishmentId`, `programFiliereId`, `name`, `levelOrder`, `terminalLevel` |
| `AcademicYearResponse` | `id`, `establishmentId`, `label`, `startYear`, `endYear`, `status` |
| `SemesterResponse` | `id`, `establishmentId`, `academicLevelId`, `academicYearId`, `name`, `semesterOrder`, `termType`, `startDate`, `endDate`, `lifecycleStatus` |
| `SubjectModuleResponse` | `id`, `semesterId`, `code`, `title`, `academicDomainIds` |
| `AcademicDomainResponse` | `id`, `establishmentId`, `code`, `name` |
| `AcademicRankResponse` | `id`, `establishmentId`, `code`, `name`, `seniorityOrder`, `canHoldModuleResponsibility`, `status` |

### Registration and Groups

| Response | Fields used for display and chaining |
|---|---|
| `AcademicRegistrationResponse` | `id` (the UI calls this `academicRegistrationId`), `studentId`, `establishmentId`, `programFiliereId`, `academicLevelId`, `academicYearId`, `status` |
| `SemesterRegistrationResponse` | `id`, `academicRegistrationId`, `semesterId`, `semesterName`, `semesterOrder` |
| `ModuleRegistrationResponse` | `id`, `semesterRegistrationId`, `subjectModuleId`, `subjectModuleCode`, `subjectModuleTitle`, `originAcademicLevelId`, `inscriptionNumber`, `status` |
| `ClassGroupResponse` | `id`, `establishmentId`, `programFiliereId`, `academicLevelId`, `academicYearId`, `name`, `status` |
| `StudentClassAssignmentResponse` | `id`, `academicRegistrationId`, `semesterRegistrationId`, `semesterId`, `classGroupId` |
| `TeachingGroupResponse` | `id`, `semesterId`, `sourceClassGroupId`, `sourceClassGroupName`, `name`, `groupType`, `members` |
| `TeachingGroupMemberResponse` | `semesterRegistrationId`, `studentId`, `apogeeCode`, `firstName`, `lastName`, `secondInscription` |

### Teaching and Scheduling

| Response | Fields used for display and chaining |
|---|---|
| `TeachingRequirementResponse` | `id`, `subjectModuleId`, `moduleTeachingComponentId`, `componentType`, `audienceType`, `sourceClassGroupId`, `teachingGroupId`, `status` |
| `TeachingAssignmentResponse` | `id`, `establishmentId`, `teachingRequirementId`, `professorId`, `subjectModuleId`, `programFiliereId`, `academicLevelId`, `academicYearId`, `semesterId`, `teachingGroupId`, `componentType`, `status` and display labels |
| `ModuleClassResponsibilityResponse` | `id`, `establishmentId`, `professorId`, `subjectModuleId`, `classGroupId`, `academicYearId`, `semesterId`, `status` and display labels |
| `SemesterScheduleResponse` | `id`, `establishmentId`, `academicYearId`, `semesterId`, `publicationStatus`, `publishedAt` |
| `ScheduleEntryResponse` | `id`, `semesterScheduleId`, `teachingAssignmentId`, `subjectModuleId`, `professorId`, `dayOfWeek`, `startTime`, `endTime`, room, block, class, teaching-group, and audience fields |
| `RoomResponse` | `id`, `establishmentId`, `blockId`, `blockCode`, `code`, `name`, `roomType`, `capacity`, `status` |

### Exams, Grades, Results, and Attendance

| Response | Fields used for display and chaining |
|---|---|
| `ExamScheduleResponse` | `id`, `establishmentId`, `academicYearId`, `semesterId`, `sessionType`, `publicationStatus`, `startDate`, `endDate` |
| `ModuleExamResponse` | `id`, `examScheduleId`, `subjectModuleId`, `classGroupId`, `examDate`, `startTime`, `endTime`, `roomId`, `roomCode`, `candidateListGeneratedAt` |
| `ExamCandidateResponse` | `id`, `moduleExamId`, `studentId`, `moduleRegistrationId`, identifiers, exam group, room, date, time, and session fields |
| `StudentGradeResponse` | `gradeRecordId`, `student/module/exam/registration IDs`, `gradeValue`, `finalGradeValue`, `sessionType`, `zeroGradeReason`, `publishedAt`, `moduleResultStatus`, `inscriptionNumber`, source/effective year and semester IDs |
| `GradeSheetResponse` | `moduleExamId`, `subjectModuleId`, `classGroupId`, `workflowStatus`, `grades` |
| `SemesterResultResponse` | `id`, `semesterRegistrationId`, `academicRuleProfileId`, `semesterAverage`, `resultStatus`, `evaluatedAt` |
| `ProgressionDecisionResponse` | `id`, `academicRegistrationId`, `academicRuleProfileId`, `annualAverage`, `outstandingModuleCount`, `decisionStatus`, `decidedAt` |
| `AbsenceRecordResponse` | `id`, `studentId`, `teachingAssignmentId`, `moduleRegistrationId`, `subjectModuleId`, `semesterId`, `academicYearId`, `absenceDate`, `justified`, `justificationNote`, labels |
| `AbsenceJustificationResponse` | `id`, `absenceId`, `studentId`, `teachingAssignmentId`, `reason`, `status`, document metadata, `decisionNote`, submission/review dates, student/module labels |

## Authentication and Platform Context

### `GET /api/v1/auth/me`

**Endpoint:** `GET /api/v1/auth/me`

Returns `CurrentUserResponse` for the access token supplied with the request. The response establishes
the authenticated `userAccountId`, business `roleEntityId`, role, account status, Establishment
scope, university email, and display identity. It is the canonical operation for initializing an
authenticated session and determining which workspace and self-service operations apply. It does
not return the complete Student, Professor, Admin, or Super Admin business profile; use
`roleEntityId` with the corresponding detail endpoint when those fields are required.

### `GET /api/v1/university`

**Endpoint:** `GET /api/v1/university`

Returns the platform's bootstrapped `UniversityResponse`. No identifier or query parameter is
required because the current deployment exposes one University root. The response supplies the
University identifier and name used in governance views. Establishments are a separate collection
and are loaded through the University Establishments operation.

### `GET /api/v1/university/{id}`

**Endpoint:** `GET /api/v1/university/{id}`

Returns one `UniversityResponse` by University UUID. Use this operation when a stored
`universityId` must be verified or translated into its display name. A successful response proves
that the University exists, but does not include its Establishments.

### `GET /api/v1/university/{universityId}/establishments`

**Endpoint:** `GET /api/v1/university/{universityId}/establishments`

Returns `EstablishmentResponse[]` owned by the selected University. Optional `query` searches the
Establishment name, `type` restricts the collection to `SCHOOL`, `FACULTY`, or `INSTITUTE`, and
`status` restricts it to `ACTIVE`, `INACTIVE`, or `ARCHIVED`. Filters may be combined. The response
is the Root governance directory and supplies the Establishment IDs needed to enter an
Establishment-scoped management context.

### `GET /api/v1/establishments/{id}`

**Endpoint:** `GET /api/v1/establishments/{id}`

Returns one `EstablishmentResponse` by Establishment UUID, including its University ownership,
name, type, and lifecycle status. This is the canonical Establishment detail operation after an ID
has been resolved. It does not include Departments, users, Programs, or Academic Years; those
resources are read through their own Establishment-scoped collections.

## Managed Users and Permissions

### Super Admins

#### `GET /api/v1/establishments/{establishmentId}/super-admins`

**Endpoint:** `GET /api/v1/establishments/{establishmentId}/super-admins`

Returns `SuperAdminProfileResponse[]` for one Establishment. Optional `query` searches the
implemented identity fields and optional `status` filters account lifecycle state. The collection
is bounded by Establishment even when no filters are supplied. Use each response's `id` as the
`superAdminId` in the detail operation; `accountId` belongs to the authentication record and is not
the Super Admin profile identifier.

#### `GET /api/v1/super-admins/{superAdminId}`

**Endpoint:** `GET /api/v1/super-admins/{superAdminId}`

Returns one `SuperAdminProfileResponse` by Super Admin role-entity UUID. The response combines the
account state, Establishment ownership, university email, and personal profile fields required by
the Super Admin detail view. It does not return Admin Permission Grants because Super Admin
authority is defined by the role rather than per-Admin grants.

### Admins

#### `GET /api/v1/establishments/{id}/admins`

**Endpoint:** `GET /api/v1/establishments/{id}/admins`

Returns `AdminProfileResponse[]` owned by one Establishment. Optional `query` searches Admin
identity, `status` filters account state, and `createdFrom`/`createdTo` form an inclusive account
creation date range. Any subset of these filters may be used. The response identifies Admins with
the profile field `id`, which is required by Admin detail and Permission Grant operations.

#### `GET /api/v1/admins/{id}`

**Endpoint:** `GET /api/v1/admins/{id}`

Returns one `AdminProfileResponse` by Admin role-entity UUID. It is the canonical account and
personal-information read for an Admin and includes Establishment ownership and current account
status. Permissions are intentionally not embedded; read them from the Permission Grants
operation so profile and authorization data remain separate.

#### `GET /api/v1/permissions`

**Endpoint:** `GET /api/v1/permissions`

Returns the complete `PermissionResponse[]` catalog known to the backend. Every record provides the
permission ID, stable code, and display name used by Admin authorization configuration. The catalog
does not indicate which Admin holds a permission; compare it with that Admin's Permission Grants.

#### `GET /api/v1/admins/{id}/permission-grants`

**Endpoint:** `GET /api/v1/admins/{id}/permission-grants`

Returns `AdminPermissionGrantsResponse` for one Admin role-entity UUID. The response identifies the
Admin and Establishment and lists the permissions currently granted to that Admin. An empty
permission collection means that no granular Admin permissions are assigned; it is not equivalent
to Super Admin authority.

### Students

#### `GET /api/v1/establishments/{establishmentId}/students`

**Endpoint:** `GET /api/v1/establishments/{establishmentId}/students`

Returns the unpaged Student accounts owned by one Establishment. The optional `query` searches the
implemented identity fields. `status` filters account lifecycle state. `enrolledFrom` and
`enrolledTo` filter `initialEnrollmentDate` inclusively. The response is
`StudentProfileResponse[]`; the business identifier for dependent Student operations is
`studentId`. This operation is suitable for an already bounded Establishment list. The paginated
directory operation is preferred for interactive or global Student search.

#### `GET /api/v1/establishments/{establishmentId}/students/directory`

**Endpoint:** `GET /api/v1/establishments/{establishmentId}/students/directory`

Provides the primary paginated Student search. `query` can identify a Student by full name, first
name, last name, university email, Apogee code, national Student code/CNE, or CIN. Account filters
are `status`, `enrolledFrom`, and `enrolledTo`. Academic filters are `academicYearId`,
`programPathId`, `programFiliereId`, `academicLevelId`, `semesterId`, and `registrationStatus`.
Pagination uses zero-based `page` and `size` from 1 to 100. The response is
`StudentDirectoryPageResponse`; records are in `content`, and every record is a complete
`StudentProfileResponse`. Use `studentId`, not `userAccountId`, in Student detail, grades,
registrations, and schedule paths. A unique person request should combine the server-side query
with exact response-field comparison when names are not guaranteed unique.

#### `GET /api/v1/students/{studentId}`

**Endpoint:** `GET /api/v1/students/{studentId}`

Returns one `StudentProfileResponse` for the Student role entity identified by `studentId`. It
provides account state, institutional identifiers, personal identity, contact information, and
initial enrollment date. This is the canonical Student profile read after an ID has been resolved.
It does not return academic registration, class placement, modules, grades, or schedules; those are
separate historical resources.

#### `GET /api/v1/students/{studentId}/academic-registrations`

**Endpoint:** `GET /api/v1/students/{studentId}/academic-registrations`

Returns the Student's annual `AcademicRegistrationResponse[]` history. Each record identifies one
Program, Academic Level, Academic Year, and registration status. The registration identifier is
the response field `id`. The selected registration field is id, while downstream paths call it
`academicRegistrationId`. Select a registration
by `academicYearId` and, when needed, `academicLevelId` or `programFiliereId`. Do not assume the
active registration answers a question about S1 or S2: inspect historical registrations when the
requested Semester belongs to an earlier level.

#### `GET /api/v1/students/{studentId}/grades`

**Endpoint:** `GET /api/v1/students/{studentId}/grades`

Returns the selected Student's published grades, notes, marks, and Module results as
`StudentGradeResponse[]`. Optional filters are
`academicYearId`, `academicLevelId`, and `semesterId`; supplying the most specific known context
prevents unrelated historical results from being mixed. `resultView=EFFECTIVE` is the default and
returns the grade currently used for academic calculation, including a later second-inscription
replacement. `resultView=ORIGINAL` returns the preserved historical grade before replacement.
Each row identifies its Module Exam, Subject Module, session type, inscription number, entered
grade, final module grade, module result status, and source year/Semester. This operation returns
module-level grades; Semester validation is read from the Semester Result operation.

#### `GET /api/v1/students/{studentId}/schedule-entries`

**Endpoint:** `GET /api/v1/students/{studentId}/schedule-entries`

Returns published `StudentScheduleEntryResponse[]` visible to the selected Student. The backend has
already applied Class Group and TD/TP Teaching Group membership, so the result is the Student's
actual timetable rather than the entire Semester schedule. Each row contains the academic context,
Module, component type, Professor, day, time, Block, and Room. Historical years and Semesters are
distinguished by the IDs and labels in each response row.

### Professors

#### `GET /api/v1/establishments/{establishmentId}/professors`

**Endpoint:** `GET /api/v1/establishments/{establishmentId}/professors`

Searches Professor profiles inside one Establishment. `query` searches Professor identity and
institutional identifiers, `status` filters account state, `joinedFrom` and `joinedTo` constrain
the inclusive hire-date range, and `academicDomainId` limits results to Professors with that
Expertise. The response is `ProfessorProfileResponse[]`; use `professorId` for all Professor detail,
Expertise, Teaching Assignment, responsibility, and schedule relationships.

#### `GET /api/v1/professors/{professorId}`

**Endpoint:** `GET /api/v1/professors/{professorId}`

Returns the canonical `ProfessorProfileResponse` for one Professor role entity. The response
includes employee number, Academic Rank, hire date, maximum weekly teaching minutes, account state,
identity, and contact fields. Teaching workload and assigned Modules are separate resources.

#### `GET /api/v1/professors/{professorId}/expertise`

**Endpoint:** `GET /api/v1/professors/{professorId}/expertise`

Returns `ProfessorExpertiseResponse` for one Professor. `academicDomains` contains the domain ID,
code, and name used by teaching-assignment eligibility. An empty list means that no Expertise has
been configured; it does not mean that the Professor has no Teaching Assignments.

## Academic Structure

### Departments, Paths, Cycles, and Programs

#### `GET /api/v1/establishments/{establishmentId}/departments`

**Endpoint:** `GET /api/v1/establishments/{establishmentId}/departments`

Returns every `DepartmentResponse` directly owned by one Establishment. The operation has no search
or pagination parameters because the collection is bounded by Establishment. Select a Department
by exact `name` when resolving a human description, and retain its `id` for Department detail and
Program/Filiere collection operations.

#### `GET /api/v1/departments/{departmentId}`

**Endpoint:** `GET /api/v1/departments/{departmentId}`

Returns one `DepartmentResponse` by UUID. The response confirms its Establishment ownership and
display name. Programs offered by the Department are not embedded and must be read from the
Department Program/Filiere collection.

#### `GET /api/v1/establishments/{establishmentId}/program-paths`

**Endpoint:** `GET /api/v1/establishments/{establishmentId}/program-paths`

Returns the Establishment's `ProgramPathResponse[]`, such as Standard and Excellence paths. There
are no query parameters, so a named path is selected by comparing `name` in the returned bounded
collection. A Program Path classifies Programs but does not own their Levels or Semesters.

#### `GET /api/v1/program-paths/{programPathId}`

**Endpoint:** `GET /api/v1/program-paths/{programPathId}`

Returns one `ProgramPathResponse` by UUID. Use it to translate a Program's `programPathId` into its
display name and verify that both resources belong to the same Establishment. Programs using the
path are discovered through Department Program collections rather than this detail response.

#### `GET /api/v1/establishments/{establishmentId}/degree-cycles`

**Endpoint:** `GET /api/v1/establishments/{establishmentId}/degree-cycles`

Returns the configured `DegreeCycleResponse[]` for one Establishment, such as Licence, Master, or
Engineering Cycle. The endpoint has no query parameters. Select by exact `name` and retain `id` to
match the `degreeCycleId` carried by a Program/Filiere.

#### `GET /api/v1/degree-cycles/{degreeCycleId}`

**Endpoint:** `GET /api/v1/degree-cycles/{degreeCycleId}`

Returns one `DegreeCycleResponse` by UUID. The response provides the display identity and
Establishment ownership of the cycle. Academic Levels are defined by Programs, not returned
directly from the Degree Cycle.

#### `GET /api/v1/departments/{departmentId}/program-filieres`

**Endpoint:** `GET /api/v1/departments/{departmentId}/program-filieres`

Returns every `ProgramFiliereResponse` offered by one Department. Each Program includes its own
Establishment, Program Path, Degree Cycle, code, and name identifiers. Select a Program by exact
`code` when available or by exact `name`. When the Department is unknown, first list Establishment
Departments and inspect this bounded collection for each Department; no Establishment-wide Program
search operation currently exists.

#### `GET /api/v1/program-filieres/{programFiliereId}`

**Endpoint:** `GET /api/v1/program-filieres/{programFiliereId}`

Returns one `ProgramFiliereResponse` by Program/Filiere UUID. This is the canonical operation for
recovering the Program name, code, Department, Program Path, Degree Cycle, and Establishment from a
stored Program ID. Levels are a dependent collection and are loaded separately.

### Academic Years, Levels, and Semesters

#### `GET /api/v1/establishments/{establishmentId}/academic-years`

**Endpoint:** `GET /api/v1/establishments/{establishmentId}/academic-years`

Returns every configured `AcademicYearResponse` owned by the Establishment. There are no query
parameters. `status=ACTIVE` identifies the current operational year, `status=PLANNED` identifies a
future year, and closed records provide historical context. `label`, `startYear`, and `endYear`
describe the same period but `id` is required by all dependent operations. Do not infer an Academic
Year UUID from the label.

#### `GET /api/v1/academic-years/{academicYearId}`

**Endpoint:** `GET /api/v1/academic-years/{academicYearId}`

Returns one `AcademicYearResponse` by UUID. Use this operation when an Academic Year ID is already
available and its label, lifecycle state, or Establishment ownership must be displayed or checked.
It does not return Levels, Semesters, registrations, or schedules.

#### `GET /api/v1/program-filieres/{programFiliereId}/academic-levels`

**Endpoint:** `GET /api/v1/program-filieres/{programFiliereId}/academic-levels`

Returns the stable `AcademicLevelResponse[]` curriculum structure of one Program/Filiere. Typical
names are L1, L2, L3, M1, and M2. `levelOrder` defines progression order inside the Program and
`terminalLevel` identifies the final level of its Degree Cycle. Academic Levels are not recreated
per year. Their Semesters are resolved separately with an Academic Year parameter.

#### `GET /api/v1/academic-levels/{academicLevelId}`

**Endpoint:** `GET /api/v1/academic-levels/{academicLevelId}`

Returns one `AcademicLevelResponse`, including its parent `programFiliereId`, Establishment,
display name, progression order, and terminal-level flag. This operation is useful for proving that
an Academic Registration's `academicLevelId` is M1 or M2 before interpreting its Semesters.

#### `GET /api/v1/academic-levels/{academicLevelId}/semesters`

**Endpoint:** `GET /api/v1/academic-levels/{academicLevelId}/semesters`

Requires `academicYearId` and returns `SemesterResponse[]` for exactly one Academic Level in one
Academic Year. This two-part scope matters because Semester dates, Modules, and lifecycle can change
between years. Select a named Semester with `name`, such as `S3`. Use `termType` for Autumn/Spring
period selection. Use `semesterOrder` only for first-versus-second position inside the selected
Academic Level; it is normally 1 or 2 and must not be compared to the numeric suffix in S3 or S4.
Every result contains the Semester's start/end dates and derived lifecycle status.

#### `GET /api/v1/semesters/{semesterId}`

**Endpoint:** `GET /api/v1/semesters/{semesterId}`

Returns one `SemesterResponse` by UUID. The response identifies its Academic Level and Academic
Year, which allows callers to verify that a Semester ID belongs to the intended historical context.
The UUID is then used for Modules, Teaching Groups, teaching requirements, results, and schedules.

### Modules, Domains, and Teaching Components

#### `GET /api/v1/semesters/{semesterId}/subject-modules`

**Endpoint:** `GET /api/v1/semesters/{semesterId}/subject-modules`

Returns the `SubjectModuleResponse[]` curriculum attached to exactly one Semester. There are no
query parameters; select a Module by stable `code` or exact `title`. The response includes Academic
Domain IDs, but teaching components, requirements, assignments, grades, and exams remain separate
resources. A Module with the same title in another Semester is a different resource and can have a
different UUID and curriculum definition.

#### `GET /api/v1/subject-modules/{subjectModuleId}`

**Endpoint:** `GET /api/v1/subject-modules/{subjectModuleId}`

Returns one `SubjectModuleResponse` by UUID, including its parent Semester, code, title, and linked
Academic Domains. Use the parent `semesterId` to recover the Level and Academic Year context when a
workflow starts from a Module ID. This operation does not identify enrolled Students or assigned
Professors.

#### `GET /api/v1/subject-modules/{subjectModuleId}/teaching-components`

**Endpoint:** `GET /api/v1/subject-modules/{subjectModuleId}/teaching-components`

Returns the Module's `ModuleTeachingComponentResponse[]`. Each component describes a Course, TD, or
TP delivery requirement through `componentType`, `audienceMode`, `sessionsPerWeek`,
`sessionDurationMinutes`, and `requiredRoomType`. Components define curriculum delivery needs;
Teaching Requirements turn them into concrete class or subgroup audiences, and Teaching
Assignments connect those requirements to Professors.

#### `GET /api/v1/establishments/{establishmentId}/academic-domains`

**Endpoint:** `GET /api/v1/establishments/{establishmentId}/academic-domains`

Returns all `AcademicDomainResponse[]` configured for the Establishment. Each domain has an ID,
code, and name and can classify Subject Modules and Professor Expertise. The operation has no
query parameters; named domains are selected from the bounded collection by code or name.

#### `GET /api/v1/academic-domains/{academicDomainId}`

**Endpoint:** `GET /api/v1/academic-domains/{academicDomainId}`

Returns one `AcademicDomainResponse` by UUID. Use it to resolve an Academic Domain ID found on a
Module or Professor Expertise response. It does not return all associated Modules or Professors.

#### `GET /api/v1/establishments/{establishmentId}/academic-ranks`

**Endpoint:** `GET /api/v1/establishments/{establishmentId}/academic-ranks`

Returns the Establishment's `AcademicRankResponse[]`. Each rank includes a stable code, name,
seniority order, responsibility eligibility, and lifecycle status. The rank information supports
Professor display and teaching-assignment policy interpretation; it does not itself list the
Professors who hold the rank.

### Academic Rules

#### `GET /api/v1/establishments/{establishmentId}/academic-rule-profiles`

**Endpoint:** `GET /api/v1/establishments/{establishmentId}/academic-rule-profiles`

Returns every `AcademicRuleProfileResponse` defined by one Establishment. A profile contains the
configured threshold values, attendance and Rattrapage policies, lifecycle status, version, and
structured `ruleDefinition` used to evaluate Module, Semester, Academic Level, progression, and
failure decisions. Listing profiles does not prove that a profile applies to a particular Level or
year; that relationship is represented by Rule Assignments.

#### `GET /api/v1/academic-rule-profiles/{academicRuleProfileId}`

**Endpoint:** `GET /api/v1/academic-rule-profiles/{academicRuleProfileId}`

Returns one complete `AcademicRuleProfileResponse` by UUID. Use it after a Semester Result,
Progression Decision, or Rule Assignment identifies the applied profile. The profile explains the
configuration used by the calculation but is not itself a calculated Student result.

#### `GET /api/v1/academic-levels/{academicLevelId}/rule-assignments`

**Endpoint:** `GET /api/v1/academic-levels/{academicLevelId}/rule-assignments`

Returns historical `AcademicLevelRuleAssignmentResponse[]` for one Level. Assignments associate a
Rule Profile with an Academic Year and may include Semester-specific overrides represented by the
contract. Select by `academicYearId` before interpreting results from a given year. The Level can
therefore retain one stable identity while its governing policy evolves between years or differs
between its Semesters.

#### `GET /api/v1/academic-level-rule-assignments/{assignmentId}`

**Endpoint:** `GET /api/v1/academic-level-rule-assignments/{assignmentId}`

Returns one `AcademicLevelRuleAssignmentResponse` by assignment UUID. This operation is used when
the exact assignment is already referenced and its Level, year, Semester scope, or Rule Profile
must be verified. Retrieve the linked Rule Profile separately to inspect its full rule definition.

## Academic Registration and Grouping

### Annual and Semester Registration

#### `GET /api/v1/establishments/{establishmentId}/academic-registrations`

**Endpoint:** `GET /api/v1/establishments/{establishmentId}/academic-registrations`

Returns annual `AcademicRegistrationResponse[]` inside one Establishment. Optional UUID filters are
`academicYearId`, `programFiliereId`, `academicLevelId`, `semesterId`, and `classGroupId`.
`status` filters the annual registration lifecycle, and `query` searches the associated Student.
This is the cohort-oriented operation for questions such as which Students are registered in M1
for a given year. It returns annual registrations, not one row per Semester. `semesterId` and
`classGroupId` constrain the annual registrations through their dependent assignments.

#### `GET /api/v1/academic-registrations/{academicRegistrationId}`

**Endpoint:** `GET /api/v1/academic-registrations/{academicRegistrationId}`

Returns one annual `AcademicRegistrationResponse`. The response proves the relationship between
`studentId`, `programFiliereId`, `academicLevelId`, and `academicYearId`. Its `id` is passed to
Semester Registration, Class Assignment, and Progression Decision operations.

#### `GET /api/v1/academic-registrations/{academicRegistrationId}/semester-registrations`

**Endpoint:** `GET /api/v1/academic-registrations/{academicRegistrationId}/semester-registrations`

Returns every `SemesterRegistrationResponse` attached to one annual registration. Normal records
represent the two Semesters of the registered Academic Level. Additional records can represent
earlier Semesters that contain carried second-inscription Modules. Select a requested S1/S2/S3/S4
using `semesterName`. `semesterOrder` only expresses first or second period within the Semester's
own Level and can produce multiple matches when carried Semesters are present.

#### `GET /api/v1/semester-registrations/{semesterRegistrationId}/module-registrations`

**Endpoint:** `GET /api/v1/semester-registrations/{semesterRegistrationId}/module-registrations`

Returns `ModuleRegistrationResponse[]` for one Student Semester Registration. Each row identifies
the Subject Module, status, `inscriptionNumber`, and `originAcademicLevelId`. A value greater than
one for `inscriptionNumber` marks a repeated registration. This operation is the authoritative way
to distinguish a complete regular Semester registration from a carried Semester registration that
contains only debt Modules.

#### `GET /api/v1/academic-registrations/{registrationId}/semesters/{semesterId}/class-assignment`

**Endpoint:** `GET /api/v1/academic-registrations/{registrationId}/semesters/{semesterId}/class-assignment`

Returns the `StudentClassAssignmentResponse` connecting one annual registration and one Semester
to its Class Group. The response also contains `semesterRegistrationId`, making it useful when a
workflow starts from annual registration plus Semester instead of from Semester Registration.
Absence of an assignment means the Student has no Class Group placement for that Semester.

### Class and Teaching Groups

#### `GET /api/v1/academic-levels/{academicLevelId}/class-groups`

**Endpoint:** `GET /api/v1/academic-levels/{academicLevelId}/class-groups`

Requires `academicYearId` and returns `ClassGroupResponse[]` for one Level in that year. A Class
Group partitions the annual cohort into operational classes and is reused across the Level's
Semesters. Select by group `name` only after Level and Academic Year have been fixed because names
such as Group A repeat across contexts.

#### `GET /api/v1/class-groups/{classGroupId}`

**Endpoint:** `GET /api/v1/class-groups/{classGroupId}`

Returns one `ClassGroupResponse` by UUID. The response identifies its Program, Academic Level,
Academic Year, name, and status and therefore supplies enough ownership data to recover its full
academic context. It does not contain the Student roster or TD/TP subgroups.

#### `GET /api/v1/academic-levels/{academicLevelId}/class-groups/roster`

**Endpoint:** `GET /api/v1/academic-levels/{academicLevelId}/class-groups/roster`

Requires both `academicYearId` and `semesterId` and returns `ClassGroupRosterResponse`. The response
groups eligible Student Academic Registrations by Class Group and also identifies registrations
that remain unassigned. The Semester parameter makes carried and active Semester participation
visible without changing the annual ownership of the Class Group. Use this operation for cohort
placement, not for Course/TD/TP Teaching Assignment audiences.

#### `GET /api/v1/academic-levels/{academicLevelId}/teaching-group-policies`

**Endpoint:** `GET /api/v1/academic-levels/{academicLevelId}/teaching-group-policies`

Requires `academicYearId` and returns the Level's `TeachingGroupPolicyResponse[]` for that year.
Policies define minimum and maximum sizes for TD and TP subdivisions and support deterministic
group generation. They describe grouping constraints only; generated groups and actual Student
membership are read from the Semester Teaching Groups operation.

#### `GET /api/v1/semesters/{semesterId}/teaching-groups`

**Endpoint:** `GET /api/v1/semesters/{semesterId}/teaching-groups`

Returns `TeachingGroupRosterResponse` for all TD and TP groups in one Semester. Each Teaching Group
identifies its source Class Group, group type, name, and member records. Member data includes the
Student and Semester Registration identifiers and marks second-inscription participation. This is
the authoritative subgroup membership used by Teaching Requirements, schedules, attendance, and
Student-specific timetable filtering.

## Teaching Delivery and Scheduling

### Requirements, Assignments, and Responsibility

#### `GET /api/v1/semesters/{semesterId}/teaching-requirements`

**Endpoint:** `GET /api/v1/semesters/{semesterId}/teaching-requirements`

Returns `TeachingRequirementResponse[]` generated for the Semester's Module Teaching Components.
Each requirement identifies the Subject Module, component type, audience mode, source Class Group
or Teaching Group, and status. It represents one concrete delivery need, such as a Course for a
whole Class Group or a TP for one subgroup. A requirement does not identify a Professor until a
Teaching Assignment exists.

#### `GET /api/v1/establishments/{establishmentId}/teaching-assignment-rank-preferences`

**Endpoint:** `GET /api/v1/establishments/{establishmentId}/teaching-assignment-rank-preferences`

Returns all `TeachingAssignmentRankPreferenceResponse[]` configured by the Establishment. These
records express which Academic Ranks are preferred for Course, TD, or TP assignment and in what
priority. They guide automatic assignment but do not guarantee that a Professor was selected; the
actual result is represented by Teaching Assignments.

#### `GET /api/v1/establishments/{establishmentId}/teaching-assignments`

**Endpoint:** `GET /api/v1/establishments/{establishmentId}/teaching-assignments`

Returns every `TeachingAssignmentResponse` in the Establishment. The operation has no server-side
query filters, so narrow the returned bounded collection using `professorId`, `academicYearId`,
`semesterId`, `subjectModuleId`, `teachingRequirementId`, component type, or status. An assignment
connects exactly one Professor to one concrete Teaching Requirement and carries the full academic
context needed for workload and scheduling.

#### `GET /api/v1/teaching-assignments/{teachingAssignmentId}`

**Endpoint:** `GET /api/v1/teaching-assignments/{teachingAssignmentId}`

Returns one `TeachingAssignmentResponse` by UUID. Use it to verify the Professor, Module,
requirement, audience, Semester, Academic Year, and assignment state before reading its Students or
Absences. This operation is also the canonical source when another record only contains a
`teachingAssignmentId`.

#### `GET /api/v1/teaching-assignments/{teachingAssignmentId}/students`

**Endpoint:** `GET /api/v1/teaching-assignments/{teachingAssignmentId}/students`

Returns the exact `TeachingAssignmentStudentResponse[]` audience for one assignment. For a Course,
the audience can be the whole Class Group; for TD or TP, it can be one Teaching Group. Membership
includes regular and eligible second-inscription Students. This roster is the correct basis for
attendance and Professor class views and must not be replaced by an entire Level roster.

#### `GET /api/v1/establishments/{establishmentId}/module-class-responsibilities`

**Endpoint:** `GET /api/v1/establishments/{establishmentId}/module-class-responsibilities`

Returns all `ModuleClassResponsibilityResponse[]` in one Establishment. Match the collection by
Professor, Subject Module, Class Group, Academic Year, Semester, and status. Responsibility is
separate from teaching delivery: it identifies who owns assessment and academic follow-up for one
Module and Class Group, even when other Professors teach TD or TP components.

#### `GET /api/v1/module-class-responsibilities/{responsibilityId}`

**Endpoint:** `GET /api/v1/module-class-responsibilities/{responsibilityId}`

Returns one `ModuleClassResponsibilityResponse` by UUID. The response provides the responsible
Professor and exact Module/Class/Year/Semester scope. Use this relationship when authorizing grade
management or resolving the Professor responsible for a scheduled Module Exam.

### Semester Timetables

#### `GET /api/v1/establishments/{establishmentId}/semester-schedules`

**Endpoint:** `GET /api/v1/establishments/{establishmentId}/semester-schedules`

Returns all `SemesterScheduleResponse[]` in the Establishment. Because there are no query
parameters, identify the intended timetable by matching both `academicYearId` and `semesterId`;
also inspect publication status when the schedule is intended for Students or Professors. A
Semester Schedule is the timetable container and does not embed its scheduled sessions.

#### `GET /api/v1/semester-schedules/{scheduleId}`

**Endpoint:** `GET /api/v1/semester-schedules/{scheduleId}`

Returns one `SemesterScheduleResponse` by UUID. It confirms the Establishment, Academic Year,
Semester, publication status, and publication time of the timetable. Use the entries collection to
read actual days, times, Professors, audiences, and Rooms.

#### `GET /api/v1/semester-schedules/{scheduleId}/entries`

**Endpoint:** `GET /api/v1/semester-schedules/{scheduleId}/entries`

Returns every `ScheduleEntryResponse` contained in one Semester Schedule. Each entry links a
Teaching Assignment to a weekday, start/end time, Room, Block, Module, Professor, and Course/TD/TP
audience. Multiple entries can exist for one Module because its teaching components and groups are
scheduled independently. Filter by source Class Group and Teaching Group when rendering a specific
class timetable.

#### `GET /api/v1/schedule-entries/{scheduleEntryId}`

**Endpoint:** `GET /api/v1/schedule-entries/{scheduleEntryId}`

Returns one `ScheduleEntryResponse` by UUID. This detail read is useful when a schedule cell or
stored link identifies a single session. The response retains the parent schedule and Teaching
Assignment IDs, allowing the complete timetable and delivery context to be recovered.

### Facilities

#### `GET /api/v1/establishments/{establishmentId}/blocks`

**Endpoint:** `GET /api/v1/establishments/{establishmentId}/blocks`

Returns the Establishment's `BlockResponse[]`. Blocks are optional physical groupings for Rooms and
are identified by code and name. There are no query filters; match the bounded collection locally.
Standalone amphitheatres or Rooms are not omitted merely because they do not belong to a Block;
they are available through the Room collection.

#### `GET /api/v1/blocks/{blockId}`

**Endpoint:** `GET /api/v1/blocks/{blockId}`

Returns one `BlockResponse` by UUID and confirms its Establishment ownership, code, and name. It
does not embed Rooms. To list Rooms in that Block, read the Establishment Room collection and match
`blockId`.

#### `GET /api/v1/establishments/{establishmentId}/rooms`

**Endpoint:** `GET /api/v1/establishments/{establishmentId}/rooms`

Returns every `RoomResponse` in one Establishment, including Rooms inside Blocks and standalone
Rooms whose `blockId` is null. The response provides Room code, name, type, capacity, status, and
optional Block identity. There are no server-side filters, so Room selection by Block, Room type,
minimum capacity, availability state, or code is performed on this collection. Schedule and exam
conflict checks still depend on dated entries, not on this static Room record alone.

#### `GET /api/v1/rooms/{roomId}`

**Endpoint:** `GET /api/v1/rooms/{roomId}`

Returns one `RoomResponse` by UUID. Use it to verify capacity, Room type, status, and optional Block
ownership for a scheduled class or exam allocation. It does not report whether the Room is free at
a given time.

## Examination

### Exam Periods and Module Exams

#### `GET /api/v1/establishments/{establishmentId}/exam-schedules`

**Endpoint:** `GET /api/v1/establishments/{establishmentId}/exam-schedules`

Returns all `ExamScheduleResponse[]` in one Establishment. An Exam Schedule is an examination
period for one Academic Year, Semester, and session type, not a single Subject Module exam. There
are no query filters, so select the intended record by matching `academicYearId`, `semesterId`,
`sessionType` (`NORMAL` or `RATTRAPAGE`), and publication state. Its start and end dates define the
period containing the Module Exams.

#### `GET /api/v1/exam-schedules/{examScheduleId}`

**Endpoint:** `GET /api/v1/exam-schedules/{examScheduleId}`

Returns one `ExamScheduleResponse` by UUID. The response confirms its Semester, Academic Year,
Normal/Rattrapage session, date range, and publication state. The actual subject exams, times, Class
Groups, and room allocations are dependent resources.

#### `GET /api/v1/exam-schedules/{examScheduleId}/module-exams`

**Endpoint:** `GET /api/v1/exam-schedules/{examScheduleId}/module-exams`

Returns every `ModuleExamResponse` scheduled inside one examination period. Each record identifies
one Subject Module and Class Group with an exam date and start/end time. A Module can therefore
have separate Module Exam records for different Class Groups or sessions. Select using both
`subjectModuleId` and `classGroupId` when the request names a specific class.

#### `GET /api/v1/module-exams/{moduleExamId}`

**Endpoint:** `GET /api/v1/module-exams/{moduleExamId}`

Returns one `ModuleExamResponse` by UUID. It is the canonical source for the scheduled exam's
period, Subject Module, Class Group, date, time, candidate-generation state, and any directly
represented Room data. Exam Group room allocations, candidate rosters, and grades are loaded from
their dedicated Module Exam operations.

### Exam Groups, Rooms, and Candidates

#### `GET /api/v1/exam-schedules/{examScheduleId}/class-groups/{classGroupId}/exam-groups`

**Endpoint:** `GET /api/v1/exam-schedules/{examScheduleId}/class-groups/{classGroupId}/exam-groups`

Returns `ExamGroupPlanResponse` for one Class Group across the complete examination period. The
plan describes the split count, generated Exam Groups, Student membership, and group sizes. These
groups exist to distribute one class across Rooms and are separate from teaching TD/TP groups. The
same membership is reused by all Module Exams in that Exam Schedule unless the plan is regenerated.

#### `GET /api/v1/module-exams/{moduleExamId}/room-allocations`

**Endpoint:** `GET /api/v1/module-exams/{moduleExamId}/room-allocations`

Returns `ExamRoomAllocationResponse[]` for one Module Exam. Each allocation connects one Exam Group
from the period-level plan to the Room used for this specific exam and exposes the Room information
required by the published timetable. Different Module Exams can allocate the same Exam Group to
different Rooms. An empty collection means that no group-level room allocation is recorded for the
selected exam.

#### `GET /api/v1/module-exams/{moduleExamId}/candidates`

**Endpoint:** `GET /api/v1/module-exams/{moduleExamId}/candidates`

Returns the eligible `ExamCandidateResponse[]` for exactly one Module Exam. Every candidate links a
Student and Module Registration to the scheduled exam and includes institutional identifiers,
Exam Group, allocated Room, date, time, and session context. Ineligible Students are not returned.
This roster is the source for attendance at the exam and for Grade Sheet candidate rows.

## Grades and Academic Decisions

### Grade Sheets and Final Module Results

#### `GET /api/v1/module-exams/{moduleExamId}/grade-sheet`

**Endpoint:** `GET /api/v1/module-exams/{moduleExamId}/grade-sheet`

Returns one `GradeSheetResponse` for a Normal or Rattrapage Module Exam. `workflowStatus` describes
the sheet as a whole and `grades` contains candidate rows with Student identity, Module Registration,
inscription number, entered grade, absence/zero reason, publication state, and publication date.
This is the operational sheet used to review one scheduled exam. It is not the calculated final
Module result and it does not represent a complete Semester.

#### `GET /api/v1/semesters/{semesterId}/class-groups/{classGroupId}/final-results`

**Endpoint:** `GET /api/v1/semesters/{semesterId}/class-groups/{classGroupId}/final-results`

Returns calculated `FinalResultResponse[]` for the selected Semester and Class Group. Optional
`subjectModuleId` restricts the result to one Module; omitting it returns every Module result in the
cohort. Each row identifies the Student and Module Registration, inscription number, final grade,
and Module result status. The final grade incorporates the configured Normal/Rattrapage policy.
This operation answers Module validation questions, not Semester validation questions.

#### `GET /api/v1/semesters/{semesterId}/class-groups/{classGroupId}/semester-results`

**Endpoint:** `GET /api/v1/semesters/{semesterId}/class-groups/{classGroupId}/semester-results`

Returns management-oriented `ManagedSemesterResultResponse[]` for the whole cohort. Every row
contains Student identity, Semester average, result status, counts of validated/compensated/non-
validated Modules, and evaluation time. `secondInscriptionOnly=true` identifies a carried-only
record and the `original*` fields point to the original year, Level, Semester, and Class Group.
This operation is best for listing or comparing the Semester decisions of many Students.

#### `GET /api/v1/semester-registrations/{semesterRegistrationId}/result`

**Endpoint:** `GET /api/v1/semester-registrations/{semesterRegistrationId}/result`

Returns one `SemesterResultResponse` for exactly one Semester Registration. The response includes
`semesterRegistrationId`, applied Academic Rule Profile, Semester average, `resultStatus`, and
evaluation time. It does not repeat Student identity or Semester name. Those facts come from the
preceding Student -> Academic Registration -> Semester Registration chain and must remain attached
when interpreting the result. `resultStatus=VALIDATED` confirms Semester validation;
`NOT_VALIDATED` confirms that the Semester itself was not validated even if annual progression may
later allow promotion with debt.

### Progression and Graduation

#### `GET /api/v1/academic-registrations/{academicRegistrationId}/progression-decision`

**Endpoint:** `GET /api/v1/academic-registrations/{academicRegistrationId}/progression-decision`

Returns one `ProgressionDecisionResponse` for the selected annual Academic Registration. The
decision records the applied Rule Profile, annual average, outstanding Module count, decision
status, and decision date. It answers whether that annual registration is promoted, promoted by
compensation, promoted with debt, repeated, failed, or otherwise resolved by the configured rules.
It is distinct from either Semester's individual validation status.

#### `GET /api/v1/academic-levels/{academicLevelId}/academic-years/{academicYearId}/progression-decisions`

**Endpoint:** `GET /api/v1/academic-levels/{academicLevelId}/academic-years/{academicYearId}/progression-decisions`

Returns `ManagedProgressionDecisionResponse[]` for the complete cohort in one Academic Level and
Academic Year. Rows combine Student identity, both Semester results, annual average, outstanding
Modules, and progression decision. This is the management list and reporting read; use the single
Academic Registration operation when one Student's ownership chain is already known.

#### `GET /api/v1/academic-levels/{academicLevelId}/academic-years/{academicYearId}/graduation-decisions`

**Endpoint:** `GET /api/v1/academic-levels/{academicLevelId}/academic-years/{academicYearId}/graduation-decisions`

Returns `GraduationDecisionResponse[]` for a terminal Academic Level in one Academic Year. The
records identify Students who were evaluated for graduation, the Degree Cycle and terminal Level,
graduation status, graduation average, and decision date. This operation is meaningful only for a
Level marked terminal; non-terminal advancement is represented by Progression Decisions.

## Attendance and Justifications

### `GET /api/v1/teaching-assignments/{teachingAssignmentId}/absences`

**Endpoint:** `GET /api/v1/teaching-assignments/{teachingAssignmentId}/absences`

Returns `AbsenceRecordResponse[]` recorded for one exact Teaching Assignment. Because the
assignment already defines the Professor, Module, component, and audience, this is the natural
roster-oriented read for a Professor managing attendance. Each absence identifies the Student,
Module Registration, date, justification state, and academic context.

### `GET /api/v1/establishments/{establishmentId}/absences`

**Endpoint:** `GET /api/v1/establishments/{establishmentId}/absences`

Returns Establishment-wide `AbsenceRecordResponse[]` and supports optional `studentId`,
`academicYearId`, `semesterId`, `subjectModuleId`, and `justified` filters. Filters are cumulative
and should be supplied whenever the requested context is known. This operation supports management
search and Student academic-history views; it does not return the uploaded justification document.

### `GET /api/v1/teaching-assignments/{assignmentId}/absence-justifications`

**Endpoint:** `GET /api/v1/teaching-assignments/{assignmentId}/absence-justifications`

Returns `AbsenceJustificationResponse[]` submitted against Absences belonging to one Teaching
Assignment. Each response links the justification to its Absence and Student, exposes review
status, reason, document metadata, decision note, and submission/review dates. Use this collection
for the responsible Professor's review queue and match by `absenceId` when a specific absence is
being inspected.

### `GET /api/v1/absence-justifications/{justificationId}/document`

**Endpoint:** `GET /api/v1/absence-justifications/{justificationId}/document`

Downloads the supporting document attached to one Absence Justification. The response is binary
content with its media type and download metadata supplied by HTTP headers rather than a JSON DTO.
Read the justification metadata first to obtain the file name, type, review status, and ownership.
A missing document produces a not-found response even when the Justification itself exists.

### `GET /api/v1/attendance/qr-sessions/{sessionId}`

**Endpoint:** `GET /api/v1/attendance/qr-sessions/{sessionId}`

Returns the current `AttendanceQrSessionResponse` for a Professor-owned QR attendance session. The
response describes the session state, rotation/expiry information, and current attendance capture
context. It is an operational polling endpoint used while a QR session is open, not a historical
absence report. Session access remains restricted to its authorized Professor, and rotating tokens
must not be exposed in general management views.

## Actor Self-Service Reads

These operations are part of the backend contract but are not substitutes for management reads.

### Professor Context

#### `GET /api/v1/me/teaching-assignments`

**Endpoint:** `GET /api/v1/me/teaching-assignments`

Returns the authenticated Professor's `TeachingAssignmentResponse[]`. Ownership is derived from
the access token, so no `professorId` is accepted and assignments belonging to other Professors are
never included. The response covers Course, TD, and TP delivery and carries the academic, Module,
audience, and assignment status context needed by the Professor workspace.

#### `GET /api/v1/me/module-class-responsibilities`

**Endpoint:** `GET /api/v1/me/module-class-responsibilities`

Returns the authenticated Professor's `ModuleClassResponsibilityResponse[]`. These records define
the Module/Class contexts in which the Professor owns assessment and academic follow-up. They are
not interchangeable with Teaching Assignments: a Professor may teach a TD or TP without being the
responsible Professor for grades and exams.

#### `GET /api/v1/me/modules/{subjectModuleId}/classes/{classGroupId}/students`

**Endpoint:** `GET /api/v1/me/modules/{subjectModuleId}/classes/{classGroupId}/students`

Returns the `TeachingAssignmentStudentResponse[]` roster that the authenticated Professor is
authorized to view for one Module and Class Group. The backend derives access from the Professor's
Teaching Assignments and Module Class Responsibility rather than trusting the two path IDs alone.
The result supports Professor class, grades, and attendance views while preventing access to
unrelated classes.

#### `GET /api/v1/me/schedule-entries`

**Endpoint:** `GET /api/v1/me/schedule-entries`

Returns `ScheduleEntryResponse[]` belonging to the authenticated Professor. The entries include
published teaching sessions and retain their Academic Year, Semester, Program, Level, Module,
Class/Teaching Group, day, time, and Room context. The self-service operation is preferred over
loading every Establishment schedule and filtering by Professor in client code.

#### `GET /api/v1/me/exams`

**Endpoint:** `GET /api/v1/me/exams`

Returns `ProfessorExamResponse[]` for exams visible to the authenticated responsible Professor.
Each record provides the Module, class, Academic Year, Semester, Normal/Rattrapage session, date,
time, Room allocation, and grade workflow context needed by the Professor exam workspace. A
Professor who only teaches TD/TP and does not hold Module Class Responsibility is not granted grade
or exam-management visibility through this endpoint.

### Student Context

#### `GET /api/v1/me/academic-contexts`

**Endpoint:** `GET /api/v1/me/academic-contexts`

Returns historical and current `StudentAcademicContextResponse[]` for the authenticated Student.
Each context combines Program/Filiere, Program Path, Degree Cycle, Academic Level, Academic Year,
Semester, Class Group, and TD/TP Teaching Group placement. It is the primary read for initializing
Student navigation because it exposes valid context combinations without requiring the frontend to
reconstruct the full registration chain manually.

#### `GET /api/v1/me/module-registrations`

**Endpoint:** `GET /api/v1/me/module-registrations`

Returns the authenticated Student's `StudentModuleRegistrationResponse[]`. The response includes
regular Module registrations and carried second inscriptions, with Subject Module and academic
context labels suitable for the Student's Modules view. Use `inscriptionNumber` and origin fields
to distinguish repeated Modules from the normal curriculum of the current Level.

#### `GET /api/v1/me/student-schedule-entries`

**Endpoint:** `GET /api/v1/me/student-schedule-entries`

Returns published `StudentScheduleEntryResponse[]` for the authenticated Student. Class Group and
TD/TP Teaching Group membership are applied by the backend, so a Student receives only sessions
addressed to the whole cohort or to a group containing that Student. Records include enough year
and Semester context to separate current schedules from history.

#### `GET /api/v1/me/exam-invitations`

**Endpoint:** `GET /api/v1/me/exam-invitations`

Returns eligible `StudentExamInvitationResponse[]` for the authenticated Student. Invitations
identify the Module Exam, Normal/Rattrapage session, date, time, Exam Group, and allocated Room. An
exam is absent from the response when the Student is not an eligible generated candidate; the
operation does not expose other Students in the same Exam Group.

#### `GET /api/v1/me/grades`

**Endpoint:** `GET /api/v1/me/grades`

Returns the authenticated Student's published `StudentGradeResponse[]`. Optional `academicYearId`,
`academicLevelId`, and `semesterId` filters isolate a historical context. `resultView=EFFECTIVE`
returns the grade currently used after second inscription, while `ORIGINAL` preserves the earlier
historical result. Normal and Rattrapage rows, final Module grade, and Module result status remain
distinct fields; Semester averages and decisions come from academic decision resources.

#### `GET /api/v1/me/absences`

**Endpoint:** `GET /api/v1/me/absences`

Returns every `AbsenceRecordResponse` visible to the authenticated Student. Each record identifies
the Module, teaching context, academic period, absence date, and whether it has been justified. The
endpoint is ownership-filtered and cannot be used to inspect another Student's attendance.

#### `GET /api/v1/me/absence-justifications`

**Endpoint:** `GET /api/v1/me/absence-justifications`

Returns the authenticated Student's `AbsenceJustificationResponse[]`, including pending, approved,
and rejected submissions. The response provides the linked Absence, reason, document metadata,
review decision, and dates. Supporting file bytes are downloaded through the separate document
operation after authorization verifies ownership.

#### `GET /api/v1/me/progression-decisions`

**Endpoint:** `GET /api/v1/me/progression-decisions`

Returns historical `ManagedProgressionDecisionResponse[]` for the authenticated Student. Each
record combines the Academic Year and Level context, Semester results, annual average, outstanding
Module count, and final progression status. It supports current and historical result views without
allowing a Student-supplied `studentId`.

#### `GET /api/v1/me/graduation-decisions`

**Endpoint:** `GET /api/v1/me/graduation-decisions`

Returns `GraduationDecisionResponse[]` belonging to the authenticated Student. Records identify the
terminal Academic Level, Degree Cycle, Academic Year, graduation status, average, and decision
date. The collection can be empty when the Student has not reached a terminal Level or when no
graduation decision has been generated.

## AI Retrieval Diagnostic

### `GET /api/v1/ai/retrieval`

**Endpoint:** `GET /api/v1/ai/retrieval`

Requires a natural-language `query` and accepts `limit` from 1 to 10, defaulting to 5. It returns
`KnowledgeRetrievalResponse`, which reports the highest-ranking API and UI documentation chunks,
their source metadata, and retrieval scores. This is a diagnostic operation for evaluating the
knowledge corpus. It does not query university business records, execute a navigation plan, or
replace any domain endpoint described above.

## Business Read Processes

The following processes describe how individual operations combine into a complete, traceable
answer. Every step retains the identifiers and ownership fields returned by the previous step.

### Identify a Student Reliably

Use the paginated directory when a request contains a Student name, Apogee code, CNE/national
Student code, university email, or CIN.

```text
GET /api/v1/establishments/{establishmentId}/students/directory
  query={name-or-identifier}
  page=0
  size=25
```

The returned records are under `content`. A unique result supplies `studentId`. If several people
share a name, compare another supplied identifier; do not select the first record arbitrarily.
`userAccountId` identifies authentication and is not accepted by Student academic endpoints.

After resolving `studentId`, `GET /api/v1/students/{studentId}` can retrieve the canonical profile.
Academic history must be read separately because a Student profile has no current Level or Semester
field.

### Resolve a Student's Academic Registration History

```text
GET /api/v1/students/{studentId}/academic-registrations
```

Each returned registration is one annual placement. To select a named Academic Year, resolve the
year through `GET /api/v1/establishments/{establishmentId}/academic-years` and compare
`AcademicRegistrationResponse.academicYearId`. To select M1 or M2, resolve the Program's Levels and
compare `academicLevelId` rather than assuming the active registration.

The registration `status` describes that annual record. `ACTIVE` usually identifies current study,
while `COMPLETED` keeps the previous year's academic record available. A request that explicitly
names a Semester must be resolved against the Academic Level that owns that Semester, even when the
Student currently has a newer active registration.

### Determine Whether a Student Validated S1, S2, S3, or S4

This process answers questions such as:

- Did Lina Idrissi validate S3?
- Did Lina validate S3 in M2?
- Did the Student validate S1 and S2?
- What was the Student's S3 average?

First resolve the Student and load all annual registrations:

```text
GET /api/v1/establishments/{establishmentId}/students/directory?query=Lina Idrissi
  -> StudentProfileResponse.studentId
GET /api/v1/students/{studentId}/academic-registrations
  -> annual registration candidates
```

If an Academic Level such as M2 is named, resolve the Level identity:

```text
GET /api/v1/departments/{departmentId}/program-filieres
  -> Program/Filiere
GET /api/v1/program-filieres/{programFiliereId}/academic-levels
  -> AcademicLevelResponse where name equals M2
```

When the Student registration already provides `programFiliereId` and `academicLevelId`, the detail
operations can verify those IDs without rediscovering the Program through every Department:

```text
GET /api/v1/academic-levels/{academicLevelId}
GET /api/v1/program-filieres/{programFiliereId}
```

Select the Student's annual registration whose `academicLevelId` matches the requested Level. Then:

```text
GET /api/v1/academic-registrations/{academicRegistrationId}/semester-registrations
  -> match semesterName exactly: S1, S2, S3, or S4
GET /api/v1/semester-registrations/{semesterRegistrationId}/result
  -> inspect resultStatus and semesterAverage
```

The selected registration field is id, and that value fills the `academicRegistrationId` path
parameter. There is no separate `academicRegistrationId` field in `AcademicRegistrationResponse`.

Do not compare `semesterOrder` with 3 to find S3. S3 is normally the first Semester of M2 and may
have `semesterOrder=1`. Do not select an annual registration only because its Academic Year is
ACTIVE when the requested Semester belongs to a completed M1 registration.

The final evidence consists of all four linked records:

```text
StudentProfileResponse
AcademicRegistrationResponse
SemesterRegistrationResponse with semesterName=S3
SemesterResultResponse with resultStatus=VALIDATED or NOT_VALIDATED
```

If no Semester Result exists, the API has no calculated decision to report. Grade values alone do
not authorize inventing a Semester validation decision.

### Determine Whether Both Semesters of a Level Were Validated

For a question about S1 and S2 or both Semesters of M1, resolve one annual registration for that
Level and year, then read its Semester Registrations once. Select S1 and S2 independently by
`semesterName`, and call the Semester Result endpoint once for each selected
`semesterRegistrationId`.

```text
AcademicRegistration M1
  -> SemesterRegistration S1 -> SemesterResult S1
  -> SemesterRegistration S2 -> SemesterResult S2
```

Report each decision separately. "Both validated" is true only when both result records have
`resultStatus=VALIDATED`. A carried S2 registration under a later M2 annual registration is not the
complete original M1 S2 record and must not replace this lookup.

### Read Current Effective Grades and Original Historical Grades

Resolve `studentId`, the requested Academic Year, Academic Level, and Semester IDs before filtering
grades. Then call:

```text
GET /api/v1/students/{studentId}/grades
  academicYearId={academicYearId}
  academicLevelId={academicLevelId}
  semesterId={semesterId}
  resultView=EFFECTIVE
```

`EFFECTIVE` answers what grade currently counts after second inscription. `ORIGINAL` answers what
was recorded before the replacement. `sessionType=NORMAL` and `sessionType=RATTRAPAGE` identify
exam-session grades. `finalGradeValue` and `moduleResultStatus` identify the calculated Module
outcome. Semester validation still comes from `SemesterResultResponse`.

For a second-inscription investigation, use `sourceAcademicYearId`, `sourceSemesterId`, `revised`,
and `inscriptionNumber` to explain where the effective grade originated. Preserve both original and
effective reads when the question asks how a result changed.

### Read a Cohort's Final Module and Semester Results

Resolve Program, Academic Level, Academic Year, Semester, and Class Group. Then use:

```text
GET /api/v1/semesters/{semesterId}/class-groups/{classGroupId}/final-results
GET /api/v1/semesters/{semesterId}/class-groups/{classGroupId}/semester-results
```

The first operation returns one row per Student Module Registration and can be restricted with
`subjectModuleId`. The second returns one row per Student Semester Registration with its average and
validation decision. These operations are cohort views; the single-Student ownership chain remains
available through `studentId` and `semesterRegistrationId` in their responses.

### Resolve a Program, Level, Year, and Semester

Programs belong to Departments, while Academic Years belong directly to the Establishment.

```text
GET /api/v1/establishments/{establishmentId}/academic-years
  -> select status=ACTIVE or the requested label
GET /api/v1/establishments/{establishmentId}/departments
GET /api/v1/departments/{departmentId}/program-filieres for each Department
  -> select Program by exact code or name
GET /api/v1/program-filieres/{programFiliereId}/academic-levels
  -> select Level by name
GET /api/v1/academic-levels/{academicLevelId}/semesters?academicYearId={academicYearId}
  -> select Semester by name or termType
```

Program Path and Degree Cycle are IDs on `ProgramFiliereResponse`. Their detail endpoints provide
display names when needed. A Program can be found by code such as IL or by full name such as
Software Engineering. When the Department is unknown, each Department's Program collection must be
inspected because no Establishment-wide Program search operation exists.

### Read a Published Class Timetable

Resolve the academic context and Class Group:

```text
GET /api/v1/academic-levels/{academicLevelId}/class-groups?academicYearId={academicYearId}
GET /api/v1/establishments/{establishmentId}/semester-schedules
  -> select academicYearId + semesterId
GET /api/v1/semester-schedules/{scheduleId}/entries
```

`SemesterScheduleResponse.publicationStatus` determines whether the schedule is published.
`ScheduleEntryResponse.sourceClassGroupId` and Teaching Group fields identify the audience.
Course entries may target the whole cohort, while TD and TP entries can target subgroups. A Student-
specific timetable should use `/students/{studentId}/schedule-entries`, where audience filtering is
already applied.

### Read an Examination Period and Module Exams

An Exam Schedule represents one Normal or Rattrapage examination period for one Semester. It is not
one Module exam.

```text
GET /api/v1/establishments/{establishmentId}/exam-schedules
  -> select academicYearId + semesterId + sessionType
GET /api/v1/exam-schedules/{examScheduleId}/module-exams
  -> select subjectModuleId + classGroupId
GET /api/v1/module-exams/{moduleExamId}
```

The Module Exam provides the date and time. Exam Group planning belongs to the whole Exam Schedule
and Class Group, while room allocations are specific to a Module Exam. Candidates are read from
`/module-exams/{moduleExamId}/candidates`, and the grade sheet is read from
`/module-exams/{moduleExamId}/grade-sheet`.

### Read Professor Teaching, Responsibility, and Schedule

Resolve the Professor with the Establishment Professor search. Teaching Assignment and Module Class
Responsibility are different resources:

- Teaching Assignment identifies the Course, TD, or TP requirement actually delivered by the
  Professor to an audience.
- Module Class Responsibility identifies the Professor responsible for assessment and academic
  follow-up for one Module and Class Group.

```text
GET /api/v1/establishments/{establishmentId}/professors?query={identity}
GET /api/v1/establishments/{establishmentId}/teaching-assignments
  -> select professorId and academic context
GET /api/v1/establishments/{establishmentId}/module-class-responsibilities
  -> select professorId and academic context
GET /api/v1/establishments/{establishmentId}/semester-schedules
GET /api/v1/semester-schedules/{scheduleId}/entries
  -> select professorId or teachingAssignmentId
```

For the authenticated Professor, `/me/teaching-assignments`, `/me/module-class-responsibilities`,
and `/me/schedule-entries` are already ownership-filtered and should be preferred.

### Read Absences and Supporting Justifications

An Absence Record belongs to a Student's Module Registration and the Teaching Assignment where the
absence occurred. Management can query Establishment absences using supported IDs:

```text
GET /api/v1/establishments/{establishmentId}/absences
  studentId={studentId}
  academicYearId={academicYearId}
  semesterId={semesterId}
  subjectModuleId={subjectModuleId}
  justified={true-or-false}
```

To inspect justifications in one teaching context:

```text
GET /api/v1/teaching-assignments/{assignmentId}/absence-justifications
  -> match absenceId or studentId
GET /api/v1/absence-justifications/{justificationId}/document
  -> binary supporting document
```

The binary document operation is for download and does not return JSON metadata. Metadata is
already present in `AbsenceJustificationResponse`.

### Read Progression and Graduation Decisions

Progression belongs to an annual Academic Registration. The single-record operation is:

```text
GET /api/v1/academic-registrations/{academicRegistrationId}/progression-decision
```

The cohort operation uses the Level and Academic Year:

```text
GET /api/v1/academic-levels/{academicLevelId}/academic-years/{academicYearId}/progression-decisions
```

It returns Student identity, both Semester results, annual average, outstanding Module count, and
decision status. Semester validation and progression are not equivalent: a Student can have a non-
validated Semester but receive a progression decision that permits advancement with Module debt.

Graduation is available only for a terminal Academic Level:

```text
GET /api/v1/academic-levels/{academicLevelId}/academic-years/{academicYearId}/graduation-decisions
```

The result contains the Degree Cycle, terminal Level, graduation status, and graduation average.
It is a separate decision from annual progression.

### Read Admin Permissions

Resolve an Admin through the Establishment Admin collection and then read grants:

```text
GET /api/v1/establishments/{id}/admins?query={identity}
GET /api/v1/admins/{id}/permission-grants
GET /api/v1/permissions
```

The grant response identifies assigned permissions for one Admin. The permission catalog defines
all available permission codes and display names; the absence of a grant means the Admin does not
hold that permission. Super Admin authority does not depend on Admin Permission Grants.

## Compact Resolution Recipes

### Find a Person

Student:

```text
GET /api/v1/establishments/{establishmentId}/students/directory?query={name-or-identifier}
  -> inspect content
  -> select studentId using exact response fields
```

Professor:

```text
GET /api/v1/establishments/{establishmentId}/professors?query={name-or-identifier}
  -> select professorId
```

Admin:

```text
GET /api/v1/establishments/{id}/admins?query={name-or-identifier}
  -> select id
```

### Resolve a Program Academic Context

Use for Program/Filiere curriculum, students, teaching plan, schedules, exams, grades,
progression, and graduation.

```text
GET /api/v1/establishments/{establishmentId}/academic-years
  -> select status ACTIVE for current year or match label
GET /api/v1/establishments/{establishmentId}/departments
GET /api/v1/departments/{departmentId}/program-filieres for each Department
  -> match Program code or name
GET /api/v1/program-filieres/{programFiliereId}/academic-levels
  -> match level name
GET /api/v1/academic-levels/{academicLevelId}/semesters?academicYearId={academicYearId}
  -> match Semester name or period
```

The Semester endpoint requires `academicYearId` as a query parameter. Do not omit it and then
attempt to filter a cross-year response.

### Open a Student Academic Record

```text
GET /api/v1/establishments/{establishmentId}/students/directory?query=Lina Idrissi
  -> selected Student field is studentId
GET /api/v1/establishments/{establishmentId}/academic-years
  -> select current or requested year
GET /api/v1/students/{studentId}/academic-registrations
  -> match academicYearId and requested context
  -> selected registration field is id
```

The frontend route placeholder is named `academicRegistrationId`, but the API response field is
`id`. Build it with `{{registration.id}}`, never `{{registration.academicRegistrationId}}`.

### Open Student Semester Grades

```text
student directory search
  -> current/requested academic year
  -> Student academic registration for that year
  -> Program Academic Levels; select registration.academicLevelId and requested level name
  -> Level Semesters with required academicYearId query; select requested Semester
  -> /management/students/{studentId}/academic-record/{registrationId}?tab=grades&semesterId={semesterId}
```

The UI loads the grades after navigation. Do not read the grades endpoint merely to construct the
route. Read it only when grade values are part of the requested information.

### Open a Program Schedule

```text
resolve Program Academic Context
  -> /management/programs/{programFiliereId}?academicYearId={academicYearId}&academicLevelId={academicLevelId}&semesterId={semesterId}&section=schedule
```

Opening only the Program parent is incomplete when the user asked for the schedule.

### Resolve a Class Schedule

```text
resolve Program Academic Context
GET /api/v1/academic-levels/{academicLevelId}/class-groups?academicYearId={academicYearId}
  -> select classGroupId
GET /api/v1/establishments/{establishmentId}/semester-schedules
  -> select academicYearId + semesterId
GET /api/v1/semester-schedules/{scheduleId}/entries
  -> select entries whose class/teaching audience applies
```

### Resolve an Exam or Grade Sheet

```text
resolve Program Academic Context
GET /api/v1/establishments/{establishmentId}/exam-schedules
  -> select academicYearId + semesterId + sessionType
GET /api/v1/exam-schedules/{examScheduleId}/module-exams
  -> match subjectModuleId and classGroupId
GET /api/v1/module-exams/{moduleExamId}/grade-sheet
```

### Resolve Professor Teaching and Timetable

```text
GET /api/v1/establishments/{establishmentId}/professors?query={name-or-identifier}
  -> professorId
GET /api/v1/establishments/{establishmentId}/teaching-assignments
  -> match professorId and requested academic context
GET /api/v1/establishments/{establishmentId}/semester-schedules
  -> match assignment semester/year
GET /api/v1/semester-schedules/{scheduleId}/entries
  -> match teachingAssignmentId or professorId
```

### Resolve Attendance and Justification

```text
resolve Student or Teaching Assignment
GET /api/v1/establishments/{establishmentId}/absences with supported UUID filters
  -> absenceId + teachingAssignmentId
GET /api/v1/teaching-assignments/{assignmentId}/absence-justifications
  -> match absenceId or studentId
```

## Collection Traversal and Record Selection

Some resources do not have an Establishment-wide search endpoint. Programs are the main example:
the collection is exposed below a Department. A consumer that knows the Program name but not the
Department must list the Establishment's Departments and inspect each Department's Program
collection. This traversal must remain bounded to the returned parent collection.

Record identity is resolved using the following rules:

- UUID fields use exact equality.
- Stable codes and names use case-insensitive equality when user-entered capitalization may differ.
- Partial matching is appropriate only for a search field explicitly described as free text.
- Multiple conditions on one record are combined with AND. For a person, first and last name must
  match the same returned profile.
- A unique collection response can be selected directly. An empty response means no record was
  found. Multiple responses remain ambiguous until a documented identifier or context distinguishes
  one of them.
- Paged responses are incomplete unless every needed page has been read. A unique identity search
  should narrow with `query` instead of scanning unfiltered pages.
- Child records retain their parent IDs. Those IDs establish ownership and must be preserved with a
  final result instead of presenting the child object without its context.

## Contract Maintenance

The generated backend OpenAPI document is authoritative for path and query parameter existence.
This document supplies business meaning and cross-endpoint workflows. If controller code changes,
regenerate OpenAPI and update this file in the same feature change.
