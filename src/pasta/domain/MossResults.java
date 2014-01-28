package pasta.domain;

import java.util.LinkedList;

public class MossResults {
	private LinkedList<MossPairings>  pairings = new LinkedList<MossPairings>();
	private String link;
	private String date;
	
	public String getLink(){
		return link;
	}
	
	public void setLink(String link){
		this.link = link;
	}
	
	public String getDate(){
		return date;
	}
	
	public void setDate(String date){
		this.date = date;
	}

	public LinkedList<MossPairings> getPairings(){
		return pairings;
	}
	
	public void addPairing(String student1, String student2,
			int percentage1, int percentage2, int lines){
		pairings.add(new MossPairings(student1, student2, percentage1, percentage2, lines));
	}
}
