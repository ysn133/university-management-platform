create table department (
    id uuid primary key,
    establishment_id uuid not null references establishment(id),
    name varchar(255) not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint uk_department_establishment_name unique (establishment_id, name)
);

insert into permission (id, code, name, created_at, updated_at) values
    ('00000000-0000-0000-0000-000000000123', 'DEPARTMENT_UPDATE', 'Update departments', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000124', 'DEPARTMENT_DELETE', 'Delete departments', current_timestamp, current_timestamp);
