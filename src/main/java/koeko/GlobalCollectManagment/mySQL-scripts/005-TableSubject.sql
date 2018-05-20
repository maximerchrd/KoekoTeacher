-- DROP TABLE global_collect.subject

CREATE TABLE IF NOT EXISTS koeko_collect.subject (  
 SBJ_MUID      char(15),
 SBJ_SUBJECT   varchar(100),
 SBJ_UPD_DTS   timestamp,
  PRIMARY KEY (SBJ_MUID),
  UNIQUE KEY `MUID_UNIQUE` (`SBJ_MUID`), UNIQUE (SBJ_SUBJECT)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
