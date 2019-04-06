--+------------+
--|            |
--|   SCHEMA   |
--|            |
--+------------+

DROP SCHEMA IF EXISTS projet CASCADE;
CREATE SCHEMA projet;

--+------------+
--|            |
--|   TYPES    |
--|            |
--+------------+

CREATE TYPE projet.STATUS AS ENUM('N', 'A', 'M');

--+------------+
--|            |
--|   TABLES   |
--|            |
--+------------+

CREATE TABLE projet.utilisateurs (
	no_utilisateur SERIAL PRIMARY KEY,
	mail VARCHAR(100) NOT NULL UNIQUE CHECK (mail SIMILAR TO '_%@__%.__%'),
	pseudo VARCHAR(100) NOT NULL UNIQUE CHECK (pseudo<>''),
	mot_de_passe VARCHAR(100) NOT NULL CHECK(mot_de_passe<>''),
	reputation INTEGER NOT NULL CHECK(reputation >= 0),
	statut projet.STATUS NOT NULL,
	derniere_date_privilege TIMESTAMP,
	compte_active BOOLEAN NOT NULL
);

CREATE TABLE projet.questions (
	no_question SERIAL PRIMARY KEY,
	titre VARCHAR(100) NOT NULL CHECK(titre<>''),
	contenu VARCHAR(200) NOT NULL CHECK(contenu<>''),
	date_creation TIMESTAMP NOT NULL,
	date_derniere_edition TIMESTAMP NULL,
	no_utilisateur INTEGER REFERENCES projet.utilisateurs (no_utilisateur) NOT NULL,
	no_utilisateur_derniere_edition INTEGER REFERENCES projet.utilisateurs (no_utilisateur),
	cloturee BOOLEAN NOT NULL
);

CREATE TABLE projet.tags_existant (
	no_tag_existant SERIAL PRIMARY KEY,
	libelle VARCHAR(10) NOT NULL UNIQUE CHECK(libelle<>'')
);

CREATE TABLE projet.reponses (
	no_question INTEGER REFERENCES projet.questions (no_question) NOT NULL,
	contenu VARCHAR(200)  NOT NULL CHECK(contenu<>''),
	date_creation TIMESTAMP NOT NULL,
	no_utilisateur INTEGER REFERENCES projet.utilisateurs (no_utilisateur) NOT NULL,
	score INTEGER NOT NULL,
	no_reponse INTEGER NOT NULL CHECK(no_reponse > 0),
	PRIMARY KEY (no_question, no_reponse)
);

CREATE TABLE projet.tags (
	no_tag_existant INTEGER REFERENCES projet.tags_existant (no_tag_existant) NOT NULL,
	no_question INTEGER REFERENCES projet.questions (no_question) NOT NULL,
	PRIMARY KEY (no_question, no_tag_existant)
);

CREATE TABLE projet.votes(
	no_utilisateur INTEGER REFERENCES projet.utilisateurs (no_utilisateur) NOT NULL,
	positivite BOOLEAN NOT NULL,
	no_question INTEGER NOT NULL,
	no_reponse INTEGER NOT NULL,
	FOREIGN KEY (no_question,no_reponse) REFERENCES projet.reponses(no_question, no_reponse),
	PRIMARY KEY (no_utilisateur, no_question, no_reponse)
);

--+------------+
--|            |
--| PROCEDURES |
--|            |
--+------------+

--RECUPERATION D'UN MOT DE PASSE
CREATE OR REPLACE FUNCTION projet.recuperer_mdp(VARCHAR(100))
RETURNS VARCHAR(100) AS $$
DECLARE
	pseudo_p ALIAS FOR $1;
	mdp VARCHAR(100);
BEGIN
	SELECT U.mot_de_passe FROM projet.utilisateurs U WHERE pseudo_p = U.pseudo INTO mdp;
	RETURN mdp;
END;
$$ LANGUAGE plpgsql;

--CONNEXION UTILISATEUR
CREATE OR REPLACE FUNCTION projet.connexion(varchar(100))
RETURNS integer AS $$ 
DECLARE 
	pseudo_p ALIAS FOR $1;
	no_utilisateur_p integer; 
BEGIN 
	IF NOT EXISTS(SELECT * FROM projet.utilisateurs util WHERE util.pseudo = pseudo_p and util.compte_active = true)
		THEN RAISE 'le pseudo/le mot de passe est incorrect ou votre compte a été desactivé ';
	END IF;											 
	SELECT util.no_utilisateur FROM projet.utilisateurs util WHERE util.pseudo = pseudo_p INTO no_utilisateur_p;
	RETURN no_utilisateur_p;
END;
$$ LANGUAGE plpgsql;

-- INSCRIPTION D'UN NOUVEL UTILISATEUR
CREATE OR REPLACE FUNCTION  projet.inscription_utilisateur(varchar(100),varchar(100),varchar(100)) 
RETURNS integer AS $$ 
DECLARE 
	mail_p ALIAS FOR $1;
	pseudo_p ALIAS FOR $2;
	mot_de_passe_p ALIAS FOR $3;	
	no_utilisateur_p integer;
BEGIN 
	INSERT INTO projet.utilisateurs(mail,pseudo,mot_de_passe,reputation,statut,compte_active) VALUES(mail_p,pseudo_p,mot_de_passe_p,0,'N',true) RETURNING no_utilisateur INTO no_utilisateur_p;
	RETURN no_utilisateur_p;
END;
$$ LANGUAGE plpgsql;

--DESACTIVER UTILISATEUR (ADMIN)
CREATE OR REPLACE FUNCTION projet.desactiver_utilisateur(varchar(100)) 
RETURNS integer AS $$ 
DECLARE 
	pseudo_p ALIAS FOR $1;																
	no_utilisateur_p integer;	
BEGIN 		
	IF NOT EXISTS(SELECT * FROM projet.utilisateurs util WHERE util.pseudo = pseudo_p)
		THEN RAISE 'cet utilisateur n existe pas';
	END IF; 
	UPDATE projet.utilisateurs SET compte_active = false WHERE pseudo = pseudo_p RETURNING no_utilisateur INTO no_utilisateur_p;
	RETURN no_utilisateur_p;																
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION projet.compte_desactive() RETURNS TRIGGER AS $$ 
BEGIN	
	IF(OLD.compte_active = false)
		THEN RAISE 'le compte de cet utilisateur est déjà desactivé ';
	END IF;		
	RETURN NEW;
END;
$$ LANGUAGE plpgsql;

--MONTER LE STATUT (ADMIN) 
CREATE OR REPLACE FUNCTION projet.modifier_statut_plus_eleve(projet.STATUS,varchar(100))
RETURNS integer AS $$
DECLARE 
	statut_p ALIAS FOR $1;
	pseudo_p ALIAS FOR $2;
	statut_actuel_p projet.STATUS;
	no_utilisateur_p integer;
BEGIN 
	IF EXISTS(SELECT util.statut FROM projet.utilisateurs util WHERE util.pseudo = pseudo_p and util.compte_active= false)
		THEN RAISE 'le compte de cet utilisateur a ete desactivé';
	ELSE
		SELECT util.statut FROM projet.utilisateurs util WHERE util.pseudo = pseudo_p INTO statut_actuel_p;
	END IF;
	UPDATE projet.utilisateurs SET statut = statut_p WHERE pseudo = pseudo_p RETURNING no_utilisateur into no_utilisateur_p;
	RETURN no_utilisateur_p;																									  
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION  projet.statut_existant() RETURNS TRIGGER AS $$ 
BEGIN
	IF((NEW.statut='A' and OLD.statut= 'M' ) or (NEW.statut='N' and OLD.statut= 'M') or (NEW.statut='N' and OLD.statut= 'A'))
		THEN RAISE 'toute action de rétrogradation est interdite';
	END IF;
	RETURN NEW;
END;
$$ LANGUAGE plpgsql;	
							
--AJOUTER QUESTION
CREATE OR REPLACE FUNCTION projet.ajouter_question(varchar(100),varchar(200),integer )
RETURNS integer AS $$ 
DECLARE 
	titre_p ALIAS FOR $1;
	contenu_p ALIAS FOR $2;
	no_utilisateur_question_p ALIAS FOR $3;																								 
	no_question_p integer;
BEGIN
	INSERT INTO projet.questions(titre,contenu,date_creation,no_utilisateur,cloturee) VALUES(titre_p,contenu_p,CURRENT_TIMESTAMP,no_utilisateur_question_p,false) RETURNING no_question INTO no_question_p;
	RETURN no_question_p;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION  projet.ajout_question() RETURNS TRIGGER AS $$ 
BEGIN	
	IF NOT EXISTS(SELECT util.no_utilisateur FROM projet.utilisateurs util WHERE util.no_utilisateur=NEW.no_utilisateur AND util.compte_active = true)
		THEN RAISE 'le compte de cet utilisateur est desactive';
	END IF;
	RETURN NEW;
END;
$$ LANGUAGE plpgsql;

--CLOTURER UNE QUESTION 
CREATE OR REPLACE FUNCTION projet.cloturer_question(integer, integer)
RETURNS integer AS $$
DECLARE 
	no_utilisateur_p ALIAS FOR $1;																						 
	no_question_p ALIAS FOR $2;															 
	no_question_cloturee integer;																							  
BEGIN 																									  
	IF NOT EXISTS(SELECT * FROM projet.utilisateurs util WHERE util.no_utilisateur = no_utilisateur_p AND util.compte_active = true AND util.statut='M')
	THEN RAISE 'cet utilisateur n existe pas ou n a pas encore la permission pour cloturer une question';
	END IF;
	UPDATE projet.questions	SET cloturee = true WHERE no_question = no_question_p RETURNING no_question INTO no_question_cloturee;
	RETURN no_question_cloturee;
END;
$$ LANGUAGE plpgsql;
					 
--MODIFIER UNE QUESTION 
CREATE OR REPLACE FUNCTION projet.modifier_question(varchar(100),varchar(200),integer,integer)
RETURNS integer AS $$																				  
DECLARE 																									  
	titre_p ALIAS FOR $1;
	contenu_p ALIAS FOR $2;
	no_utilisateur_p ALIAS FOR $3;																	  
	no_question_p ALIAS FOR $4;			
	no_question_modifiee integer;																						 
BEGIN 																  
	IF (NOT EXISTS(SELECT * FROM projet.utilisateurs util WHERE util.no_utilisateur = no_utilisateur_p and util.compte_active=true and util.statut IN('A','M')) AND (no_utilisateur_p not IN(SELECT qtn.no_utilisateur FROM projet.questions qtn WHERE qtn.no_question = no_question_p)))
		THEN RAISE 'cet utilisateur n a aucun droit de modification sur ce type de question';
	END IF;
	UPDATE projet.questions SET titre = titre_p ,contenu = contenu_p , date_derniere_edition = CURRENT_TIMESTAMP , no_utilisateur_derniere_edition = no_utilisateur_p WHERE no_question = no_question_p RETURNING no_question INTO no_question_modifiee;
	RETURN no_question_modifiee;
END; 
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION  projet.modification_question() RETURNS TRIGGER AS $$ 
BEGIN	
	IF(NEW.titre = '') 
		THEN NEW.titre=OLD.titre;
	END IF;
	IF(NEW.contenu = '') 
		THEN NEW.contenu=OLD.contenu;
	END IF;
	RETURN NEW; 
END;
$$ LANGUAGE plpgsql;

--AJOUTER REPONSE
CREATE OR REPLACE FUNCTION projet.ajouter_reponse(varchar(200),integer,integer )
RETURNS integer AS $$ 
DECLARE 
	contenu_p ALIAS FOR $1;
	no_question_p ALIAS FOR $2;
	no_utilisateur_p ALIAS FOR $3;																								 
	no_reponse_ajoute_p integer;
	no_question_no_reponse_p integer;
BEGIN
	SELECT count(rps.no_reponse)+1 FROM projet.reponses rps	WHERE rps.no_question = no_question_p INTO no_reponse_ajoute_p;
	INSERT INTO projet.reponses(no_question,contenu,date_creation,no_utilisateur,score,no_reponse) VALUES(no_question_p,contenu_p,CURRENT_TIMESTAMP,no_utilisateur_p,0,no_reponse_ajoute_p) RETURNING no_question,no_reponse INTO no_question_no_reponse_p;
	RETURN no_question_no_reponse_p;
END;	
$$ LANGUAGE plpgsql;	

CREATE OR REPLACE FUNCTION  projet.ajout_reponse() RETURNS TRIGGER AS $$ 
BEGIN	
	IF NOT EXISTS(SELECT * FROM projet.utilisateurs WHERE no_utilisateur=NEW.no_utilisateur AND compte_active = true)
		THEN RAISE 'le compte de cet utilisateur est desactive';
	END IF;
	RETURN NEW;
END;
$$ LANGUAGE plpgsql;	
							
--MODIFIER CONTENU REPONSE 
CREATE OR REPLACE FUNCTION projet.modifier_reponse(varchar(200),integer,integer,integer )
RETURNS integer AS $$																				  
DECLARE 																									  
	contenu_p ALIAS FOR $1;																	  
	no_reponse_p ALIAS FOR $2;			
	no_question_p ALIAS FOR $3;
	no_utilisateur_p ALIAS FOR $4;
	no_reponse_modifiee integer;	
	no_question_no_reponse_p integer;																					 
BEGIN 																  																							  
	IF NOT EXISTS(SELECT * FROM projet.utilisateurs WHERE no_utilisateur=no_utilisateur_p AND compte_active = true)
		THEN RAISE 'le compte de cet utilisateur est desactive'; END IF;
	IF (NOT EXISTS(SELECT * FROM projet.utilisateurs util WHERE util.no_utilisateur = no_utilisateur_p and util.compte_active= true and util.statut IN('A','M')) AND (no_utilisateur_p not IN(SELECT rps.no_utilisateur FROM projet.reponses rps WHERE rps.no_reponse = no_reponse_p)))
		THEN RAISE 'cet utilisateur n a aucun droit de modification sur ce type de reponse' ;
	END IF;
	UPDATE projet.reponses SET contenu = contenu_p WHERE no_question = no_question_p and no_reponse = no_reponse_p RETURNING no_question,no_reponse INTO no_question_no_reponse_p;
	RETURN no_question_no_reponse_p;
END; 
$$ LANGUAGE plpgsql;	

CREATE OR REPLACE FUNCTION projet.pouvoir_modifier_reponse()
RETURNS TRIGGER AS $$ 
BEGIN 
	IF (NOT EXISTS(SELECT * FROM projet.utilisateurs util WHERE util.no_utilisateur = NEW.no_utilisateur and util.statut IN('A','M')) AND (NEW.no_utilisateur not IN(SELECT rps.no_utilisateur FROM projet.reponses rps WHERE rps.no_reponse = NEW.no_reponse)))
		THEN RAISE 'cet utilisateur n a aucun droit de modification sur ce type de reponse';
	END IF;
	RETURN NEW;
END;
$$ LANGUAGE plpgsql;

--INSERER VOTE
CREATE OR REPLACE FUNCTION projet.inserer_vote(INTEGER, BOOLEAN, INTEGER, INTEGER) 
RETURNS INTEGER AS $$
DECLARE 
	no_utilisateur_p ALIAS FOR $1;
	positivite_p ALIAS FOR $2;
	no_question_p ALIAS FOR $3;
	no_reponse_p ALIAS FOR $4;
	id_vote INTEGER;
BEGIN
	INSERT INTO projet.votes(no_utilisateur, positivite, no_question, no_reponse) VALUES (no_utilisateur_p, positivite_p, no_question_p, no_reponse_p) RETURNING no_utilisateur,no_question,no_reponse  INTO id_vote;
	RETURN id_vote ;
END;
$$ LANGUAGE plpgsql;

--VOTER 
CREATE OR REPLACE FUNCTION projet.pouvoir_voter() 
RETURNS TRIGGER AS $$
DECLARE	
	utilisateur_rec RECORD; 
BEGIN	
	IF NOT EXISTS(SELECT util.no_utilisateur FROM projet.utilisateurs util WHERE util.no_utilisateur=NEW.no_utilisateur AND compte_active = true)
		THEN RAISE 'le compte de cet utilisateur est desactive';
	END IF;
	IF (NEW.no_utilisateur in(SELECT rps.no_utilisateur FROM projet.reponses rps WHERE rps.no_reponse = NEW.no_reponse and rps.no_question =NEW.no_question))
		THEN RAISE 'Vous ne pouvez pas voter pour votre propre réponse';
		END IF;							  
	SELECT util.statut,util.derniere_date_privilege	FROM projet.utilisateurs util WHERE util.no_utilisateur = NEW.no_utilisateur INTO utilisateur_rec;
	IF (utilisateur_rec.statut = 'N')
		THEN RAISE 'Votre statut ne vous permet pas de voter.';
	END IF;							  
	IF(utilisateur_rec.statut = 'A' AND NEW.positivite = FALSE )
		THEN RAISE 'ton statut ne permet de voter uniquement positivement';
	END IF;							  
	IF (utilisateur_rec.statut = 'A' and EXTRACT(EPOCH FROM current_timestamp-utilisateur_rec.derniere_date_privilege)/3600 < 24)
		THEN RAISE 'Vous n avez pas encore la permission de voter à nouveau.';
	END IF;	
	RETURN NEW;
END;
$$ LANGUAGE plpgsql;

--UPDATE STATUT 
CREATE OR REPLACE FUNCTION projet.update_statut()
RETURNS TRIGGER AS $$
DECLARE	
	utilisateur_rec RECORD;
	reputation_util_rec integer;
	maj_statut projet.STATUS;
BEGIN	
	SELECT util.no_utilisateur, util.statut, util.reputation
	FROM projet.utilisateurs util
	WHERE util.no_utilisateur IN(SELECT rps.no_utilisateur FROM projet.reponses rps WHERE rps.no_reponse= NEW.no_reponse and rps.no_question = NEW.no_question )
	INTO utilisateur_rec;
	maj_statut = utilisateur_rec.statut; --? ':=' plutôt ???
	IF (NEW.positivite = TRUE)
		THEN reputation_util_rec = utilisateur_rec.reputation + 5;
		IF (utilisateur_rec.statut = 'A' AND reputation_util_rec >= 10)
			THEN maj_statut = 'M';
		END IF;
		IF (utilisateur_rec.statut = 'N' AND reputation_util_rec >= 5)
			THEN maj_statut = 'A';
		END IF;			  
		UPDATE projet.utilisateurs SET reputation = reputation_util_rec, statut = maj_statut WHERE no_utilisateur= utilisateur_rec.no_utilisateur;
	END IF;							  
	UPDATE projet.utilisateurs SET derniere_date_privilege = CURRENT_TIMESTAMP WHERE no_utilisateur = NEW.no_utilisateur;
	RETURN NEW;
END;
$$ LANGUAGE plpgsql;
						
--UPDATE SCORE REPONSE 
CREATE OR REPLACE FUNCTION projet.update_score()
RETURNS TRIGGER AS $$
DECLARE 
	score_p integer:=1;
BEGIN	
	IF (NEW.positivite = FALSE)
		THEN score_p= -1;
	END IF;
	UPDATE projet.reponses SET score = score + score_p WHERE no_reponse= NEW.no_reponse and no_question = NEW.no_question;
	RETURN NEW;
END;
$$ LANGUAGE plpgsql;

--INSERER TAG EXISTANT
CREATE OR REPLACE FUNCTION projet.inserer_tag_dans_existant(VARCHAR(10))
RETURNS INTEGER AS $$
DECLARE
	libelle_p ALIAS FOR $1;
	no_tag_existant_t INTEGER;
BEGIN
	INSERT INTO projet.tags_existant (libelle) VALUES (libelle_p) RETURNING no_tag_existant INTO no_tag_existant_t;
	RETURN no_tag_existant_t;
END
$$ LANGUAGE plpgsql;

--INSERER TAG
CREATE OR REPLACE FUNCTION projet.inserer_tag(integer, VARCHAR(10),integer)
RETURNS INTEGER AS $$
DECLARE
	no_question_p ALIAS FOR $1;
	libelle_p ALIAS FOR $2;
	no_utilisateur_p ALIAS FOR $3;
	no_tag_existant_t INTEGER;
	no_tag_p INTEGER;	
BEGIN	
	IF NOT EXISTS(SELECT util.no_utilisateur FROM projet.utilisateurs util WHERE util.no_utilisateur= no_utilisateur_p AND util.compte_active = true)
		THEN RAISE 'le compte de cet utilisateur est desactive';
	END IF;
	IF NOT EXISTS(SELECT te.no_tag_existant FROM projet.tags_existant te WHERE te.libelle LIKE lower(libelle_p))
		THEN RAISE 'Ce tag n existe pas.';
	ELSE
		SELECT te.no_tag_existant FROM projet.tags_existant te WHERE te.libelle LIKE  lower(libelle_p) INTO no_tag_existant_t;
		INSERT INTO projet.tags (no_question, no_tag_existant) VALUES (no_question_p, no_tag_existant_t) RETURNING no_question,no_tag_existant INTO no_tag_p;
		RETURN no_tag_p;
	END IF;
END;
$$ LANGUAGE plpgsql;

--VERIFICATION (MAX 5)
CREATE OR REPLACE FUNCTION projet.max_tags()
RETURNS TRIGGER AS $$
DECLARE		
	nb_tags INTEGER;
BEGIN
	SELECT COUNT(*)	FROM projet.tags tgs WHERE tgs.no_question = NEW.no_question INTO nb_tags;
	IF (nb_tags > 2)
		THEN RAISE 'Il ne peut y avoir plus de 5 tags par question.' ;
	END IF;
	RETURN NEW;
END;
$$ LANGUAGE plpgsql;

--+------------+
--|            |
--|  TRIGGERS  |
--|            |
--+------------+

CREATE TRIGGER trigger_statut_existant AFTER UPDATE ON projet.utilisateurs FOR EACH ROW EXECUTE PROCEDURE projet.statut_existant();
CREATE TRIGGER trigger_ajout_question AFTER INSERT ON projet.questions FOR EACH ROW EXECUTE PROCEDURE projet.ajout_question();
CREATE TRIGGER trigger_modification_question BEFORE UPDATE ON projet.questions FOR EACH ROW EXECUTE PROCEDURE projet.modification_question();
CREATE TRIGGER trigger_ajout_reponse AFTER INSERT ON projet.reponses FOR EACH ROW EXECUTE PROCEDURE projet.ajout_reponse();
CREATE TRIGGER trigger_pouvoir_modifier_reponse AFTER UPDATE ON projet.reponses FOR EACH ROW EXECUTE PROCEDURE projet.pouvoir_modifier_reponse();
CREATE TRIGGER trigger_pouvoir_voter BEFORE INSERT ON projet.votes FOR EACH ROW EXECUTE PROCEDURE projet.pouvoir_voter();
CREATE TRIGGER trigger_update_statut AFTER INSERT ON projet.votes FOR EACH ROW EXECUTE PROCEDURE projet.update_statut();
CREATE TRIGGER trigger_update_score AFTER INSERT ON projet.votes FOR EACH ROW EXECUTE PROCEDURE projet.update_score();
CREATE TRIGGER trigger_compte_desactive AFTER UPDATE ON projet.utilisateurs FOR EACH ROW EXECUTE PROCEDURE projet.compte_desactive();
CREATE TRIGGER trigger_max_tags AFTER INSERT ON projet.tags FOR EACH ROW EXECUTE PROCEDURE projet.max_tags();

--+------------+
--|            |
--|   VIEWS    |
--|            |
--+------------+

CREATE OR REPLACE VIEW projet.voir_toutes_les_questions AS
	SELECT * FROM
	((SELECT qtn.date_creation AS "DATE CREATION", qtn.no_question AS "N°" , util.pseudo AS "PSEUDO UTILISATEUR" , 
	qtn.date_derniere_edition AS "DATE DERNIERE EDITION" , util2.pseudo AS "DERNIERE EDITION PAR" , qtn.titre AS "TITRE QUESTION" , qtn.contenu AS "CONTENU QUESTION"	
	FROM projet.questions qtn, projet.utilisateurs util, projet.utilisateurs util2
	WHERE  util.no_utilisateur = qtn.no_utilisateur and qtn.cloturee = false  AND EXISTS (SELECT util3.no_utilisateur FROM projet.utilisateurs util3 WHERE util3.no_utilisateur = qtn.no_utilisateur_derniere_edition) AND util2.no_utilisateur = qtn.no_utilisateur_derniere_edition)
	UNION
	(SELECT qtn.date_creation AS "DATE CREATION", qtn.no_question AS "N°" , util.pseudo AS "PSEUDO UTILISATEUR" , 
	NULL AS "DATE DERNIERE EDITION" , NULL AS "DERNIERE EDITION PAR" , qtn.titre AS "TITRE QUESTION" , qtn.contenu AS "CONTENU QUESTION"
	FROM projet.questions qtn, projet.utilisateurs util, projet.utilisateurs util2
	WHERE  util.no_utilisateur = qtn.no_utilisateur and qtn.cloturee= false AND NOT EXISTS (SELECT util3.no_utilisateur FROM projet.utilisateurs util3 WHERE util3.no_utilisateur = qtn.no_utilisateur_derniere_edition)))
	AS "ALL QUESTIONS"
	ORDER BY "DATE CREATION" DESC;
	

CREATE OR REPLACE VIEW projet.voir_toutes_les_reponses AS
	SELECT R.no_question AS "N° QUESTION", R.no_reponse AS "N°", R.date_creation AS "DATE CREATION", U.pseudo AS "REPONSE DE ", R.score AS "SCORE", R.contenu AS "CONTENU"
	FROM projet.reponses R, projet.utilisateurs U
	WHERE R.no_utilisateur = U.no_utilisateur AND R.no_question IN (SELECT qtn.no_question FROM projet.questions qtn WHERE qtn.cloturee = FALSE)
	ORDER BY "SCORE" DESC, "DATE CREATION" DESC;

--+------------+
--|            |
--|   GRANTS   |
--|            |
--+------------+

GRANT CONNECT ON DATABASE databasename TO username;
GRANT USAGE ON SCHEMA projet TO username;

GRANT UPDATE ON SEQUENCE projet.utilisateurs_no_utilisateur_seq TO username;
GRANT SELECT ON SEQUENCE projet.utilisateurs_no_utilisateur_seq TO username;
GRANT UPDATE ON SEQUENCE projet.questions_no_question_seq TO username;
GRANT SELECT ON SEQUENCE projet.questions_no_question_seq TO username;

GRANT SELECT ON TABLE projet.voir_toutes_les_questions TO username;
GRANT SELECT ON TABLE projet.voir_toutes_les_reponses TO username;

GRANT SELECT ON TABLE projet.utilisateurs, projet.questions, projet.reponses, projet.tags, projet.votes, projet.tags_existant TO username;
GRANT INSERT ON TABLE projet.utilisateurs, projet.questions, projet.reponses, projet.tags, projet.votes TO username;
GRANT UPDATE ON TABLE projet.utilisateurs, projet.questions, projet.reponses TO username;