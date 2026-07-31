alter table teaching_assignment rename to module_class_responsibility;

alter table schedule_entry
    rename column teaching_assignment_id to module_class_responsibility_id;

alter table module_exam
    drop column teaching_assignment_id;

alter table absence_record
    rename column teaching_assignment_id to module_class_responsibility_id;

alter table module_class_responsibility
    rename constraint uk_teaching_assignment_context
    to uk_module_class_responsibility_context;

alter table module_class_responsibility
    rename constraint ck_teaching_assignment_status
    to ck_module_class_responsibility_status;

insert into module_teaching_component (
    id,
    subject_module_id,
    component_type,
    sessions_per_week,
    session_duration_minutes,
    audience_mode,
    maximum_group_size,
    required_room_type,
    created_at,
    updated_at
)
select distinct
    responsibility.subject_module_id,
    responsibility.subject_module_id,
    'COURSE',
    1,
    60,
    'CLASS_GROUP',
    cast(null as integer),
    'CLASSROOM',
    current_timestamp,
    current_timestamp
from module_class_responsibility responsibility
where not exists (
    select 1
    from module_teaching_component component
    where component.subject_module_id = responsibility.subject_module_id
);

insert into teaching_group (
    id,
    semester_id,
    source_class_group_id,
    name,
    audience_type,
    created_at,
    updated_at
)
select
    responsibility.id,
    responsibility.semester_id,
    responsibility.class_group_id,
    class_group.name,
    'CLASS_GROUP',
    responsibility.created_at,
    responsibility.updated_at
from module_class_responsibility responsibility
join class_group on class_group.id = responsibility.class_group_id
where not exists (
    select 1
    from teaching_group existing_group
    where existing_group.semester_id = responsibility.semester_id
      and existing_group.source_class_group_id = responsibility.class_group_id
      and existing_group.audience_type = 'CLASS_GROUP'
)
and not exists (
    select 1
    from module_class_responsibility earlier
    where earlier.semester_id = responsibility.semester_id
      and earlier.class_group_id = responsibility.class_group_id
      and cast(earlier.id as varchar) < cast(responsibility.id as varchar)
);

create table teaching_requirement (
    id uuid primary key,
    module_teaching_component_id uuid not null
        references module_teaching_component(id),
    teaching_group_id uuid not null references teaching_group(id),
    status varchar(30) not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint uk_teaching_requirement_component_group
        unique (module_teaching_component_id, teaching_group_id),
    constraint ck_teaching_requirement_status
        check (status in ('ACTIVE', 'INACTIVE'))
);

insert into teaching_requirement (
    id,
    module_teaching_component_id,
    teaching_group_id,
    status,
    created_at,
    updated_at
)
select
    responsibility.id,
    (
        select component.id
        from module_teaching_component component
        where component.subject_module_id = responsibility.subject_module_id
        order by
            case component.component_type
                when 'COURSE' then 1
                when 'TD' then 2
                else 3
            end
        fetch first 1 row only
    ),
    (
        select teaching_group.id
        from teaching_group
        where teaching_group.semester_id = responsibility.semester_id
          and teaching_group.source_class_group_id = responsibility.class_group_id
          and teaching_group.audience_type = 'CLASS_GROUP'
        fetch first 1 row only
    ),
    'ACTIVE',
    responsibility.created_at,
    responsibility.updated_at
from module_class_responsibility responsibility
where not exists (
    select 1
    from module_class_responsibility earlier
    where earlier.subject_module_id = responsibility.subject_module_id
      and earlier.class_group_id = responsibility.class_group_id
      and earlier.semester_id = responsibility.semester_id
      and cast(earlier.id as varchar) < cast(responsibility.id as varchar)
);

create table teaching_assignment (
    id uuid primary key,
    teaching_requirement_id uuid not null references teaching_requirement(id),
    professor_id uuid not null references professor(id),
    status varchar(30) not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint uk_teaching_assignment_professor_requirement
        unique (professor_id, teaching_requirement_id),
    constraint ck_teaching_assignment_status
        check (status in ('ACTIVE', 'INACTIVE'))
);

insert into teaching_assignment (
    id,
    teaching_requirement_id,
    professor_id,
    status,
    created_at,
    updated_at
)
select
    responsibility.id,
    (
        select requirement.id
        from teaching_requirement requirement
        join module_teaching_component component
            on component.id = requirement.module_teaching_component_id
        join teaching_group
            on teaching_group.id = requirement.teaching_group_id
        where component.subject_module_id = responsibility.subject_module_id
          and teaching_group.source_class_group_id = responsibility.class_group_id
          and teaching_group.semester_id = responsibility.semester_id
        fetch first 1 row only
    ),
    responsibility.professor_id,
    responsibility.status,
    responsibility.created_at,
    responsibility.updated_at
from module_class_responsibility responsibility;

alter table schedule_entry add column teaching_assignment_id uuid;
update schedule_entry
set teaching_assignment_id = module_class_responsibility_id;
alter table schedule_entry alter column teaching_assignment_id set not null;
alter table schedule_entry
    add constraint fk_schedule_entry_teaching_assignment
    foreign key (teaching_assignment_id) references teaching_assignment(id);
alter table schedule_entry drop column module_class_responsibility_id;

alter table absence_record add column teaching_assignment_id uuid;
update absence_record
set teaching_assignment_id = module_class_responsibility_id;
alter table absence_record alter column teaching_assignment_id set not null;
alter table absence_record
    add constraint fk_absence_record_teaching_assignment
    foreign key (teaching_assignment_id) references teaching_assignment(id);
alter table absence_record
    drop constraint uk_absence_registration_assignment_date;
drop index idx_absence_teaching_assignment;
alter table absence_record drop column module_class_responsibility_id;
alter table absence_record
    add constraint uk_absence_registration_assignment_date
    unique (module_registration_id, teaching_assignment_id, absence_date);
create index idx_absence_teaching_assignment
    on absence_record (teaching_assignment_id, absence_date);

update permission
set code = 'MODULE_CLASS_RESPONSIBILITY_VIEW',
    name = 'View module class responsibilities',
    updated_at = current_timestamp
where id = '00000000-0000-0000-0000-000000000146';

update permission
set code = 'MODULE_CLASS_RESPONSIBILITY_CREATE',
    name = 'Create module class responsibilities',
    updated_at = current_timestamp
where id = '00000000-0000-0000-0000-000000000147';

update permission
set code = 'MODULE_CLASS_RESPONSIBILITY_DELETE',
    name = 'Remove module class responsibilities',
    updated_at = current_timestamp
where id = '00000000-0000-0000-0000-000000000148';

insert into permission (id, code, name, created_at, updated_at) values
    ('00000000-0000-0000-0000-000000000172', 'TEACHING_REQUIREMENT_VIEW', 'View teaching requirements', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000173', 'TEACHING_REQUIREMENT_GENERATE', 'Generate teaching requirements', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000174', 'TEACHING_ASSIGNMENT_VIEW', 'View teaching assignments', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000175', 'TEACHING_ASSIGNMENT_CREATE', 'Create teaching assignments', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000176', 'TEACHING_ASSIGNMENT_DELETE', 'Remove teaching assignments', current_timestamp, current_timestamp);
