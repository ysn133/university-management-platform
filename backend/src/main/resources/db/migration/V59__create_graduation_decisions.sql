create table graduation_decision (
    id uuid primary key,
    terminal_academic_registration_id uuid not null references academic_registration(id),
    decision_status varchar(20) not null,
    graduation_average numeric(5, 2) not null,
    decided_at timestamp not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint uk_graduation_decision_terminal_registration
        unique (terminal_academic_registration_id),
    constraint ck_graduation_decision_status
        check (decision_status in ('GRADUATED')),
    constraint ck_graduation_decision_average
        check (graduation_average >= 0 and graduation_average <= 20)
);
