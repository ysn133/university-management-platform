create table academic_level (
    id uuid primary key,
    program_filiere_id uuid not null references program_filiere(id),
    name varchar(100) not null,
    level_order smallint not null check (level_order > 0),
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint uk_academic_level_program_name unique (program_filiere_id, name),
    constraint uk_academic_level_program_order unique (program_filiere_id, level_order)
);

insert into permission (id, code, name, created_at, updated_at) values
    ('00000000-0000-0000-0000-000000000131', 'ACADEMIC_LEVEL_UPDATE', 'Update academic levels', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000132', 'ACADEMIC_LEVEL_DELETE', 'Delete academic levels', current_timestamp, current_timestamp);
