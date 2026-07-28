create table academic_rule_profile (
    id uuid primary key,
    establishment_id uuid not null references establishment(id),
    name varchar(255) not null,
    version integer not null,
    module_validation_threshold numeric(5, 2) not null,
    compensation_minimum_threshold numeric(5, 2) not null,
    semester_validation_average numeric(5, 2) not null,
    annual_validation_average numeric(5, 2),
    maximum_module_inscriptions integer not null,
    session_grade_policy varchar(50) not null,
    allow_progression_with_debt boolean not null,
    maximum_carried_modules integer not null,
    status varchar(30) not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint uk_academic_rule_profile_name_version
        unique (establishment_id, name, version),
    constraint ck_academic_rule_profile_version check (version > 0),
    constraint ck_academic_rule_profile_module_threshold
        check (module_validation_threshold between 0.00 and 20.00),
    constraint ck_academic_rule_profile_compensation_threshold
        check (
            compensation_minimum_threshold between 0.00 and 20.00
            and compensation_minimum_threshold <= module_validation_threshold
        ),
    constraint ck_academic_rule_profile_semester_average
        check (semester_validation_average between 0.00 and 20.00),
    constraint ck_academic_rule_profile_annual_average
        check (
            annual_validation_average is null
            or annual_validation_average between 0.00 and 20.00
        ),
    constraint ck_academic_rule_profile_maximum_inscriptions
        check (maximum_module_inscriptions > 0),
    constraint ck_academic_rule_profile_session_policy
        check (
            session_grade_policy in (
                'BEST_GRADE',
                'RATTRAPAGE_REPLACES_NORMAL',
                'RATTRAPAGE_CAPPED_AT_VALIDATION_THRESHOLD'
            )
        ),
    constraint ck_academic_rule_profile_progression_debt
        check (
            (allow_progression_with_debt and maximum_carried_modules > 0)
            or (not allow_progression_with_debt and maximum_carried_modules = 0)
        ),
    constraint ck_academic_rule_profile_status
        check (status in ('ACTIVE', 'INACTIVE'))
);

insert into permission (id, code, name, created_at, updated_at) values
    ('00000000-0000-0000-0000-000000000159', 'ACADEMIC_RULE_PROFILE_VIEW', 'View academic rule profiles', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000160', 'ACADEMIC_RULE_PROFILE_CREATE', 'Create academic rule profiles', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000161', 'ACADEMIC_RULE_PROFILE_UPDATE', 'Update academic rule profiles', current_timestamp, current_timestamp);
