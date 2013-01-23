package pasta.domain;

public class PASTACompUserResult implements Comparable{

	private String username;
	private Double percentage;
	
	public PASTACompUserResult(String username, double percentage){
		this.username = username;
		this.percentage = percentage;
	}
	
	public String getUsername() {
		return username;
	}

	public Double getPercentage() {
		return percentage;
	}

	@Override
	public int compareTo(Object o) {
		return ((PASTACompUserResult)o).getPercentage().compareTo(percentage); 
	}

}
