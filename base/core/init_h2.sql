create table if not exists sys_config (
    key_ varchar(300) PRIMARY KEY,
    value_ text null
);

delete from sys_config;

insert into sys_config(key_,value_) values('db.key1', 'db.val1');