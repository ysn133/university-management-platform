alter table academic_level
    add column terminal_level boolean not null default false;

alter table progression_decision
    drop constraint ck_progression_decision_status;

alter table progression_decision
    add constraint ck_progression_decision_status check (
        decision_status in (
            'PROMOTED',
            'PROMOTED_BY_COMPENSATION',
            'PROMOTED_WITH_DEBT',
            'LEVEL_VALIDATED',
            'REPEAT',
            'FAILED'
        )
    );
