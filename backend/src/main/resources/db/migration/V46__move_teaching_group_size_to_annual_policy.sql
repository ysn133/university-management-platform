create table teaching_group_policy (
    id uuid primary key,
    academic_level_id uuid not null references academic_level(id),
    academic_year_id uuid not null references academic_year(id),
    group_type varchar(20) not null,
    maximum_group_size integer not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint uk_teaching_group_policy_context
        unique (academic_level_id, academic_year_id, group_type),
    constraint ck_teaching_group_policy_type
        check (group_type in ('TD', 'TP')),
    constraint ck_teaching_group_policy_size
        check (maximum_group_size > 0)
);

insert into teaching_group_policy (
    id,
    academic_level_id,
    academic_year_id,
    group_type,
    maximum_group_size,
    created_at,
    updated_at
)
select
    migrated.id,
    migrated.academic_level_id,
    migrated.academic_year_id,
    migrated.component_type,
    migrated.maximum_group_size,
    current_timestamp,
    current_timestamp
from (
    select
        component.id,
        semester.academic_level_id,
        semester.academic_year_id,
        component.component_type,
        min(component.maximum_group_size) over (
            partition by
                semester.academic_level_id,
                semester.academic_year_id,
                component.component_type
        ) as maximum_group_size,
        row_number() over (
            partition by
                semester.academic_level_id,
                semester.academic_year_id,
                component.component_type
            order by cast(component.id as varchar)
        ) as policy_order
    from module_teaching_component component
    join subject_module on subject_module.id = component.subject_module_id
    join semester on semester.id = subject_module.semester_id
    where component.audience_mode = 'SUBGROUP'
      and component.component_type in ('TD', 'TP')
      and component.maximum_group_size is not null
) migrated
where migrated.policy_order = 1;

alter table teaching_group add column group_type varchar(20);

update teaching_group teaching_group
set group_type = case
    when exists (
        select 1
        from module_teaching_component component
        join subject_module on subject_module.id = component.subject_module_id
        where subject_module.semester_id = teaching_group.semester_id
          and component.audience_mode = 'SUBGROUP'
          and component.component_type = 'TP'
    ) then 'TP'
    else 'TD'
end
where teaching_group.audience_type = 'SUBGROUP';

alter table teaching_group add constraint ck_teaching_group_type
    check (
        (audience_type = 'SUBGROUP' and group_type in ('TD', 'TP'))
        or (audience_type <> 'SUBGROUP' and group_type is null)
    );

alter table module_teaching_component
    drop constraint ck_module_teaching_component_subgroup_size;
alter table module_teaching_component
    drop constraint ck_module_teaching_component_group_size;
alter table module_teaching_component drop column maximum_group_size;

insert into permission (id, code, name, created_at, updated_at) values
    ('00000000-0000-0000-0000-000000000191', 'TEACHING_GROUP_POLICY_VIEW', 'View teaching group policies', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000192', 'TEACHING_GROUP_POLICY_UPDATE', 'Update teaching group policies', current_timestamp, current_timestamp);
