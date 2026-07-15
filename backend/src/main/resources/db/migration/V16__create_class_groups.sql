create table class_group (
    id uuid primary key,
    academic_level_id uuid not null references academic_level(id),
    academic_year_id uuid not null references academic_year(id),
    name varchar(100) not null,
    status varchar(30) not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint uk_class_group_level_year_name unique (academic_level_id, academic_year_id, name),
    constraint ck_class_group_status check (status in ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

insert into permission (id, code, name, created_at, updated_at) values
    ('00000000-0000-0000-0000-000000000137', 'CLASS_GROUP_UPDATE', 'Update classes and groups', current_timestamp, current_timestamp);
