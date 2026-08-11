alter table semester add column start_date date;
alter table semester add column end_date date;

update semester s set
    start_date = case when s.term_type = 'AUTUMN' then cast(concat(ay.start_year, '-09-01') as date) else cast(concat(ay.end_year, '-02-01') as date) end,
    end_date = case when s.term_type = 'AUTUMN' then cast(concat(ay.end_year, '-01-31') as date) else cast(concat(ay.end_year, '-06-30') as date) end
from academic_year ay where ay.id = s.academic_year_id;

alter table semester alter column start_date set not null;
alter table semester alter column end_date set not null;
alter table semester add constraint ck_semester_dates check (end_date >= start_date);

alter table exam_schedule add column start_date date;
alter table exam_schedule add column end_date date;
update exam_schedule es set start_date = s.end_date + 1, end_date = s.end_date + 14 from semester s where s.id = es.semester_id;
alter table exam_schedule alter column start_date set not null;
alter table exam_schedule alter column end_date set not null;
alter table exam_schedule add constraint ck_exam_schedule_dates check (end_date >= start_date);

alter table module_exam add column room_id uuid references room(id);
create index idx_module_exam_room_date on module_exam (room_id, exam_date, start_time);
