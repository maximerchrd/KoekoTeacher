DELIMITER //
-- drop procedure koeko_collect.spSetQuestionMultipleChoice;//

CREATE PROCEDURE koeko_collect.spSetQuestionMultipleChoice (in parLEVEL int,
								in parQUESTION varchar(500),
								in parOPTION0 varchar(100),
								in parOPTION1 varchar(100),
								in parOPTION2 varchar(100),
								in parOPTION3 varchar(100),
								in parOPTION4 varchar(100),
								in parOPTION5 varchar(100),
								in parOPTION6 varchar(100),
								in parOPTION7 varchar(100),
								in parOPTION8 varchar(100),
								in parOPTION9 varchar(100),
								in parNB_CORRECT_ANS int,
								in parIMAGE_PATH  varchar(100),
								inout parMUID varchar(15),
                                in parOWNER varchar(15),
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
      insert into question_multiple_choice (QMC_LEVEL, QMC_QUESTION, QMC_OPTION0, QMC_OPTION1, QMC_OPTION2, QMC_OPTION3,
		QMC_OPTION4, QMC_OPTION5, QMC_OPTION6, QMC_OPTION7, QMC_OPTION8, QMC_OPTION9,
		QMC_NB_CORRECT_ANS, QMC_IMAGE_PATH, QMC_MUID, QMC_OWNER, QMC_UPD_DTS)
		values (parLEVEL, parQUESTION, parOPTION0, parOPTION1, parOPTION2, parOPTION3, parOPTION4, parOPTION5, parOPTION6, parOPTION7, parOPTION8, parOPTION9,
			    parNB_CORRECT_ANS, parIMAGE_PATH, newMUID, parOWNER, parUPD_DATE);
	  insert into koeko_collect.to_update VALUE (newMUID);
	else
      set newMUID = parMUID;
      select QMC_UPD_DTS into @globalTimeStamp from question_multiple_choice where QMC_MUID=newMUID;
      if @globalTimeStamp < parUPD_DATE then
        update question_multiple_choice 
			set QMC_LEVEL=parLEVEL, QMC_QUESTION=parQUESTION, QMC_OPTION0=parOPTION0, QMC_OPTION1=parOPTION1, QMC_OPTION2=parOPTION2, QMC_OPTION3=parOPTION3,
				QMC_OPTION4=parOPTION4, QMC_OPTION5=parOPTION5, QMC_OPTION6=parOPTION6, QMC_OPTION7=parOPTION7, QMC_OPTION8=parOPTION8, QMC_OPTION9=parOPTION9, 
				QMC_NB_CORRECT_ANS=parNB_CORRECT_ANS,
				QMC_IMAGE_PATH=parIMAGE_PATH, QMC_UPD_DTS=parUPD_DATE
			where QMC_MUID=newMUID;
        insert into koeko_collect.to_update VALUE (newMUID);
      end if;
	end if;
    
	set parMUID = newMUID;
    -- SELECT 'OK COMMIT';
  COMMIT;
END;//

DELIMITER ;
