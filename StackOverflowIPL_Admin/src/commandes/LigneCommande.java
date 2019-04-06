package commandes;

import java.sql.Connection;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import body.Utilisateur;
import requetes.LecteurRequete;
import requetes.StringRequete;
import commandes.Commande;

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
		
		String pseudo, statut, tag_existant;
		String action = Commande.getDescFromCommandeName(commande);
		boolean done;
		try {
			
			switch (commande) {
			
			case "cmds":		System.out.println("Description des commandes administrateur :\n\n"+Commande.getCommandes().stream().map(a -> a.getNom() + " : " + a.getDesc()).collect(Collectors.joining("\n")));
								System.out.println("");
								break;
			
			case "quitter":		System.out.println("\nMerci d'avoir utilisé StackOverflowIPL.");
								user.stopBrowsing();
								break;
								
			case "desactiver":	System.out.println("Vous allez désactiver un utilisateur.");
								pseudo = lr.lis("Pseudonyme : ", false);
								done = -1 != lr.executeProcedureReturningInteger(conn, StringRequete.stringDesactiverUtilisateur, action, pseudo);
								if (done) System.out.println("Le compte de cet utilisateur a bien été désactivé !");
								break;
										
			case "promouvoir":	System.out.println("Vous allez promouvoir le statut d'un utilisateur.");
								pseudo = lr.lis("Pseudonyme : ", false);
								statut = lr.lis("Nouveau statut ( [n]ormal, [a]vancé ou [m]aster ): ", false).substring(0, 1).toUpperCase();
								done = -1 != lr.executeProcedureReturningInteger(conn, StringRequete.stringPromouvoirStatutUtilisateur, action, statut, pseudo);
								if (done) System.out.println("Cet utilisateur a bien été promu au rang : "+Utilisateur.getStatusFullName(statut)+" !");
								break;				
								
			case "tag":			System.out.println("Vous allez ajouter un tag parmi ceux existant");
								tag_existant = lr.lis("Nom du tag : ", false);
								done = -1 != lr.executeProcedureReturningInteger(conn, StringRequete.stringInsererTagListe, action, tag_existant);
								if (done) System.out.println("Le tag a bien été ajouté à la liste.");
								break;
			
			case "historique":	pseudo = lr.lis("Pseudo de l'utilisateur concerné : ", false);
								String regexDate = "^[0-9]{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])$";
								Pattern pattern = Pattern.compile(regexDate);
								String dateInit = lr.lis("Date initiale (yyyy-mm-dd) : ", false);
								while (!pattern.matcher(dateInit).matches()) {
									System.out.println("Date inexistante.");
									dateInit = lr.lis("Date initiale (yyyy-mm-dd) : ", false);
								}
								String dateFin = lr.lis("Date finale (yyyy-mm-dd) : ", false);
								while (!pattern.matcher(dateFin).matches()) {
									System.out.println("Date inexistante.");
									dateFin = lr.lis("Date finale (yyyy-mm-dddd) : ", false);
								}
								System.out.println(lr.getViewAllQuestions(conn, StringRequete.stringQuestionsDatesUtilisateur, action, pseudo, dateInit, dateFin));
								System.out.println(lr.getViewAllAnswers(conn, user, StringRequete.stringReponsesDatesUtilisateur, action, pseudo, dateInit, dateFin));
								break;
				
			default :			System.out.println("Cette commande n'a pas été reconnue.");
								break;
			
			}				

		} catch (IllegalArgumentException e) {
			System.out.println("Commande invalide.");
			System.out.println(e.getMessage());
			//e.printStackTrace();
		}
		
	}
}