update teaching_group_policy
set minimum_group_size = 25,
    maximum_group_size = 30,
    updated_at = current_timestamp
where minimum_group_size = 1
  and maximum_group_size = 25;
