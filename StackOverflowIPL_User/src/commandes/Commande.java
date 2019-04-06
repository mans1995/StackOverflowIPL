package commandes;

import java.util.ArrayList;
import java.util.List;

public class Commande {	
	
	private static List<Commande> commandes = new ArrayList<>();	
	private static int lastNo = -1;
	
	private int no;	
	private String nom, desc;
	private List<Integer> states;
	
	public Commande(String nom, String desc, int...status) {
		this.no = ++lastNo;
		this.nom = nom;
		this.desc = desc;		
		this.states = new ArrayList<Integer>();
		for (int s : status) {
			this.states.add(s);
		}
		commandes.add(this);
	}
	
	public String getNom() {
		return this.nom;
	}
	
	public String getDesc() {
		return this.desc;
	}
	
	public int getNo() {
		return this.no;
	}
	
	public List<Integer> getStates() {
		return this.states;
	}
	
	public boolean myStatesContain(int s) {
		return this.states.contains(s);
	}
	
	public static List<Commande> getCommandes() {
		return commandes;
	}
	
	public static String getDescFromCommandeName(String name) {
		for (Commande c : commandes) {
			if (name.equals(c.nom)) {
				return c.nom;
			}
		}
		return "Commande non trouvée";
	}

}
