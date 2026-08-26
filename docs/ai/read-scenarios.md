# Supported Read Questions and Resolution Scenarios

## Purpose

This document maps the questions a management user may ask to the exact implemented read-only API
workflow needed to answer the question or open the corresponding page. It complements the endpoint
reference: the endpoint reference defines individual contracts, while this document defines complete
business questions that may require several dependent GET requests.

Only registered GET endpoints may be used. Never invent a shorter actor-oriented endpoint because a
management page displays data assembled from several resources. Apply the caller's establishment
context and existing authorization to every request.

## Identity and Platform Questions

### Identify the Current User, Role, and Establishment

Questions covered: "Who am I?", "What role am I logged in with?", "Which establishment am I
managing?", and "What is my account status?"

1. Call `GET /api/v1/auth/me`.
2. Read `accountId`, `role`, `roleEntityId`, `establishmentId`, `email`, and account/profile fields.
3. If establishment details are requested and `establishmentId` exists, call
   `GET /api/v1/establishments/{id}`.

Do not infer the caller from the current UI route.

### Read University and Establishment Information

Questions covered: university name, establishment name, code, status, address, contact details, and
which establishments belong to the university.

- Current university: `GET /api/v1/university`.
- University by UUID: `GET /api/v1/university/{id}`.
- Establishments of a university: `GET /api/v1/university/{universityId}/establishments`.
- Establishment details: `GET /api/v1/establishments/{id}`.

To find an establishment by name or code, list the university establishments and match the returned
`name` or `code` case-insensitively. There is no establishment search endpoint.

## Managed Account Questions

### Find a Super Admin or Read Establishment Leadership

Questions covered: "Who are the Super Admins of this establishment?", finding one Super Admin by
name/email/CIN, account status, or opening their record.

1. Call `GET /api/v1/establishments/{establishmentId}/super-admins`.
2. Match identity fields such as `firstName`, `lastName`, `universityEmail`, or `cin` when a specific
   person is requested.
3. Read the selected record with `GET /api/v1/super-admins/{superAdminId}` when full details are
   needed.
4. Open `/management/establishments/{establishmentId}/super-admins` for the directory.

### Find, Count, or Filter Establishment Admins

Questions covered: finding an Admin, listing active/inactive/locked/archived Admins, counting Admins,
and filtering by creation or join dates.

Call `GET /api/v1/establishments/{id}/admins` with supported query parameters such as `query`,
`status`, `createdFrom`, and `createdTo`. Use `query` for names, email, CIN, or other supported
identity text. Count the returned records only after applying the requested filters. Use
`GET /api/v1/admins/{id}` for one selected Admin.

Open `/management/establishments/{establishmentId}/admins` for the Admin directory or
`/management/establishments/{establishmentId}/admins/{adminId}` for one record.

### Determine an Admin's Permissions

Questions covered: "What can this Admin manage?", "Can this Admin manage schedules?", comparing an
Admin's grants with available permissions, and opening permission management.

1. Resolve the Admin with `GET /api/v1/establishments/{id}/admins?query={identity}`.
2. Call `GET /api/v1/admins/{id}/permission-grants` using the Admin response `id`.
3. Call `GET /api/v1/permissions` when permission names/descriptions or the complete catalog is
   needed.
4. Compare permission codes exactly; do not infer permissions from the Admin's title.

## Student Identity and Registration Questions

### Find a Student by Name or Identifier

Questions covered: finding a Student by first/last name, Apogee code, CNE, CIN, email, or student ID.

Use `GET /api/v1/establishments/{establishmentId}/students?query={identity}` for direct identity
search. Match `studentId`, `firstName`, and `lastName` from the response. Use
`GET /api/v1/students/{studentId}` for full personal and account information.

Do not use the academic-registration directory unless the question also includes an academic year,
program, level, semester, or class context.

### How Many Students Are Registered in a Cohort, Level, Semester, or Academic Year

Questions covered: students in a Program/Filiere, Academic Level, Semester, Class Group, Academic
Year, Program Path, or combinations of these filters.

Use `GET /api/v1/establishments/{establishmentId}/students/directory` with the supported IDs:
`academicYearId`, `programFiliereId`, `academicLevelId`, `semesterId`, `classGroupId`, and other
documented filters. Apply `query` when an identity search is also requested. Use returned pagination
metadata when counting or traversing multiple pages. `totalElements` is the complete filtered count;
never treat the first page's `content` size as the total.

Open `/management/establishments/{establishmentId}/students` for the global directory or the
Program curriculum Students section when the question provides a Program context.

### Read a Student's Academic History

Questions covered: current registration, previous years, Program/Filiere history, Academic Levels,
registration status, repeated years, and historical records.

1. Resolve the Student.
2. Call `GET /api/v1/students/{studentId}/academic-registrations`.
3. Use each registration's `academicYearId`, `programFiliereId`, and `academicLevelId` to resolve
   labels through the relevant Academic Structure endpoints when labels are not already present.
4. Sort by Academic Year `startYear` for chronological questions.
5. Use `GET /api/v1/academic-registrations/{academicRegistrationId}` for one selected annual record.

Open `/management/establishments/{establishmentId}/students/{studentId}` and select the relevant
Academic History record.

### Read a Student's Semester and Module Registrations

Questions covered: which Semesters or Modules a Student is registered in, first/second inscription,
carried modules, active module registrations, and the original Semester of a debt module.

1. Resolve the Student's annual Academic Registration.
2. Call `GET /api/v1/academic-registrations/{academicRegistrationId}/semester-registrations`.
3. Select the Semester Registration by `semesterId` or returned Semester context.
4. Call `GET /api/v1/semester-registrations/{semesterRegistrationId}/module-registrations`.
5. Inspect `inscriptionNumber`, status, original registration/result references, and Subject Module
   IDs. `inscriptionNumber > 1` identifies a later Module inscription.

Do not assume every Module Registration belongs to the Student's current Semester; carried modules
retain their original academic context.

### Determine a Student's Class Group and Teaching Groups

Questions covered: class assignment, TD group, TP group, cohort group, and whether a Student belongs
to a particular teaching audience.

1. Resolve the annual Academic Registration and Semester.
2. Call
   `GET /api/v1/academic-registrations/{registrationId}/semesters/{semesterId}/class-assignment`.
3. Read Teaching Groups from `GET /api/v1/semesters/{semesterId}/teaching-groups` and identify the
   groups whose membership includes the Student/registration when membership data is exposed.
4. For a complete level roster grouped by class, call
   `GET /api/v1/academic-levels/{academicLevelId}/class-groups/roster?academicYearId={academicYearId}`.

## Student Grades and Decisions

### Read a Student's Normal, Rattrapage, or Effective Final Grades

Questions covered: grades in a Module/Semester/Academic Level/Academic Year, Normal Session grades,
Rattrapage grades, final effective grades, absent results, validated/non-validated Modules, averages,
and second-inscription replacement results.

1. Resolve the Student.
2. Call `GET /api/v1/students/{studentId}/grades` with `academicYearId`, `academicLevelId`,
   `semesterId`, `sessionType`, or `view` when the question supplies them.
3. Use `view=ORIGINAL` for the historical result as originally published and `view=EFFECTIVE` for
   the current result after later inscriptions. Do not mix the two views in one conclusion.
4. Use `sessionType=NORMAL` or `sessionType=RATTRAPAGE` only when that session is explicitly asked.
5. Match Subject Modules by returned `subjectModuleId`, code, or title.

For the authenticated Student use `GET /api/v1/me/grades` with the same documented filters.

### Determine Whether a Student Validated a Semester

Questions covered: "Did Lina validate S3?", Semester average, validation by compensation, original
versus updated Semester result, and why a Semester is not validated.

1. Resolve the Student, annual Academic Registration, and Semester Registration.
2. Call `GET /api/v1/semester-registrations/{semesterRegistrationId}/result`.
3. Read `resultStatus`, `semesterAverage`, compensation information, and original/effective result
   fields exactly as returned.
4. If no result exists, state that no Semester Result is available; do not calculate an official
   validation decision from individual grades.

### Determine Whether a Student Validated an Academic Level or Can Progress

Questions covered: promoted, promoted by compensation, promoted with module debt, repeat, failed,
terminal-level validation, annual average, and outstanding Module count.

1. Resolve the annual Academic Registration.
2. Call `GET /api/v1/academic-registrations/{academicRegistrationId}/progression-decision`.
3. Read the persisted decision status and explanation fields. Do not infer promotion solely from one
   Semester result.
4. For the whole cohort, call
   `GET /api/v1/academic-levels/{academicLevelId}/academic-years/{academicYearId}/progression-decisions`.

### Determine Graduation Status

Questions covered: graduated/not graduated, graduation average, graduation decision for one Student,
and graduates of a final Academic Level.

- Authenticated Student: `GET /api/v1/me/graduation-decisions`.
- Managed cohort: `GET /api/v1/academic-levels/{academicLevelId}/academic-years/{academicYearId}/graduation-decisions`.

Resolve the Student in the returned decision collection by `studentId`. Graduation is not equivalent
to ordinary progression and must not be inferred from a terminal Academic Level alone.

## Professor Questions

### Find a Professor and Read Their Profile

Questions covered: finding a Professor by name, employee number, email, CIN, status, hire date,
Academic Domain expertise, or Academic Rank.

Use `GET /api/v1/establishments/{establishmentId}/professors` with `query`, `status`, `joinedFrom`,
`joinedTo`, or `academicDomainId`. Select the returned `professorId`, then use
`GET /api/v1/professors/{professorId}` for details and
`GET /api/v1/professors/{professorId}/expertise` for approved Academic Domains.

### Read What a Professor Teaches

Questions covered: Teaching Assignments, Course/TD/TP delivery, Modules, classes, teaching groups,
audiences, active assignments, and all teaching assigned to one Professor.

1. Resolve the Professor.
2. Call `GET /api/v1/establishments/{establishmentId}/teaching-assignments`.
3. Filter the returned collection by exact `professorId`.
4. Resolve `subjectModuleId`, Teaching Requirement, Class Group, or Teaching Group details only when
   the response does not already contain the requested labels.

For the authenticated Professor, use `GET /api/v1/me/teaching-assignments`, which is already scoped.

### Read Modules and Classes a Professor Is Responsible For

Questions covered: assessment responsibility, grade ownership, exam responsibility, Module/Class
responsibility, and the difference between teaching and academic responsibility.

1. Resolve the Professor.
2. Call `GET /api/v1/establishments/{establishmentId}/module-class-responsibilities`.
3. Filter by exact `professorId`.
4. Do not substitute Teaching Assignments: TD/TP teaching does not automatically grant grade or exam
   responsibility.

For the authenticated Professor use `GET /api/v1/me/module-class-responsibilities`.

### Check Professor Availability, Free Time, or Scheduling Conflicts

Questions covered: "Is Professor Yassine available Monday morning?", free/busy at a date, weekday,
or time range, existing sessions, overlapping teaching, and where/when a Professor teaches.

There is no implemented management endpoint named
`professors/{professorId}/schedule-entries`. Never invent that path. Management assembles the answer
through Semester Schedules:

1. Resolve the Professor using
   `GET /api/v1/establishments/{establishmentId}/professors?query={identity}`.
2. Resolve the requested Academic Year and, when supplied, term/Semester context.
3. Call `GET /api/v1/establishments/{establishmentId}/semester-schedules`.
4. Keep schedules in the relevant Academic Year/term. If the question gives no period, inspect the
   current active Academic Year and active/current Semester period rather than all history.
5. For each relevant schedule call `GET /api/v1/semester-schedules/{scheduleId}/entries`.
6. Keep entries whose `professorId` equals the resolved Professor's `professorId`.
7. For weekday availability, compare `dayOfWeek` case-insensitively.
8. For time availability, an existing entry conflicts when
   `entry.startTime < requestedEndTime` AND `entry.endTime > requestedStartTime`.
9. "Morning" is not a precise stored value. If the user gives no hours, report the Professor's
   morning entries instead of inventing a universal morning boundary, or ask for a time range when a
   strict availability decision is required.

The management UI destination is
`/management/professors/{professorId}?tab=schedule`.

### Calculate a Professor's Teaching Load

Questions covered: weekly assigned hours/minutes, number of assignments, number of Modules/classes,
remaining capacity, and whether the Professor exceeds their configured load.

1. Resolve the Professor and read `maximumWeeklyTeachingMinutes`.
2. Read establishment Teaching Assignments and filter by `professorId`.
3. Read relevant published Schedule Entries through the Semester Schedule workflow and filter by
   `professorId`.
4. Sum `endTime - startTime` for the requested Academic Year and term only.
5. Compare the sum with `maximumWeeklyTeachingMinutes`. Do not count inactive assignments without a
   scheduled entry as delivered time.

## Academic Structure Questions

### Resolve a Program/Filiere from Department, Path, or Cycle

Questions covered: finding a Program by code/name, Programs in a Department, Standard/Excellence
Programs, Programs in a Degree Cycle, and opening a Program curriculum.

1. List Departments, Program Paths, and Degree Cycles for the Establishment when their IDs are not
   known.
2. Call `GET /api/v1/departments/{departmentId}/program-filieres` for candidate Programs.
3. Match `code` or `name`; additionally compare `programPathId` and `degreeCycleId` when the question
   includes Path or Cycle.
4. Open `/management/programs/{programFiliereId}` with the selected academic context query values.

### Resolve Current or Historical Academic Context

Questions covered: current Academic Year, planned/closed years, M1/M2 or other Academic Levels, S1-S4
Semesters, Autumn/Spring period, and Semester dates/status.

1. Call `GET /api/v1/establishments/{establishmentId}/academic-years` and select `status=ACTIVE` for
   "current academic year".
2. Resolve the Program, then call
   `GET /api/v1/program-filieres/{programFiliereId}/academic-levels`.
3. Match Academic Level `name` case-insensitively.
4. Call `GET /api/v1/academic-levels/{academicLevelId}/semesters?academicYearId={academicYearId}`.
5. Match Semester name and inspect `termType`, start/end dates, and derived status.

### Read Modules in a Semester or Program Context

Questions covered: Semester curriculum, Module code/title, Modules in M1/S1, Academic Domains,
Course/TD/TP components, sessions per week, duration, and room requirements.

1. Resolve Program, Academic Level, Academic Year, and Semester.
2. Call `GET /api/v1/semesters/{semesterId}/subject-modules`.
3. Select a Module by code/title when needed, then call
   `GET /api/v1/subject-modules/{subjectModuleId}`.
4. Call `GET /api/v1/subject-modules/{subjectModuleId}/teaching-components` for Course/TD/TP details.
5. Resolve Academic Domains through `GET /api/v1/establishments/{establishmentId}/academic-domains`
   and Module domain IDs where required.

### Read Academic Rules Applied to a Level or Semester

Questions covered: validation threshold, compensation, progression, maximum inscriptions, attendance
limits, which profile applied in a year/Semester, and historical rule assignments.

1. Resolve Academic Level and Academic Year/Semester.
2. Call `GET /api/v1/academic-levels/{academicLevelId}/rule-assignments`.
3. Select the assignment whose Academic Year and optional Semester scope matches the requested
   context.
4. Resolve its profile with `GET /api/v1/academic-rule-profiles/{academicRuleProfileId}`.
5. Report configured rules; do not recompute an official Student decision unless the relevant result
   endpoint is also read.

## Grouping and Delivery Questions

### Read Class Groups and Rosters

Questions covered: available Class Groups, number of students per group, members of Group A/B,
unassigned Students, and class assignment for an Academic Level and Year.

1. Call
   `GET /api/v1/academic-levels/{academicLevelId}/class-groups?academicYearId={academicYearId}`.
2. Call
   `GET /api/v1/academic-levels/{academicLevelId}/class-groups/roster?academicYearId={academicYearId}`
   for membership and counts.
3. Match Class Group `id`/`name`; count roster members, not configured capacity.

### Read TD and TP Teaching Groups

Questions covered: TD/TP groups, subgroup membership, source Class Group, group size policy, and which
subgroup a Student attends.

1. Call `GET /api/v1/academic-levels/{academicLevelId}/teaching-group-policies` for configured
   minimum/maximum sizes.
2. Call `GET /api/v1/semesters/{semesterId}/teaching-groups` for generated groups and membership.
3. Filter by component/group type (`TD` or `TP`) and source Class Group.

### Read Teaching Requirements and Assignment Coverage

Questions covered: required Course/TD/TP delivery, assigned/unassigned requirements, responsible
Professor, audience, and missing teaching coverage.

1. Call `GET /api/v1/semesters/{semesterId}/teaching-requirements`.
2. Call `GET /api/v1/establishments/{establishmentId}/teaching-assignments`.
3. Match Teaching Assignment `teachingRequirementId` to each requirement.
4. Requirements without an active matching assignment are unassigned.
5. Resolve Professor and Module labels only when absent from responses.

## Timetable and Facility Questions

### Read a Class, Semester, or Program Timetable

Questions covered: weekly schedule, sessions on a weekday, Course/TD/TP timetable, room, Professor,
class/teaching-group audience, published/draft schedule, and historical schedules.

1. Resolve Academic Year, Program, Academic Level, Semester, and optional Class Group.
2. Call `GET /api/v1/establishments/{establishmentId}/semester-schedules`.
3. Select schedules by `academicYearId`, `semesterId`, and optional `classGroupId`.
4. Call `GET /api/v1/semester-schedules/{scheduleId}/entries` for each selected schedule.
5. Filter entries by weekday, Module, Professor, Class Group, Teaching Group, or room when requested.

Open the Program curriculum route with `section=schedule`, plus `academicYearId`, `academicLevelId`,
and `semesterId`.

### Check Room Availability or Room Conflicts

Questions covered: whether a room is free, what occupies a room, free rooms at a day/time, room
capacity/type/block, and schedule conflicts.

1. Resolve Rooms with `GET /api/v1/establishments/{establishmentId}/rooms`; apply `blockId`, status,
   room type, or identity filtering where supported.
2. Resolve relevant Semester Schedules for the current/requested Academic Year and term.
3. Read their entries and keep entries whose `roomId` matches.
4. Apply the overlap rule:
   `entry.startTime < requestedEndTime` AND `entry.endTime > requestedStartTime`.
5. A room with no overlapping entry is available in the inspected scheduling context. State the
   context used; do not claim universal availability across uninspected Academic Years/terms.

### Find Rooms by Capacity, Type, or Block

Questions covered: Amphitheatres, computer labs, rooms in a Block, standalone rooms, rooms fitting a
group size, and active/inactive facilities.

1. List Blocks through `GET /api/v1/establishments/{establishmentId}/blocks` when Block context is
   requested.
2. Call `GET /api/v1/establishments/{establishmentId}/rooms` with supported filters.
3. Compare `capacity >= requestedCapacity`; match room type/status exactly.
4. A null `blockId` identifies a standalone room or Amphitheatre outside a Block.

## Examination Questions

### Read Normal or Rattrapage Exam Planning

Questions covered: Exam Schedule dates, Normal/Rattrapage Session, exams in a Semester/Class,
upcoming exams, Module date/time, and published planning.

1. Resolve Academic Year and Semester.
2. Call `GET /api/v1/establishments/{establishmentId}/exam-schedules`.
3. Select by `academicYearId`, `semesterId`, `sessionType`, Class Group, and publication status as
   supported.
4. Call `GET /api/v1/exam-schedules/{examScheduleId}/module-exams`.
5. Filter Module Exams by Subject Module, Class Group, date, or requested time.

### Read Exam Groups, Rooms, or Candidate Lists

Questions covered: exam-room groups, which room a group uses, candidate/invitation list, whether a
Student may sit an exam, and room allocation for a Module Exam.

- Exam group plan:
  `GET /api/v1/exam-schedules/{examScheduleId}/class-groups/{classGroupId}/exam-groups`.
- Room allocations: `GET /api/v1/module-exams/{moduleExamId}/room-allocations`.
- Eligible candidate list: `GET /api/v1/module-exams/{moduleExamId}/candidates`.

Resolve the Student by `studentId` in the candidate response. Absence from the candidate collection
means the Student is not included in that generated list; do not invent a specific exclusion reason
unless another response provides it.

## Grade Administration Questions

### Read an Exam Grade Sheet

Questions covered: grades entered for one Module Exam, missing grades, earned/absent result reason,
draft/published status, and all candidates with their grades.

1. Resolve the Exam Schedule and Module Exam.
2. Call `GET /api/v1/module-exams/{moduleExamId}/grade-sheet`.
3. Use candidate/grade fields exactly as returned. A missing Grade Record is different from a grade
   of zero and from an `ABSENT` reason.

### Read Final Module Results for a Class

Questions covered: final effective grade after Normal/Rattrapage, validated/non-validated Module,
compensation, class results by Module, and students invited to Rattrapage.

Call
`GET /api/v1/semesters/{semesterId}/class-groups/{classGroupId}/final-results` with documented view or
session filters. Select by Student and Subject Module. Use official returned status instead of
recreating the Academic Rule engine in the AI.

### Read Semester Results for a Class

Questions covered: Student averages, validated/not validated Semester, compensated Semester,
original/effective result, and the result table for a Class Group.

Call
`GET /api/v1/semesters/{semesterId}/class-groups/{classGroupId}/semester-results`. Match the Student
by `studentId`. Use `GET /api/v1/semester-registrations/{semesterRegistrationId}/result` when the
question concerns one known Semester Registration.

## Attendance Questions

### Read Absences for a Class, Assignment, Student, or Module

Questions covered: absent Students, number of absences, justified/unjustified absences, absences in a
Module, and attendance records created by a Professor.

- One Teaching Assignment: `GET /api/v1/teaching-assignments/{teachingAssignmentId}/absences`.
- Establishment management: `GET /api/v1/establishments/{establishmentId}/absences` with supported
  Student, Module, Professor, assignment, status, or date filters.
- Authenticated Student: `GET /api/v1/me/absences`.

Count records after applying the requested filters. Do not interpret lack of an Absence Record as a
complete attendance history when the question's date/assignment context was not inspected.

### Read Absence Justifications and Documents

Questions covered: pending/approved/rejected justification, reason, supporting document, Professor
review, and justifications for one Teaching Assignment or Student.

- Professor assignment context:
  `GET /api/v1/teaching-assignments/{assignmentId}/absence-justifications`.
- Authenticated Student: `GET /api/v1/me/absence-justifications`.
- Supporting file: `GET /api/v1/absence-justifications/{justificationId}/document`.

Only request the document endpoint when the user asks to inspect/download the supporting file.

## Counting, Comparison, and Availability Rules

### Count Records Correctly

Questions covered: "How many Students/Admins/Professors/rooms/Modules/exams?"

1. Use the endpoint owned by the requested resource and apply all stated context filters.
2. For paginated responses, use `totalElements`; do not count only the current `content` array.
3. For non-paginated responses, count the complete returned array.
4. State the Academic Year, Semester, status, or establishment scope used when it affects the count.

### Compare Two People, Groups, Periods, or Results

Questions covered: comparing Student averages, Professor workloads, group sizes, Semester results,
year-over-year counts, or Normal versus Rattrapage outcomes.

Resolve both sides through the same endpoint family and same academic/filter context. Do not compare
an original result with an effective result, or one Professor's current-term workload with another
Professor's all-history assignments.

### Interpret Natural Time Expressions

Questions covered: today, Monday, morning, afternoon, current Semester, current Academic Year,
upcoming, previous year, and last Semester.

- Current Academic Year means the record with `status=ACTIVE`.
- Current Semester/term must be resolved from the active year's Semester dates/status where possible.
- Weekday names map to `dayOfWeek` values such as `MONDAY`.
- "Morning" and "afternoon" are not universal stored boundaries. Return matching sessions or ask for
  exact times before making a strict free/busy decision.
- "Upcoming" means dates/times after the current date/time within the requested academic context.

## Navigation Versus Direct Answer

### Choose Direct Answer Mode

Use `ANSWER` when the user asks for a fact, count, status, availability decision, comparison, date,
time, grade, average, or explanation that can be supported by retrieved API data. The plan must read
all records required for the answer; the final answer must use only verified response data.

### Choose Navigation Mode

Use `NAVIGATE` when the user asks to open, show, go to, manage, inspect in the interface, or view a
page. Resolve every ID required by the documented management route. Never return a route containing
an unresolved name or invented identifier.

When the question both asks a fact and asks to open its page, prefer navigation only if opening the
page is the explicit final action; otherwise answer the fact directly.
