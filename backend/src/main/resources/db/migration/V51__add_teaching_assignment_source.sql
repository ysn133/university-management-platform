alter table teaching_assignment
    add column assignment_source varchar(20) not null default 'MANUAL';

alter table teaching_assignment
    add constraint ck_teaching_assignment_source
    check (assignment_source in ('MANUAL', 'AUTOMATIC'));

alter table teaching_assignment
    alter column assignment_source drop default;
