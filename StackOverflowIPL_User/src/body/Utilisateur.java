package body;

public class Utilisateur {
	
	public final static int BROWSING_PAGE = 0;
	public final static int BROWSING_QUESTION = 1;
	public final static int MAX_NB_BROWSING_STATES = 2;
	
	private int noUtilisateur;
	private int browsingAction;
	private boolean browsing;
	private int currentNoQuestion;
	
	public Utilisateur(int noUtilisateur) {
		this.noUtilisateur = noUtilisateur;
		this.browsingAction = BROWSING_PAGE;
		this.browsing = true;
		this.currentNoQuestion = -1;
	}	
	
	public int getNoUtilisateur() {
		return this.noUtilisateur;
	}
	
	public void setCurrentNoQuestion(int no) {
		this.currentNoQuestion = no;
	}
	
	public int getCurrentNoQuestion() {
		return this.currentNoQuestion;
	}
	
	public boolean isBrowsing() {
		return this.browsing;
	}
	
	public void stopBrowsing() {
		this.browsing = false;
	}
	
	public boolean isStateIn(Integer[] states) {
		boolean in = false;
		for (Integer i : states) {
			if (i == this.browsingAction) in = true;
		}
		return in;
	}
	
	public boolean isBrowsingPage() {
		return this.browsingAction == BROWSING_PAGE;
	}
	
	public boolean isBrowsingQuestion() {
		return this.browsingAction == BROWSING_QUESTION;
	}
	
	public void browsePage() {
		this.browsingAction = BROWSING_PAGE;
	}
	
	public void browseQuestion() {
		this.browsingAction = BROWSING_QUESTION;
	}
	
	public int getBrowsingAction() {
		return this.browsingAction;
	}

}
