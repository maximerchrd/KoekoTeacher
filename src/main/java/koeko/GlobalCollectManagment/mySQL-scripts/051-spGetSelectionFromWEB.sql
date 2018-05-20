DELIMITER //
-- drop procedure global_collect.spGetSelectionFromWEB;//

CREATE PROCEDURE koeko_collect.spGetSelectionFromWEB(in parMUID varchar(15))
BEGIN
  DECLARE EXIT HANDLER FOR SQLEXCEPTION
  BEGIN
    ROLLBACK;
  END;
  START TRANSACTION;
    insert into koeko_collect.selection (SEL_TYPE, SEL_QUESTION_MUID, SEL_PRF_MUID) 
		values ('qcm', '', '201805010000005');
  COMMIT;
END;//

DELIMITER ;
