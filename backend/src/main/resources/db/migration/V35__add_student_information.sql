alter table user_profile add column place_of_birth varchar(255);
alter table user_profile add column nationality varchar(100);
alter table user_profile add column cin varchar(50);
alter table user_profile add constraint uk_user_profile_cin unique (cin);

alter table student add column apogee_code varchar(50);
alter table student add column national_student_code varchar(50);
alter table student add column initial_enrollment_date date;

update student
set apogee_code = 'LEGACY-' || replace(cast(id as varchar), '-', '')
where apogee_code is null;

alter table student alter column apogee_code set not null;
alter table student add constraint uk_student_apogee_code unique (apogee_code);
alter table student add constraint uk_student_national_code unique (national_student_code);
