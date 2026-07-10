create table user_account (
    id uuid primary key,
    university_email varchar(255) not null unique,
    password_hash varchar(255) not null,
    role_type varchar(50) not null,
    account_status varchar(50) not null,
    last_login_at timestamp null,
    created_at timestamp not null,
    updated_at timestamp not null
);
