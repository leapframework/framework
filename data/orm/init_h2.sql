create table if not exists view_table (
  id varchar(36) not null,

  col1 varchar(100) null,

  col2 varchar(100) null,

  col3 varchar(100) null,

  PRIMARY KEY (id)
);

create or replace view if not exists view_entity as select * from view_table;