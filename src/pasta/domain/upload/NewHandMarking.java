package pasta.domain.upload;

public class NewHandMarking {
	private String name;

	public String getName() {
		return name;
	}
	
	public String getShortName(){
		return name.replace(" ", "");
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
