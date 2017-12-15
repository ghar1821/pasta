package pasta.domain.template;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import pasta.domain.BaseEntity;
import pasta.domain.VerboseName;
import pasta.domain.result.UnitTestResult;

/**
 * Container class for a weighted unit test.
 * <p>
 * This class contains a link to a unit test assessment module to an assessment
 * and the weight of the unit test assessment module in the assessment.
 * 
 * Using this, you can have the same unit test assessment module as part of multiple
 * assessments with different weights.
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2012-11-14
 *
 */

@Entity
@Table (name = "weighted_unit_tests")
@VerboseName("weighted unit test module")
public class WeightedUnitTest extends BaseEntity implements Comparable<WeightedUnitTest> {
	
	private static final long serialVersionUID = 2594905907808283182L;

	private double weight;
	
	private boolean secret;
	
	@Column(name= "group_work")
	private boolean groupWork;
	
	@ManyToOne
	@JoinColumn(name = "unit_test_id")
	private UnitTest test;
	
	@ManyToOne
	@JoinColumn(name = "assessment_id")
	private Assessment assessment;
	
	public WeightedUnitTest() {
		setWeight(0);
		setSecret(false);
		setGroupWork(false);
	}
	
	public double getWeight() {
		return weight;
	}
	public void setWeight(double weight) {
		this.weight = weight;
	}
	
	public boolean isSecret() {
		return secret;
	}
	public void setSecret(boolean secret) {
		this.secret = secret;
	}
	
	public boolean isGroupWork() {
		return groupWork;
	}
	public void setGroupWork(boolean groupWork) {
		this.groupWork = groupWork;
	}
	
	public UnitTest getTest() {
		return test;
	}
	public void setTest(UnitTest test) {
		this.test = test;
	}
	
	public Assessment getAssessment() {
		return assessment;
	}
	public void setAssessment(Assessment assessment) {
		this.assessment = assessment;
	}
	
	@Override
	public int compareTo(WeightedUnitTest other) {
		if(other == null) {
			return 1;
		}
		if(other.groupWork && !this.groupWork) {
			return 1;
		}
		if(this.groupWork && !other.groupWork) {
			return -1;
		}
		if(other.secret && !this.secret) {
			return 1;
		}
		if(this.secret && !other.secret) {
			return -1;
		}
		int diff;
		if(this.getTest() != null) {
			diff = this.getTest().compareTo(other.getTest());
			if(diff != 0) {
				return diff;
			}
		}
		diff = (this.weight < other.weight ? -1 : (this.weight > other.weight ? 1 : 0));
		if(diff != 0) {
			return diff;
		}
		if(this.getId() == null) {
			return other.getId() == null ? 0 : -1;
		}
		if(other.getId() == null) {
			return 1;
		}
		return (this.getId() < other.getId() ? -1 : (this.getId() > other.getId() ? 1 : 0));
	}
	
	@Override
	public String toString() {
		return  "{ID:" + this.getId() + " for " + (this.test == null ? "null" : this.test.getId()) + (secret ? " (secret)" : "") + "}";
	}

	/*===========================
	 * CONVENIENCE RELATIONSHIPS
	 * 
	 * Making unidirectional many-to-one relationships into bidirectional 
	 * one-to-many relationships for ease of deletion by Hibernate
	 *===========================
	 */
	@OneToMany(mappedBy = "weightedUnitTest", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
	private List<UnitTestResult> unitTestResults;
}
