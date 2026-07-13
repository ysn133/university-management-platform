create table academic_year (
    id uuid primary key,
    establishment_id uuid not null references establishment(id),
    label varchar(9) not null,
    start_year smallint not null,
    end_year smallint not null,
    status varchar(30) not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint uk_academic_year_establishment_label unique (establishment_id, label),
    constraint uk_academic_year_establishment_start unique (establishment_id, start_year),
    constraint ck_academic_year_consecutive_years check (end_year = start_year + 1),
    constraint ck_academic_year_status check (status in ('PLANNED', 'ACTIVE', 'CLOSED'))
);

insert into permission (id, code, name, created_at, updated_at) values
    ('00000000-0000-0000-0000-000000000133', 'ACADEMIC_YEAR_UPDATE', 'Update academic years', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000134', 'ACADEMIC_YEAR_DELETE', 'Delete academic years', current_timestamp, current_timestamp);
