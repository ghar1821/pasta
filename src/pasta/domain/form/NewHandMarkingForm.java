package pasta.domain.form;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * Form object for a new hand marking assessment module.
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2012-12-13
 *
 */
public class NewHandMarkingForm {
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
