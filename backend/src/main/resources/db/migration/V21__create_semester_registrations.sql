create table semester_registration (
    id uuid primary key,
    academic_registration_id uuid not null references academic_registration(id),
    semester_id uuid not null references semester(id),
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint uk_semester_registration_academic_registration_semester
        unique (academic_registration_id, semester_id)
);
