alter table semester
    add column term_type varchar(20);

update semester
set term_type = case
    when mod(semester_order, 2) = 1 then 'AUTUMN'
    else 'SPRING'
end;

alter table semester
    alter column term_type set not null;

alter table semester
    add constraint chk_semester_term_type
        check (term_type in ('AUTUMN', 'SPRING'));
