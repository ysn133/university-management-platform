create table teaching_group (
    id uuid primary key,
    semester_id uuid not null references semester(id),
    source_class_group_id uuid references class_group(id),
    name varchar(255) not null,
    audience_type varchar(30) not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint uk_teaching_group_semester_name unique (semester_id, name),
    constraint ck_teaching_group_audience_type
        check (audience_type in ('WHOLE_COHORT', 'CLASS_GROUP', 'SUBGROUP')),
    constraint ck_teaching_group_source_class
        check (
            (audience_type = 'WHOLE_COHORT' and source_class_group_id is null)
            or (audience_type in ('CLASS_GROUP', 'SUBGROUP') and source_class_group_id is not null)
        )
);

create table teaching_group_membership (
    id uuid primary key,
    teaching_group_id uuid not null references teaching_group(id),
    semester_registration_id uuid not null references semester_registration(id),
    created_at timestamp not null,
    constraint uk_teaching_group_membership
        unique (teaching_group_id, semester_registration_id)
);
