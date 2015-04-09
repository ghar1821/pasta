package pasta.domain.result;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table (name = "competition_result_data")
public class CompetitionResultData implements Serializable, Comparable<CompetitionResultData> {

	private static final long serialVersionUID = 3839231480263371596L;

	@Id
	@GeneratedValue
	private long id;
	
	private String username;
	private String category;
	private String value;
	
	public CompetitionResultData() { }
	
	public CompetitionResultData(String username, String category, String value) {
		this.username = username;
		this.category = category;
		this.value = value;
	}

	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((category == null) ? 0 : category.hashCode());
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CompetitionResultData other = (CompetitionResultData) obj;
		if (category == null) {
			if (other.category != null)
				return false;
		} else if (!category.equals(other.category))
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	
	@Override
	public int compareTo(CompetitionResultData o) {
		int diff = this.getUsername().compareTo(o.getUsername());
		if(diff != 0) {
			return diff;
		}
		diff = this.getCategory().compareTo(o.getCategory());
		if(diff != 0) {
			return diff;
		}
		try {
			double value1 = new Double(this.getValue());
			double value2 = new Double(o.getValue());
			return value1 < value2 ? -1 : value1 > value2 ? 1 : 0;
		} catch (NumberFormatException e) {
			return this.getValue().compareTo(o.getValue());
		}
	}
}

