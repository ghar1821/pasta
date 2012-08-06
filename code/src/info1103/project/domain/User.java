package info1103.project.domain;

import java.util.Map;

/**
 * Contianer class to hold the user data.
 * @author Alex
 *
 */
public class User implements Comparable<User>{
	String unikey;
	boolean tutor = false;
	
	// history
	Map<String, Double> markHistory;

	public String getUnikey() {
		return unikey;
	}

	public void setUnikey(String unikey) {
		this.unikey = unikey;
	}

	public boolean isTutor() {
		return tutor;
	}

	public void setTutor(boolean tutor) {
		this.tutor = tutor;
	}

	public Map<String, Double> getMarkHistory() {
		return markHistory;
	}

	public void setMarkHistory(Map<String, Double> markHistory) {
		this.markHistory = markHistory;
	}
	
	public String toString(){
		return unikey+","+tutor;
	}
	
	 public int compareTo( User otherUser ) {
		return unikey.compareTo(otherUser.getUnikey());
	}
}
