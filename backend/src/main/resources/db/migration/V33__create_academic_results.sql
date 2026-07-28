create table module_result (
    id uuid primary key,
    module_registration_id uuid not null references module_registration(id),
    academic_rule_profile_id uuid not null references academic_rule_profile(id),
    final_grade_value numeric(5, 2) not null,
    result_status varchar(10) not null,
    calculated_at timestamp not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint uk_module_result_registration unique (module_registration_id),
    constraint ck_module_result_value check (
        final_grade_value >= 0 and final_grade_value <= 20
    ),
    constraint ck_module_result_status check (
        result_status in ('V', 'AV', 'NV')
    )
);

create table semester_result (
    id uuid primary key,
    semester_registration_id uuid not null references semester_registration(id),
    academic_rule_profile_id uuid not null references academic_rule_profile(id),
    semester_average numeric(5, 2) not null,
    result_status varchar(30) not null,
    evaluated_at timestamp not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint uk_semester_result_registration unique (semester_registration_id),
    constraint ck_semester_result_average check (
        semester_average >= 0 and semester_average <= 20
    ),
    constraint ck_semester_result_status check (
        result_status in ('VALIDATED', 'NON_VALIDATED')
    )
);

create table progression_decision (
    id uuid primary key,
    academic_registration_id uuid not null references academic_registration(id),
    academic_rule_profile_id uuid not null references academic_rule_profile(id),
    decision_status varchar(30) not null,
    annual_average numeric(5, 2) not null,
    outstanding_module_count integer not null,
    decided_at timestamp not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint uk_progression_decision_registration unique (academic_registration_id),
    constraint ck_progression_decision_average check (
        annual_average >= 0 and annual_average <= 20
    ),
    constraint ck_progression_decision_outstanding check (
        outstanding_module_count >= 0
    ),
    constraint ck_progression_decision_status check (
        decision_status in (
            'PROMOTED',
            'PROMOTED_WITH_DEBT',
            'REPEAT',
            'FAILED'
        )
    )
);

create index idx_module_result_rule_profile
    on module_result (academic_rule_profile_id);

create index idx_semester_result_rule_profile
    on semester_result (academic_rule_profile_id);

create index idx_progression_decision_rule_profile
    on progression_decision (academic_rule_profile_id);
