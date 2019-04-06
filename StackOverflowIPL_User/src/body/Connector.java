package body;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import requetes.LecteurRequete;
import requetes.StringRequete;

public class Connector implements StringRequete {

  private String userName, password;
  private Connection connection;

  public Connector() {

  }

  public void vérifierDriver() {
    try {
      Class.forName("org.postgresql.Driver");
    } catch (ClassNotFoundException e) {
      System.out.println("Driver PostgreSQL manquant !");
      System.exit(1);
    }
  }

  public void connecter(String userName, String password) {
    this.userName = userName;
    this.password = password;
    String url = "jdbc:postgresql://ip.ip.ip.ip:5432/databasename";
    try {
      this.connection = DriverManager.getConnection(url, this.userName, this.password);
    } catch (SQLException e) {
      System.out.println("Impossible de joindre le serveur !");
      System.out.println(e.getMessage());
      System.exit(1);
    }
  }



  public Connection getConnection() {
    return this.connection;
  }

  public Utilisateur logSignIn(LecteurRequete lr) {
    // connexion ou inscription
    String tempAns = lr.lis("Voulez-vous vous [c]onnecter ou vous [i]nscrire ?", true);
    String inscOUconn = tempAns.length() > 0 ? tempAns.substring(0, 1).toUpperCase() : "";
    while ("".equals(inscOUconn) || (!"I".equals(inscOUconn) && !"C".equals(inscOUconn))) {
      System.out.println("Réponse non comprise.");
      tempAns = lr.lis("Voulez-vous vous [c]onnecter ou vous [i]nscrire ?", true);
      inscOUconn = tempAns.length() > 0 ? tempAns.substring(0, 1).toUpperCase() : "";
    }
    int idUtilisateur;
    if ("C".equals(inscOUconn)) {
      idUtilisateur = this.connecterUtilisateur(this.getConnection(), lr);
    } else {
      idUtilisateur = this.inscrireUtilisateur(this.getConnection(), lr);
    }
    return new Utilisateur(idUtilisateur);
  }

  private int connecterUtilisateur(Connection conn, LecteurRequete lr) {
    int id = -1;
    String pseudo, motDePasse;
    while (id == -1) {
      pseudo = lr.lis(stringDemanderPseudo, true);
      motDePasse = lr.lis(stringDemanderMotDePasse, true);
      String motDePasseDB = lr.getHashedPassword(conn, pseudo);
      boolean motDePasseOK = false;
      try {
        motDePasseOK = BCrypt.checkpw(motDePasse, motDePasseDB);
      } catch (NullPointerException e) {

      } catch (IllegalArgumentException e) {
        System.out
            .println("Erreur : cet utilisateur es probablement inscrit directement dans la DB.");
      }
      if (motDePasseOK) {
        id = lr.executeProcedureReturningInteger(conn, StringRequete.stringConnexionClient,
            stringActionConnexionClient, pseudo);
        if (id != -1)
          System.out.println("Authentification réussie !");
      } else {
        System.out.println("Échec de l'authentification.\n");
      }
    }
    return id;
  }

  private int inscrireUtilisateur(Connection conn, LecteurRequete lr) {
    int id = -1;
    String pseudo, motDePasse, mail;
    mail = lr.lis("Adresse mail : ", false);
    pseudo = lr.lis("Pseudonyme : ", false);
    motDePasse = lr.lis("Mot de passe : ", false);
    id = lr.executeProcedureReturningInteger(conn, StringRequete.stringAjouterUtilisateur,
        "inscription d'un utilisateur", mail, pseudo, BCrypt.hashpw(motDePasse, BCrypt.gensalt()));
    while (id == -1) {
      mail = lr.lis("Adresse mail : ", false);
      pseudo = lr.lis("Pseudonyme : ", false);
      motDePasse = lr.lis("Mot de passe : ", false);
      id = lr.executeProcedureReturningInteger(conn, StringRequete.stringAjouterUtilisateur,
          "inscritpion d'un utilisateur", mail, pseudo,
          BCrypt.hashpw(motDePasse, BCrypt.gensalt()));
    }
    return id;
  }

}
