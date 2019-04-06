package requetes;

public interface StringRequete {

	// Tables
	public final static String stringDesactiverUtilisateur = "SELECT * FROM projet.desactiver_utilisateur(?);";
	public final static String stringPromouvoirStatutUtilisateur = "SELECT * FROM projet.modifier_statut_plus_eleve(CAST(? AS projet.STATUS), ?);";
	public final static String stringInsererTagListe = "SELECT * FROM projet.inserer_tag_dans_existant(?);";

	// Vues
	public final static String stringTitreQuestion = "SELECT * FROM projet.voir_toutes_les_questions WHERE \"N°\" = CAST(? AS INTEGER);";	
	public final static String stringQuestionsDatesUtilisateur = "SELECT * FROM projet.voir_toutes_les_questions WHERE \"PSEUDO UTILISATEUR\" = ? AND \"DATE CREATION\" BETWEEN CAST(? AS TIMESTAMP) AND CAST(? AS TIMESTAMP);";
	public final static String stringReponsesDatesUtilisateur = "SELECT * FROM projet.voir_toutes_les_reponses WHERE \"REPONSE DE \" = ? AND \"DATE CREATION\" BETWEEN CAST(? AS TIMESTAMP) AND CAST(? AS TIMESTAMP);";
	
}