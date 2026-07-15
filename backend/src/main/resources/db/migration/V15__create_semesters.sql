create table semester (
    id uuid primary key,
    academic_level_id uuid not null references academic_level(id),
    academic_year_id uuid not null references academic_year(id),
    name varchar(100) not null,
    semester_order smallint not null check (semester_order > 0),
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint uk_semester_level_year_name unique (academic_level_id, academic_year_id, name),
    constraint uk_semester_level_year_order unique (academic_level_id, academic_year_id, semester_order)
);

insert into permission (id, code, name, created_at, updated_at) values
    ('00000000-0000-0000-0000-000000000135', 'SEMESTER_UPDATE', 'Update semesters', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000136', 'SEMESTER_DELETE', 'Delete semesters', current_timestamp, current_timestamp);
