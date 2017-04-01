create table if not exists test_none_mapping_columns (
  id int IDENTITY ,

  col1 varchar(100) null,

  col2 varchar(100) null,

  col3 varchar(100) null,

  PRIMARY KEY (id)
)