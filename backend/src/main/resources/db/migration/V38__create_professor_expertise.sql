create table professor_expertise (
    id uuid primary key,
    professor_id uuid not null references professor(id),
    academic_domain_id uuid not null references academic_domain(id),
    created_at timestamp not null,
    constraint uk_professor_expertise unique (professor_id, academic_domain_id)
);

insert into permission (id, code, name, created_at, updated_at) values
    ('00000000-0000-0000-0000-000000000168', 'PROFESSOR_EXPERTISE_VIEW', 'View professor expertise', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000169', 'PROFESSOR_EXPERTISE_UPDATE', 'Update professor expertise', current_timestamp, current_timestamp);
