create table subject_module (
    id uuid primary key,
    semester_id uuid not null references semester(id),
    code varchar(255) not null,
    title varchar(255) not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint uk_subject_module_semester_code unique (semester_id, code)
);

insert into permission (id, code, name, created_at, updated_at) values
    ('00000000-0000-0000-0000-000000000138', 'SUBJECT_MODULE_UPDATE', 'Update subjects and modules', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000139', 'SUBJECT_MODULE_DELETE', 'Delete subjects and modules', current_timestamp, current_timestamp);
