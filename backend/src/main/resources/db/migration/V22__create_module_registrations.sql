create table module_registration (
    id uuid primary key,
    semester_registration_id uuid not null references semester_registration(id),
    subject_module_id uuid not null references subject_module(id),
    origin_academic_level_id uuid references academic_level(id),
    inscription_number smallint not null check (inscription_number > 0),
    status varchar(30) not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint uk_module_registration_semester_subject
        unique (semester_registration_id, subject_module_id),
    constraint ck_module_registration_status check (
        status in ('ACTIVE', 'COMPLETED', 'CANCELLED')
    )
);
