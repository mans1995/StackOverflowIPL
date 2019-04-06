package requetes;

public interface StringRequete {
	
	public final static String stringRecevoirMotDePasseHashe = "SELECT * FROM projet.recuperer_mdp(?);";
	public final static String stringActionRecevoirMotDePasseHashe = "recevoir le mot de passe hashé pout un utilisateur";
	
	public final static String stringDemanderPseudo = "Quel est votre pseudo ?";
	public final static String stringDemanderMotDePasse = "Quel est votre mot de passe ?";	
	
	public final static String stringConnexionClient = "SELECT * FROM projet.connexion(?);";
	public final static String stringActionConnexionClient = "connexion du client";
	
	public final static String stringAjouterUtilisateur = "SELECT * FROM projet.inscription_utilisateur(?, ?, ?);";
	public final static String stringDesactiverUtilisateur = "SELECT * FROM projet.desactiver_utilisateur(?);";
	public final static String stringPromouvoirStatutUtilisateur = "SELECT * FROM projet.modifier_statut_plus_eleve(CAST(? AS projet.STATUS), ?);";
	public final static String stringAjouterQuestion = "SELECT * FROM projet.ajouter_question(?, ?, CAST(? AS INTEGER));";
	public final static String stringInsererTagQuestion = "SELECT * FROM projet.inserer_tag(CAST(? AS INTEGER), ?, CAST(? AS INTEGER));";
	public final static String stringInsererTagListe = "SELECT * FROM projet.inserer_tag_dans_existant(?);";
	public final static String stringCloturerQuestion = "SELECT * FROM projet.cloturer_question(CAST(? AS INTEGER), CAST(? AS INTEGER));";
	public final static String stringModifierQuestion = "SELECT * FROM projet.modifier_question(?, ?, CAST(? AS INTEGER), CAST(? AS INTEGER));";
	public final static String stringAjouterReponse = "SELECT * FROM projet.ajouter_reponse(?, CAST(? AS INTEGER), CAST(? AS INTEGER));";
	public final static String stringModifierReponse = "SELECT * FROM projet.modifier_reponse(?, CAST(? AS INTEGER), CAST(? AS INTEGER), CAST(? AS INTEGER));";
	public final static String stringVoter = "SELECT * FROM projet.inserer_vote(CAST(? AS INTEGER), CAST(? AS BOOLEAN), CAST(? AS INTEGER), CAST(? AS INTEGER));";
	public final static String stringQuestions = "SELECT * FROM projet.voir_toutes_les_questions;";	
	
	// Vues
	public final static String stringReponses = "SELECT * FROM projet.voir_toutes_les_reponses WHERE \"N° QUESTION\" = CAST(? AS INTEGER);";
	public final static String stringTitreQuestion = "SELECT * FROM projet.voir_toutes_les_questions WHERE \"N°\" = CAST(? AS INTEGER);";
	public final static String stringQuestionsPosees = "SELECT * FROM projet.voir_toutes_les_questions WHERE \"PSEUDO UTILISATEUR\" IN (SELECT util.pseudo FROM projet.utilisateurs util WHERE util.no_utilisateur = CAST(? AS INTEGER));";
	public final static String stringQuestionsRepondues = "SELECT * FROM projet.voir_toutes_les_questions WHERE \"N°\" IN (SELECT rps.no_question FROM projet.reponses rps WHERE rps.no_utilisateur = CAST(? AS INTEGER));";
	public final static String stringQuestionsTag = "SELECT * FROM projet.voir_toutes_les_questions WHERE \"N°\" IN (SELECT tgs.no_question FROM projet.tags tgs WHERE tgs.no_tag_existant IN (SELECT tgsxstt.no_tag_existant FROM projet.tags_existant tgsxstt WHERE tgsxstt.libelle LIKE lower(?)));";
	public final static String stringQuestionsDatesUtilisateur = "SELECT * FROM projet.voir_toutes_les_questions WHERE \"PSEUDO UTILISATEUR\" = ? AND \"DATE CREATION\" BETWEEN CAST(? AS TIMESTAMP) AND CAST(? AS TIMESTAMP);";
	public final static String stringReponsesDatesUtilisateur = "SELECT * FROM projet.voir_toutes_les_reponses WHERE \"REPONSE DE \" = ? AND \"DATE CREATION\" BETWEEN CAST(? AS TIMESTAMP) AND CAST(? AS TIMESTAMP);";
	
}
