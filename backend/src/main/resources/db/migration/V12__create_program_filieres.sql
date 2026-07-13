create table program_filiere (
    id uuid primary key,
    department_id uuid not null references department(id),
    degree_cycle_id uuid not null references degree_cycle(id),
    program_path_id uuid not null references program_path(id),
    code varchar(100) not null,
    name varchar(255) not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint uk_program_filiere_context_code unique (
        department_id,
        degree_cycle_id,
        program_path_id,
        code
    )
);

insert into permission (id, code, name, created_at, updated_at) values
    ('00000000-0000-0000-0000-000000000129', 'PROGRAM_FILIERE_UPDATE', 'Update programs and filieres', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000130', 'PROGRAM_FILIERE_DELETE', 'Delete programs and filieres', current_timestamp, current_timestamp);
