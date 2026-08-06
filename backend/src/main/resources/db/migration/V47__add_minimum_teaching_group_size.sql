alter table teaching_group_policy
    add column minimum_group_size integer not null default 1;

alter table teaching_group_policy
    alter column minimum_group_size drop default;

alter table teaching_group_policy
    add constraint ck_teaching_group_policy_minimum_size
        check (minimum_group_size > 0);

alter table teaching_group_policy
    add constraint ck_teaching_group_policy_size_range
        check (minimum_group_size <= maximum_group_size);
