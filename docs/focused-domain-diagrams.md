# Focused Domain Diagrams

These diagrams present the domain model as smaller business views. Each diagram covers one coherent area, shows the principal relationships, and limits attributes to those needed to understand the model.

## 1. Identity and Governance

Scope:

- Shows the separation between authentication identity, shared personal profile data, and business role profiles.
- Shows how university and establishment governance roles are organized.

```mermaid
classDiagram
class UserAccount {
  universityEmail
  accountStatus
}

class UserProfile {
  firstName
  lastName
  birthDate
  placeOfBirth
  nationality
  cin
  sex
  phoneNumber
}

class University {
  name
}

class Establishment {
  name
  status
}

class RootSuperAdmin
class SuperAdmin
class Admin
class Professor
class Student {
  apogeeCode
  nationalStudentCode
  initialEnrollmentDate
}
class Permission {
  code
  name
}

class AdminPermissionGrant {
  grantedAt
}

UserAccount --> UserProfile : personal_profile
UserAccount --> RootSuperAdmin : profile
UserAccount --> SuperAdmin : profile
UserAccount --> Admin : profile
UserAccount --> Professor : profile
UserAccount --> Student : profile

University --> Establishment : contains

Establishment --> SuperAdmin : has
Establishment --> Admin : has
Establishment --> Professor : has
Establishment --> Student : has

Admin --> AdminPermissionGrant : has_grant
AdminPermissionGrant --> Permission : references
```

## 2. Academic Structure

Scope:

- Shows the academic hierarchy inside an establishment.
- Clarifies department, degree cycle, program path, program/filiere, level, yearly semester, and subject/module structure.

```mermaid
classDiagram
class Establishment {
  name
  status
}

class Department {
  name
}

class DegreeCycle {
  name
}

class ProgramPath {
  name
}

class ProgramFiliere {
  code
  name
}

class AcademicLevel {
  name
  order
  terminalLevel
}

class AcademicYear {
  label
  startYear
  endYear
  status
}

class Semester {
  name
  order
  termType
  startDate
  endDate
  lifecycleStatus
}

class SubjectModule {
  code
  title
}

class ClassGroup {
  name
}

class AcademicRuleProfile {
  name
  version
  ruleDefinition
  status
}

class AcademicRuleSet {
  moduleRules
  semesterRules
  academicLevelRules
  progressionRules
  useSharedSemesterRules
  autumnSemesterRules
  springSemesterRules
}

class AcademicLevelRuleAssignment {
  status
}

Establishment --> Department : contains
Establishment --> DegreeCycle : defines
Establishment --> ProgramPath : defines
Establishment --> AcademicYear : manages
Establishment --> AcademicRuleProfile : defines
AcademicRuleProfile --> AcademicRuleSet : rule_definition

Department --> ProgramFiliere : offers
DegreeCycle --> ProgramFiliere : frames
ProgramPath --> ProgramFiliere : classifies

ProgramFiliere --> AcademicLevel : contains
AcademicLevel --> Semester : contains
AcademicYear --> Semester : scopes
Semester --> SubjectModule : contains

AcademicLevel --> ClassGroup : has_groups
AcademicYear --> ClassGroup : scoped_by

AcademicLevel --> AcademicLevelRuleAssignment : governed_by
AcademicYear --> AcademicLevelRuleAssignment : effective_for
AcademicRuleProfile --> AcademicLevelRuleAssignment : assigned_through
```

## 3. Student Registration and Academic Path

Scope:

- Shows how a student is registered academically.
- Clarifies annual academic-level registration, automatic semester and module registration, later class assignment, and inscription history.

```mermaid
classDiagram
class UserAccount {
  universityEmail
  accountStatus
}

class Student {
  apogeeCode
  nationalStudentCode
  initialEnrollmentDate
}

class ProgramFiliere {
  code
  name
}

class AcademicLevel {
  name
  order
  terminalLevel
}

class AcademicYear {
  label
  startYear
  endYear
  status
}

class Semester {
  name
  order
  termType
  startDate
  endDate
  lifecycleStatus
}

class ClassGroup {
  name
}

class SubjectModule {
  code
  title
}

class AcademicRegistration {
  status
}

class SemesterRegistration

class UserAccount

class Student

class Semester

class StudentClassAssignment

class ModuleRegistration {
  inscriptionNumber
  status
}

UserAccount --> Student : profile

Student --> AcademicRegistration : registers_in
ProgramFiliere --> AcademicRegistration : target
AcademicYear --> AcademicRegistration : scoped_by
AcademicYear --> Semester : scopes

AcademicLevel --> AcademicRegistration : registered_level

AcademicRegistration --> SemesterRegistration : contains
Semester --> SemesterRegistration : semester
SemesterRegistration --> StudentClassAssignment : has_class_assignment
StudentClassAssignment --> ClassGroup : assigned_to
AcademicYear --> ClassGroup : scopes

SemesterRegistration --> ModuleRegistration : includes
SubjectModule --> ModuleRegistration : module
AcademicLevel --> ModuleRegistration : origin_for_carried_module
```

## 4. Teaching Delivery and Academic Planning

Scope:

- Shows how Course, TD, and TP requirements become professor assignments and scheduled room allocations.
- Separates Professor expertise, generated teaching audiences, and concrete teaching requirements.
- Shows separate Normal and Rattrapage schedules containing module-level exam occurrences.

```mermaid
classDiagram
class UserAccount {
  universityEmail
  accountStatus
}

class Professor {
  employeeNumber
  maximumWeeklyTeachingMinutes
}

class AcademicRank {
  code
  name
  seniorityOrder
  canHoldModuleResponsibility
  status
}

class TeachingAssignmentRankPreference {
  componentType
  priority
  status
}

class AcademicDomain {
  name
}

class ProfessorExpertise

class Establishment {
  name
  status
}

class AcademicYear {
  label
  startYear
  endYear
  status
}

class Semester {
  name
  order
  termType
  startDate
  endDate
  lifecycleStatus
}

class SubjectModule {
  code
  title
}

class ModuleTeachingComponent {
  componentType
  sessionsPerWeek
  sessionDurationMinutes
  audienceMode
  requiredRoomType
}

class TeachingGroupPolicy {
  groupType
  minimumGroupSize
  maximumGroupSize
}

class SubjectModuleDomain

class ClassGroup {
  name
}

class TeachingGroup {
  name
  audienceType
  groupType
  sourceClassGroup
}

class TeachingGroupMembership

class SemesterRegistration

class TeachingRequirement {
  status
}

class TeachingAssignment {
  status
  assignmentSource
}

class ModuleClassResponsibility {
  status
}

class SemesterSchedule {
  publicationStatus
  publishedAt
}

class ScheduleEntry {
  dayOfWeek
  startTime
  endTime
}

class Block {
  code
  name
  status
}

class Room {
  code
  name
  roomType
  capacity
  status
}

class ExamSchedule {
  publicationStatus
  sessionType
  startDate
  endDate
}

class ModuleExam {
  examDate
  startTime
  endTime
  candidateListGeneratedAt
}

class ExamGroup {
  label
  groupOrder
}

class ExamGroupMembership

class ExamRoomAllocation

UserAccount --> Professor : profile

Professor --> ProfessorExpertise : has_expertise
ProfessorExpertise --> AcademicDomain : domain
Establishment --> AcademicRank : defines
AcademicRank --> Professor : classifies
Establishment --> TeachingAssignmentRankPreference : configures
TeachingAssignmentRankPreference --> AcademicRank : prefers
SubjectModule --> ModuleTeachingComponent : delivery_components
AcademicLevel --> TeachingGroupPolicy : grouping_policy
AcademicYear --> TeachingGroupPolicy : effective_for
SubjectModule --> SubjectModuleDomain : classified_by
SubjectModuleDomain --> AcademicDomain : domain
Semester --> TeachingGroup : teaching_audiences
ClassGroup --> TeachingGroup : source_class
TeachingGroup --> TeachingGroupMembership : contains
TeachingGroupMembership --> SemesterRegistration : member
ModuleTeachingComponent --> TeachingRequirement : generates
TeachingGroup --> TeachingRequirement : target_audience
TeachingRequirement --> TeachingAssignment : assigned_through
Professor --> TeachingAssignment : teaches
SubjectModule --> ModuleClassResponsibility : responsibility_for
ClassGroup --> ModuleClassResponsibility : scoped_to
Professor --> ModuleClassResponsibility : responsible_for
AcademicYear --> ModuleClassResponsibility : effective_for
Semester --> ModuleClassResponsibility : effective_for

Establishment --> SemesterSchedule : owns
Establishment --> AcademicDomain : defines
Establishment --> Block : owns
Establishment --> Room : owns
Block --> Room : optionally_groups
AcademicYear --> SemesterSchedule : scoped_by
Semester --> SemesterSchedule : scoped_by
SemesterSchedule --> ScheduleEntry : contains
TeachingAssignment --> ScheduleEntry : schedules
Room --> ScheduleEntry : hosts

Establishment --> ExamSchedule : owns
AcademicYear --> ExamSchedule : scoped_by
Semester --> ExamSchedule : scoped_by
ExamSchedule --> ModuleExam : contains
ModuleExam --> SubjectModule : for_module
ModuleExam --> ClassGroup : for_group
ExamSchedule --> ExamGroup : defines_exam_groups
ClassGroup --> ExamGroup : split_from
ExamGroup --> ExamGroupMembership : contains
ExamGroupMembership --> SemesterRegistration : member
ModuleExam --> ExamRoomAllocation : room_allocations
ExamGroup --> ExamRoomAllocation : allocated_group
Room --> ExamRoomAllocation : hosts
```

Notes:

- `ClassGroup` remains the administrative grouping. Whole-cohort audiences combine classes, while class and subgroup teaching groups are derived from one source class and never mix its students with another class.
- `sessionsPerWeek` supports modules taught more than once each week.
- Course, TD, and TP components share the Subject/Module domains but may have different audience sizes, Professors, and room types.
- Rank preferences are establishment-specific and ordered independently for Course, TD, and TP assignments.
- Rank preference is applied after expertise and workload eligibility; it does not make an otherwise ineligible Professor assignable.
- Every room belongs directly to an establishment. A block is optional and only groups rooms in the same establishment, so standalone rooms and amphitheatres remain valid.
- An `ExamGroup` is a temporary split of one Class Group for the full Normal or Rattrapage `ExamSchedule`; it is not a teaching or administrative Class Group.
- `ExamGroupMembership` keeps the same student split across all module exams in that examination schedule.
- `ExamRoomAllocation` assigns each exam group to a room for one `ModuleExam`, allowing rooms to differ between module exams without changing group membership.
- When no split is needed, the Class Group is represented by one exam group and one room allocation for each module exam.
- Components are copied into a new academic year with their Subject/Module configuration, then changed independently when needed.
- Generation produces a draft. Publication remains an explicit management action.

## 5. Assessment, Progression, and Attendance

Scope:

- Shows how raw exam grades produce a calculated final module result for the semester.
- Shows progression decisions separately from raw grades.
- Shows professor-recorded attendance and exam eligibility in teaching context.

```mermaid
classDiagram
class AcademicRegistration {
  status
}

class SemesterRegistration

class UserAccount

class Student

class Professor

class AcademicLevel

class AcademicYear

class AcademicLevelRuleAssignment {
  status
}

class AcademicRuleProfile {
  name
  version
  ruleDefinition
  status
}

class AcademicRuleSet {
  moduleRules
  semesterRules
  academicLevelRules
  progressionRules
  useSharedSemesterRules
  autumnSemesterRules
  springSemesterRules
}

class ModuleRegistration {
  inscriptionNumber
  status
}

class ModuleExam {
  examDate
  startTime
  candidateListGeneratedAt
}

class GradeRecord {
  gradeValue
  zeroGradeReason
  workflowStatus
}

class ModuleResult {
  finalGradeValue
  resultStatus
  originalFinalGradeValue
  originalResultStatus
  calculatedAt
}

class SemesterResult {
  semesterAverage
  resultStatus
  originalSemesterAverage
  originalResultStatus
  evaluatedAt
}

class ProgressionDecision {
  decisionStatus
  annualAverage
  outstandingModuleCount
}

class GraduationDecision {
  decisionStatus
  graduationAverage
  decidedAt
}

class TeachingAssignment {
  status
  assignmentSource
}

class ModuleClassResponsibility {
  status
}

class AbsenceRecord {
  absenceDate
  justified
}

class AttendanceQrSession {
  attendanceDate
  token
  tokenExpiresAt
  closesAt
}

class AbsenceJustification {
  reason
  status
  submittedAt
  reviewedAt
}

class UploadedDocument {
  originalFilename
  contentType
  sizeBytes
  purpose
  status
}

class ExamCandidate

ModuleExam --> ExamCandidate : has_candidates
ModuleRegistration --> ExamCandidate : invited_for
ModuleExam --> GradeRecord : produces
GradeRecord --> ModuleRegistration : result_for
ModuleRegistration --> ModuleResult : resolves_to
AcademicLevel --> ModuleRegistration : origin_for_carried_module
GradeRecord --> ModuleResult : contributes_to
AcademicRuleProfile --> ModuleResult : calculates_under
AcademicRuleProfile --> AcademicRuleSet : rule_definition
AcademicLevel --> AcademicLevelRuleAssignment : governed_by
AcademicYear --> AcademicLevelRuleAssignment : effective_for
AcademicRuleProfile --> AcademicLevelRuleAssignment : assigned_through
SemesterRegistration --> Semester : registers_for
AcademicRegistration --> AcademicLevel : registered_level
AcademicRegistration --> AcademicYear : scoped_by

AcademicRegistration --> ProgressionDecision : results_in
AcademicRuleProfile --> ProgressionDecision : policy_for
AcademicRegistration --> GraduationDecision : completion_results_in

AcademicRegistration --> SemesterRegistration : contains
SemesterRegistration --> SemesterResult : results_in
ModuleResult --> SemesterResult : evaluated_by
AcademicRuleProfile --> SemesterResult : calculates_under
ModuleRegistration --> AbsenceRecord : has_absence
TeachingAssignment --> AbsenceRecord : records
TeachingAssignment --> AttendanceQrSession : opens_for
Professor --> AttendanceQrSession : controls
AbsenceRecord --> AbsenceJustification : has_submissions
Student --> AbsenceJustification : submits
Professor --> AbsenceJustification : reviews
AbsenceJustification "1" --> "1" UploadedDocument : supports_with
UserAccount --> UploadedDocument : owns
```
