create table if not exists tt1 (
    key_ varchar(300) PRIMARY KEY,
    value_ text null
);

create table if not exists tt2 (
    key_ varchar(300) PRIMARY KEY,
    value_ text null
);

drop procedure if exists sp1;

delimiter //;

CREATE PROCEDURE sp1(IN inputParam VARCHAR(255))
BEGIN
    SELECT inputParam;
END//