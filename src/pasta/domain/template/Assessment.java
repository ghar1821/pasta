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

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Size;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import pasta.domain.PASTAUser;
import pasta.domain.release.ReleaseRule;
import pasta.util.Language;

/**
 * Container class for the assessment.
 * <p>
 * Contains zero to many:
 * <ul>
 * 	<li>unit test assessment modules</li>
 * 	<li>secret unit test assessment modules</li>
 * 	<li>hand marking assessment modules</li>
 * 	<li>competition assessment modules</li>
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
	<competitionSuite>
		<competition name="name" weight="double"/>
		...
		<competition name="name" weight="double" [secret="true|false"]/>
	</competitionSuite>
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
public class Assessment implements Serializable, Comparable<Assessment>{

	private static final long serialVersionUID = -387829953944113890L;

	@Transient
	protected final Log logger = LogFactory.getLog(getClass());
	
	@Id
	@GeneratedValue
	private long id;
	
	private String name;
	private double marks;
	private Date dueDate = new Date();
	
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
	
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval=true)
	@JoinTable(name="assessment_unit_tests",
	joinColumns=@JoinColumn(name = "assessment_id"),
	inverseJoinColumns=@JoinColumn(name = "unit_test_id"))
	@LazyCollection(LazyCollectionOption.FALSE)
	private Set<WeightedUnitTest> unitTests = new TreeSet<WeightedUnitTest>();
	
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval=true)
	@JoinColumn(name="assessment_id")
	@LazyCollection(LazyCollectionOption.FALSE)
	private Set<WeightedHandMarking> handMarking = new TreeSet<WeightedHandMarking>();
	
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval=true)
	@JoinColumn(name="assessment_id")
	@LazyCollection(LazyCollectionOption.FALSE)
	private Set<WeightedCompetition> competitions = new TreeSet<WeightedCompetition>();
	
	@ElementCollection
	@Enumerated(EnumType.STRING)
	@JoinTable(name = "assessment_languages", joinColumns = @JoinColumn(name = "assessment_id"))
	@Column(name = "language")
	@LazyCollection(LazyCollectionOption.FALSE)
	private Set<Language> submissionLanguages = new TreeSet<Language>();
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	
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
		String sample = getSolutionName();
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
		List<String> dirs = new LinkedList<String>();
		for(WeightedUnitTest weightedTest : getAllUnitTests()) {
			String dir = weightedTest.getTest().getSubmissionCodeRoot();
			if(dir != null && !dir.isEmpty()) {
				dirs.add(dir);
			}
		}
		return dirs;
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
	public void setHandMarking(Collection<WeightedHandMarking> handMarking) {
		for(WeightedHandMarking template : this.handMarking) {
			template.setAssessment(null);
		}
		this.handMarking.clear();
		for(WeightedHandMarking template : handMarking) {
			addHandMarking(template);
		}
	}
	
	public Set<WeightedCompetition> getCompetitions() {
		return competitions;
	}
	public void setCompetitions(Collection<WeightedCompetition> competitions) {
		for(WeightedCompetition comp : this.competitions) {
			comp.setAssessment(null);
		}
		this.competitions.clear();
		for(WeightedCompetition comp : competitions) {
			addCompetition(comp);
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
	
	public boolean isClosedFor(PASTAUser user) {
		if(!isReleasedTo(user)) {
			return true;
		}
		Date dueDate = getDueDate();
		if(dueDate == null) {
			return false;
		}
		Date extension = user.getExtensions().get(getId());
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

	public void addCompetition(WeightedCompetition weightedComp) {
		if(getCompetitions().add(weightedComp)) {
			weightedComp.setAssessment(this);
		}
	}
	
	public void addCompetitions(Collection<WeightedCompetition> competitions) {
		for(WeightedCompetition comp : competitions) {
			addCompetition(comp);
		}
	}
	
	public void removeCompetition(WeightedCompetition weightedComp) {
		if(getCompetitions().remove(weightedComp)) {
			weightedComp.setAssessment(null);
		}
	}
	
	public void removeCompetitions(Collection<WeightedCompetition> weightedComps) {
		for(WeightedCompetition comp : weightedComps) {
			removeCompetition(comp);
		}
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
		return (new Date()).after(getDueDate());
	}  
	
	public double getWeighting(UnitTest test){
		for(WeightedUnitTest myTest: unitTests){
			if(test == myTest.getTest()){
				return myTest.getWeight();
			}
		}
		return 0;
	}
	
	public double getWeighting(HandMarking test){
		for(WeightedHandMarking myTest: handMarking){
			if(test == myTest.getHandMarking()){
				return myTest.getWeight();
			}
		}
		return 0;
	}

//	/**
//	 * See string representation in class description.
//	 */
//	@Override
//	public String toString() {
//		String output = "";
//		output += "<assessment>" + System.getProperty("line.separator");
//		output += "\t<name>" + getName() + "</name>" + System.getProperty("line.separator");
//		output += "\t<category>" + getCategory() + "</category>" + System.getProperty("line.separator");
//		if(getReleasedClasses() != null){
//			output += "\t<releasedClasses>" + getReleasedClasses() + "</releasedClasses>" + System.getProperty("line.separator");
//		}
//		if(getSpecialRelease() != null){
//			output += "\t<specialRelease>" + getSpecialRelease() + "</specialRelease>" + System.getProperty("line.separator");
//		}
//		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy");
//		output += "\t<dueDate>" + sdf.format(getDueDate()) + "</dueDate>" + System.getProperty("line.separator");
//		output += "\t<marks>" + getMarks() + "</marks>" + System.getProperty("line.separator");
//		output += "\t<submissionsAllowed>" + getNumSubmissionsAllowed() + "</submissionsAllowed>"
//				+ System.getProperty("line.separator");
//		output += "\t<countUncompilable>" + isCountUncompilable() + "</countUncompilable>" + System.getProperty("line.separator");
//		if (unitTests.size() + secretUnitTests.size() > 0) {
//			output += "\t<unitTestSuite>" + System.getProperty("line.separator");
//			for (WeightedUnitTest unitTest : unitTests) {
//				output += "\t\t<unitTest id=\"" + unitTest.getTest().getId() + "\" weight=\""
//						+ unitTest.getWeight() + "\"/>" + System.getProperty("line.separator");
//			}
//			for (WeightedUnitTest unitTest : secretUnitTests) {
//				output += "\t\t<unitTest id=\"" + unitTest.getTest().getId() + "\" weight=\""
//						+ unitTest.getWeight() + "\" secret=\"true\" />" + System.getProperty("line.separator");
//			}
//			output += "\t</unitTestSuite>" + System.getProperty("line.separator");
//		}
//		// handMarks
//		if (handMarking.size() > 0) {
//			output += "\t<handMarkingSuite>" + System.getProperty("line.separator");
//			for (WeightedHandMarking handMarks : handMarking) {
//				output += "\t\t<handMarks id=\"" + handMarks.getHandMarking().getId() + "\" weight=\""
//						+ handMarks.getWeight() + "\"/>" + System.getProperty("line.separator");
//			}
//			output += "\t</handMarkingSuite>" + System.getProperty("line.separator");
//		}
//		// all competitions
//		if (competitions.size() > 0) {
//			output += "\t<competitionSuite>" + System.getProperty("line.separator");
//			for (WeightedCompetition comp : competitions) {
//				output += "\t\t<competition name=\"" + comp.getCompetition().getFileAppropriateName() + "\" weight=\""
//						+ comp.getWeight() + "\"/>" + System.getProperty("line.separator");
//			}
//			output += "\t</competitionSuite>" + System.getProperty("line.separator");
//		}
//		output += "</assessment>" + System.getProperty("line.separator");
//		return output;
//	}
//	
	@Override
	public int compareTo(Assessment o) {
		return getName().compareTo(o.getName());
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Assessment other = (Assessment) obj;
		if (id != other.id)
			return false;
		return true;
	}
}
