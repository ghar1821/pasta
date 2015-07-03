package pasta.domain.template;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;


/**
 * @author Joshua Stretton
 * @version 1.0
 * @since 19 Jun 2015
 */
@Entity
@Table(name="black_box_tests")
public class BlackBoxTest extends UnitTest {
	private static final long serialVersionUID = -4974380327385340788L;

	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn (name="bb_test_id")
	@LazyCollection(LazyCollectionOption.FALSE)
	private List<BlackBoxTestCase> testCases;
	
	public BlackBoxTest() {
		init();
	}

	public BlackBoxTest(String name, boolean tested) {
		super(name, tested);
		init();
	}
	
	public void init() {
		testCases = new ArrayList<BlackBoxTestCase>();
	}

	public List<BlackBoxTestCase> getTestCases() {
		return testCases;
	}
	public void setTestCases(List<BlackBoxTestCase> testCases) {
		if(this.testCases == null || testCases == null) {
			this.testCases = testCases;
		} else {
			this.testCases.clear();
			this.testCases.addAll(testCases);
		}
	}
}
