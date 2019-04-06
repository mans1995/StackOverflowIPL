package body;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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


}
