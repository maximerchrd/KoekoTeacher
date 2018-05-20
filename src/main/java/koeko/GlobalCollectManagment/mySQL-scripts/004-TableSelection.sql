-- DROP TABLE global_collect.selection

CREATE TABLE IF NOT EXISTS koeko_collect.selection (
 SEL_TYPE      varchar(4),  
 SEL_QUESTION_MUID  char(15),
 SEL_PRF_MUID  char(15),
  PRIMARY KEY (SEL_PRF_MUID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
