package commandes;

import java.sql.Connection;
import java.util.stream.Collectors;
import body.Utilisateur;
import requetes.LecteurRequete;
import requetes.StringRequete;

public class LigneCommande {
	
	private Connection conn;
	private LecteurRequete lr;
	private Utilisateur user;
	
	public LigneCommande(Connection conn, LecteurRequete lr, Utilisateur user) {
		this.conn = conn;
		this.lr = lr;
		this.user = user;
	}
	
	public void trouveCommande(String commande) {
		
		String titreQuestion, contenuQuestion, vote, contenuReponse, tag; 
		int no_question, no_reponse;
		String action = Commande.getDescFromCommandeName(commande);
		boolean done;
		try {
			
			switch (commande) {
			
			case "cmds":		if (user.isBrowsingPage() || user.isBrowsingQuestion()) {
									// affiche toutes les commandes qui peuvent être utilisés dans l'état actuel de CurrentWindow
									System.out.println("Description des commandes pour le contexte en cours :\n\n"+Commande.getCommandes().stream().filter(a -> a.myStatesContain(user.getBrowsingAction())).map(a -> a.getNom() + " : " + a.getDesc()).collect(Collectors.joining("\n")));
									System.out.println("");									
								}
								break;
			
			case "quitter":		if (user.isBrowsingPage()) {
									System.out.println("\nMerci d'avoir utilisé StackOverflowIPL.");
									user.stopBrowsing();
								}
								else if (user.isBrowsingQuestion()) {
									System.out.println("Fermeture de la question.");
									user.browsePage();
									user.setCurrentNoQuestion(-1);
								}
								break;

			case "ajouterQ":	if (user.isBrowsingPage()) {
									System.out.println("Vous allez ajouter une question");
									titreQuestion = lr.lis("Titre de la question : ", false);
									contenuQuestion = lr.lis("Contenu de la question : ", false);
									no_question = lr.executeProcedureReturningInteger(conn, StringRequete.stringAjouterQuestion, action, titreQuestion, contenuQuestion, Integer.toString(user.getNoUtilisateur()));
									if (no_question != -1) System.out.println("La question a bien été ajoutée.");																			
								}
								break;
								
			case "ajouterT":	if (user.isBrowsingPage()) {
									System.out.println("Vous allez ajouter un/des tag(s) à une question");
									no_question = Integer.parseInt(lr.lis("Numéro de la question : ", false));
									tag = lr.lis("Insérer un tag (espace pour arrêter) : ", false);									
									while (!"".equals(tag)) {										
										done = -1 != lr.executeProcedureReturningInteger(conn, StringRequete.stringInsererTagQuestion, action, Integer.toString(no_question), tag, Integer.toString(user.getNoUtilisateur()));
										if (done) System.out.println("Tag ajouté.");
										tag = lr.lis("Insérer un tag (espace pour arrêter) : ", false);
									}
								}
								else if (user.isBrowsingQuestion()) {
									System.out.println("Vous allez ajouter un/des tag(s) à cette question");
									tag = lr.lis("Insérer un tag (espace pour arrêter) : ", false);									
									while (!"".equals(tag)) {										
										done = -1 != lr.executeProcedureReturningInteger(conn, StringRequete.stringInsererTagQuestion, action, Integer.toString(user.getCurrentNoQuestion()), tag, Integer.toString(user.getNoUtilisateur()));
										if (done) System.out.println("Tag ajouté.");
										tag = lr.lis("Insérer un tag (espace pour arrêter) : ", false);
									}
								}
								break;
								
			case "cloturerQ":	if (user.isBrowsingQuestion()) {	
									System.out.println(user.getNoUtilisateur());
									done = -1 != lr.executeProcedureReturningInteger(conn, StringRequete.stringCloturerQuestion, action, Integer.toString(user.getNoUtilisateur()), Integer.toString(user.getCurrentNoQuestion()));
									if (done) {
										System.out.println("La question a bien été clôturée.");									
										user.browsePage(); // on sort de la liste des réponses vu que la question a été clôturée
									}
								}
								break;
								
			case "modifierQ":	if (user.isBrowsingPage()) {
									System.out.println("Vous allez modifier le titre et/ou contenu d'une question");
									no_question = Integer.parseInt(lr.lis("Numéro de la question : ", false));
									titreQuestion = lr.lis("Nouveau titre : ", false);
									contenuQuestion = lr.lis("Nouveau contenu : ", false);								
									done = -1 != lr.executeProcedureReturningInteger(conn, StringRequete.stringModifierQuestion, action, titreQuestion, contenuQuestion, Integer.toString(user.getNoUtilisateur()), Integer.toString(no_question));
									if (done) System.out.println("La question a bien été modifiée.");
								}
								else if (user.isBrowsingQuestion()) {
									System.out.println("Vous allez modifier le titre et/ou contenu d'une question");
									titreQuestion = lr.lis("Nouveau titre : ", false);
									contenuQuestion = lr.lis("Nouveau contenu : ", false);								
									done = -1 != lr.executeProcedureReturningInteger(conn, StringRequete.stringModifierQuestion, action, titreQuestion, contenuQuestion, Integer.toString(user.getNoUtilisateur()), Integer.toString(user.getCurrentNoQuestion()));
									if (done) System.out.println("La question a bien été modifiée.");
									System.out.println(lr.getViewAllAnswers(conn, user, StringRequete.stringReponses, action, Integer.toString(user.getCurrentNoQuestion())));
								}
								break;
								
			case "ajouterR":	if (user.isBrowsingQuestion()) {
									System.out.println("Vous allez répondre à une question.");
									contenuReponse = lr.lis("Réponse : ", false);
									done = -1 != lr.executeProcedureReturningInteger(conn, StringRequete.stringAjouterReponse, action, contenuReponse, Integer.toString(user.getCurrentNoQuestion()), Integer.toString(user.getNoUtilisateur()));
									if (done) System.out.println("La réponse a bien été soumise"); 
									System.out.println(lr.getViewAllAnswers(conn, user, StringRequete.stringReponses, action, Integer.toString(user.getCurrentNoQuestion())));
								}
								break;
								
			case "modifierR":	if (user.isBrowsingQuestion()) {
									System.out.println("Vous allez modifier une réponse.");
									no_reponse = Integer.parseInt(lr.lis("Numéro de la réponse : ", false));
									contenuReponse = lr.lis("Nouvelle réponse : ", false);
									done = -1 != lr.executeProcedureReturningInteger(conn, StringRequete.stringModifierReponse, action, contenuReponse, Integer.toString(no_reponse), Integer.toString(user.getCurrentNoQuestion()), Integer.toString(user.getNoUtilisateur()));
									if (done) System.out.println("La réponse a bien été modifiée");
									System.out.println(lr.getViewAllAnswers(conn, user, StringRequete.stringReponses, action, Integer.toString(user.getCurrentNoQuestion())));
								}
								break;
								
			case "voter":		if (user.isBrowsingQuestion()) {
									System.out.println("Vous allez voter pour une réponse.");
									no_reponse = Integer.parseInt(lr.lis("Numéro de la réponse : ", false));
									vote = lr.lis("Vote ( [p]ositif ou [n]égatif ): ", false).substring(0, 1).toUpperCase();
									while (!"P".equals(vote) && !"N".equals(vote)) {
										System.out.println("Réponse invalide.");
										vote = lr.lis("Vote ( [p]ositif ou [n]égatif ): ", false).substring(0, 1).toUpperCase();
									}
									vote = "P".equals(vote) ? "TRUE" : "FALSE";
									done = -1 != lr.executeProcedureReturningInteger(conn, StringRequete.stringVoter, action, Integer.toString(user.getNoUtilisateur()), vote, Integer.toString(user.getCurrentNoQuestion()), Integer.toString(no_reponse));
									if (done) System.out.println("Vote enregistré.");
									System.out.println(lr.getViewAllAnswers(conn, user, StringRequete.stringReponses, action, Integer.toString(user.getCurrentNoQuestion())));
								}
								break;
								
			case "questions":	if (user.isBrowsingPage()) {
									System.out.println(lr.getViewAllQuestions(conn, StringRequete.stringQuestions, action));
								}
								break;
								
			case "reponses":	if (user.isBrowsingPage()) {
									System.out.println("Vous allez accéder à une question en particulier.");
									no_question = Integer.parseInt(lr.lis("Numéro de la question : ", false));
									user.setCurrentNoQuestion(no_question);
									System.out.println(lr.getViewAllAnswers(conn, user, StringRequete.stringReponses, action, Integer.toString(no_question)));
									user.browseQuestion();
								}
								break;
								
			case "questionsR":	if (user.isBrowsingPage()) {
									System.out.println(lr.getViewAllQuestions(conn, StringRequete.stringQuestionsRepondues, action, Integer.toString(user.getNoUtilisateur())));
								}
								break;
			
			case "questionsT":	if (user.isBrowsingPage()) {				
									tag = lr.lis("Tag recherché : ", false);
									System.out.println(lr.getViewAllQuestions(conn, StringRequete.stringQuestionsTag, action, tag));
								}
								break;
			
			case "questionsP":	if (user.isBrowsingPage()) {
									System.out.println(lr.getViewAllQuestions(conn, StringRequete.stringQuestionsPosees, action, Integer.toString(user.getNoUtilisateur())));
								}
								break;
								
			default :			System.out.println("Cette commande n'a pas été reconnue.");
								break;
			
			}				

		} catch (IllegalArgumentException e) {
			System.out.println("Commande invalide.");
			System.out.println(e.getMessage()); // A RETIRER !!!?
			//e.printStackTrace();
		}
		
	}
}