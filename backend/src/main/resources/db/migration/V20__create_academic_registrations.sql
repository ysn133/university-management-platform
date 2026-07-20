create table academic_registration (
    id uuid primary key,
    student_id uuid not null references student(id),
    program_filiere_id uuid not null references program_filiere(id),
    academic_level_id uuid not null references academic_level(id),
    academic_year_id uuid not null references academic_year(id),
    status varchar(30) not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint uk_academic_registration_student_year unique (student_id, academic_year_id),
    constraint ck_academic_registration_status check (
        status in ('ACTIVE', 'SUSPENDED', 'COMPLETED', 'CANCELLED')
    )
);
