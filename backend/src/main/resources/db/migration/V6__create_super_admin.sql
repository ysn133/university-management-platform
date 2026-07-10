create table super_admin (
    id uuid primary key,
    user_account_id uuid not null unique references user_account(id),
    establishment_id uuid not null references establishment(id),
    created_at timestamp not null,
    updated_at timestamp not null
);
