create table permission (
    id uuid primary key,
    code varchar(100) not null unique,
    name varchar(255) not null,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table admin_permission_grant (
    id uuid primary key,
    admin_id uuid not null references admin(id),
    permission_id uuid not null references permission(id),
    granted_at timestamp not null,
    unique (admin_id, permission_id)
);

insert into permission (id, code, name, created_at, updated_at) values
    ('00000000-0000-0000-0000-000000000101', 'DEPARTMENT_VIEW', 'View departments', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000102', 'DEPARTMENT_CREATE', 'Create departments', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000103', 'PROGRAM_FILIERE_VIEW', 'View programs and filieres', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000104', 'PROGRAM_FILIERE_CREATE', 'Create programs and filieres', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000105', 'DEGREE_CYCLE_VIEW', 'View degree cycles', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000106', 'DEGREE_CYCLE_CREATE', 'Create degree cycles', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000107', 'PROGRAM_PATH_VIEW', 'View program paths', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000108', 'PROGRAM_PATH_CREATE', 'Create program paths', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000109', 'ACADEMIC_LEVEL_VIEW', 'View academic levels', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000110', 'ACADEMIC_LEVEL_CREATE', 'Create academic levels', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000111', 'ACADEMIC_YEAR_VIEW', 'View academic years', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000112', 'ACADEMIC_YEAR_CREATE', 'Create academic years', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000113', 'SEMESTER_VIEW', 'View semesters', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000114', 'SEMESTER_CREATE', 'Create semesters', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000115', 'SUBJECT_MODULE_VIEW', 'View subjects and modules', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000116', 'SUBJECT_MODULE_CREATE', 'Create subjects and modules', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000117', 'CLASS_GROUP_VIEW', 'View classes and groups', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000118', 'CLASS_GROUP_CREATE', 'Create classes and groups', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000119', 'STUDENT_VIEW', 'View students', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000120', 'STUDENT_CREATE', 'Create students', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000121', 'PROFESSOR_VIEW', 'View professors', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000122', 'PROFESSOR_CREATE', 'Create professors', current_timestamp, current_timestamp);
