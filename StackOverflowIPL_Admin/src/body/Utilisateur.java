package body;

public class Utilisateur {
	
	public static enum StatutsChar {N, A, M};
	public static final String STATUS[] = {"Normal", "Avancé", "Master"};
	
	private boolean browsing;
	
	public Utilisateur() {
		this.browsing = true;
	}
	
	public static String getStatusFullName(String statut) {
		return Utilisateur.STATUS[Utilisateur.StatutsChar.valueOf(statut).ordinal()];
	}
	
	public boolean isBrowsing() {
		return this.browsing;
	}
	
	public void stopBrowsing() {
		this.browsing = false;
	}
	

}