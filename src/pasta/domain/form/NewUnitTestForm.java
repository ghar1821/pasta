package pasta.domain.form;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * Form object for a new unit test assessment module.
 * 
 * @author Joshua Stretton
 * @version 3.0
 * @since 2012-11-15
 *
 */
public class NewUnitTestForm {
	@NotEmpty
	@Length(max=256)
	private String name;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
