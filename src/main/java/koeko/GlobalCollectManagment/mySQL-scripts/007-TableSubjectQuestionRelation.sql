-- DROP TABLE global_collect.subject_question_relation

CREATE TABLE IF NOT EXISTS koeko_collect.subject_question_relation (
 SQR_SBJ_MUID    varchar(15),  
 SQR_QUE_MUID    varchar(15),
 SQR_QUE_TYP   CHAR(3),
 SQR_LEVEL     int, PRIMARY KEY (SQR_SBJ_MUID, SQR_QUE_MUID),
  UNIQUE KEY `MUID_UNIQUE` (SQR_SBJ_MUID, SQR_QUE_MUID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
