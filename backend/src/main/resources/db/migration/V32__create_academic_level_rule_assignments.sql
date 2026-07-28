create table academic_level_rule_assignment (
    id uuid primary key,
    academic_level_id uuid not null references academic_level(id) on delete cascade,
    academic_year_id uuid not null references academic_year(id),
    academic_rule_profile_id uuid not null references academic_rule_profile(id),
    status varchar(30) not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint uk_academic_level_rule_assignment_level_year
        unique (academic_level_id, academic_year_id),
    constraint ck_academic_level_rule_assignment_status
        check (status in ('ACTIVE', 'INACTIVE'))
);

insert into permission (id, code, name, created_at, updated_at) values
    ('00000000-0000-0000-0000-000000000162', 'ACADEMIC_RULE_ASSIGNMENT_VIEW', 'View academic rule assignments', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000163', 'ACADEMIC_RULE_ASSIGNMENT_CREATE', 'Create academic rule assignments', current_timestamp, current_timestamp);
