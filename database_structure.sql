create table users
(
    id                   serial
        constraint users_pk
            primary key,
    username             varchar(50)  not null,
    password             varchar(100) not null,
    email                varchar(100) not null,
    datetime_of_creation timestamp    not null
);

alter table users
    owner to postgres;

create table sessions
(
    "user"        integer      not null
        constraint user__fk
            references users
            on update cascade on delete cascade,
    session_token varchar(100) not null,
    expiration    timestamp    not null
);

comment on table sessions is 'Table containing temporary session keys';

alter table sessions
    owner to postgres;

create table programs
(
    id   serial
        constraint programs_pk
            primary key,
    name varchar(100) not null
);

comment on table programs is 'A table to contain the study programs that FOI offers';

comment on column programs.id is 'The primary key';

comment on column programs.name is 'The name of th study program';

alter table programs
    owner to postgres;

create table semesters
(
    id        serial
        constraint semesters_pk
            primary key,
    year      varchar(9) not null,
    "winter?" boolean    not null
);

alter table semesters
    owner to postgres;

create table courses
(
    id       serial
        constraint courses_pk
            primary key,
    name     varchar(100) not null,
    semester integer      not null
        constraint courses_semesters_id_fk
            references semesters,
    ects     integer      not null
);

alter table courses
    owner to postgres;

create table course_progam
(
    course_id  integer not null
        constraint course_progam_courses_id_fk
            references courses,
    program_id integer not null
        constraint course_progam_programs_id_fk
            references programs
);

alter table course_progam
    owner to postgres;

create table categories
(
    id           serial
        constraint categories_pk
            primary key,
    course_id    integer      not null
        constraint categories_courses_id_fk
            references courses,
    name         varchar(100) not null,
    points       integer      not null,
    requirements integer      not null
);

alter table categories
    owner to postgres;

create table subcategories
(
    id           serial
        constraint subcategories_pk
            primary key,
    category_id  integer      not null
        constraint subcategories_categories_id_fk
            references categories,
    name         varchar(100) not null,
    points       integer      not null,
    requirements integer      not null
);

alter table subcategories
    owner to postgres;

create table user_courses
(
    id        serial
        constraint user_courses_pk
            primary key,
    user_id   integer not null
        constraint user_courses_users_id_fk
            references users,
    course_id integer not null
        constraint user_courses_courses_id_fk
            references courses,
    is_active boolean not null
);

alter table user_courses
    owner to postgres;

create table course_categories
(
    id             serial
        constraint course_categories_pk
            primary key,
    user_course_id integer not null
        constraint course_categories_user_courses_id_fk
            references user_courses
            on update cascade on delete cascade,
    category_id    integer not null
        constraint course_categories_categories_id_fk
            references categories
            on update cascade on delete cascade,
    points         integer not null
);

alter table course_categories
    owner to postgres;

create table category_subcategories
(
    user_course_category_id integer not null
        constraint category_subcategories_course_categories_id_fk
            references course_categories
            on update cascade on delete cascade,
    subcategory_id          integer
        constraint category_subcategories_subcategories_id_fk
            references subcategories
            on update cascade on delete cascade,
    points                  integer not null
);

alter table category_subcategories
    owner to postgres;


create table admins
(
    id         serial
        constraint admins_pk
            primary key,
    user_id    integer   not null
        constraint admins_users_id_fk
            references users,
    time_added timestamp not null
);

alter table admins
    owner to postgres;

create table admin_course
(
    admin      integer   not null
        constraint admin_course_admins_id_fk
            references admins,
    course     integer   not null
        constraint admin_course_courses_id_fk
            references courses,
    date_added timestamp not null
);

alter table admin_course
    owner to postgres;

