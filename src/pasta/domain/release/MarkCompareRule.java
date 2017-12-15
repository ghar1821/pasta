/*
MIT License

Copyright (c) 2012-2017 PASTA Contributors (see CONTRIBUTORS.txt)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

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
 * {@code asPercentage = true} and {@code compareMark = 50}, then all students
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
			mark = asPercentage ? latestCompareResult.getAutoMarkPercentage() * 100.0 : latestCompareResult.getAutoMarks();
			break;
		case MANUAL_MARK:
			mark = asPercentage ? latestCompareResult.getHandMarkPercentage() * 100.0 : latestCompareResult.getHandMarks();
			break;
		case OVERALL:
			mark = asPercentage ? latestCompareResult.getPercentage() * 100.0 : latestCompareResult.getMarks();
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
			.append(" is ").append(compareMode).append(' ').append(compareMark).append(asPercentage ? "%" : "");
		return sb.toString();
	}
}
