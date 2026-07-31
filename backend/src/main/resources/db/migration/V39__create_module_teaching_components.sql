create table module_teaching_component (
    id uuid primary key,
    subject_module_id uuid not null references subject_module(id),
    component_type varchar(20) not null,
    sessions_per_week smallint not null,
    session_duration_minutes integer not null,
    audience_mode varchar(30) not null,
    maximum_group_size integer,
    required_room_type varchar(100) not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint uk_module_teaching_component_type unique (subject_module_id, component_type),
    constraint ck_module_teaching_component_type
        check (component_type in ('COURSE', 'TD', 'TP')),
    constraint ck_module_teaching_component_sessions check (sessions_per_week > 0),
    constraint ck_module_teaching_component_duration check (session_duration_minutes > 0),
    constraint ck_module_teaching_component_audience
        check (audience_mode in ('WHOLE_COHORT', 'CLASS_GROUP', 'SUBGROUP')),
    constraint ck_module_teaching_component_group_size
        check (maximum_group_size is null or maximum_group_size > 0),
    constraint ck_module_teaching_component_subgroup_size
        check (audience_mode <> 'SUBGROUP' or maximum_group_size is not null),
    constraint ck_module_teaching_component_room_type
        check (required_room_type in ('LECTURE_HALL', 'CLASSROOM', 'COMPUTER_LAB'))
);

create table teaching_component_domain (
    id uuid primary key,
    module_teaching_component_id uuid not null references module_teaching_component(id),
    academic_domain_id uuid not null references academic_domain(id),
    constraint uk_teaching_component_domain
        unique (module_teaching_component_id, academic_domain_id)
);

insert into permission (id, code, name, created_at, updated_at) values
    ('00000000-0000-0000-0000-000000000170', 'MODULE_TEACHING_COMPONENT_VIEW', 'View module teaching components', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000171', 'MODULE_TEACHING_COMPONENT_UPDATE', 'Update module teaching components', current_timestamp, current_timestamp);
