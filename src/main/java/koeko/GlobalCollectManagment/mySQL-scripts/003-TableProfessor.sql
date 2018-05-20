-- DROP TABLE global_collect.professor

CREATE TABLE IF NOT EXISTS koeko_collect.professor (
 PRF_ALIAS     varchar(45),  
 PRF_MUID      char(15),
 PRF_UPD_DATE  timestamp,
  PRIMARY KEY (PRF_MUID),
  UNIQUE KEY `MUID_UNIQUE` (`PRF_MUID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
