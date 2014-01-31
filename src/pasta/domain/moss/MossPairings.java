package pasta.domain.moss;

public class MossPairings {
	private String student1;
	private String student2;
	private int percentage1;
	private int percentage2;
	private int lines;
	
	public MossPairings(String student1, String student2,
			int percentage1, int percentage2, int lines){
		this.student1 = student1;
		this.student2 = student2;
		this.percentage1 = percentage1;
		this.percentage2 = percentage2;
		this.lines = lines;
	}
	
	public String getStudent1(){
		return student1;
	}
	
	public String getStudent2(){
		return student2;
	}
	
	public int getPercentage1(){
		return percentage1;
	}
	
	public int getPercentage2(){
		return percentage2;
	}
	
	public int getLines(){
		return lines;
	}
	
	public int getMaxPercentage(){
		return Math.max(percentage1, percentage2);
	}
}
