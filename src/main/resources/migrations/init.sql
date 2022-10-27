create table user (
    login varchar(50) primary key
);

create table measurement (
    id bigint identity primary key,
    user_login varchar(50) not null,
    date_saved datetime not null,
    foreign key (user_login) references user (login) on delete cascade
);

create table consumption (
    id bigint identity primary key,
    measurement_id bigint not null,
    type varchar(30) not null,
    value numeric not null,
    unique (measurement_id, type),
    foreign key (measurement_id) references measurement (id) on delete cascade
);