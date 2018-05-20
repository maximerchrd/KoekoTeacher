-- DROP TABLE koeko_collect.question_multiple_choice

CREATE TABLE IF NOT EXISTS koeko_collect.question_multiple_choice (
 QMC_MUID           char(15),
 QMC_LEVEL          INT     NOT NULL,
 QMC_QUESTION       TEXT    NOT NULL,
 QMC_OPTION0        TEXT    NOT NULL,
 QMC_OPTION1        TEXT    NOT NULL,
 QMC_OPTION2        TEXT    NOT NULL,
 QMC_OPTION3        TEXT    NOT NULL,
 QMC_OPTION4        TEXT    NOT NULL,
 QMC_OPTION5        TEXT    NOT NULL,
 QMC_OPTION6        TEXT    NOT NULL,
 QMC_OPTION7        TEXT    NOT NULL,
 QMC_OPTION8        TEXT    NOT NULL,
 QMC_OPTION9        TEXT    NOT NULL,
 QMC_NB_CORRECT_ANS INT     NOT NULL,
 QMC_IMAGE_PATH     TEXT    NOT NULL,
 QMC_OWNER          char(15),
 QMC_UPD_DTS        timestamp,
  PRIMARY KEY (QMC_MUID),
  UNIQUE KEY `QMC_MUID_UNIQUE` (`QMC_MUID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;