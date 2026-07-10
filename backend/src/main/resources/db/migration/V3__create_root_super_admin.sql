create table root_super_admin (
    id uuid primary key,
    user_account_id uuid not null unique references user_account(id),
    created_at timestamp not null,
    updated_at timestamp not null
);
