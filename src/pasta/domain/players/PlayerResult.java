package pasta.domain.players;

import java.util.Date;

public class PlayerResult {

	String name;
	Date firstUploaded;
	
	double officialRating;
	int officialRanking;
	
	int officialWin;
	int officialDraw;
	int officialLoss;
	
	int unofficialWin;
	int unofficialDraw;
	int unofficialLoss;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Date getFirstUploaded() {
		return firstUploaded;
	}
	public void setFirstUploaded(Date firstUploaded) {
		this.firstUploaded = firstUploaded;
	}
	public double getOfficialRating() {
		return officialRating;
	}
	public void setOfficialRating(double officialRating) {
		this.officialRating = officialRating;
	}
	public int getOfficialRanking() {
		return officialRanking;
	}
	public void setOfficialRanking(int officialRanking) {
		this.officialRanking = officialRanking;
	}
	public int getOfficialWin() {
		return officialWin;
	}
	public void setOfficialWin(int officialWin) {
		this.officialWin = officialWin;
	}
	public int getOfficialDraw() {
		return officialDraw;
	}
	public void setOfficialDraw(int officialDraw) {
		this.officialDraw = officialDraw;
	}
	public int getOfficialLoss() {
		return officialLoss;
	}
	public void setOfficialLoss(int officialLoss) {
		this.officialLoss = officialLoss;
	}
	public int getUnofficialWin() {
		return unofficialWin;
	}
	public void setUnofficialWin(int unofficialWin) {
		this.unofficialWin = unofficialWin;
	}
	public int getUnofficialDraw() {
		return unofficialDraw;
	}
	public void setUnofficialDraw(int unofficialDraw) {
		this.unofficialDraw = unofficialDraw;
	}
	public int getUnofficialLoss() {
		return unofficialLoss;
	}
	public void setUnofficialLoss(int unofficialLoss) {
		this.unofficialLoss = unofficialLoss;
	}
	
	
}
