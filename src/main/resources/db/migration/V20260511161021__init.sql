create table user (
    id bigint primary key auto_increment,
    name varchar(100) not null
);

create table ticket (
    id bigint primary key auto_increment,
    serial varchar(100) not null,
    user_id bigint not null
);

insert into user(name) values ('testUser');
