DELIMITER $$
drop procedure koeko_collect.spSyncToWEB;$$
drop procedure koeko_collect.spSyncSQRelation;$$

CREATE PROCEDURE koeko_collect.spSyncToWEB()
BEGIN
  DECLARE done INT DEFAULT FALSE;
  -- SUbject part
  DECLARE muid CHAR(15);
  DECLARE subjectName varchar(100);
  DECLARE upddts timestamp;
  -- questions qcm
  DECLARE difficultyLevel int;
  DECLARE question text;
  DECLARE option0 text;
  DECLARE option1 text;
  DECLARE option2 text;
  DECLARE option3 text;
  DECLARE option4 text;
  DECLARE option5 text;
  DECLARE option6 text;
  DECLARE option7 text;
  DECLARE option8 text;
  DECLARE option9 text;
  DECLARE nbCorrectAnswer int;
  DECLARE imagePath text;
  DECLARE owner CHAR(15);
  -- relation subject-question
  DECLARE cur1 CURSOR FOR SELECT SBJ_MUID, SBJ_SUBJECT, SBJ_UPD_DTS FROM `subject`
    join koeko_collect.to_update on TUP_MUID=SBJ_MUID;
  DECLARE cursorQCM CURSOR FOR
    SELECT `question_multiple_choice`.`QMC_MUID`,
    `question_multiple_choice`.`QMC_LEVEL`,
    `question_multiple_choice`.`QMC_QUESTION`,
    `question_multiple_choice`.`QMC_OPTION0`,
    `question_multiple_choice`.`QMC_OPTION1`,
    `question_multiple_choice`.`QMC_OPTION2`,
    `question_multiple_choice`.`QMC_OPTION3`,
    `question_multiple_choice`.`QMC_OPTION4`,
    `question_multiple_choice`.`QMC_OPTION5`,
    `question_multiple_choice`.`QMC_OPTION6`,
    `question_multiple_choice`.`QMC_OPTION7`,
    `question_multiple_choice`.`QMC_OPTION8`,
    `question_multiple_choice`.`QMC_OPTION9`,
    `question_multiple_choice`.`QMC_NB_CORRECT_ANS`,
    `question_multiple_choice`.`QMC_IMAGE_PATH`,
    `question_multiple_choice`.`QMC_OWNER`,
    `question_multiple_choice`.`QMC_UPD_DTS`
  FROM `koeko_collect`.`question_multiple_choice`
  join koeko_collect.to_update on TUP_MUID=QMC_MUID;
  DECLARE cursorSQR CURSOR FOR
    SELECT `subject_question_relation`.`SQR_SBJ_MUID`,
    `subject_question_relation`.`SQR_QUE_MUID`,
    `subject_question_relation`.`SQR_QUE_TYP`,
    `subject_question_relation`.`SQR_LEVEL`
	FROM `koeko_collect`.`subject_question_relation`;

 
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE; 
  DECLARE EXIT HANDLER FOR SQLEXCEPTION
  BEGIN
	select 'Rollback';
    ROLLBACK;
  END;

  start transaction;
  OPEN cur1;

  sbj_loop: LOOP
    FETCH cur1 INTO muid, subjectName, upddts;
    -- select 'Fetch', muid, subjectName, upddts, done;
    IF done THEN
      LEAVE sbj_loop;
    END IF;
    select count(IDENTIFIER), MODIF_DATE into @web_cnt, @web_dts from koeko_website.subjects 
		where IDENTIFIER=muid;
         select @web_cnt, @web_dts;
    IF @web_cnt=0 THEN
	  -- Le muid n'existe pas sur le site
      INSERT INTO koeko_website.subjects (IDENTIFIER,`SUBJECT`,MODIF_DATE)
		values (muid, subjectName, upddts);
    ELSEIF not isnull(@web_dts) and @web_dts < upddts THEN
      UPDATE koeko_website.subjects set `SUBJECT`=subjectName, MODIF_DATE=upddts
		where IDENTIFIER=muid;
    END IF;
  END LOOP;
  CLOSE cur1;


  SET done = FALSE;
  OPEN cursorQCM;
  qcm_loop: LOOP
    FETCH cursorQCM INTO muid, difficultyLevel, question, option0, option1, option2,
      option3, option4, option5, option6, option7, option8, option9, nbCorrectAnswer,
      imagePath, `owner`, upddts;
     select 'Fetch qcm', muid, question, upddts, done;
    IF done THEN
      LEAVE qcm_loop;
    END IF;
    select count(IDENTIFIER), MODIF_DATE into @web_cnt, @web_dts from koeko_website.multiple_choice_questions
		where IDENTIFIER=muid;
         select @web_cnt, @web_dts;
    IF @web_cnt=0 THEN
		select 'Insert question ', muid, difficultyLevel, question, option0, option1, option2,
         option3, option4, option5, option6, option7, option8, option9,
         nbCorrectAnswer, imagePath, 1, upddts;
	  -- Le muid n'existe pas sur le site
      INSERT INTO koeko_website.multiple_choice_questions (`IDENTIFIER`,
		`DIFFICULTY_LEVEL`,	`QUESTION`,	`OPTION0`, `OPTION1`, `OPTION2`,
		`OPTION3`, `OPTION4`, `OPTION5`, `OPTION6`,	`OPTION7`, `OPTION8`,
		`OPTION9`, `NB_CORRECT_ANS`, `IMAGE_PATH`, `RATING`, `MODIF_DATE`)
		VALUES
		(muid, difficultyLevel, question, option0, option1, option2,
         option3, option4, option5, option6, option7, option8, option9,
         nbCorrectAnswer, imagePath, '1', upddts);
         select 'Inserted successfully';
    ELSEIF not isnull(@web_dts) and @web_dts < upddts THEN
       select 'Update question ', muid;
      UPDATE `koeko_website`.`multiple_choice_questions` SET
		`IDENTIFIER` = muid,
		`DIFFICULTY_LEVEL` = difficultyLevel,
		`QUESTION` = question,
		`OPTION0` = option0,
		`OPTION1` = option1,
		`OPTION2` = option2,
		`OPTION3` = option3,
		`OPTION4` = option4,
		`OPTION5` = option5,
		`OPTION6` = option6,
		`OPTION7` = option7,
		`OPTION8` = option8,
		`OPTION9` = option9,
		`NB_CORRECT_ANS` = nbCorrectAnswer,
		`IMAGE_PATH` = imagePath,
		-- `RATING` = <{RATING: }>,
		`MODIF_DATE` = upddts
		WHERE `IDENTIFIER` = muid;
    END IF;
    
    -- Process the relation question-subject for this question
    -- Remove the relations
     select 'Remove relations for ', muid;
    DELETE FROM  koeko_website.relation_question_subject
      WHERE IDENTIFIER_QUESTION=muid;
	
    -- Insert all relations from subjects to this question
    select 'call spSyncSQRelation';
    call koeko_collect.spSyncSQRelation(muid);
  END LOOP;
  CLOSE cursorQCM;
  
  -- If everything is synchronized, clean the to_update table
  -- so that we don't update twice
  TRUNCATE TABLE koeko_collect.to_update;

   select 'Commit';
  commit;
END;$$


CREATE PROCEDURE koeko_collect.spSyncSQRelation(in parQUE_MUID varchar(15))
BEGIN
  DECLARE done INT DEFAULT FALSE;

  -- relation subject-question
  DECLARE sbj_muid CHAR(15);
  DECLARE que_typ char(3);
  DECLARE difficultyLevel int;
  DECLARE upddts timestamp;
   
  -- relation subject-question
  DECLARE cursorSQR CURSOR FOR
    SELECT SQR_SBJ_MUID, SQR_QUE_TYP, SQR_LEVEL
	FROM `koeko_collect`.`subject_question_relation`
    WHERE SQR_QUE_MUID=parQUE_MUID;
   
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
   select 'Declare not found handler';
  
  select 'Open cursor sqr';
  SET done = FALSE;
  OPEN cursorSQR;
  sqr_loop: LOOP
    FETCH cursorSQR INTO sbj_muid, que_typ, difficultyLevel;
     select 'Fetch SQR', sbj_muid, que_typ, difficultyLevel, done;
    IF done THEN
      LEAVE sqr_loop;
	  -- Le muid n'existe pas sur le site
      INSERT INTO koeko_website.relation_question_subject (`IDENTIFIER`,
		`IDENTIFIER_QUESTION`,	`IDENTIFIER_SUBJECT`)
		VALUES
		(null, parQUE_MUID, sbj_muid); -- , que_typ, difficultyLevel);
    END IF;
  END LOOP;

  CLOSE cursorSQR;
END;$$
  
DELIMITER ;
