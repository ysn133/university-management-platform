# Database Design

This document defines the relational database design, including tables, attributes, ownership, relationships, keys, constraints, and persistence rules.

## 1. Design Scope

The design covers:

- university and establishment structure
- identity and access control
- academic structure
- academic registration and teaching assignment
- schedules, exams, grades, and progression
- absences and exam eligibility
- uploaded evidence and absence-justification records

## 2. Cross-Cutting Conventions

### 2.1 Primary keys

- All main tables use `uuid` primary keys.

### 2.2 Timestamps

Unless stated otherwise, operational tables include:

- `created_at`
- `updated_at`

Historical and workflow-heavy tables may also include business timestamps such as:

- `published_at`
- `submitted_at`
- `occurred_at`
- `read_at`

### 2.3 Status fields

Status fields are stored as constrained strings and mapped to application enums.

Examples:

- account status
- role status
- registration status
- publication status
- workflow status

### 2.4 Historical preservation

- Academic history is preserved.
- Deactivation and archiving do not delete historical records.
- Academic records use restricted, non-destructive lifecycle operations.

### 2.5 Establishment ownership

Establishment scope stays explicit in persisted data:

- either directly on the table,
- or through an owning parent relation.

## 3. Identity and Governance Tables

### 3.1 `university`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `name` | varchar | no | university name |
| `created_at` | timestamp | no | |
| `updated_at` | timestamp | no | |

Constraints:

- one logical university root

### 3.2 `establishment`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `university_id` | uuid | no | FK -> `university.id` |
| `name` | varchar | no | establishment name |
| `type` | varchar | yes | faculty, school, institute, etc. |
| `status` | varchar | no | active / inactive / archived style state |
| `created_at` | timestamp | no | |
| `updated_at` | timestamp | no | |

### 3.3 `user_account`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `university_email` | varchar | no | immutable after creation except authorized correction |
| `password_hash` | varchar | no | |
| `role_type` | varchar | no | authoritative role for the account |
| `account_status` | varchar | no | active / locked / deactivated / archived |
| `last_login_at` | timestamp | yes | |
| `created_at` | timestamp | no | |
| `updated_at` | timestamp | no | |

Constraints:

- unique(`university_email`)

### 3.4 `user_profile`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `user_account_id` | uuid | no | FK -> `user_account.id` |
| `first_name` | varchar | no | |
| `last_name` | varchar | no | |
| `birth_date` | date | yes | |
| `place_of_birth` | varchar | yes | required for newly created Student profiles |
| `nationality` | varchar(100) | yes | required for newly created Student profiles |
| `cin` | varchar(50) | yes | unique when provided |
| `sex` | varchar | yes | controlled value |
| `phone_number` | varchar | yes | |
| `profile_picture_path` | varchar | yes | file path, storage key, or URL |
| `created_at` | timestamp | no | |
| `updated_at` | timestamp | no | |

Constraints:

- unique(`user_account_id`)
- unique(`cin`) when provided

Note:

- `user_account` is authoritative for login identity and the single assigned role.
- Personal data shared across all roles lives in `user_profile`.
- Role-specific extension data and institutional relationships live in the matching role table.

### 3.5 `root_super_admin`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `user_account_id` | uuid | no | FK -> `user_account.id` |
| `created_at` | timestamp | no | |
| `updated_at` | timestamp | no | |

Constraints:

- unique(`user_account_id`)

### 3.6 `super_admin`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `user_account_id` | uuid | no | FK -> `user_account.id` |
| `establishment_id` | uuid | no | FK -> `establishment.id` |
| `created_at` | timestamp | no | |
| `updated_at` | timestamp | no | |

Constraints:

- unique(`user_account_id`)

### 3.7 `admin`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `user_account_id` | uuid | no | FK -> `user_account.id` |
| `establishment_id` | uuid | no | FK -> `establishment.id` |
| `created_at` | timestamp | no | |
| `updated_at` | timestamp | no | |

Constraints:

- unique(`user_account_id`)

### 3.8 `academic_rank`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `establishment_id` | uuid | no | FK -> `establishment.id` |
| `code` | varchar(50) | no | stable establishment-local code |
| `name` | varchar(100) | no | display name |
| `seniority_order` | integer | no | lower value means greater seniority |
| `can_hold_module_responsibility` | boolean | no | controls responsibility eligibility |
| `status` | varchar(20) | no | ACTIVE / INACTIVE |
| `created_at` | timestamp | no | |
| `updated_at` | timestamp | no | |

Constraints:

- unique(`establishment_id`, `code`)
- unique(`establishment_id`, `name`)
- `seniority_order` must be positive.

### 3.9 `professor`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `user_account_id` | uuid | no | FK -> `user_account.id` |
| `establishment_id` | uuid | no | FK -> `establishment.id` |
| `employee_number` | varchar(50) | no | university employment identifier |
| `academic_rank_id` | uuid | no | FK -> `academic_rank.id` |
| `hire_date` | date | yes | |
| `maximum_weekly_teaching_minutes` | integer | no | explicit teaching capacity |
| `created_at` | timestamp | no | |
| `updated_at` | timestamp | no | |

Constraints:

- unique(`user_account_id`)
- unique(`employee_number`)
- `maximum_weekly_teaching_minutes` must be positive.
- Professor and academic rank must belong to the same establishment.

### 3.10 `student`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `user_account_id` | uuid | no | FK -> `user_account.id` |
| `establishment_id` | uuid | no | FK -> `establishment.id` |
| `apogee_code` | varchar(50) | no | university academic identifier |
| `national_student_code` | varchar(50) | yes | Massar/CNE; unique when provided |
| `initial_enrollment_date` | date | yes | required for newly created students |
| `created_at` | timestamp | no | |
| `updated_at` | timestamp | no | |

Constraints:

- unique(`user_account_id`)
- unique(`apogee_code`)
- unique(`national_student_code`) when provided

The Apogee code identifies the student academically across the university. Massar/CNE is optional for cases where no Moroccan national student identifier is available. Program, level, year, semester, and class/group are not student attributes; they are retained in academic registration records.

### 3.11 `permission`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `code` | varchar | no | machine-readable permission code |
| `name` | varchar | no | display label |
| `created_at` | timestamp | no | |
| `updated_at` | timestamp | no | |

Constraints:

- unique(`code`)

### 3.12 `admin_permission_grant`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `admin_id` | uuid | no | FK -> `admin.id` |
| `permission_id` | uuid | no | FK -> `permission.id` |
| `granted_at` | timestamp | no | |

Constraints:

- unique(`admin_id`, `permission_id`)

Note:

- refresh token session state is not stored in the relational schema
- refresh token rotation, revocation, and expiry are handled through Redis

## 4. Academic Structure Tables

### 4.1 `department`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `establishment_id` | uuid | no | FK -> `establishment.id` |
| `name` | varchar | no | |
| `created_at` | timestamp | no | |
| `updated_at` | timestamp | no | |

Constraints:

- unique(`establishment_id`, `name`)

### 4.2 `program_path`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `establishment_id` | uuid | no | FK -> `establishment.id` |
| `name` | varchar | no | regular path, excellence path, etc. |
| `created_at` | timestamp | no | |
| `updated_at` | timestamp | no | |

Constraints:

- unique(`establishment_id`, `name`)

### 4.3 `degree_cycle`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `establishment_id` | uuid | no | FK -> `establishment.id` |
| `name` | varchar | no | Licence, Master, Engineering Cycle, etc. |
| `created_at` | timestamp | no | |
| `updated_at` | timestamp | no | |

Constraints:

- unique(`establishment_id`, `name`)

### 4.4 `program_filiere`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `department_id` | uuid | no | FK -> `department.id` |
| `degree_cycle_id` | uuid | no | FK -> `degree_cycle.id` |
| `program_path_id` | uuid | no | FK -> `program_path.id` |
| `code` | varchar | no | |
| `name` | varchar | no | |
| `created_at` | timestamp | no | |
| `updated_at` | timestamp | no | |

Constraints:

- unique(`department_id`, `degree_cycle_id`, `program_path_id`, `code`)

### 4.5 `academic_level`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `program_filiere_id` | uuid | no | FK -> `program_filiere.id` |
| `name` | varchar | no | L1, L2, M1, M2, etc. |
| `level_order` | smallint | no | ordering inside the program |
| `terminal_level` | boolean | no | true when this is the program's final academic level |
| `created_at` | timestamp | no | |
| `updated_at` | timestamp | no | |

Constraints:

- unique(`program_filiere_id`, `name`)
- unique(`program_filiere_id`, `level_order`)

### 4.6 `academic_year`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `establishment_id` | uuid | no | FK -> `establishment.id` |
| `label` | varchar | no | example: 2025-2026 |
| `start_year` | smallint | no | extracted from `label`, example: 2025 |
| `end_year` | smallint | no | extracted from `label`, example: 2026 |
| `status` | varchar | no | `PLANNED`, `ACTIVE`, or `CLOSED` |
| `created_at` | timestamp | no | |
| `updated_at` | timestamp | no | |

Constraints:

- unique(`establishment_id`, `label`)
- unique(`establishment_id`, `start_year`)
- `end_year = start_year + 1`

### 4.7 `semester`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `academic_level_id` | uuid | no | FK -> `academic_level.id` |
| `academic_year_id` | uuid | no | FK -> `academic_year.id` |
| `name` | varchar | no | S1, S2, etc. |
| `semester_order` | smallint | no | |
| `term_type` | varchar(20) | no | AUTUMN / SPRING |
| `start_date` | date | no | Editable first teaching date |
| `end_date` | date | no | Editable final teaching date |
| `created_at` | timestamp | no | |
| `updated_at` | timestamp | no | |

Constraints:

- unique(`academic_level_id`, `academic_year_id`, `name`)
- unique(`academic_level_id`, `academic_year_id`, `semester_order`)

Rule:

- The Academic Level and Academic Year referenced by a Semester must belong to the same establishment.
- `term_type` groups semesters delivered during the same teaching period without introducing a separate calendar entity.
- `end_date` must be on or after `start_date`.
- Lifecycle is derived at read time: `PLANNED` before `start_date`, `ACTIVE` through `end_date`, and `FINISHED` afterward.

### 4.8 `academic_rule_profile`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `establishment_id` | uuid | no | FK -> `establishment.id` |
| `name` | varchar | no | |
| `version` | integer | no | Positive version within the establishment and profile name |
| `module_validation_threshold` | numeric(5,2) | no | Direct module validation threshold |
| `compensation_minimum_threshold` | numeric(5,2) | no | Lowest grade eligible for compensation |
| `semester_validation_average` | numeric(5,2) | no | Required semester average |
| `annual_validation_average` | numeric(5,2) | yes | Required annual average when applicable |
| `minimum_individually_validated_modules_per_semester` | integer | no | Minimum direct module validations required by semester rules |
| `maximum_non_validated_modules_per_semester` | integer | no | Maximum remaining NV modules accepted by semester rules |
| `allow_inter_semester_compensation` | boolean | no | Enables compensation across the academic level's two terms |
| `minimum_individually_validated_modules_per_academic_level` | integer | no | Minimum direct module validations required across the academic level |
| `rule_definition` | json | yes | Module, shared or term-specific Semester, academic-level, and progression decision rules; null profiles use the backend default rule set |
| `maximum_module_inscriptions` | integer | no | Maximum registrations for the same module |
| `session_grade_policy` | varchar | no | BEST_GRADE / RATTRAPAGE_REPLACES_NORMAL / RATTRAPAGE_CAPPED_AT_VALIDATION_THRESHOLD |
| `allow_progression_with_debt` | boolean | no | Whether progression with NV modules is allowed |
| `maximum_carried_modules` | integer | no | Maximum NV modules allowed when progressing |
| `maximum_unjustified_absences` | integer | no | Maximum counted absences before exclusion |
| `absence_exclusion_policy` | varchar | no | NORMAL_ONLY / NORMAL_AND_RATTRAPAGE |
| `status` | varchar | no | ACTIVE / INACTIVE |
| `created_at` | timestamp | no | |
| `updated_at` | timestamp | no | |

Constraints:

- Grade and average thresholds must be between `0.00` and `20.00`.
- unique(`establishment_id`, `name`, `version`)
- `compensation_minimum_threshold` cannot exceed `module_validation_threshold`.
- `maximum_module_inscriptions` must be positive.
- `minimum_individually_validated_modules_per_semester`, `maximum_non_validated_modules_per_semester`, and `minimum_individually_validated_modules_per_academic_level` cannot be negative.
- When inter-semester compensation is disabled, `minimum_individually_validated_modules_per_academic_level` must be zero.
- `maximum_carried_modules` must be zero when progression with debt is disabled.
- `maximum_unjustified_absences` cannot be negative.
- `rule_definition` either uses one shared Semester rule set or defines complete Autumn and Spring rule sets.
- The Academic Level and Academic Year assignment remains the only profile assignment; Semester behavior is selected from the profile using `semester.term_type`.
- Under `RATTRAPAGE_CAPPED_AT_VALIDATION_THRESHOLD`, the raw Rattrapage grade is retained while its contribution to `module_result.final_grade_value` cannot exceed `module_validation_threshold`; a higher Normal grade is preserved.
- A profile already used by a module result, semester result, or progression decision is historically stable; policy changes create a new profile/version.

### 4.9 `academic_level_rule_assignment`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `academic_level_id` | uuid | no | FK -> `academic_level.id` |
| `academic_year_id` | uuid | no | FK -> `academic_year.id` |
| `academic_rule_profile_id` | uuid | no | FK -> `academic_rule_profile.id` |
| `status` | varchar | no | ACTIVE / INACTIVE |
| `created_at` | timestamp | no | |
| `updated_at` | timestamp | no | |

Constraints:

- unique(`academic_level_id`, `academic_year_id`)
- The level, academic year, and rule profile must belong to the same establishment.
- An academic level cannot be used for registration in an academic year until this assignment exists.
- The profile may be reused by assignments for multiple academic levels.
- The profile and every assigned level must belong to the same establishment.

### 4.10 `subject_module`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `semester_id` | uuid | no | FK -> `semester.id` |
| `code` | varchar | no | |
| `title` | varchar | no | |
| `created_at` | timestamp | no | |
| `updated_at` | timestamp | no | |

Constraints:

- unique(`semester_id`, `code`)

Note:

- `title` is not unique. The same module title may appear in different programs or at different academic levels.
- A later Academic Year may reuse the same module code in a new Semester record without changing earlier academic history.
- Program/Filiere and establishment ownership are derived through `Semester -> AcademicLevel -> ProgramFiliere`.

### 4.11 `class_group`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `academic_level_id` | uuid | no | FK -> `academic_level.id` |
| `academic_year_id` | uuid | no | FK -> `academic_year.id` |
| `name` | varchar | no | |
| `status` | varchar | no | |
| `created_at` | timestamp | no | |
| `updated_at` | timestamp | no | |

Constraints:

- unique(`academic_level_id`, `academic_year_id`, `name`)

## 5. Academic Registration and Assignment Tables

### 5.1 `academic_registration`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `student_id` | uuid | no | FK -> `student.id` |
| `program_filiere_id` | uuid | no | FK -> `program_filiere.id` |
| `academic_level_id` | uuid | no | FK -> `academic_level.id` |
| `academic_year_id` | uuid | no | FK -> `academic_year.id` |
| `status` | varchar | no | |
| `created_at` | timestamp | no | |
| `updated_at` | timestamp | no | |

Constraints:

- unique(`student_id`, `academic_year_id`)
- `academic_level_id` must reference a level belonging to `program_filiere_id`.

### 5.2 `semester_registration`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `academic_registration_id` | uuid | no | FK -> `academic_registration.id` |
| `semester_id` | uuid | no | FK -> `semester.id` |
| `created_at` | timestamp | no | |
| `updated_at` | timestamp | no | |

Constraints:

- unique(`academic_registration_id`, `semester_id`)
- The semester must belong to the same academic year and academic level as the annual academic registration.
- Exactly two semester registrations are created automatically with each annual academic registration.

### 5.3 `student_class_assignment`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `semester_registration_id` | uuid | no | FK -> `semester_registration.id` |
| `class_group_id` | uuid | no | FK -> `class_group.id` |
| `created_at` | timestamp | no | |
| `updated_at` | timestamp | no | |

Constraints:

- unique(`semester_registration_id`)
- The class/group must belong to the same academic year and academic level as the semester registration.
- Assignment happens after semester registration and is controlled by authorized administration.
- The class/group assignment is not replaced after it is established for the semester.

### 5.4 `module_registration`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `semester_registration_id` | uuid | no | FK -> `semester_registration.id` |
| `subject_module_id` | uuid | no | FK -> `subject_module.id` |
| `origin_academic_level_id` | uuid | yes | FK -> `academic_level.id`, set when carried from previous level |
| `inscription_number` | smallint | no | first, second, or later inscription according to policy |
| `status` | varchar | no | |
| `created_at` | timestamp | no | |
| `updated_at` | timestamp | no | |

Constraints:

- unique(`semester_registration_id`, `subject_module_id`)
- The subject/module must belong to the semester referenced by the semester registration.
- `inscription_number` must be positive. Its maximum is enforced from the applicable academic rules rather than hardcoded in the table.
- `origin_academic_level_id` remains null for normal current-level modules and is set only when a module is carried from another level.
- A carried module uses an additional semester registration for the module's actual current-year semester. The parent academic registration remains anchored to the student's current level.
- The carried semester and the student's current semester must have the same `semester_order`, preserving the parallel-term rule such as S1 with S3 and S2 with S4.

### 5.5 `academic_domain`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `establishment_id` | uuid | no | FK -> `establishment.id` |
| `code` | varchar(50) | no | stable domain code |
| `name` | varchar(255) | no | |
| `created_at` | timestamp | no | |
| `updated_at` | timestamp | no | |

Constraints:

- unique(`establishment_id`, `code`)

### 5.6 `professor_expertise`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `professor_id` | uuid | no | FK -> `professor.id` |
| `academic_domain_id` | uuid | no | FK -> `academic_domain.id` |
| `created_at` | timestamp | no | |

Constraints:

- unique(`professor_id`, `academic_domain_id`)
- Professor and domain must belong to the same establishment.

### 5.7 `module_teaching_component`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `subject_module_id` | uuid | no | FK -> `subject_module.id` |
| `component_type` | varchar | no | COURSE / TD / TP |
| `sessions_per_week` | smallint | no | |
| `session_duration_minutes` | integer | no | |
| `audience_mode` | varchar | no | WHOLE_COHORT / CLASS_GROUP / SUBGROUP |
| `required_room_type` | varchar(100) | no | LECTURE_HALL / CLASSROOM / COMPUTER_LAB |
| `created_at` | timestamp | no | |
| `updated_at` | timestamp | no | |

Constraints:

- unique(`subject_module_id`, `component_type`)
- `sessions_per_week` and `session_duration_minutes` must be positive.
- A Course component cannot use `SUBGROUP` audience mode.

### 5.8 `subject_module_domain`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `subject_module_id` | uuid | no | FK -> `subject_module.id` |
| `academic_domain_id` | uuid | no | FK -> `academic_domain.id` |

Constraints:

- unique(`subject_module_id`, `academic_domain_id`)
- Subject/module and domain must belong to the same establishment.

### 5.9 `teaching_group_policy`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `academic_level_id` | uuid | no | FK -> `academic_level.id` |
| `academic_year_id` | uuid | no | FK -> `academic_year.id` |
| `group_type` | varchar | no | TD / TP |
| `minimum_group_size` | integer | no | minimum size for a generated subgroup |
| `maximum_group_size` | integer | no | maximum size for a generated subgroup |
| `created_at` | timestamp | no | |
| `updated_at` | timestamp | no | |

Constraints:

- unique(`academic_level_id`, `academic_year_id`, `group_type`)
- Both group sizes must be positive and `minimum_group_size` must not exceed `maximum_group_size`.
- The academic level and academic year must belong to the same establishment.
- One annual policy applies to both semesters of the academic level.

### 5.10 `teaching_group`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `semester_id` | uuid | no | FK -> `semester.id` |
| `source_class_group_id` | uuid | yes | FK -> `class_group.id`; null only for the whole cohort |
| `name` | varchar(255) | no | generated audience label |
| `audience_type` | varchar | no | WHOLE_COHORT / CLASS_GROUP / SUBGROUP |
| `group_type` | varchar | yes | TD / TP for SUBGROUP; null otherwise |
| `created_at` | timestamp | no | |
| `updated_at` | timestamp | no | |

Constraints:

- unique(`semester_id`, `name`)
- `WHOLE_COHORT` has no source class group.
- `CLASS_GROUP` and `SUBGROUP` must reference their source class group.
- A `SUBGROUP` must have a TD or TP `group_type`; other audience types must not have one.
- Every active semester registration must have a class assignment before generation.
- TD and TP subgroups are generated independently inside each source class only when the semester requires that subgroup type. Generation uses the matching annual size range and balances memberships. A source class smaller than the minimum remains one subgroup; an infeasible multi-group split is rejected.

### 5.11 `teaching_group_membership`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `teaching_group_id` | uuid | no | FK -> `teaching_group.id` |
| `semester_registration_id` | uuid | no | FK -> `semester_registration.id` |
| `created_at` | timestamp | no | |

Constraints:

- unique(`teaching_group_id`, `semester_registration_id`)
- Teaching group and semester registration must reference the same semester.

### 5.12 `block`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `establishment_id` | uuid | no | FK -> `establishment.id` |
| `code` | varchar(50) | no | |
| `name` | varchar(255) | no | |
| `status` | varchar | no | ACTIVE / INACTIVE |
| `created_at` | timestamp | no | |
| `updated_at` | timestamp | no | |

Constraints:

- unique(`establishment_id`, `code`)
- A block belongs to one establishment and may group many rooms.

### 5.13 `room`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `establishment_id` | uuid | no | FK -> `establishment.id` |
| `block_id` | uuid | yes | FK -> `block.id`; null for a standalone room |
| `code` | varchar(50) | no | |
| `name` | varchar(255) | no | |
| `room_type` | varchar(100) | no | classroom, amphitheater, laboratory, etc. |
| `capacity` | integer | no | |
| `status` | varchar | no | ACTIVE / INACTIVE |
| `created_at` | timestamp | no | |
| `updated_at` | timestamp | no | |

Constraints:

- unique(`establishment_id`, `code`)
- `capacity` must be positive.
- The optional block must belong to the same establishment as the room.
- Establishment ownership is direct and does not depend on `block_id`.

### 5.14 `module_class_responsibility`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `professor_id` | uuid | no | FK -> `professor.id` |
| `subject_module_id` | uuid | no | FK -> `subject_module.id` |
| `class_group_id` | uuid | no | FK -> `class_group.id` |
| `academic_year_id` | uuid | no | FK -> `academic_year.id` |
| `semester_id` | uuid | no | FK -> `semester.id` |
| `status` | varchar | no | ACTIVE / INACTIVE |
| `created_at` | timestamp | no | |
| `updated_at` | timestamp | no | |

Constraints:

- Only one active responsibility may exist for one subject/module and class group in an academic period.
- The Professor, module, class group, academic year, and semester must belong to the same establishment context.
- Deactivation preserves the historical responsibility.

### 5.15 `teaching_requirement`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `module_teaching_component_id` | uuid | no | FK -> `module_teaching_component.id` |
| `teaching_group_id` | uuid | no | FK -> `teaching_group.id` |
| `status` | varchar | no | ACTIVE / INACTIVE |
| `created_at` | timestamp | no | |
| `updated_at` | timestamp | no | |

Constraints:

- unique(`module_teaching_component_id`, `teaching_group_id`)
- Component and teaching group must belong to the same academic-year semester context.

### 5.16 `teaching_assignment`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `professor_id` | uuid | no | FK -> `professor.id` |
| `teaching_requirement_id` | uuid | no | FK -> `teaching_requirement.id` |
| `status` | varchar | no | |
| `assignment_source` | varchar(20) | no | MANUAL / AUTOMATIC |
| `created_at` | timestamp | no | |
| `updated_at` | timestamp | no | |

Constraints:

- Only one `ACTIVE` assignment may exist for one teaching requirement.
- The Professor must satisfy every required expertise domain and remain within maximum weekly teaching workload across the same establishment, academic year, and term type.
- Automatic Course assignments require an academic rank that may hold module responsibility.
- Automatic generation preserves existing active assignments and saves each requirement it can resolve. Requirements without an eligible Professor remain unassigned and are reported to the caller.
- Unassignment changes the status to `INACTIVE` instead of deleting the historical row.

### 5.17 `teaching_assignment_rank_preference`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `establishment_id` | uuid | no | FK -> `establishment.id` |
| `component_type` | varchar(20) | no | COURSE / TD / TP |
| `academic_rank_id` | uuid | no | FK -> `academic_rank.id` |
| `priority` | integer | no | position in the component-specific order |
| `status` | varchar(20) | no | ACTIVE / INACTIVE |
| `created_at` | timestamp | no | |
| `updated_at` | timestamp | no | |

Constraints:

- unique(`establishment_id`, `component_type`, `academic_rank_id`)
- unique(`establishment_id`, `component_type`, `priority`)
- Establishment and academic rank must match.
- `priority` must be positive.
- Course preferences may contain only ranks allowed to hold module responsibility.

## 6. Planning, Examination, and Assessment Tables

### 6.1 `semester_schedule`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `establishment_id` | uuid | no | FK -> `establishment.id` |
| `academic_year_id` | uuid | no | FK -> `academic_year.id` |
| `semester_id` | uuid | no | FK -> `semester.id` |
| `publication_status` | varchar | no | |
| `published_at` | timestamp | yes | set when the schedule is published |
| `created_at` | timestamp | no | |
| `updated_at` | timestamp | no | |

Constraints:

- unique(`establishment_id`, `academic_year_id`, `semester_id`)

### 6.2 `schedule_entry`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `semester_schedule_id` | uuid | no | FK -> `semester_schedule.id` |
| `teaching_assignment_id` | uuid | no | FK -> `teaching_assignment.id` |
| `room_id` | uuid | no | FK -> `room.id` |
| `day_of_week` | varchar | no | |
| `start_time` | time | no | |
| `end_time` | time | no | |
| `created_at` | timestamp | no | |
| `updated_at` | timestamp | no | |

Constraints:

- `end_time` must be after `start_time`.
- `day_of_week` uses the standard Monday through Sunday values.
- The application prevents overlapping entries for the same Professor, teaching group, or room.
- Room type and capacity must satisfy the teaching component and teaching group.
- The number of weekly entries for a requirement must match `sessions_per_week` before the schedule is complete.
- Published entries remain visible to Professors and Students while authorized management retains schedule editing control.

### 6.3 `exam_schedule`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `establishment_id` | uuid | no | FK -> `establishment.id` |
| `academic_year_id` | uuid | no | FK -> `academic_year.id` |
| `semester_id` | uuid | no | FK -> `semester.id` |
| `session_type` | varchar | no | NORMAL / RATTRAPAGE |
| `publication_status` | varchar | no | |
| `start_date` | date | no | First date of the examination period |
| `end_date` | date | no | Last date of the examination period |
| `created_at` | timestamp | no | |
| `updated_at` | timestamp | no | |

Constraints:

- unique(`establishment_id`, `academic_year_id`, `semester_id`, `session_type`)
- Normal and rattrapage schedules are planned and published independently.
- `end_date` must be on or after `start_date`.
- A draft may be prepared before teaching ends, but publication requires the Semester to be `FINISHED`.

### 6.4 `module_exam`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `exam_schedule_id` | uuid | no | FK -> `exam_schedule.id` |
| `subject_module_id` | uuid | no | FK -> `subject_module.id` |
| `class_group_id` | uuid | no | FK -> `class_group.id` |
| `room_id` | uuid | yes | Legacy primary room retained during migration; new planning uses `exam_room_allocation` |
| `exam_date` | date | no | |
| `start_time` | time | no | |
| `end_time` | time | yes | |
| `location` | varchar | yes | Legacy room label retained during migration |
| `candidate_list_generated_at` | timestamp | yes | distinguishes a generated empty candidate list from a list not yet generated |
| `created_at` | timestamp | no | |
| `updated_at` | timestamp | no | |

Constraints:

- unique(`exam_schedule_id`, `subject_module_id`, `class_group_id`)
- `end_time` must be later than `start_time` when it is provided.
- `exam_date` must be inside the parent exam schedule's date range.
- Room allocation is managed through `exam_room_allocation`.
- A recurring teaching entry blocks the room only while its Semester is within its start and end dates.
- The responsible Professor is resolved through the active module-class responsibility matching the module, class group, academic year, and semester.
- Deleting a draft exam schedule also deletes its module exams. Published exam schedules cannot be deleted.

### 6.5 `exam_group`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `exam_schedule_id` | uuid | no | FK -> `exam_schedule.id` |
| `class_group_id` | uuid | no | source Class Group |
| `label` | varchar(50) | no | example: A-E1 |
| `group_order` | integer | no | deterministic display/distribution order |
| `created_at` | timestamp | no | |
| `updated_at` | timestamp | no | |

Constraints:

- unique(`exam_schedule_id`, `class_group_id`, `group_order`)
- unique(`exam_schedule_id`, `class_group_id`, `label`)
- One generated group represents no split; multiple groups divide the source Class Group evenly.

### 6.6 `exam_group_membership`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `exam_group_id` | uuid | no | FK -> `exam_group.id` |
| `semester_registration_id` | uuid | no | assigned Student semester context |
| `created_at` | timestamp | no | |

The memberships remain stable across every module exam in the examination schedule.

### 6.7 `exam_room_allocation`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `module_exam_id` | uuid | no | FK -> `module_exam.id` |
| `exam_group_id` | uuid | no | FK -> `exam_group.id` |
| `room_id` | uuid | no | FK -> `room.id` |
| `created_at` | timestamp | no | |
| `updated_at` | timestamp | no | |

Constraints:

- unique(`module_exam_id`, `exam_group_id`)
- unique(`module_exam_id`, `room_id`)
- Room capacity must cover the assigned exam-group membership count.
- Allocated rooms must not overlap another exam or an active teaching session.

### 6.8 `exam_candidate`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `module_exam_id` | uuid | no | FK -> `module_exam.id` |
| `module_registration_id` | uuid | no | FK -> `module_registration.id` |
| `created_at` | timestamp | no | |
| `updated_at` | timestamp | no | |

Constraints:

- unique(`module_exam_id`, `module_registration_id`)
- Every row represents a student authorized and invited to attend; excluded students have no row.
- `module_exam.candidate_list_generated_at` records that generation completed even when no students qualified.
- Candidate generation is locked after grade entry starts for the module exam.

### 6.9 `grade_record`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `module_registration_id` | uuid | no | FK -> `module_registration.id` |
| `module_exam_id` | uuid | no | FK -> `module_exam.id` |
| `grade_value` | numeric(5,2) | no | |
| `zero_grade_reason` | varchar | yes | ABSENT / EARNED_ZERO |
| `workflow_status` | varchar | no | DRAFT / SUBMITTED / REVIEWED / APPROVED / PUBLISHED |
| `published_at` | timestamp | yes | |
| `created_at` | timestamp | no | |
| `updated_at` | timestamp | no | |

Constraints:

- unique(`module_registration_id`, `module_exam_id`)
- `grade_value` must be between `0.00` and `20.00`.
- A `0.00` grade requires `zero_grade_reason`; a positive grade requires it to be null.
- The responsible Professor is resolved through the Module Class Responsibility attached to `module_exam_id`.

### 6.10 `module_result`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `module_registration_id` | uuid | no | FK -> `module_registration.id` |
| `academic_rule_profile_id` | uuid | no | FK -> `academic_rule_profile.id` used for the calculation |
| `final_grade_value` | numeric(5,2) | no | system-calculated result |
| `result_status` | varchar | no | AV / V / NV |
| `original_final_grade_value` | numeric(5,2) | yes | snapshot before the first later-inscription replacement |
| `original_result_status` | varchar | yes | original AV / V / NV snapshot |
| `calculated_at` | timestamp | no | |
| `created_at` | timestamp | no | |
| `updated_at` | timestamp | no | |

Constraints:

- unique(`module_registration_id`)
- `final_grade_value` must be between `0.00` and `20.00`.
- The row is calculated from published Normal and applicable Rattrapage `grade_record` rows for the same module registration.
- The calculation uses the applicable academic rule profile, including validation, compensation eligibility, and session-selection rules.
- Session resolution assigns `V` or `NV`; semester compensation may change `NV` to `AV` without changing `final_grade_value`.
- Application users cannot directly create or update this row.
- Semester, academic year, academic level, student, module, and inscription number are derived through `module_registration_id`.
- A later inscription updates the effective values of the original module result. The original values and all published grade records remain unchanged and queryable.

### 6.11 `semester_result`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `semester_registration_id` | uuid | no | FK -> `semester_registration.id` |
| `academic_rule_profile_id` | uuid | no | FK -> `academic_rule_profile.id` used for evaluation |
| `semester_average` | numeric(5,2) | no | arithmetic average until module coefficients are introduced |
| `result_status` | varchar | no | VALIDATED / NON_VALIDATED |
| `original_semester_average` | numeric(5,2) | yes | snapshot before the first later-inscription recalculation |
| `original_result_status` | varchar | yes | original VALIDATED / NON_VALIDATED snapshot |
| `evaluated_at` | timestamp | no | |
| `created_at` | timestamp | no | |
| `updated_at` | timestamp | no | |

Constraints:

- unique(`semester_registration_id`)
- `semester_average` must be between `0.00` and `20.00`.
- A semester is `VALIDATED` only when all active module results are `V` or `AV`.
- Compensation is evaluated only when every active module registration has a module result.
- Later-inscription results recalculate the effective original semester; they do not enter the current academic level's average.

### 6.12 `progression_decision`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `academic_registration_id` | uuid | no | FK -> `academic_registration.id` |
| `academic_rule_profile_id` | uuid | no | FK -> `academic_rule_profile.id` used for the decision |
| `decision_status` | varchar | no | PROMOTED / PROMOTED_BY_COMPENSATION / PROMOTED_WITH_DEBT / LEVEL_VALIDATED / REPEAT / FAILED |
| `annual_average` | numeric(5,2) | no | arithmetic average until module coefficients are introduced |
| `outstanding_module_count` | integer | no | remaining NV module results |
| `decided_at` | timestamp | no | |
| `created_at` | timestamp | no | |
| `updated_at` | timestamp | no | |

Constraints:

- unique(`academic_registration_id`)
- `decision_status` is PROMOTED / PROMOTED_BY_COMPENSATION / PROMOTED_WITH_DEBT / LEVEL_VALIDATED / REPEAT / FAILED.
- `outstanding_module_count` cannot be negative.
- The decision is calculated only after every registered semester has a semester result.
- A successful decision for an academic level marked `terminal_level` is stored as `LEVEL_VALIDATED`; graduation is handled separately.

### 6.13 `graduation_decision`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `terminal_academic_registration_id` | uuid | no | FK -> `academic_registration.id` |
| `decision_status` | varchar | no | `GRADUATED` |
| `graduation_average` | numeric(5,2) | no | mean of completed academic-level annual averages |
| `decided_at` | timestamp | no | |
| `created_at` | timestamp | no | |
| `updated_at` | timestamp | no | |

Constraints:

- unique(`terminal_academic_registration_id`)
- `graduation_average` must be between `0.00` and `20.00`.
- The linked registration must belong to a terminal academic level.
- Every configured academic level in the same program must have a successful latest progression decision.

## 7. Attendance and Document Tables

### 7.1 `absence_record`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `module_registration_id` | uuid | no | FK -> `module_registration.id` |
| `teaching_assignment_id` | uuid | no | FK -> `teaching_assignment.id` |
| `absence_date` | date | no | |
| `justified` | boolean | no | Defaults to false |
| `justification_note` | varchar(1000) | yes | Accepted justification summary retained for existing absence queries |
| `created_at` | timestamp | no | |
| `updated_at` | timestamp | no | |

Constraints:

- unique(`module_registration_id`, `teaching_assignment_id`, `absence_date`)
- The accepted reason may be projected onto the absence record for eligibility queries; submission and decision history is stored separately.
- Student, subject/module, academic year, and semester are derived through the module registration.
- Only the active professor from the matching teaching assignment can create or change the record.

### 7.2 `uploaded_document`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `owner_user_account_id` | uuid | no | FK -> `user_account.id` |
| `storage_key` | varchar(500) | no | Unique private object-storage key |
| `original_filename` | varchar(255) | no | Display filename only |
| `content_type` | varchar(100) | no | Validated PDF, JPEG, or PNG MIME type |
| `size_bytes` | bigint | no | Maximum 5 MB for absence evidence |
| `purpose` | varchar(60) | no | `ABSENCE_JUSTIFICATION` |
| `status` | varchar(30) | no | `TEMPORARY`, `ATTACHED`, or `DELETED` |
| `created_at` | timestamp | no | |
| `updated_at` | timestamp | no | |

### 7.3 `absence_justification`

| Column | Type | Null | Notes |
|---|---|---|---|
| `id` | uuid | no | primary key |
| `absence_record_id` | uuid | no | FK -> `absence_record.id` |
| `submitted_by_student_id` | uuid | no | FK -> `student.id` |
| `document_id` | uuid | no | Unique FK -> `uploaded_document.id` |
| `reason` | varchar(1500) | no | Student explanation |
| `status` | varchar(30) | no | `PENDING`, `ACCEPTED`, or `REJECTED` |
| `reviewed_by_professor_id` | uuid | yes | FK -> `professor.id` |
| `decision_note` | varchar(1000) | yes | Professor response |
| `submitted_at` | timestamp | no | |
| `reviewed_at` | timestamp | yes | |
| `created_at` | timestamp | no | |
| `updated_at` | timestamp | no | |

Only the Student who owns the absence may submit. Only the responsible Professor may decide it. One absence may retain several rejected submissions but may have no more than one pending submission.

QR attendance sessions are intentionally absent from the PostgreSQL schema. Their token, expiry, Professor, teaching assignment, and checked-in Student identifiers are temporary Redis data. Confirmed absences are persisted through `absence_record`.

## 8. AI Support Tables

### 8.1 `ai_knowledge_embedding`

| Column | Type | Null | Notes |
|---|---|---|---|
| `chunk_id` | varchar(36) | no | primary key derived from the knowledge chunk |
| `source` | varchar(20) | no | API or UI knowledge source |
| `title` | text | no | section title used during retrieval |
| `content` | text | no | indexed Markdown chunk |
| `content_hash` | varchar(64) | no | detects content changes |
| `embedding` | vector(384) | no | multilingual semantic embedding |
| `updated_at` | timestamp with time zone | no | last index update |

Indexes:

- B-tree index on `source`
- HNSW cosine-distance index on `embedding`

The table contains API and UI knowledge only. It does not store user conversations or academic records. The pgvector extension and this table are created by the vendor-specific Flyway migration.

## 9. Key Relationship Summary

Important relationship directions:

- `Establishment` owns most operational data directly or indirectly.
- `AcademicRank` replaces free-text Professor rank values with establishment-managed classification and responsibility eligibility.
- `TeachingAssignmentRankPreference` stores a separate ordered rank preference for Course, TD, and TP assignment.
- `UserAccount` is separated from both `UserProfile` and role profile tables.
- `UserProfile` stores shared personal data for every authenticated user.
- `DegreeCycle` and `ProgramPath` are establishment-level academic classifications.
- `ProgramFiliere` belongs to a `Department`, a `DegreeCycle`, and a `ProgramPath`.
- `AcademicRegistration` is the annual anchor for a student's program/filiere, academic level, and academic year.
- `SemesterRegistration` carries each semester the student attends, including an earlier-level semester used for a carried module.
- `StudentClassAssignment` links that semester registration to a class/group after registration.
- `ModuleRegistration` preserves normal and carried-module inscription history through `inscription_number`.
- `ModuleTeachingComponent` defines yearly Course, TD, and TP delivery requirements.
- `TeachingGroup` and `TeachingGroupMembership` represent the concrete student audiences used by planning.
- `TeachingRequirement -> TeachingAssignment -> ScheduleEntry` separates required delivery, Professor allocation, and room/time allocation.
- `Establishment -> Room` records direct ownership, while nullable `Room -> Block` supports optional physical grouping.
- `ProfessorExpertise` and `SubjectModuleDomain` provide the qualification match used by schedule generation.
- `ExamSchedule -> ModuleExam` models a separately publishable Normal or Rattrapage schedule and its module exams.
- `GradeRecord` retains both module-registration and module-exam context.
- `ModuleResult` stores one calculated module result per module registration after the applicable published exam results and academic rules are evaluated.
- `SemesterResult` stores compensation and validation outcomes for one semester registration.
- `ai_knowledge_embedding` is isolated operational support data for read-only AI knowledge retrieval.

## 10. Important Constraints

- `user_account.university_email` is unique and immutable except authorized correction.
- `user_account.role_type` is mandatory and authoritative for the assigned role.
- `user_account.account_status` is the authoritative lifecycle status for login and access.
- Each `user_account` has exactly one `user_profile`.
- Each `user_account` has exactly one assigned role.
- A role profile links to exactly one `user_account`.
- The role profile table must match `user_account.role_type`.
- `SuperAdmin`, `Admin`, `Professor`, and `Student` belong to exactly one `Establishment`.
- Students cannot self-create registrations, class assignments, or module registrations.
- Professors cannot self-create expertise approvals, teaching requirements, assignments, or schedule entries.
- Every Professor references an active academic rank from the same establishment when created or updated.
- Rank preferences prioritize eligible Professors but never override expertise, workload, establishment, or academic-period constraints.
- One teaching requirement has at most one active Professor assignment.
- `AcademicRegistration` is annual and has at most one active record per student and academic year in the normal model.
- `SemesterRegistration` is created automatically for the two configured semesters of the registered level and academic year. Additional semester registrations are created when approved carried modules are taught with an earlier-level cohort.
- `StudentClassAssignment` is semester-aware and is managed separately from registration.
- Class groups may be created manually or generated from active annual registrations. Generation balances the cohort under minimum and maximum size constraints and creates all semester assignments atomically.
- Bulk class assignment accepts annual academic registrations but persists one `StudentClassAssignment` for each matching semester registration.
- Teaching groups are derived from completed class assignments. TD and TP subgroup generation uses the annual level/year policy and never mixes source classes.
- `TeachingGroup`, `TeachingRequirement`, and `TeachingAssignment` are semester-aware through their planning relationships.
- Schedule generation must satisfy expertise, workload, requirement coverage, room type, room capacity, and non-overlap constraints.
- A schedule entry references a managed room in the same establishment; free-text teaching locations are not used.
- An incomplete or infeasible generated plan cannot be published.
- Published grades, schedules, absences, semesters, and academic records remain historically visible.
- `GradeRecord` belongs to one `ModuleRegistration` and one `ModuleExam`.
- `ModuleResult`, `SemesterResult`, and `ProgressionDecision` are system-calculated and cannot be edited directly.
- One active `AcademicLevelRuleAssignment` selects the reusable rule profile for each academic level and academic year.
- `ExamSchedule.session_type` separates `NORMAL` and `RATTRAPAGE`.
