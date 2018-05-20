-- DROP TABLE global_collect.subject_question_relation

CREATE TABLE IF NOT EXISTS koeko_collect.to_update (
  TUP_MUID    varchar(15),
  PRIMARY KEY (TUP_MUID),
  UNIQUE KEY `TUP_UNIQUE` (TUP_MUID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
