DELIMITER $$
-- drop procedure koeko_collect.spSetProfessor;$$

CREATE PROCEDURE koeko_collect.spSetProfessor(in parALIAS varchar(45),
								inout parMUID varchar(15),
								in parUPD_DATE timestamp)
BEGIN
  declare newMUID varchar(15);
  DECLARE EXIT HANDLER FOR SQLEXCEPTION
  BEGIN
    -- SELECT 'ERREUR ROLLBACK';
    ROLLBACK;
  END;
  START TRANSACTION;
	-- select parMUID;
    if isnull(parMUID) or length(trim(parMUID)) < 15 then
      call spGetNewMUID(newMUID);
      insert into professor (PRF_ALIAS, PRF_MUID, PRF_UPD_DATE) values (parALIAS, newMUID, parUPD_DATE);
	else
      set newMUID = parMUID;
      select PRF_UPD_DATE into @globalTimeStamp from professor where PRF_MUID=newMUID;
      if @globalTimeStamp < parUPD_DATE then
        update professor set PRF_ALIAS=parALIAS, PRF_UPD_DATE=parUPD_DATE where PRF_MUID=newMUID;
      end if;
	end if;
    
	set parMUID = newMUID;
    -- SELECT 'OK COMMIT';
  COMMIT;
END;$$

DELIMITER ;
