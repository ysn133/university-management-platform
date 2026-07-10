create table university (
    id uuid primary key,
    name varchar(255) not null unique,
    created_at timestamp not null,
    updated_at timestamp not null
);
