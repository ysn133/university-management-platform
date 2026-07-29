alter table academic_rule_profile
    add column maximum_unjustified_absences integer not null default 0;

alter table academic_rule_profile
    add column absence_exclusion_policy varchar(40) not null
        default 'NORMAL_AND_RATTRAPAGE';

alter table academic_rule_profile
    add constraint ck_rule_profile_maximum_absences
        check (maximum_unjustified_absences >= 0);

alter table academic_rule_profile
    add constraint ck_rule_profile_absence_exclusion
        check (absence_exclusion_policy in ('NORMAL_ONLY', 'NORMAL_AND_RATTRAPAGE'));

create table absence_record (
    id uuid primary key,
    module_registration_id uuid not null references module_registration(id),
    teaching_assignment_id uuid not null references teaching_assignment(id),
    absence_date date not null,
    justified boolean not null default false,
    justification_note varchar(1000),
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint uk_absence_registration_assignment_date
        unique (module_registration_id, teaching_assignment_id, absence_date),
    constraint ck_absence_justification_note check (
        justified = true or justification_note is null
    )
);

create index idx_absence_teaching_assignment
    on absence_record (teaching_assignment_id, absence_date);

create index idx_absence_module_registration
    on absence_record (module_registration_id, justified);

alter table module_exam
    add column candidate_list_generated_at timestamp;

create table exam_candidate (
    id uuid primary key,
    module_exam_id uuid not null references module_exam(id) on delete cascade,
    module_registration_id uuid not null references module_registration(id),
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint uk_exam_candidate_exam_registration
        unique (module_exam_id, module_registration_id)
);

create index idx_exam_candidate_exam
    on exam_candidate (module_exam_id);

create index idx_exam_candidate_registration
    on exam_candidate (module_registration_id);
