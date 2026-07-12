create table degree_cycle (
    id uuid primary key,
    establishment_id uuid not null references establishment(id),
    name varchar(255) not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint uk_degree_cycle_establishment_name unique (establishment_id, name)
);

insert into permission (id, code, name, created_at, updated_at) values
    ('00000000-0000-0000-0000-000000000127', 'DEGREE_CYCLE_UPDATE', 'Update degree cycles', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000128', 'DEGREE_CYCLE_DELETE', 'Delete degree cycles', current_timestamp, current_timestamp);
