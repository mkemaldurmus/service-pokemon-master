--Setup database
DROP DATABASE IF EXISTS pokemon;
CREATE DATABASE pokemon;

\connect pokemon

CREATE TABLE IF NOT EXISTS pokemon
(
    id           integer not null
        primary key,
    name         varchar(500),
    height       integer,
    weight       integer,
    frontdefault varchar(500),
    backdefault  varchar(500),
    evolution    varchar(500),
    stats        text
);

CREATE TABLE IF NOT EXISTS types
(
    id   serial not null
        primary key,
    pid  integer
        constraint fk_pokemon
            references pokemon,
    name varchar(500)
);

CREATE TABLE IF NOT EXISTS favorite_list
(
    list_name   varchar(100),
    pokemon_name varchar(100)
);