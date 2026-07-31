create table subject_module_domain (
    id uuid primary key,
    subject_module_id uuid not null references subject_module(id),
    academic_domain_id uuid not null references academic_domain(id),
    constraint uk_subject_module_domain
        unique (subject_module_id, academic_domain_id)
);

insert into subject_module_domain (
    id,
    subject_module_id,
    academic_domain_id
)
select
    component_domain.id,
    component.subject_module_id,
    component_domain.academic_domain_id
from teaching_component_domain component_domain
join module_teaching_component component
    on component.id = component_domain.module_teaching_component_id
where not exists (
    select 1
    from teaching_component_domain earlier_domain
    join module_teaching_component earlier_component
        on earlier_component.id = earlier_domain.module_teaching_component_id
    where earlier_component.subject_module_id = component.subject_module_id
      and earlier_domain.academic_domain_id = component_domain.academic_domain_id
      and cast(earlier_domain.id as varchar) < cast(component_domain.id as varchar)
);

drop table teaching_component_domain;
