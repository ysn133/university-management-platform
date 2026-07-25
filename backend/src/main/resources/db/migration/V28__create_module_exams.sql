create table module_exam (
    id uuid primary key,
    exam_schedule_id uuid not null references exam_schedule(id) on delete cascade,
    subject_module_id uuid not null references subject_module(id),
    class_group_id uuid not null references class_group(id),
    teaching_assignment_id uuid references teaching_assignment(id),
    exam_date date not null,
    start_time time not null,
    end_time time,
    location varchar(255),
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint uk_module_exam_context
        unique (exam_schedule_id, subject_module_id, class_group_id),
    constraint ck_module_exam_time_range check (
        end_time is null or end_time > start_time
    )
);

create index idx_module_exam_schedule_date
    on module_exam (exam_schedule_id, exam_date, start_time);

create index idx_module_exam_group_date
    on module_exam (class_group_id, exam_date, start_time);
