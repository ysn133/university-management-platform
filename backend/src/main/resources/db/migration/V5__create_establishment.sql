create table establishment (
    id uuid primary key,
    university_id uuid not null references university(id),
    name varchar(255) not null,
    type varchar(50) not null,
    status varchar(50) not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint uk_establishment_university_name unique (university_id, name)
);
