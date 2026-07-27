create table grade_record (
    id uuid primary key,
    module_registration_id uuid not null references module_registration(id),
    module_exam_id uuid not null references module_exam(id),
    grade_value numeric(5, 2) not null,
    zero_grade_reason varchar(30),
    workflow_status varchar(30) not null,
    published_at timestamp,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint uk_grade_record_registration_exam
        unique (module_registration_id, module_exam_id),
    constraint ck_grade_record_value check (
        grade_value >= 0 and grade_value <= 20
    ),
    constraint ck_grade_record_zero_reason check (
        (grade_value = 0 and zero_grade_reason in ('ABSENT', 'EARNED_ZERO'))
        or (grade_value > 0 and zero_grade_reason is null)
    ),
    constraint ck_grade_record_workflow_status check (
        workflow_status in (
            'DRAFT',
            'SUBMITTED',
            'REVIEWED',
            'APPROVED',
            'PUBLISHED'
        )
    )
);

create index idx_grade_record_module_exam
    on grade_record (module_exam_id);

create index idx_grade_record_student_history
    on grade_record (module_registration_id, workflow_status);

insert into permission (id, code, name, created_at, updated_at) values
    ('00000000-0000-0000-0000-000000000155', 'GRADE_VIEW', 'View grade sheets', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000156', 'GRADE_REVIEW', 'Review grade sheets', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000157', 'GRADE_APPROVE', 'Approve grade sheets', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000158', 'GRADE_PUBLISH', 'Publish grade sheets', current_timestamp, current_timestamp);
