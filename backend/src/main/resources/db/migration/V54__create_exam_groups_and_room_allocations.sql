create table exam_group (
    id uuid primary key,
    exam_schedule_id uuid not null references exam_schedule(id) on delete cascade,
    class_group_id uuid not null references class_group(id),
    label varchar(50) not null,
    group_order integer not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint uk_exam_group_order unique (exam_schedule_id, class_group_id, group_order),
    constraint uk_exam_group_label unique (exam_schedule_id, class_group_id, label)
);

create table exam_group_membership (
    id uuid primary key,
    exam_group_id uuid not null references exam_group(id) on delete cascade,
    semester_registration_id uuid not null references semester_registration(id),
    created_at timestamp not null,
    constraint uk_exam_group_member unique (exam_group_id, semester_registration_id)
);

create unique index uk_exam_schedule_student_group
    on exam_group_membership (semester_registration_id, exam_group_id);

create table exam_room_allocation (
    id uuid primary key,
    module_exam_id uuid not null references module_exam(id) on delete cascade,
    exam_group_id uuid not null references exam_group(id) on delete cascade,
    room_id uuid not null references room(id),
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint uk_exam_room_allocation_group unique (module_exam_id, exam_group_id),
    constraint uk_exam_room_allocation_room unique (module_exam_id, room_id)
);

create index idx_exam_group_schedule_class on exam_group (exam_schedule_id, class_group_id);
create index idx_exam_group_membership_group on exam_group_membership (exam_group_id);
create index idx_exam_room_allocation_room on exam_room_allocation (room_id, module_exam_id);
