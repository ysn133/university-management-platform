create table academic_domain (
    id uuid primary key,
    establishment_id uuid not null references establishment(id),
    code varchar(50) not null,
    name varchar(255) not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint uk_academic_domain_establishment_code unique (establishment_id, code)
);

insert into permission (id, code, name, created_at, updated_at) values
    ('00000000-0000-0000-0000-000000000164', 'ACADEMIC_DOMAIN_VIEW', 'View academic domains', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000165', 'ACADEMIC_DOMAIN_CREATE', 'Create academic domains', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000166', 'ACADEMIC_DOMAIN_UPDATE', 'Update academic domains', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000167', 'ACADEMIC_DOMAIN_DELETE', 'Delete academic domains', current_timestamp, current_timestamp);
