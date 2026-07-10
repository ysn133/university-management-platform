create table user_profile (
    id uuid primary key,
    user_account_id uuid not null unique references user_account(id),
    first_name varchar(255) not null,
    last_name varchar(255) not null,
    birth_date date null,
    sex varchar(50) null,
    phone_number varchar(50) null,
    profile_picture_path varchar(500) null,
    created_at timestamp not null,
    updated_at timestamp not null
);
