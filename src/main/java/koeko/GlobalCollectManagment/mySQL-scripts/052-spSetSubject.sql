DELIMITER //
-- drop procedure koeko_collect.spSetSubject;//

CREATE PROCEDURE koeko_collect.spSetSubject (
								in parSUBJECT varchar(100),
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
    -- If subject exists already, returns its MUID
	select SBJ_MUID into @existMUID from subject where SBJ_SUBJECT=parSUBJECT;
	if isnull(@existMUID) or length(trim(@existMUID)) < 15 then
		-- select parMUID;
		if isnull(parMUID) or length(trim(parMUID)) < 15 then
		  call spGetNewMUID(newMUID);
		  insert into koeko_collect.subject (SBJ_SUBJECT, SBJ_MUID, SBJ_UPD_DTS)
			values (parSUBJECT, newMUID, parUPD_DATE);
		  insert into koeko_collect.to_update VALUE (newMUID);
		else
		  set newMUID = parMUID;
		  select SBJ_UPD_DTS into @globalTimeStamp from subject where SBJ_MUID=newMUID;
		  if @globalTimeStamp < parUPD_DATE then
			update koeko_collect.subject 
				set SBJ_SUBJECT=parSUBJECT, SBJ_UPD_DTS=parUPD_DATE
				where SBJ_MUID=newMUID;
			
		    insert into koeko_collect.to_update VALUE (newMUID);
		  end if;
		end if;
	else
		set newMUID = @existMUID;
	end if;
	set parMUID = newMUID;
    -- SELECT 'OK COMMIT';
  COMMIT;
END;//

DELIMITER ;
