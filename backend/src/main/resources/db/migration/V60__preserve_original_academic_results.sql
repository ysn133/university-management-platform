alter table module_result
    add column original_final_grade_value numeric(5, 2);

alter table module_result
    add column original_result_status varchar(10);

alter table module_result
    add constraint ck_module_result_original_value check (
        original_final_grade_value is null
        or (original_final_grade_value >= 0 and original_final_grade_value <= 20)
    );

alter table module_result
    add constraint ck_module_result_original_status check (
        original_result_status is null
        or original_result_status in ('V', 'AV', 'NV')
    );

alter table semester_result
    add column original_semester_average numeric(5, 2);

alter table semester_result
    add column original_result_status varchar(30);

alter table semester_result
    add constraint ck_semester_result_original_average check (
        original_semester_average is null
        or (original_semester_average >= 0 and original_semester_average <= 20)
    );

alter table semester_result
    add constraint ck_semester_result_original_status check (
        original_result_status is null
        or original_result_status in ('VALIDATED', 'NON_VALIDATED')
    );
