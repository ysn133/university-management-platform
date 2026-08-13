insert into module_class_responsibility (
    id,
    professor_id,
    subject_module_id,
    class_group_id,
    academic_year_id,
    semester_id,
    status,
    created_at,
    updated_at
)
select
    gen_random_uuid(),
    assignment.professor_id,
    component.subject_module_id,
    class_group.id,
    semester.academic_year_id,
    semester.id,
    'ACTIVE',
    assignment.created_at,
    assignment.updated_at
from teaching_assignment assignment
join teaching_requirement requirement
    on requirement.id = assignment.teaching_requirement_id
join module_teaching_component component
    on component.id = requirement.module_teaching_component_id
join teaching_group
    on teaching_group.id = requirement.teaching_group_id
join semester
    on semester.id = teaching_group.semester_id
join class_group
    on (
        teaching_group.source_class_group_id is not null
        and class_group.id = teaching_group.source_class_group_id
    ) or (
        teaching_group.source_class_group_id is null
        and class_group.academic_level_id = semester.academic_level_id
        and class_group.academic_year_id = semester.academic_year_id
    )
where assignment.status = 'ACTIVE'
  and component.component_type = 'COURSE'
  and class_group.status = 'ACTIVE'
  and not exists (
      select 1
      from module_class_responsibility responsibility
      where responsibility.subject_module_id = component.subject_module_id
        and responsibility.class_group_id = class_group.id
        and responsibility.academic_year_id = semester.academic_year_id
        and responsibility.semester_id = semester.id
  );
