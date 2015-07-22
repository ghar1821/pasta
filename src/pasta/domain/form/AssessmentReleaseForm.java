package pasta.domain.form;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import pasta.domain.release.ClassRule;
import pasta.domain.release.CompareMode;
import pasta.domain.release.DateRule;
import pasta.domain.release.MarkCompareRule;
import pasta.domain.release.MarkType;
import pasta.domain.release.ReleaseAllResultsRule;
import pasta.domain.release.ReleaseAndRule;
import pasta.domain.release.ReleaseOrRule;
import pasta.domain.release.ReleaseResultsRule;
import pasta.domain.release.ReleaseRule;
import pasta.domain.release.StreamRule;
import pasta.domain.release.SubmissionCountRule;
import pasta.domain.release.UsernameRule;
import pasta.domain.template.Assessment;
import pasta.service.ReleaseManager;

/**
 * @author Joshua Stretton
 * @version 1.0
 * @since 24 Apr 2015
 */
public class AssessmentReleaseForm {
	private long ruleId;
	private String ruleName;
	
	// For rules types with sub-rules
	private List<AssessmentReleaseForm> rules;
	
	// All possible options when creating a rule
	private boolean asPercentage;
	private Set<String> classes;
	private Assessment compareAssessment;
	private double compareMark;
	private CompareMode compareMode;
	private MarkType markType;
	private String strDate;
	private Set<String> streams;
	private int submissionCount;
	private Set<String> usernames;
	
	public AssessmentReleaseForm() {
	}
	
	public AssessmentReleaseForm(String ruleName) {
		this(ReleaseManager.createInstance(ruleName));
	}
	
	public AssessmentReleaseForm(ReleaseRule base) {
		this.ruleId = base.getId();
		this.ruleName = base.getClass().getName();
		
		if(base instanceof ReleaseResultsRule) {
			compareAssessment = ((ReleaseResultsRule) base).getCompareAssessment();
		} else if(base instanceof ReleaseAllResultsRule) {
			compareAssessment = ((ReleaseAllResultsRule) base).getCompareAssessment();
		}
		
		if(base instanceof ClassRule) {
			classes = new TreeSet<>(((ClassRule) base).getClasses());
		} else if(base instanceof DateRule) {
			strDate = ((DateRule) base).getStrDate();
		} else if(base instanceof MarkCompareRule) {
			asPercentage = ((MarkCompareRule) base).isAsPercentage();
			compareMark = ((MarkCompareRule) base).getCompareMark();
			compareMode = ((MarkCompareRule) base).getCompareMode();
			markType = ((MarkCompareRule) base).getMarkType();
		} else if(base instanceof ReleaseAndRule) {
			rules = new ArrayList<>();
			for(ReleaseRule rule : ((ReleaseAndRule) base).getRules()) {
				rules.add(new AssessmentReleaseForm(rule));
			}
		} else if(base instanceof ReleaseOrRule) {
			rules = new ArrayList<>();
			for(ReleaseRule rule : ((ReleaseOrRule) base).getRules()) {
				rules.add(new AssessmentReleaseForm(rule));
			}
		} else if(base instanceof StreamRule) {
			streams = new TreeSet<>(((StreamRule) base).getStreams());
		}  else if(base instanceof SubmissionCountRule) {
			compareMode = ((SubmissionCountRule) base).getCompareMode();
			submissionCount = ((SubmissionCountRule) base).getSubmissionCount();
		} else if(base instanceof UsernameRule) {
			usernames = new TreeSet<>(((UsernameRule) base).getUsernames());
		}
	}

	
	
	public long getRuleId() {
		return ruleId;
	}
	public void setRuleId(long ruleId) {
		this.ruleId = ruleId;
	}

	public String getRuleName() {
		return ruleName;
	}
	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}

	public List<AssessmentReleaseForm> getRules() {
		return rules;
	}
	public void setRules(List<AssessmentReleaseForm> rules) {
		if(this.rules == null) {
			this.rules = new ArrayList<>();
		}
		this.rules.clear();
		this.rules.addAll(rules);
	}

	public boolean isAsPercentage() {
		return asPercentage;
	}
	public void setAsPercentage(boolean asPercentage) {
		this.asPercentage = asPercentage;
	}

	public Assessment getCompareAssessment() {
		return compareAssessment;
	}
	public void setCompareAssessment(Assessment compareAssessment) {
		this.compareAssessment = compareAssessment;
	}

	public Set<String> getClasses() {
		return classes;
	}
	public void setClasses(Set<String> classes) {
		this.classes = classes;
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

	public String getStrDate() {
		return strDate;
	}
	public void setStrDate(String strDate) {
		this.strDate = strDate;
	}

	public Set<String> getStreams() {
		return streams;
	}
	public void setStreams(Set<String> streams) {
		this.streams = streams;
	}

	public int getSubmissionCount() {
		return submissionCount;
	}
	public void setSubmissionCount(int submissionCount) {
		this.submissionCount = submissionCount;
	}

	public Set<String> getUsernames() {
		return usernames;
	}
	public void setUsernames(Set<String> usernames) {
		this.usernames = usernames;
	}

	@Override
	public String toString() {
		return "AssessmentReleaseForm [ruleId=" + ruleId + ", ruleName=" + ruleName + ", rules=" + rules
				+ ", asPercentage=" + asPercentage + ", classes=" + classes + ", compareAssessment="
				+ compareAssessment + ", compareMark=" + compareMark + ", compareMode=" + compareMode
				+ ", markType=" + markType + ", strDate=" + strDate + ", streams=" + streams
				+ ", submissionCount=" + submissionCount + ", usernames=" + usernames + "]";
	}
}
