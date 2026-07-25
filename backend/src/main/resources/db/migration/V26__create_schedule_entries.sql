create table schedule_entry (
    id uuid primary key,
    semester_schedule_id uuid not null references semester_schedule(id),
    teaching_assignment_id uuid not null references teaching_assignment(id),
    day_of_week varchar(20) not null,
    start_time time not null,
    end_time time not null,
    location varchar(255),
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint ck_schedule_entry_day_of_week check (
        day_of_week in (
            'MONDAY',
            'TUESDAY',
            'WEDNESDAY',
            'THURSDAY',
            'FRIDAY',
            'SATURDAY',
            'SUNDAY'
        )
    ),
    constraint ck_schedule_entry_time_range check (end_time > start_time)
);

create index idx_schedule_entry_schedule
    on schedule_entry (semester_schedule_id);

create index idx_schedule_entry_time
    on schedule_entry (day_of_week, start_time, end_time);

insert into permission (id, code, name, created_at, updated_at) values
    ('00000000-0000-0000-0000-000000000149', 'SEMESTER_SCHEDULE_UPDATE', 'Update semester schedules', current_timestamp, current_timestamp);
