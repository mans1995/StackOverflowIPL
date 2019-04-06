package body;

import commandes.Commande;
import commandes.LigneCommande;
import requetes.LecteurRequete;

public class Application {

  private Utilisateur user;
  private LecteurRequete lr;
  private LigneCommande lc;
  private Connector connector;

  public Application() {

    System.out.println("Bienvenue sur :");
    System.out.println(soIPL);

    String userName = "username2";
    String password = "password2";
    this.connector = new Connector();
    this.connector.vérifierDriver();
    this.connector.connecter(userName, password);

    this.lr = new LecteurRequete();
    this.user = connector.logSignIn(lr);

    this.lc = new LigneCommande(this.connector.getConnection(), this.lr, this.user);

    new Commande("cmds", "liste les commandes pour le contexte en cours et les décrit",
        Utilisateur.BROWSING_PAGE, Utilisateur.BROWSING_QUESTION);
    new Commande("quitter", "cette commande vous permet de quitter l'application",
        Utilisateur.BROWSING_PAGE, Utilisateur.BROWSING_QUESTION);
    new Commande("ajouterQ", "ajoute une question pour un utilisateur", Utilisateur.BROWSING_PAGE);
    new Commande("ajouterT", "ajoute un tag à une question", Utilisateur.BROWSING_PAGE,
        Utilisateur.BROWSING_QUESTION);
    new Commande("cloturerQ", "clôture une question. Cette opération est définitive",
        Utilisateur.BROWSING_QUESTION);
    new Commande("modifierQ", "modifie une question", Utilisateur.BROWSING_PAGE,
        Utilisateur.BROWSING_QUESTION);
    new Commande("ajouterR", "ajoute une reponse à une question", Utilisateur.BROWSING_QUESTION);
    new Commande("modifierR", "modifie une réponse", Utilisateur.BROWSING_QUESTION);
    new Commande("voter", "voter pour une réponse", Utilisateur.BROWSING_QUESTION);
    new Commande("questions", "permet de voir la liste des questions", Utilisateur.BROWSING_PAGE);
    new Commande("reponses", "permet de voir la liste des réponses associées à une question",
        Utilisateur.BROWSING_PAGE);
    new Commande("questionsR", "permet de voir toutes les questions auxquelles vous avez répondu",
        Utilisateur.BROWSING_PAGE);
    new Commande("questionsT", "permet de voir toutes les questions relatives à un tag",
        Utilisateur.BROWSING_PAGE);
    new Commande("questionsP", "permet de voir toutes les questions que vous avez posées",
        Utilisateur.BROWSING_PAGE);

  }

  public void run() {
    System.out.println(
        "\nVeuillez utiliser [cmds] pour obtenir la liste des commandes pour le contexte en cours et leurs descriptions.");
    while (this.user.isBrowsing()) {
      String context = "";
      if (this.user.isBrowsingQuestion())
        context += "Question ";
      String contenu = this.lr.lis(context + "> ", false);
      this.lc.trouveCommande(contenu);
    }
  }

  private String soIPL =
      " ____  _              _     ___                  __ _                 ___ ____  _\n"
          + "/ ___|| |_  __ _  ___| | __/ _ \\__   _____ _ __ / _| | _____      __ |_ _|  _ \\| |\n"
          + "\\___ \\| __|/ _` |/ __| |/ / | | \\ \\ / / _ \\ '__| |_| |/ _ \\ \\ /\\ / /  | || |_) | | \n"
          + " ___) | |_| (_| | (__|   <| |_| |\\ V /  __/ |  |  _| | (_) \\ V  V /   | ||  __/| |___\n"
          + "|____/ \\__|\\__,_|\\___|_|\\_\\\\___/  \\_/ \\___|_|  |_| |_|\\___/ \\_/\\_/   |___|_|   |_____|\n";

}
