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
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Size;

import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.list.LazyList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

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
 * 	<li>list of usernames to whom the assessment has been specially released</li>
 * 	<li>list of classes to whom the assessment has been released (csv of STREAM.CLASS)</li>
 * 	<li>a flag to count submissions that compile towards the limit or not</li>
 * </ul>
 * 
 * String representation: 
 * 
 * <pre>{@code <assessment>
	<name>name</name>
	<category>category</category>
	<releasedClasses>STREAM1.CLASS1,...,STREAMn.CLASSn</releasedClasses>
	<specialRelease>usernames</specialRelease>
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
	
	@Column (name = "released_classes")
	private String releasedClasses;
	
	@Column (name = "special_release")
	private String specialRelease;
	
	@Column (name = "count_uncompilable")
	private boolean countUncompilable = true;
	
	/*
	 * The assessment modules have to be in a lazy list for the drag and drop
	 * Functionality on the web front end. Without this, there would be errors
	 * when adding assessment modules.
	 */
	@OneToMany (cascade = CascadeType.ALL)
	@JoinTable(name="assessment_unit_tests",
	joinColumns=@JoinColumn(name = "assessment_id"),
	inverseJoinColumns=@JoinColumn(name = "unit_test_id"))
	@LazyCollection(LazyCollectionOption.FALSE)
	private List<WeightedUnitTest> unitTests = LazyList.decorate(new ArrayList<WeightedUnitTest>(),
			FactoryUtils.instantiateFactory(WeightedUnitTest.class));
	
	@OneToMany (cascade = CascadeType.ALL)
	@JoinTable(name="assessment_secret_unit_tests",
			joinColumns=@JoinColumn(name = "assessment_id"),
			inverseJoinColumns=@JoinColumn(name = "unit_test_id"))
	@LazyCollection(LazyCollectionOption.FALSE)
	private List<WeightedUnitTest> secretUnitTests = LazyList.decorate(new ArrayList<WeightedUnitTest>(),
			FactoryUtils.instantiateFactory(WeightedUnitTest.class));
	
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval=true)
	@JoinColumn(name="assessment_id")
	@LazyCollection(LazyCollectionOption.FALSE)
	private List<WeightedHandMarking> handMarking = LazyList.decorate(new ArrayList<WeightedHandMarking>(),
			FactoryUtils.instantiateFactory(WeightedHandMarking.class));
	
	@Transient
	private List<WeightedCompetition> competitions = LazyList.decorate(new ArrayList<WeightedCompetition>(),
			FactoryUtils.instantiateFactory(WeightedCompetition.class));
	
	
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
	
	public String getReleasedClasses() {
		return releasedClasses;
	}
	public void setReleasedClasses(String released) {
		this.releasedClasses = released;
	}
	
	public String getSpecialRelease() {
		return specialRelease;
	}
	public void setSpecialRelease(String specialRelease) {
		this.specialRelease = specialRelease;
	}
	
	public boolean isCountUncompilable() {
		return countUncompilable;
	}
	public void setCountUncompilable(boolean countUncompilable) {
		this.countUncompilable = countUncompilable;
	}

	public List<WeightedUnitTest> getUnitTests() {
		return unitTests;
	}
	public void setUnitTests(List<WeightedUnitTest> unitTests) {
		this.unitTests.clear();
		this.unitTests.addAll(unitTests);
	}
	
	public List<WeightedUnitTest> getSecretUnitTests() {
		return secretUnitTests;
	}
	public void setSecretUnitTests(List<WeightedUnitTest> secretUnitTests) {
		this.secretUnitTests.clear();
		this.secretUnitTests.addAll(secretUnitTests);
	}
	
	public List<WeightedHandMarking> getHandMarking() {
		return handMarking;
	}
	public void setHandMarking(List<WeightedHandMarking> handMarking) {
		for(WeightedHandMarking template : this.handMarking) {
			template.setAssessment(null);
		}
		this.handMarking.clear();
		for(WeightedHandMarking template : handMarking) {
			addHandMarking(template);
		}
	}
	
	public List<WeightedCompetition> getCompetitions() {
		return competitions;
	}
	public void setCompetitions(List<WeightedCompetition> competitions) {
		this.competitions.clear();
		this.competitions.addAll(competitions);
	}
	
	public List<WeightedUnitTest> getAllUnitTests() {
		List<WeightedUnitTest> allUnitTests = new LinkedList<WeightedUnitTest>();
		allUnitTests.addAll(getUnitTests());
		allUnitTests.addAll(getSecretUnitTests());
		return allUnitTests;
	}
	
	public boolean isReleased() {
		return (releasedClasses != null && !releasedClasses.isEmpty()) || 
				(specialRelease != null && !specialRelease.isEmpty());
	}
	
	public String getFileAppropriateName() {
		return name.replace("[^\\w]+", "");
	}
	
	public void addUnitTest(WeightedUnitTest test) {
		getAllUnitTests().add(test);
	}
	
	public void removeUnitTest(WeightedUnitTest test) {
		getAllUnitTests().remove(test);
	}

	public void addSecretUnitTest(WeightedUnitTest test) {
		getSecretUnitTests().add(test);
	}
	
	public void removeSecretUnitTest(WeightedUnitTest test) {
		getSecretUnitTests().remove(test);
	}
	
	public void addHandMarking(WeightedHandMarking test) {
		test.setAssessment(this);
		getHandMarking().add(test);
	}
	
	public void removeHandMarking(WeightedHandMarking test) {
		test.setAssessment(null);
		getHandMarking().remove(test);
	}

	public void addCompetition(WeightedCompetition weightedComp) {
		getCompetitions().add(weightedComp);
	}
	
	public void removeCompetition(WeightedCompetition weightedComp) {
		getCompetitions().remove(weightedComp);
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
		for (WeightedUnitTest test : secretUnitTests) {
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
		for(WeightedUnitTest myTest: secretUnitTests){
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
	
	public void setGarbage(List<WeightedUnitTest> unitTests) {
	}

	/**
	 * Method to allow you to remove unit tests in the
	 * web front end.
	 * 
	 * Doesn't actually do any logic.
	 * 
	 * @return returns empty list
	 */
	public List<WeightedUnitTest> getGarbage() {
		return LazyList.decorate(new ArrayList<WeightedUnitTest>(),
				FactoryUtils.instantiateFactory(WeightedUnitTest.class));
	}
	
	/**
	 * Method to allow you to remove unit tests in the
	 * web front end.
	 * 
	 * Doesn't actually do any logic.
	 * 
	 */
	public void setCompGarbage(List<WeightedCompetition> comps) {
	}

	/**
	 * Method to allow you to remove unit tests in the
	 * web front end.
	 * 
	 * Doesn't actually do any logic.
	 * 
	 * @return returns empty list
	 */
	public List<WeightedCompetition> getCompGarbage() {
		return LazyList.decorate(new ArrayList<WeightedCompetition>(),
				FactoryUtils.instantiateFactory(WeightedCompetition.class));
	}
	
	/**
	 * Method to allow you to remove unit tests in the
	 * web front end.
	 * 
	 * Doesn't actually do any logic.
	 * 
	 */
	public void setHandGarbage(ArrayList<WeightedHandMarking> unitTests) {
	}

	/**
	 * Method to allow you to remove unit tests in the
	 * web front end.
	 * 
	 * Doesn't actually do any logic.
	 * 
	 */
	public List<WeightedHandMarking> getHandGarbage() {
		return LazyList.decorate(new ArrayList<WeightedHandMarking>(),
				FactoryUtils.instantiateFactory(WeightedHandMarking.class));
	}

	/**
	 * See string representation in class description.
	 */
	public String toString() {
		String output = "";
		output += "<assessment>" + System.getProperty("line.separator");
		output += "\t<name>" + getName() + "</name>" + System.getProperty("line.separator");
		output += "\t<category>" + getCategory() + "</category>" + System.getProperty("line.separator");
		if(getReleasedClasses() != null){
			output += "\t<releasedClasses>" + getReleasedClasses() + "</releasedClasses>" + System.getProperty("line.separator");
		}
		if(getSpecialRelease() != null){
			output += "\t<specialRelease>" + getSpecialRelease() + "</specialRelease>" + System.getProperty("line.separator");
		}
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy");
		output += "\t<dueDate>" + sdf.format(getDueDate()) + "</dueDate>" + System.getProperty("line.separator");
		output += "\t<marks>" + getMarks() + "</marks>" + System.getProperty("line.separator");
		output += "\t<submissionsAllowed>" + getNumSubmissionsAllowed() + "</submissionsAllowed>"
				+ System.getProperty("line.separator");
		output += "\t<countUncompilable>" + isCountUncompilable() + "</countUncompilable>" + System.getProperty("line.separator");
		if (unitTests.size() + secretUnitTests.size() > 0) {
			output += "\t<unitTestSuite>" + System.getProperty("line.separator");
			for (WeightedUnitTest unitTest : unitTests) {
				output += "\t\t<unitTest id=\"" + unitTest.getTest().getId() + "\" weight=\""
						+ unitTest.getWeight() + "\"/>" + System.getProperty("line.separator");
			}
			for (WeightedUnitTest unitTest : secretUnitTests) {
				output += "\t\t<unitTest id=\"" + unitTest.getTest().getId() + "\" weight=\""
						+ unitTest.getWeight() + "\" secret=\"true\" />" + System.getProperty("line.separator");
			}
			output += "\t</unitTestSuite>" + System.getProperty("line.separator");
		}
		// handMarks
		if (handMarking.size() > 0) {
			output += "\t<handMarkingSuite>" + System.getProperty("line.separator");
			for (WeightedHandMarking handMarks : handMarking) {
				output += "\t\t<handMarks id=\"" + handMarks.getHandMarking().getId() + "\" weight=\""
						+ handMarks.getWeight() + "\"/>" + System.getProperty("line.separator");
			}
			output += "\t</handMarkingSuite>" + System.getProperty("line.separator");
		}
		// all competitions
		if (competitions.size() > 0) {
			output += "\t<competitionSuite>" + System.getProperty("line.separator");
			for (WeightedCompetition comp : competitions) {
				output += "\t\t<competition name=\"" + comp.getCompetition().getShortName() + "\" weight=\""
						+ comp.getWeight() + "\"/>" + System.getProperty("line.separator");
			}
			output += "\t</competitionSuite>" + System.getProperty("line.separator");
		}
		output += "</assessment>" + System.getProperty("line.separator");
		return output;
	}
	
	@Override
	public int compareTo(Assessment o) {
		return getName().compareTo(o.getName());
	}
}
