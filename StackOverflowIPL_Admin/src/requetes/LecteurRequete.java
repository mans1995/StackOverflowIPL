package requetes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import body.Utilisateur;;

public class LecteurRequete implements StringRequete {
	
	private Scanner scanner;
	
	public LecteurRequete () {
		this.scanner = new Scanner(System.in);
	}
		
	public String lis(String msg, boolean passageLigne) {
		if (passageLigne) System.out.println(msg);
		else System.out.print(msg);
		return this.scanner.nextLine();
	}

	public String getViewAllAnswers(Connection conn, Utilisateur user, String query, String action, String...args) {
		try (PreparedStatement ps = conn.prepareStatement(query)) {			
			for (int i = 0; i < args.length; i++) {
				ps.setString(i+1, args[i]);
			}
			String fullText = "";
			try (ResultSet rs = ps.executeQuery()) {			
				
				
				Date date = null;
				int noReponse = -1, score = -1;
				String pseudo = "", contenu = "";
				
				ResultSetMetaData rsmd = rs.getMetaData();
				String colNoReponse = rsmd.getColumnName(2);
				String colDate = rsmd.getColumnName(3);
				String colPseudo = rsmd.getColumnName(4);
				String colScore = rsmd.getColumnName(5);
				String colContenu = rsmd.getColumnName(6);
				
				int borderSize = 171;						
				int tailleMaxContenu = 96;
				int tailleMaxPseudo = 25;
				
				System.out.println("");

				fullText += "+"+IntStream.range(0, borderSize).mapToObj(a -> "-").collect(Collectors.joining(""))+"+\n";
				fullText += String.format("| %-10s | %-16s | %-"+Integer.toString(tailleMaxPseudo)+"s | %-10s | %-"+Integer.toString(tailleMaxContenu)+"s |\n", colNoReponse, colDate, colPseudo, colScore, colContenu);
				fullText += "+"+IntStream.range(0, borderSize).mapToObj(a -> "-").collect(Collectors.joining(""))+"+\n";
							
				while (rs.next()) {
					
					// Réinitialisation sinon les varibles gardent les valeurs précédentes
					date = null;
					noReponse = -1; score = -1;
					pseudo = ""; contenu = "";
					
					noReponse = rs.getInt(2);				
					Timestamp timestamp = rs.getTimestamp(3);
					if (timestamp != null)date = new Date(timestamp.getTime());
					else System.out.println("La date n'a pas pu être récupérée.");
					pseudo = rs.getString(4);
					score = rs.getInt(5);
					contenu = rs.getString(6);			
					
					int nbLignesPseudo = 1;
					int nbCharPseudo = pseudo.length();
					while (nbCharPseudo > 0) {
						nbCharPseudo -= tailleMaxPseudo;
						if (nbCharPseudo > 0) nbLignesPseudo++;
					}
					
					int nbLignesContenu = 1;
					int nbCharContenu = contenu.length();
					while (nbCharContenu > 0) {
						nbCharContenu -= tailleMaxContenu;
						if (nbCharContenu > 0) nbLignesContenu++;
					}				
					
					int nbTotLignes = Math.max(nbLignesPseudo, nbLignesContenu);
					
					String formatPseudo = "%-"+Integer.toString(tailleMaxPseudo)+"s";
					String formatContenu = "%-"+Integer.toString(tailleMaxContenu)+"s";
					
					for (int i = 0; i < nbTotLignes; i++) {					
						String contentPseudo = (nbLignesPseudo >= i+1) ? pseudo.substring(i * tailleMaxPseudo, Math.min(pseudo.length(), (i+1) * tailleMaxPseudo)) : "";
						String contentContenu = (nbLignesContenu >= i+1) ? contenu.substring(i * tailleMaxContenu, Math.min(contenu.length(), (i+1) * tailleMaxContenu)) : "";					
						if (i+1 == 1) fullText += String.format("| %-10d | %-10tF %-5tR | "+formatPseudo+" | %-10d | "+formatContenu+" |\n", noReponse, date, date, contentPseudo, score, contentContenu);
						else fullText += String.format("| %-10s | %-16s | %-"+Integer.toString(tailleMaxPseudo)+"s | %-10s | %-"+Integer.toString(tailleMaxContenu)+"s |\n", "", "", contentPseudo, "", contentContenu);					
					}
					fullText += "+"+IntStream.range(0, borderSize).mapToObj(a -> "-").collect(Collectors.joining(""))+"+\n";
				}				
			}			
			return fullText;
			
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			//e.printStackTrace();
			return "Échec lors de l'obtention de cette ligne\n"; 
		}
	}
	
	public String getViewAllQuestions(Connection conn, String query, String action, String...args) {
		try (PreparedStatement ps = conn.prepareStatement(query)) {
			for (int i = 0; i < args.length; i++) {
				ps.setString(i+1, args[i]); // +1 car sql
			}
			String fullText = "";
			try (ResultSet rs = ps.executeQuery()) {				
				Date date = null, dateDernEdition = null;
				int num = -1;
				String pseudo = "", pseudoDernEdition = null, titreQuestion = "", contenuQuestion = "";				
				
				int borderSize = 171;
				int tailleMaxTitre = 25;
				int tailleMaxPseudo = 25;
				int tailleMaxContenu = 25;
				
				ResultSetMetaData rsmd = rs.getMetaData();
				String colDate = rsmd.getColumnName(1);
				String colNum = rsmd.getColumnName(2);
				String colPseudo = rsmd.getColumnName(3);
				String colDateDernEdition = rsmd.getColumnName(4);
				String colPseudoDernEdition = rsmd.getColumnName(5);
				String colTitreQuestion = rsmd.getColumnName(6);
				String colContenuQuestion = rsmd.getColumnName(7);
				
				fullText += "+"+IntStream.range(0, borderSize).mapToObj(a -> "-").collect(Collectors.joining(""))+"+\n";
				fullText += String.format("| %-16s | %-10s | %-"+Integer.toString(tailleMaxPseudo)+"s | %-25s | %-"+Integer.toString(tailleMaxPseudo)+"s | %-"+Integer.toString(tailleMaxTitre)+"s | %-"+Integer.toString(tailleMaxContenu)+"s |\n", colDate, colNum, colPseudo, colDateDernEdition, colPseudoDernEdition, colTitreQuestion, colContenuQuestion);
				fullText += "+"+IntStream.range(0, borderSize).mapToObj(a -> "-").collect(Collectors.joining(""))+"+\n";
							
				while (rs.next()) {
					
					// Réinitialisation sinon les varibles gardent les valeurs précédentes
					date = null; dateDernEdition = null;
					num = -1;
					pseudo = ""; pseudoDernEdition = null; titreQuestion = "";
					
					Timestamp timestamp = rs.getTimestamp(1);
					if (timestamp != null)date = new Date(timestamp.getTime());
					else System.out.println("La date n'a pas pu être récupérée.");
					num = rs.getInt(2);
					pseudo = rs.getString(3);
					Timestamp timestampEdit = rs.getTimestamp(4);
					if (timestampEdit != null) {
						dateDernEdition = new Date(timestampEdit.getTime());
						pseudoDernEdition = rs.getString(5);
					}
					titreQuestion = rs.getString(6);
					contenuQuestion = rs.getString(7);
					
					int nbLignesPseudo = 1;
					int nbCharPseudo = pseudo.length();
					while (nbCharPseudo > 0) {
						nbCharPseudo -= tailleMaxPseudo;
						if (nbCharPseudo > 0) nbLignesPseudo++;
					}
					
					int nbLignesPseudoDernEdition = 1;
					if (pseudoDernEdition != null) {
						int nbCharPseudoDernEdition = pseudoDernEdition.length();
						while (nbCharPseudoDernEdition > 0) {
							nbCharPseudoDernEdition -= tailleMaxPseudo;
							if (nbCharPseudoDernEdition > 0) nbLignesPseudoDernEdition++;
						}
					}
					
					int nbLignesTitreQuestion = 1;
					int nbCharTitreQuestion = titreQuestion.length();
					while (nbCharTitreQuestion > 0) {
						nbCharTitreQuestion -= tailleMaxTitre;
						if (nbCharTitreQuestion > 0) nbLignesTitreQuestion++;
					}
					
					int nbLignesContenuQuestion = 1;
					int nbCharContenuQuestion = contenuQuestion.length();
					while (nbCharContenuQuestion > 0) {
						nbCharContenuQuestion -= tailleMaxContenu;
						if (nbCharContenuQuestion > 0) nbLignesContenuQuestion++;
					}
					
					int nbTotLignes = Math.max(Math.max(nbLignesTitreQuestion, nbLignesContenuQuestion), Math.max(nbLignesPseudo, nbLignesPseudoDernEdition));

					String formatPseudo = "%-"+Integer.toString(tailleMaxPseudo)+"s";
					String formatPseudoDernEdition = "%-"+Integer.toString(tailleMaxPseudo)+"s";
					String formatTitreQuestion = "%-"+Integer.toString(tailleMaxTitre)+"s";
					String formatContenuQuestion = "%-"+Integer.toString(tailleMaxContenu)+"s";
					
					for (int i = 0; i < nbTotLignes; i++) {					
						String contentPseudo = (nbLignesPseudo >= i+1) ? pseudo.substring(i * tailleMaxPseudo, Math.min(pseudo.length(), (i+1) * tailleMaxPseudo)) : "";
						String contentTitreQuestion = (nbLignesTitreQuestion >= i+1) ? titreQuestion.substring(i * tailleMaxTitre, Math.min(titreQuestion.length(), (i+1) * tailleMaxTitre)) : "";
						String contentContenuQuestion = (nbLignesContenuQuestion >= i+1) ? contenuQuestion.substring(i * tailleMaxContenu , Math.min(contenuQuestion.length(), (i+1) * tailleMaxContenu)) : "";
						if (dateDernEdition != null) {
							String contentPseudoDernEdition = (nbLignesPseudoDernEdition >= i+1) ? pseudoDernEdition.substring(i * tailleMaxPseudo, Math.min(pseudoDernEdition.length(), (i+1) * tailleMaxPseudo)) : "";
							if (i+1 == 1) fullText += String.format("| %-10tF %-5tR | %-10d | "+formatPseudo+" | %-10tF %-14tR | "+formatPseudoDernEdition+" | "+formatTitreQuestion+" | "+formatContenuQuestion+" |\n", date, date, num, contentPseudo, dateDernEdition, dateDernEdition, contentPseudoDernEdition, contentTitreQuestion, contentContenuQuestion);
							else fullText += String.format("| %-16s | %-10s | %-"+Integer.toString(tailleMaxPseudo)+"s | %-25s | "+formatPseudoDernEdition+" | "+formatTitreQuestion+" | "+formatContenuQuestion+" |\n", "", "", contentPseudo, "", contentPseudoDernEdition, contentTitreQuestion, contentContenuQuestion);
						}
						else {
							if (i+1 == 1) fullText += String.format("| %-10tF %-5tR | %-10d | "+formatPseudo+" | %-25s | "+formatPseudoDernEdition+" | "+formatTitreQuestion+" | "+formatContenuQuestion+" |\n", date, date, num, contentPseudo, "", "", contentTitreQuestion, contentContenuQuestion); // un seul format pour deateDernEdition ici
							else fullText += String.format("| %-16s | %-10s | %-"+Integer.toString(tailleMaxPseudo)+"s | %-25s | "+formatPseudoDernEdition+" | "+formatTitreQuestion+" | "+formatContenuQuestion+" |\n", "", "", contentPseudo, "", "", contentTitreQuestion, contentContenuQuestion);
						}
					}
					fullText += "+"+IntStream.range(0, borderSize).mapToObj(a -> "-").collect(Collectors.joining(""))+"+\n";
				}
				
			}
			return fullText;			
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			//e.printStackTrace();
			return "Échec lors de l'obtention de cette ligne\n"; 
		}
	}
	
	public int executeProcedureReturningInteger(Connection conn, String query, String action, String...args){
		int returnInt = -1;
		try (PreparedStatement ps = conn.prepareStatement(query)) {
			for (int i = 0; i < args.length; i++) {
				ps.setString(i+1, args[i]);
			}
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					returnInt = rs.getInt(1);
				}
			}		
		} catch (SQLException e) {
			System.out.println("Erreur lors de : " + action);
			System.out.println(e.getMessage());
			//e.printStackTrace();
			returnInt = -1;
		}
		return returnInt;
	}
}