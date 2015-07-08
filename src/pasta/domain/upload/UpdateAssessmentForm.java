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

package pasta.domain.upload;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.apache.commons.collections4.FactoryUtils;
import org.apache.commons.collections4.list.LazyList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import pasta.domain.release.ReleaseRule;
import pasta.domain.template.Assessment;
import pasta.domain.template.WeightedCompetition;
import pasta.domain.template.WeightedHandMarking;
import pasta.domain.template.WeightedUnitTest;
import pasta.util.Language;

/**
 * Form object for updating assessments.
 * 
 * @author Joshua Stretton
 * @version 1.0
 * @since 2015-04-07
 *
 */
public class UpdateAssessmentForm {
	
	@Min(0)
	private long id;
	
	@NotEmpty
	@Length(max=256)
	private String name;
	
	@Length(max=256)
	private String category;
	
	@Min(0)
	private double marks;
	
	@NotNull
	private Date dueDate;
	
	@Min(0)
	private int numSubmissionsAllowed;
	
	private boolean countUncompilable;
	
	@Length(max=64000)
	private String description;
	
	@Length(max=64)
	private String solutionName;
	
	private Set<Language> languages;
	
	private List<WeightedUnitTest> newUnitTests;
	private List<WeightedUnitTest> newSecretUnitTests;
	private List<WeightedHandMarking> newHandMarking;
	private List<WeightedCompetition> newCompetitions;
	
	// TODO: check if 0.5 or 50% when submitting percentages -- only allow one
	private ReleaseRule releaseRule;
	
	protected final Log logger = LogFactory.getLog(getClass());
	
	public UpdateAssessmentForm(Assessment base) {
		this.id = base.getId();
		this.name = base.getName();
		this.category = base.getCategory();
		this.marks = base.getMarks();
		this.dueDate = base.getDueDate();
		this.numSubmissionsAllowed = base.getNumSubmissionsAllowed();
		this.countUncompilable = base.isCountUncompilable();
		this.description = base.getDescription();
		this.solutionName = base.getSolutionName();
		
		this.languages = new TreeSet<Language>(base.getSubmissionLanguages());
		
		/*
		 * The assessment modules have to be in a lazy list for the drag and drop
		 * Functionality on the web front end. Without this, there would be errors
		 * when adding assessment modules.
		 * TODO: not actually true any more -- normal lists should be able to be used
		 */
		this.newUnitTests = LazyList.lazyList(new ArrayList<WeightedUnitTest>(),
				FactoryUtils.instantiateFactory(WeightedUnitTest.class));
		this.newSecretUnitTests = LazyList.lazyList(new ArrayList<WeightedUnitTest>(),
				FactoryUtils.instantiateFactory(WeightedUnitTest.class));
		this.newHandMarking = LazyList.lazyList(new ArrayList<WeightedHandMarking>(),
				FactoryUtils.instantiateFactory(WeightedHandMarking.class));
		this.newCompetitions = LazyList.lazyList(new ArrayList<WeightedCompetition>(),
				FactoryUtils.instantiateFactory(WeightedCompetition.class));
	}
	
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

	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
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

	public int getNumSubmissionsAllowed() {
		return numSubmissionsAllowed;
	}
	public void setNumSubmissionsAllowed(int numSubmissionsAllowed) {
		this.numSubmissionsAllowed = numSubmissionsAllowed;
	}

	public boolean isCountUncompilable() {
		return countUncompilable;
	}
	public void setCountUncompilable(boolean countUncompilable) {
		this.countUncompilable = countUncompilable;
	}

	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public String getSolutionName() {
		return solutionName;
	}
	public void setSolutionName(String solutionName) {
		this.solutionName = solutionName;
	}

	public Set<Language> getLanguages() {
		return languages;
	}
	public void setLanguages(Set<Language> languages) {
		this.languages = languages;
	}

	public ReleaseRule getReleaseRule() {
		return releaseRule;
	}
	public void setReleaseRule(ReleaseRule releaseRule) {
		this.releaseRule = releaseRule;
	}

	public List<WeightedUnitTest> getNewUnitTests() {
		return newUnitTests;
	}
	public void setNewUnitTests(Collection<WeightedUnitTest> unitTests) {
		this.newUnitTests.clear();
		this.newUnitTests.addAll(unitTests);
	}

	public List<WeightedUnitTest> getNewSecretUnitTests() {
		return newSecretUnitTests;
	}
	public void setNewSecretUnitTests(Collection<WeightedUnitTest> secretUnitTests) {
		this.newSecretUnitTests.clear();
		this.newSecretUnitTests.addAll(secretUnitTests);
	}

	public List<WeightedHandMarking> getNewHandMarking() {
		return newHandMarking;
	}
	public void setNewHandMarking(Collection<WeightedHandMarking> handMarking) {
		this.newHandMarking.clear();
		this.newHandMarking.addAll(handMarking);
	}

	public List<WeightedCompetition> getNewCompetitions() {
		return newCompetitions;
	}
	public void setNewCompetitions(Collection<WeightedCompetition> competitions) {
		this.newCompetitions.clear();
		this.newCompetitions.addAll(competitions);
	}

	public String getStrDate() {
		if(dueDate == null) {
			return "";
		}
		return NewAssessmentForm.sdf.format(dueDate);
	}
	public void setStrDate(String date) {
		if(date == null || date.isEmpty()) {
			dueDate = null;
		}
		try {
			dueDate = NewAssessmentForm.sdf.parse(date);
		} catch (ParseException e) {
			dueDate = null;
		}
	}

	public List<WeightedUnitTest> getAllUnitTests() {
		List<WeightedUnitTest> allTests = new LinkedList<WeightedUnitTest>();
		allTests.addAll(getNewUnitTests());
		allTests.addAll(getNewSecretUnitTests());
		return allTests;
	}
}
