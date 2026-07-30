alter table professor add column employee_number varchar(50);
alter table professor add column academic_rank varchar(100);
alter table professor add column hire_date date;
alter table professor add column maximum_weekly_teaching_minutes integer;

update professor
set employee_number = 'LEGACY-' || replace(cast(id as varchar), '-', ''),
    maximum_weekly_teaching_minutes = 480
where employee_number is null;

alter table professor alter column employee_number set not null;
alter table professor alter column maximum_weekly_teaching_minutes set not null;
alter table professor add constraint uk_professor_employee_number unique (employee_number);
alter table professor add constraint ck_professor_weekly_minutes_positive
    check (maximum_weekly_teaching_minutes > 0);
