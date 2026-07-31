create table block (
    id uuid primary key,
    establishment_id uuid not null references establishment(id),
    code varchar(50) not null,
    name varchar(255) not null,
    status varchar(20) not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint uk_block_establishment_code unique (establishment_id, code),
    constraint ck_block_status check (status in ('ACTIVE', 'INACTIVE'))
);

create table room (
    id uuid primary key,
    establishment_id uuid not null references establishment(id),
    block_id uuid references block(id),
    code varchar(50) not null,
    name varchar(255) not null,
    room_type varchar(100) not null,
    capacity integer not null,
    status varchar(20) not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint uk_room_establishment_code unique (establishment_id, code),
    constraint ck_room_type
        check (room_type in ('LECTURE_HALL', 'CLASSROOM', 'COMPUTER_LAB')),
    constraint ck_room_capacity check (capacity > 0),
    constraint ck_room_status check (status in ('ACTIVE', 'INACTIVE'))
);

create index idx_block_establishment on block (establishment_id);
create index idx_room_establishment on room (establishment_id);
create index idx_room_block on room (block_id);

alter table schedule_entry add column room_id uuid references room(id);
create index idx_schedule_entry_room_time
    on schedule_entry (room_id, day_of_week, start_time, end_time);

insert into permission (id, code, name, created_at, updated_at) values
    ('00000000-0000-0000-0000-000000000177', 'BLOCK_VIEW', 'View blocks', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000178', 'BLOCK_CREATE', 'Create blocks', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000179', 'BLOCK_UPDATE', 'Update blocks', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000180', 'BLOCK_DELETE', 'Deactivate blocks', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000181', 'ROOM_VIEW', 'View rooms', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000182', 'ROOM_CREATE', 'Create rooms', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000183', 'ROOM_UPDATE', 'Update rooms', current_timestamp, current_timestamp),
    ('00000000-0000-0000-0000-000000000184', 'ROOM_DELETE', 'Deactivate rooms', current_timestamp, current_timestamp);
