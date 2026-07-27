create table student_class_assignment (
    id uuid primary key,
    semester_registration_id uuid not null references semester_registration(id),
    class_group_id uuid not null references class_group(id),
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint uk_student_class_assignment_semester_registration
        unique (semester_registration_id)
);

create index idx_student_class_assignment_group
    on student_class_assignment (class_group_id);
