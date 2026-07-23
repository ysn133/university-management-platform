create table semester_schedule (
    id uuid primary key,
    establishment_id uuid not null references establishment(id),
    academic_year_id uuid not null references academic_year(id),
    semester_id uuid not null references semester(id),
    publication_status varchar(30) not null,
    published_at timestamp,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint uk_semester_schedule_context
        unique (establishment_id, academic_year_id, semester_id),
    constraint ck_semester_schedule_publication_status check (
        publication_status in ('DRAFT', 'PUBLISHED')
    )
);

insert into permission (id, code, name, created_at, updated_at) values
    ('00000000-0000-0000-0000-000000000143', 'SEMESTER_SCHEDULE_VIEW', 'View semester schedules', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000144', 'SEMESTER_SCHEDULE_CREATE', 'Create semester schedules', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000145', 'SEMESTER_SCHEDULE_PUBLISH', 'Publish semester schedules', current_timestamp, current_timestamp);
