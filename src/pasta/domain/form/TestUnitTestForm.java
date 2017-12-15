package pasta.domain.form;

import javax.validation.constraints.Min;

import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import pasta.domain.template.UnitTest;

/**
 * Form object to test a unit test assessment module.
 * 
 * @author Joshua Stretton
 * @version 1.0
 * @since 2015-04-02
 *
 */
public class TestUnitTestForm {
	
	@Min(0)
	private Long id;
	
	@Length(max=64)
	private String solutionName;

	private CommonsMultipartFile file;
	
	public TestUnitTestForm(UnitTest base) {
		this.id = base.getId();
		this.solutionName = "";
		this.file = null;
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getSolutionName() {
		return solutionName;
	}
	public void setSolutionName(String solutionName) {
		this.solutionName = solutionName;
	}

	public CommonsMultipartFile getFile() {
		return file;
	}
	public void setFile(CommonsMultipartFile file) {
		this.file = file;
	}
}
