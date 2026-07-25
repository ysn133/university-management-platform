create table teaching_assignment (
    id uuid primary key,
    professor_id uuid not null references professor(id),
    subject_module_id uuid not null references subject_module(id),
    class_group_id uuid not null references class_group(id),
    academic_year_id uuid not null references academic_year(id),
    semester_id uuid not null references semester(id),
    status varchar(30) not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint uk_teaching_assignment_context
        unique (
            professor_id,
            subject_module_id,
            class_group_id,
            academic_year_id,
            semester_id
        ),
    constraint ck_teaching_assignment_status check (
        status in ('ACTIVE', 'INACTIVE')
    )
);

create index idx_teaching_assignment_active_scope
    on teaching_assignment (
        subject_module_id,
        class_group_id,
        academic_year_id,
        semester_id,
        status
    );

insert into permission (id, code, name, created_at, updated_at) values
    ('00000000-0000-0000-0000-000000000146', 'TEACHING_ASSIGNMENT_VIEW', 'View teaching assignments', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000147', 'TEACHING_ASSIGNMENT_CREATE', 'Create teaching assignments', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000148', 'TEACHING_ASSIGNMENT_DELETE', 'Remove teaching assignments', current_timestamp, current_timestamp);
