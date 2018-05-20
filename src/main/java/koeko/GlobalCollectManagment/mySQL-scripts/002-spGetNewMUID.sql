DELIMITER //

CREATE        PROCEDURE koeko_collect.spGetNewMUID(OUT outMUID varchar(15))
BEGIN
  DECLARE CountMUID   int;
  DECLARE MUIDValue   int;
  DECLARE iDate	   date;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
  BEGIN
    ROLLBACK;
  END;
  SET outMUID = null;
  SET iDate = DATE(curdate());

  start transaction;
  SET CountMUID = (SELECT COUNT(*) FROM GEN_MUID WHERE GEN_DATE = iDate);
  IF CountMUID = 0 THEN
    INSERT INTO GEN_MUID VALUES(iDate, 1);
    SET MUIDValue = 1;
  ELSE
    UPDATE GEN_MUID SET MUID = MUID + 1 WHERE GEN_DATE = iDate ;
    SET MUIDValue = (SELECT MUID FROM GEN_MUID WHERE GEN_DATE = iDate);
  END IF;

  SET outMUID = concat(cast((YEAR(iDate) * 10000 + MONTH(iDate) * 100 + DAY(iDate)) as char(8)), lpad(cast(MUIDValue AS char(7)), 7, '0'));
  commit;
END;//

DELIMITER ;


