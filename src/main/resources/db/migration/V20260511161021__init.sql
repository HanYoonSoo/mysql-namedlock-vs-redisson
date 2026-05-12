create table user (
    id bigint primary key auto_increment,
    name varchar(100) not null
);

create table ticket (
    id bigint primary key auto_increment,
    serial varchar(100) not null,
    user_id bigint not null
);

insert into user(name)
values ('testUser01'), ('testUser02'), ('testUser03'), ('testUser04'), ('testUser05'),
       ('testUser06'), ('testUser07'), ('testUser08'), ('testUser09'), ('testUser10'),
       ('testUser11'), ('testUser12'), ('testUser13'), ('testUser14'), ('testUser15'),
       ('testUser16'), ('testUser17'), ('testUser18'), ('testUser19'), ('testUser20'),
       ('testUser21'), ('testUser22'), ('testUser23'), ('testUser24'), ('testUser25'),
       ('testUser26'), ('testUser27'), ('testUser28'), ('testUser29'), ('testUser30'),
       ('testUser31'), ('testUser32'), ('testUser33'), ('testUser34'), ('testUser35'),
       ('testUser36'), ('testUser37'), ('testUser38'), ('testUser39'), ('testUser40'),
       ('testUser41'), ('testUser42'), ('testUser43'), ('testUser44'), ('testUser45'),
       ('testUser46'), ('testUser47'), ('testUser48'), ('testUser49'), ('testUser50');
