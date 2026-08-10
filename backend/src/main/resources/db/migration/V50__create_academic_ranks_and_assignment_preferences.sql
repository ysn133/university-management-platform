create table academic_rank (
    id uuid primary key,
    establishment_id uuid not null references establishment(id),
    code varchar(50) not null,
    name varchar(100) not null,
    seniority_order integer not null,
    can_hold_module_responsibility boolean not null,
    status varchar(20) not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint uk_academic_rank_establishment_code unique (establishment_id, code),
    constraint uk_academic_rank_establishment_name unique (establishment_id, name),
    constraint ck_academic_rank_seniority check (seniority_order > 0),
    constraint ck_academic_rank_status check (status in ('ACTIVE', 'INACTIVE'))
);

insert into academic_rank (
    id, establishment_id, code, name, seniority_order,
    can_hold_module_responsibility, status, created_at, updated_at
)
select
    ranked.id,
    ranked.establishment_id,
    ranked.code,
    ranked.name,
    ranked.seniority_order,
    ranked.can_hold_module_responsibility,
    'ACTIVE',
    current_timestamp,
    current_timestamp
from (
    select
        professor.id,
        professor.establishment_id,
        upper(replace(trim(coalesce(professor.academic_rank, 'Unspecified')), ' ', '_')) as code,
        trim(coalesce(professor.academic_rank, 'Unspecified')) as name,
        case trim(coalesce(professor.academic_rank, 'Unspecified'))
            when 'Professor' then 1
            when 'Associate Professor' then 2
            when 'Assistant Professor' then 3
            when 'Lecturer' then 4
            else 100
        end as seniority_order,
        case trim(coalesce(professor.academic_rank, 'Unspecified'))
            when 'Professor' then true
            when 'Associate Professor' then true
            else false
        end as can_hold_module_responsibility,
        row_number() over (
            partition by professor.establishment_id, lower(trim(coalesce(professor.academic_rank, 'Unspecified')))
            order by cast(professor.id as varchar)
        ) as migration_order
    from professor
) ranked
where ranked.migration_order = 1;

alter table professor add column academic_rank_id uuid;

update professor
set academic_rank_id = academic_rank.id
from academic_rank
where academic_rank.establishment_id = professor.establishment_id
  and academic_rank.name = trim(coalesce(professor.academic_rank, 'Unspecified'));

alter table professor add constraint professor_academic_rank_id_fkey
    foreign key (academic_rank_id) references academic_rank(id);
alter table professor drop column academic_rank;

create table teaching_assignment_rank_preference (
    id uuid primary key,
    establishment_id uuid not null references establishment(id),
    component_type varchar(20) not null,
    academic_rank_id uuid not null references academic_rank(id),
    priority integer not null,
    status varchar(20) not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint uk_assignment_rank_preference_rank
        unique (establishment_id, component_type, academic_rank_id),
    constraint uk_assignment_rank_preference_priority
        unique (establishment_id, component_type, priority),
    constraint ck_assignment_rank_preference_component
        check (component_type in ('COURSE', 'TD', 'TP')),
    constraint ck_assignment_rank_preference_priority check (priority > 0),
    constraint ck_assignment_rank_preference_status check (status in ('ACTIVE', 'INACTIVE'))
);
