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

package pasta.domain.form;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import pasta.docker.Language;
import pasta.docker.LanguageManager;
import pasta.domain.release.ReleaseRule;
import pasta.domain.template.Assessment;
import pasta.domain.template.WeightedHandMarking;
import pasta.domain.template.WeightedUnitTest;

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
	private Date lateDate;
	
	@Min(0)
	private int numSubmissionsAllowed;
	
	private boolean countUncompilable;
	
	@Length(max=64000)
	private String description;
	
	@Length(max=64)
	private String solutionName;
	
	private CommonsMultipartFile validatorFile;
	
	private Date groupLockDate;
	private int groupCount;
	private int groupSize;
	private boolean studentsManageGroups;
	
	private Set<Language> languages;
	
	private List<WeightedUnitTest> selectedUnitTests;
	private List<WeightedHandMarking> selectedHandMarking;
	
	private ReleaseRule releaseRule;
	
	protected final Log logger = LogFactory.getLog(getClass());
	
	public UpdateAssessmentForm(Assessment base) {
		this.id = base.getId();
		this.name = base.getName();
		this.category = base.getCategory();
		this.marks = base.getMarks();
		this.dueDate = base.getDueDate();
		this.lateDate = base.getLateDate();
		this.numSubmissionsAllowed = base.getNumSubmissionsAllowed();
		this.countUncompilable = base.isCountUncompilable();
		this.description = base.getDescription();
		this.solutionName = base.getSolutionName();
		this.groupLockDate = base.getGroupLockDate();
		this.groupCount = base.getGroupCount();
		this.groupSize = base.getGroupSize();
		this.studentsManageGroups = base.isStudentsManageGroups();
		
		this.languages = new TreeSet<Language>(base.getSubmissionLanguages());
		
		this.selectedUnitTests = new ArrayList<WeightedUnitTest>();
		this.selectedHandMarking = new ArrayList<WeightedHandMarking>();
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
	
	public Date getLateDate() {
		return lateDate;
	}
	public void setLateDate(Date lateDate) {
		this.lateDate = lateDate;
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

	public int getGroupCount() {
		return groupCount;
	}
	public void setGroupCount(int groupCount) {
		this.groupCount = groupCount;
	}

	public int getGroupSize() {
		return groupSize;
	}
	public void setGroupSize(int groupSize) {
		this.groupSize = groupSize;
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
	public String getStrGroupLock() {
		if(groupLockDate == null) {
			return "";
		}
		return NewAssessmentForm.sdf.format(groupLockDate);
	}
	public void setStrGroupLock(String groupLock) {
		if(groupLock == null || groupLock.isEmpty()) {
			groupLockDate = null;
		}
		try {
			groupLockDate = NewAssessmentForm.sdf.parse(groupLock);
		} catch (ParseException e) {
			groupLockDate = null;
		}
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

	public CommonsMultipartFile getValidatorFile() {
		return validatorFile;
	}
	public void setValidatorFile(CommonsMultipartFile validatorFile) {
		this.validatorFile = validatorFile;
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
	
	public Set<String> getStrLanguages() {
		Set<String> results = new TreeSet<String>();
		for(Language lang : languages) {
			results.add(lang.getId());
		}
		return results;
	}
	public void setStrLanguages(Set<String> languages) {
		this.languages = new TreeSet<Language>();
		if(languages == null || languages.isEmpty()) {
			return;
		}
		for(String lang : languages) {
			Language real = LanguageManager.getInstance().getLanguage(lang);
			if(real != null) {
				this.languages.add(real);
			}
		}
	}
	
	public String getStrLateDate() {
		if(lateDate == null) {
			return "";
		}
		return NewAssessmentForm.sdf.format(lateDate);
	}
	public void setStrLateDate(String date) {
		if(date == null || date.isEmpty()) {
			lateDate = null;
		}
		try {
			lateDate = NewAssessmentForm.sdf.parse(date);
		} catch (ParseException e) {
			lateDate = null;
		}
	}
	
	public List<WeightedUnitTest> getSelectedUnitTests() {
		return selectedUnitTests;
	}
	
	public List<WeightedHandMarking> getSelectedHandMarking() {
		return selectedHandMarking;
	}
}
