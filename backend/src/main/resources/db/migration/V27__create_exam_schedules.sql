create table exam_schedule (
    id uuid primary key,
    establishment_id uuid not null references establishment(id),
    academic_year_id uuid not null references academic_year(id),
    semester_id uuid not null references semester(id),
    session_type varchar(30) not null,
    publication_status varchar(30) not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint uk_exam_schedule_context
        unique (
            establishment_id,
            academic_year_id,
            semester_id,
            session_type
        ),
    constraint ck_exam_schedule_session_type check (
        session_type in ('NORMAL', 'RATTRAPAGE')
    ),
    constraint ck_exam_schedule_publication_status check (
        publication_status in ('DRAFT', 'PUBLISHED')
    )
);

create index idx_exam_schedule_establishment
    on exam_schedule (establishment_id, created_at);

insert into permission (id, code, name, created_at, updated_at) values
    ('00000000-0000-0000-0000-000000000150', 'EXAM_SCHEDULE_VIEW', 'View exam schedules', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000151', 'EXAM_SCHEDULE_CREATE', 'Create exam schedules', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000152', 'EXAM_SCHEDULE_UPDATE', 'Update exam schedules', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000153', 'EXAM_SCHEDULE_DELETE', 'Delete exam schedules', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000154', 'EXAM_SCHEDULE_PUBLISH', 'Publish exam schedules', current_timestamp, current_timestamp);
