CREATE DATABASE IF NOT EXISTS moviedb;
USE moviedb;

CREATE TABLE if not exists movies(
    id varchar(10) primary key,
    title text,
    year integer not null,
    director varchar(100) default '',
    fulltext (title)
);

CREATE TABLE IF NOT EXISTS stars(
    id varchar(10) primary key,
    name varchar(100) default '',
    birthYear integer
);

CREATE TABLE IF NOT EXISTS stars_in_movies(
    starId varchar(10) default '',
    movieId varchar(10) default '',
    primary key (starId, movieId),
    foreign key (starId) references stars(id),
    foreign key (movieId) references movies(id)
);

CREATE TABLE IF NOT EXISTS genres(
    id integer auto_increment primary key,
    name varchar(32) default ''
);

CREATE TABLE IF NOT EXISTS genres_in_movies (
    genreId integer not null,
    movieId varchar(10) default '',
    primary key (genreId, movieId),
    foreign key (genreId) references genres(id),
    foreign key (movieId) references movies(id)
);

CREATE TABLE IF NOT EXISTS creditcards (
    id varchar(20) primary key,
    firstName varchar(50) default '',
    lastName varchar(50) default '',
    expiration date not null
);

CREATE TABLE IF NOT EXISTS customers (
    id integer auto_increment primary key,
    firstName varchar(50) default '',
    lastName varchar(50) default '',
    ccId varchar(20) default '',
    address varchar(200) default '',
    email varchar(50) default '',
    password varchar(20) default '',
    foreign key (ccId) references creditcards(id)
);

CREATE TABLE IF NOT EXISTS sales (
    id integer auto_increment primary key,
    customerId integer not null,
    movieId varchar(10) default '',
    /*quantity int default 1,*/
    saleDate date not null,
    foreign key (customerId) references customers(id),
    foreign key (movieId) references movies(id)
);

CREATE TABLE IF NOT EXISTS ratings (
    movieId varchar(10) default '',
    rating float not null ,
    numVotes integer not null,
    primary key (movieId),
    foreign key (movieId) references movies(id)
);

CREATE TABLE IF NOT EXISTS employees (
    email varchar(50) primary key,
    password varchar(20) not null,
    fullname varchar(100)
);