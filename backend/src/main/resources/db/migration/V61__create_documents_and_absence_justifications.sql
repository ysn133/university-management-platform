create table uploaded_document (
    id uuid primary key,
    owner_user_account_id uuid not null references user_account(id),
    storage_key varchar(500) not null unique,
    original_filename varchar(255) not null,
    content_type varchar(100) not null,
    size_bytes bigint not null,
    purpose varchar(60) not null,
    status varchar(30) not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint ck_uploaded_document_size check (size_bytes > 0),
    constraint ck_uploaded_document_purpose check (purpose in ('ABSENCE_JUSTIFICATION')),
    constraint ck_uploaded_document_status check (status in ('TEMPORARY', 'ATTACHED', 'DELETED'))
);

create index idx_uploaded_document_owner
    on uploaded_document (owner_user_account_id, status, created_at);

create table absence_justification (
    id uuid primary key,
    absence_record_id uuid not null references absence_record(id),
    submitted_by_student_id uuid not null references student(id),
    document_id uuid not null references uploaded_document(id),
    reason varchar(1500) not null,
    status varchar(30) not null,
    reviewed_by_professor_id uuid references professor(id),
    decision_note varchar(1000),
    submitted_at timestamp not null,
    reviewed_at timestamp,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint uk_absence_justification_document unique (document_id),
    constraint ck_absence_justification_status check (status in ('PENDING', 'ACCEPTED', 'REJECTED'))
);

create index idx_absence_justification_absence
    on absence_justification (absence_record_id, submitted_at);

create index idx_absence_justification_status
    on absence_justification (status, submitted_at);
