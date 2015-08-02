package pasta.domain.release;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import pasta.domain.result.AssessmentResult;
import pasta.domain.user.PASTAUser;

/**
 * <p>
 * A rule that is met if the user has submitted to the given assessment, and
 * their mark compares to the given mark in the desired way.
 * 
 * <p>
 * For example, if a {@link MarkCompareRule} is created with {@link MarkType}
 * {@code .AUTO_MARK}, {@link CompareMode}{@code .GREATER_THAN_OR_EQUAL},
 * {@code asPercentage = true} and {@code compareMark = 0.5}, then all students
 * with an automatic mark of 50% or greater (ignoring hand marks) in the given
 * assessment will be able to see this assessment.
 * 
 * @author Joshua Stretton
 * @version 1.0
 * @since 20 Apr 2015
 */
@Entity
@Table (name = "rules_mark_compare")
public class MarkCompareRule extends ReleaseResultsRule implements Serializable {
	private static final long serialVersionUID = 5182097220832448841L;

	@Column(name = "compare_mark")
	private double compareMark;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "compare_mode")
	private CompareMode compareMode;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "mark_type")
	private MarkType markType;
	
	@Column(name = "as_percentage")
	private boolean asPercentage;
	
	public MarkCompareRule() {
	}
	
	public MarkCompareRule(double compareMark, CompareMode compareMode, MarkType markType, boolean asPercentage) {
		this.asPercentage = asPercentage;
		this.compareMark = compareMark;
		this.compareMode = compareMode;
		this.markType = markType;
	}

	@Override
	protected boolean isMet(PASTAUser user, AssessmentResult latestCompareResult) {
		if(latestCompareResult == null) {
			return false;
		}
		
		double mark;
		switch(markType) {
		case AUTO_MARK:
			mark = asPercentage ? latestCompareResult.getAutoMarkPercentage() : latestCompareResult.getAutoMarks();
			break;
		case MANUAL_MARK:
			mark = asPercentage ? latestCompareResult.getHandMarkPercentage() : latestCompareResult.getHandMarks();
			break;
		case OVERALL:
			mark = asPercentage ? latestCompareResult.getPercentage() : latestCompareResult.getMarks();
			break;
		default:
			return false;
		}
		
		switch(compareMode) {
		case EQUAL:
			return mark == compareMark;
		case GREATER_THAN:
			return mark > compareMark;
		case GREATER_THAN_OR_EQUAL:
			return mark >= compareMark;
		case LESS_THAN:
			return mark < compareMark;
		case LESS_THAN_OR_EQUAL:
			return mark <= compareMark;
		default:
			return false;
		}
	}

	public double getCompareMark() {
		return compareMark;
	}
	public void setCompareMark(double compareMark) {
		this.compareMark = compareMark;
	}

	public CompareMode getCompareMode() {
		return compareMode;
	}
	public void setCompareMode(CompareMode compareMode) {
		this.compareMode = compareMode;
	}

	public MarkType getMarkType() {
		return markType;
	}
	public void setMarkType(MarkType markType) {
		this.markType = markType;
	}

	public boolean isAsPercentage() {
		return asPercentage;
	}
	public void setAsPercentage(boolean asPercentage) {
		this.asPercentage = asPercentage;
	}

	@Override
	public String getShortDescription() {
		return "Release based on assessment mark";
	}
	@Override
	public String getDescription() {
		return "This assessment will be released according "
				+ "to their mark for a given assessment.";
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName()).append(": ");
		sb.append("Release if ").append(markType).append(" mark for assessment ")
			.append(getCompareAssessment() == null ? "null" : getCompareAssessment().getName())
			.append(" is ").append(compareMode).append(' ').append(asPercentage ? compareMark*100 : compareMark).append(asPercentage ? "%" : "");
		return sb.toString();
	}
}
