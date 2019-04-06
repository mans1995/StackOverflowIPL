package body;

import commandes.Commande;
import commandes.LigneCommande;
import requetes.LecteurRequete;

public class Application {

  private Utilisateur admin;
  private LecteurRequete lr;
  private LigneCommande lc;
  private Connector connector;

  public Application() {

    System.out.println("Bienvenue sur :");
    System.out.println(soIPL);

    String userName = "username1";
    String password = "password1";
    this.connector = new Connector();
    this.connector.vérifierDriver();
    this.connector.connecter(userName, password);

    this.lr = new LecteurRequete();
    this.admin = new Utilisateur();

    this.lc = new LigneCommande(this.connector.getConnection(), this.lr, this.admin);

    new Commande("cmds", "liste les commandes pour le contexte en cours et les décrit");
    new Commande("quitter", "cette commande vous permet de quitter l'application");
    new Commande("desactiver", "désactive un utilisateur. Cette opération est définitive");
    new Commande("promouvoir", "promeut le statut d'un utilisateur");
    new Commande("tag", "permet d'ajouter un tag à la liste des tags");
    new Commande("historique",
        "permet de voir toutes les questions et réponses d'un utilisateur entre deux dates");

  }

  public void run() {
    System.out.println(
        "\nVeuillez utiliser [cmds] pour obtenir la liste des commandes pour le contexte en cours et leurs descriptions.");
    while (this.admin.isBrowsing()) {
      String contenu = this.lr.lis("> ", false);
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
