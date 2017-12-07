/*
Copyright (c) 2014, Alex Radu
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer. 
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

The views and conclusions contained in the software and documentation are those
of the authors and should not be interpreted as representing official policies, 
either expressed or implied, of the PASTA Project.
 */

package pasta.domain.template;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import pasta.archive.ArchivableBaseEntity;
import pasta.archive.Archive;
import pasta.archive.ArchiveEntry;
import pasta.archive.ArchiveOptions;
import pasta.docker.Language;
import pasta.docker.LanguageManager;
import pasta.domain.VerboseName;
import pasta.domain.ratings.AssessmentRating;
import pasta.domain.release.ReleaseAllResultsRule;
import pasta.domain.release.ReleaseResultsRule;
import pasta.domain.release.ReleaseRule;
import pasta.domain.reporting.ReportPermission;
import pasta.domain.result.AssessmentResult;
import pasta.domain.result.AssessmentResultSummary;
import pasta.domain.user.PASTAGroup;
import pasta.domain.user.PASTAUser;
import pasta.util.ProjectProperties;

/**
 * Container class for the assessment.
 * <p>
 * Contains zero to many:
 * <ul>
 * 	<li>unit test assessment modules</li>
 * 	<li>secret unit test assessment modules</li>
 * 	<li>hand marking assessment modules</li>
 * </ul>
 * 
 * Also contains assessment specific information such as:
 * <ul>
 * 	<li>name</li>
 * 	<li>number of marks the assessment is worth</li>
 * 	<li>due date of the assessment</li>
 * 	<li>description (raw html) of the assessment</li>
 * 	<li>number of submissions allowed (0 for infinite submissions allowed)</li>
 * 	<li>category</li>
 * 	<li>a flag to count submissions that compile towards the limit or not</li>
 * </ul>
 * 
 * String representation: 
 * 
 * <pre>{@code <assessment>
	<name>name</name>
	<category>category</category>
	<dueDate>hh:mm dd/MM/yyyy</dueDate>
	<marks>double</marks>
	<submissionsAllowed>int >= 0</submissionsAllowed>
	<countUncompilable>true|false</countUncompilable>
	<unitTestSuite>
		<unitTest name="name" weight="double"/>
		...
		<unitTest name="name" weight="double" [secret="true|false"]/>
	</unitTestSuite>
	<handMarkingSuite>
		<handMarks name="name" weight="double"/>
		...
		<handMarks name="name" weight="double"/>
	</handMarkingSuite>
</assessment>}</pre>
 * 
 * All weighting is relative. If two assessment modules are weighted as 
 * 1, then they are worth 50% of the marks each.
 * 
 * <p>
 * File location on disk: $projectLocation$/template/assessment/$assessmentId$
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2012-11-13
 *
 */
@Entity
@Table (name = "assessments")
@VerboseName("assessment")
public class Assessment extends ArchivableBaseEntity implements Comparable<Assessment> {

	private static final long serialVersionUID = -387829953944113890L;

	public static final String TUTOR_CATEGORY_PREFIX = "*";

	private String name;
	private double marks;
	private Date dueDate = new Date();
	private Date lateDate = null;
	
	@Column (length = 64000) // Controls database column size
	@Size (max = 64000) // Validates max length when attempting to insert into database
	private String description;
	
	@Column (name = "num_submission_allowed")
	private int numSubmissionsAllowed;
	
	private String category;
	
	@Column (name = "solution_name")
	private String solutionName;
	
	@OneToOne (cascade = CascadeType.ALL)
	@JoinColumn (name = "release_rule_id")
	private ReleaseRule releaseRule;
	
	@Column (name = "count_uncompilable")
	private boolean countUncompilable = true;
	
	@OneToMany (
			cascade = CascadeType.ALL,
			orphanRemoval = true,
			mappedBy = "assessment"
	)
	@LazyCollection(LazyCollectionOption.FALSE)
	private Set<WeightedUnitTest> unitTests = new TreeSet<WeightedUnitTest>();
	
	@OneToMany (
			cascade = CascadeType.ALL, 
			orphanRemoval = true, 
			mappedBy = "assessment"
	)
	@LazyCollection(LazyCollectionOption.FALSE)
	private Set<WeightedHandMarking> handMarking = new TreeSet<WeightedHandMarking>();
	
	@ElementCollection
	@JoinTable(name = "assessment_languages", joinColumns = @JoinColumn(name = "assessment_id"))
	@Column(name = "language")
	@LazyCollection(LazyCollectionOption.FALSE)
	private Set<Language> submissionLanguages = new TreeSet<Language>();
	
	@Column (name="group_lock_date")
	private Date groupLockDate;
	
	@Column (name="group_count")
	private int groupCount = 0;
	
	@Column (name="group_size")
	private int groupSize = 2;
	
	@Column (name="students_manage_groups")
	private boolean studentsManageGroups = true;
	
	@Column (name="custom_validator_name")
	private String customValidatorName;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public double getMarks() {
		return marks;
	}
	public void setMarks(double marks) {
		this.marks = marks;
	}
	
	public Date getDueDate() {
		return dueDate;
	}
	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}
	
	public Date getLateDate() {
		return lateDate;
	}
	public void setLateDate(Date lateDate) {
		this.lateDate = lateDate;
	}
	
	public Date getLastSubmitDate() {
		if(getLateDate() == null) {
			return getDueDate();
		}
		if(getLateDate().after(getDueDate())) {
			return getLateDate();
		}
		return getDueDate();
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public int getNumSubmissionsAllowed() {
		return numSubmissionsAllowed;
	}
	public void setNumSubmissionsAllowed(int numSubmissionsAllowed) {
		this.numSubmissionsAllowed = numSubmissionsAllowed;
	}
	
	public String getCategory() {
		if(category == null){
			return "";
		}
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	
	public String getSolutionName() {
		return solutionName;
	}
	public String getShortSolutionName() {
		if(this.solutionName != null && isAllowed(LanguageManager.getInstance().getLanguage("java"))) {
			return solutionName.substring(solutionName.lastIndexOf('.') + 1);
		}
		return solutionName;
	}
	public void setSolutionName(String solutionName) {
		this.solutionName = solutionName;
	}
	
	public boolean isCountUncompilable() {
		return countUncompilable;
	}
	public void setCountUncompilable(boolean countUncompilable) {
		this.countUncompilable = countUncompilable;
	}

	public ReleaseRule getReleaseRule() {
		return releaseRule;
	}
	public void setReleaseRule(ReleaseRule releaseRule) {
		this.releaseRule = releaseRule;
	}
	
	public Set<Language> getSubmissionLanguages() {
		return submissionLanguages;
	}
	public void setSubmissionLanguages(Set<Language> submissionLanguages) {
		this.submissionLanguages = submissionLanguages;
	}
	public boolean isAllowed(Language thisLanguage) {
		return submissionLanguages.isEmpty() || submissionLanguages.contains(thisLanguage);
	}
	public String getSampleSubmissionName() {
		String sample = getShortSolutionName();
		if(sample == null) {
			return "";
		}
		if(getSubmissionLanguages().isEmpty()) {
			return sample;
		}
		Language sampleLang = getSubmissionLanguages().iterator().next();
		if(sampleLang.getExtensions().isEmpty()) {
			return sample;
		}
		return sample + "." + sampleLang.getExtensions().iterator().next();
	}
	
	public List<String> getExpectedDirectories() {
		if(!isAutoMarked()) {
			return null;
		}
		Set<String> dirs = new TreeSet<String>();
		for(WeightedUnitTest weightedTest : getAllUnitTests()) {
			String dir = weightedTest.getTest().getSubmissionCodeRoot();
			if(dir != null && !dir.isEmpty()) {
				dirs.add(dir);
			}
		}
		return new LinkedList<>(dirs);
	}
	
	public int getGroupCount() {
		return groupCount;
	}
	public void setGroupCount(int groupCount) {
		this.groupCount = groupCount;
	}
	public boolean isGroupWork() {
		return this.groupCount != 0;
	}
	public boolean isUnlimitedGroupCount() {
		return groupCount == -1;
	}
	
	public int getGroupSize() {
		return groupSize;
	}
	public void setGroupSize(int groupSize) {
		this.groupSize = groupSize;
	}
	public boolean isUnlimitedGroupSize() {
		return groupSize == -1;
	}
	
	public boolean isStudentsManageGroups() {
		return studentsManageGroups;
	}
	public void setStudentsManageGroups(boolean studentsManageGroups) {
		this.studentsManageGroups = studentsManageGroups;
	}
	
	public Date getGroupLockDate() {
		return groupLockDate;
	}
	public void setGroupLockDate(Date groupLockDate) {
		this.groupLockDate = groupLockDate;
	}
	public boolean isGroupsLocked() {
		if(isClosed()) {
			return true;
		}
		if(groupLockDate == null) {
			return false;
		}
		return new Date().after(groupLockDate);
	}
	
	public boolean isOnlyIndividualWork() {
		return hasWork() && isConsistentGroupWork(false);
	}
	public boolean isOnlyGroupWork() {
		return hasWork() && isConsistentGroupWork(true);
	}
	private boolean hasWork() {
		return !(unitTests.isEmpty() && handMarking.isEmpty());
	}
	// Named for bean convention
	public boolean isHasWork() {
		return hasWork();
	}
	private boolean isConsistentGroupWork(boolean isGroupWork) {
		for(WeightedUnitTest module : unitTests){
			if(module.isGroupWork() != isGroupWork) {
				return false;
			}
		}
		for(WeightedHandMarking module : handMarking){
			if(module.isGroupWork() != isGroupWork) {
				return false;
			}
		}
		return true;
	}
	
	public boolean isCustomValidator() {
		return customValidatorName != null && !customValidatorName.isEmpty();
	}
	public File getCustomValidator() {
		if(!isCustomValidator()) {
			return null;
		}
		return new File(ProjectProperties.getInstance().getAssessmentValidatorLocation() + 
				getId() + "/" + customValidatorName);
	}
	public String getCustomValidatorName() {
		return customValidatorName;
	}
	public void setCustomValidatorName(String customValidatorName) {
		this.customValidatorName = customValidatorName;
	}
	
	public Set<WeightedUnitTest> getUnitTests() {
		Set<WeightedUnitTest> unitTests = new TreeSet<>();
		for(WeightedUnitTest test : getAllUnitTests()) {
			if(!test.isSecret()) {
				unitTests.add(test);
			}
		}
		return unitTests;
	}
	public void setUnitTests(Collection<WeightedUnitTest> unitTests) {
		this.unitTests.clear();
		this.unitTests.addAll(unitTests);
	}
	
	public Set<WeightedUnitTest> getSecretUnitTests() {
		Set<WeightedUnitTest> unitTests = new TreeSet<>();
		for(WeightedUnitTest test : getAllUnitTests()) {
			if(test.isSecret()) {
				unitTests.add(test);
			}
		}
		return unitTests;
	}
	
	public Set<WeightedHandMarking> getHandMarking() {
		return handMarking;
	}
	public Set<WeightedHandMarking> getIndividualHandMarking() {
		Set<WeightedHandMarking> results = new TreeSet<WeightedHandMarking>();
		for(WeightedHandMarking hm : getHandMarking()) {
			if(!hm.isGroupWork())
				results.add(hm);
		}
		return results;
	}
	public Set<WeightedHandMarking> getGroupHandMarking() {
		Set<WeightedHandMarking> results = new TreeSet<WeightedHandMarking>();
		for(WeightedHandMarking hm : getHandMarking()) {
			if(hm.isGroupWork())
				results.add(hm);
		}
		return results;
	}
	public void setHandMarking(Collection<WeightedHandMarking> handMarking) {
		for(WeightedHandMarking template : this.handMarking) {
			template.setAssessment(null);
		}
		this.handMarking.clear();
		for(WeightedHandMarking template : handMarking) {
			addHandMarking(template);
		}
	}
	
	public Set<WeightedUnitTest> getAllUnitTests() {
		return unitTests;
	}
	
	public boolean isReleased() {
		return (releaseRule != null);
	}
	
	public boolean isReleasedTo(PASTAUser user) {
		return user.isTutor() || (isReleased() && releaseRule.isReleased(user, this));
	}
	
	public String getReleaseDescription() {
		if(isReleased()) {
			return releaseRule.toString();
		}
		return "None";
	}
	
	public boolean isClosedFor(PASTAUser user, Date extension) {
		if(!isReleasedTo(user)) {
			return true;
		}
		Date dueDate = getLastSubmitDate();
		if(dueDate == null) {
			return false;
		}
		if(extension != null) {
			if(extension.after(dueDate)) {
				dueDate = extension;
			}
		}
		return new Date().after(dueDate);
	}
	
	public String getFileAppropriateName() {
		return name.replaceAll("[^\\w]+", "");
	}
	
	public void addUnitTest(WeightedUnitTest test) {
		if(getAllUnitTests().add(test)) {
			test.setAssessment(this);
		}
	}
	
	public void addUnitTests(Collection<WeightedUnitTest> tests) {
		for(WeightedUnitTest test : tests) {
			addUnitTest(test);
		}
	}
	
	public void removeUnitTest(WeightedUnitTest test) {
		if(getAllUnitTests().remove(test)) {
			test.setAssessment(null);
		}
	}
	
	public boolean isAutoMarked() {
		return !this.getAllUnitTests().isEmpty();
	}
	
	public void removeUnitTests(Collection<WeightedUnitTest> weightedUnitTests) {
		for(WeightedUnitTest test : weightedUnitTests) {
			removeUnitTest(test);
		}
	}
	
	public void addHandMarking(WeightedHandMarking test) {
		if(getHandMarking().add(test)) {
			test.setAssessment(this);
		}
	}
	
	public void addHandMarkings(Collection<WeightedHandMarking> handMarkings) {
		for(WeightedHandMarking handMarking : handMarkings) {
			addHandMarking(handMarking);
		}
	}
	
	public void removeHandMarking(WeightedHandMarking test) {
		if(getHandMarking().remove(test)) {
			test.setAssessment(null);
		}
	}
	
	public void removeHandMarkings(Collection<WeightedHandMarking> weightedHandMarkings) {
		for(WeightedHandMarking handMarking : weightedHandMarkings) {
			removeHandMarking(handMarking);
		}
	}
	
	public boolean isHandMarked() {
		return !this.getHandMarking().isEmpty();
	}

	public String getSimpleDueDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		return sdf.format(dueDate);
	}

	public void setSimpleDueDate(String date) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
			dueDate = sdf.parse(date.trim());
		} catch (ParseException e) {
			logger.error("Could not parse date", e);
		}
	}

	public boolean isCompletelyTested() {
		for (WeightedUnitTest test : unitTests) {
			if (!test.getTest().isTested()) {
				return false;
			}
		}
		return true;
	}

	public boolean isClosed() {
		return (new Date()).after(getLastSubmitDate());
	}  
	
	public double getWeighting(UnitTest test){
		for(WeightedUnitTest myTest: unitTests){
			if(test.getId() == myTest.getTest().getId()){
				return myTest.getWeight();
			}
		}
		return 0;
	}
	
	public double getWeighting(HandMarking test){
		for(WeightedHandMarking myTest: handMarking){
			if(test.getId() == myTest.getHandMarking().getId()){
				return myTest.getWeight();
			}
		}
		return 0;
	}
	
	public double getRawTotalWeight() {
		double weight = 0;
		for(WeightedUnitTest module: unitTests){
			weight += module.getWeight();
		}
		for(WeightedHandMarking module: handMarking){
			weight += module.getWeight();
		}
		return weight;
	}
	
	public List<String> getAllTestNames() {
		List<String> testNames = new ArrayList<>();
		for(WeightedUnitTest test : getAllUnitTests()) {
			testNames.addAll(test.getTest().getAllTestNames());
		}
		return testNames;
	}

	@Override
	public int compareTo(Assessment o) {
		return getName().compareTo(o.getName());
	}
	
	@Override
	public void prepareForArchive(ArchiveOptions options) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void rebuildFromArchive(Archive archive, ArchivableBaseEntity existing) {
		Iterator<WeightedUnitTest> wutIt = this.getAllUnitTests().iterator();
		while(wutIt.hasNext()) {
			WeightedUnitTest wut = wutIt.next();
			UnitTest correct = (UnitTest) archive.getUnarchived(new ArchiveEntry(wut.getTest()));
			if(correct == null) {
				wutIt.remove();
			} else {
				wut.setTest(correct);
			}
		}
		Iterator<WeightedHandMarking> whmIt = this.getHandMarking().iterator();
		while(whmIt.hasNext()) {
			WeightedHandMarking whm = whmIt.next();
			HandMarking correct = (HandMarking) archive.getUnarchived(new ArchiveEntry(whm.getHandMarking()));
			if(correct == null) {
				whmIt.remove();
			} else {
				whm.setHandMarking(correct);
			}
		}
	}
	
	/*===========================
	 * CONVENIENCE RELATIONSHIPS
	 * 
	 * Making unidirectional many-to-one relationships into bidirectional 
	 * one-to-many relationships for ease of deletion by Hibernate
	 *===========================
	 */
	@OneToMany(mappedBy = "assessment", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
	private List<AssessmentRating> ratings;
	@OneToMany(mappedBy = "assessment", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
	private List<AssessmentExtension> extensions;
	@OneToMany(mappedBy = "assessment", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
	private List<AssessmentResult> results;
	@OneToMany(mappedBy = "id.assessment", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
	private List<AssessmentResultSummary> summaries;
	@OneToMany(mappedBy = "compareAssessment", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
	private List<ReleaseResultsRule> releaseResultsRules;
	@OneToMany(mappedBy = "compareAssessment", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
	private List<ReleaseAllResultsRule> releaseAllResultsRules;
	@OneToMany(mappedBy = "assessment", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
	private List<PASTAGroup> groups;
	@OneToMany(mappedBy = "id.assessment", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
	private List<ReportPermission> reportPermissions;
}
