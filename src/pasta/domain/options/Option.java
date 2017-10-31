package pasta.domain.options;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = "options")
public class Option {

	@Id
	@Column(name="option_key", length = 255)
	@Length(max = 255)
	private String key;
	
	@Column (length = 64000)
	@Size (max = 64000)
	private String value;
	
	public Option() {
		
	}

	public Option(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
