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

package pasta.repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import pasta.domain.form.NewHandMarkingForm;
import pasta.domain.result.DueDateComparator;
import pasta.domain.template.Assessment;
import pasta.domain.template.HandMarkData;
import pasta.domain.template.HandMarking;
import pasta.domain.template.WeightedCompetition;
import pasta.domain.template.WeightedField;
import pasta.domain.template.WeightedHandMarking;
import pasta.domain.template.WeightedUnitTest;
import pasta.util.ProjectProperties;

/**
 * Data Access Object for Assessments.
 * <p>
 * This class is responsible for all of the interaction between the data layer
 * (disk in this case) and the system for assessments. This includes writing the
 * assessment properties to disk and loading the assessment properties from disk
 * when the system starts. It also handles all of the changes to the objects and
 * holds them cached. There should only be one instance of this object running
 * in the system at any time.
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2012-11-13
 */

@Transactional
@Repository("assessmentDAO")
@DependsOn("projectProperties")
public class AssessmentDAO {

	protected final Log logger = LogFactory.getLog(getClass());

	@Autowired
	private SessionFactory sessionFactory;
	
	public AssessmentDAO() {
	}
	
	public Map<String, Set<Assessment>> getAllAssessmentsByCategory() {
		Map<String, Set<Assessment>> assessments = new TreeMap<String, Set<Assessment>>();
		for(Assessment assessment : getAllAssessments()) {
			Set<Assessment> sameCategory = assessments.get(assessment.getCategory());
			if(sameCategory == null) {
				sameCategory = new TreeSet<Assessment>(new DueDateComparator());
				assessments.put(assessment.getCategory(), sameCategory);
			}
			sameCategory.add(assessment);
		}
		return assessments;
	}

	public Collection<HandMarking> getHandMarkingList() {
		return ProjectProperties.getInstance().getHandMarkingDAO().getAllHandMarkings();
	}

	public Collection<Assessment> getAssessmentList() {
		return getAllAssessments();
	}
	
	/**
	 * Go through all assessments and remove test from them.
	 * 
	 * @param unitTestId unit test id to remove.
	 */
	public void unlinkUnitTest(long unitTestId) {
		// go through all assessments and remove the unit test from them
		for(Assessment assessment: getAllAssessments()){
			for(WeightedUnitTest test: assessment.getAllUnitTests()){
				if(test.getTest().getId() == unitTestId){
					assessment.removeUnitTest(test);
				}
			}
		}
	}
	
	/**
	 * Delete an assessment from the system.
	 * <p>
	 * 
	 * Iterates over all competitions and removes itself from them.
	 * Competitions are the only assessment modules that contain
	 * a link to the assessments they are used in.
	 * 
	 * @param id the id of the assessment 
	 */
	public void removeAssessment(long id) {
		Assessment assessmentToRemove = getAssessment(id);
		if(assessmentToRemove == null){
			return;
		}
		
		delete(assessmentToRemove);
	}
	
	/**
	 * Iterates over all assessments and removes itself from them.
	 * 
	 * @param id the id of the competition
	 */
	public void unlinkCompetition(long id) {
		// go through all assessments and remove the competition from them
		for (Assessment assessment : getAllAssessments()) {
			for (WeightedCompetition comp : assessment.getCompetitions()) {
				if (comp.getCompetition().getId() == id) {
					assessment.removeCompetition(comp);
				}
			}
		}
	}
	
	/**
	 * Iterates over all assessments and removes itself from them.
	 * 
	 * @param handMarkingId the id of the hand marking template 
	 */
	public void unlinkHandMarking(long handMarkingId) {
		// remove from assessments
		for(Assessment ass : getAllAssessments()) {
			Set<WeightedHandMarking> marking = ass.getHandMarking();
			for (WeightedHandMarking template : marking) {
				if(template.getHandMarking().getId() == handMarkingId) {
					ass.removeHandMarking(template);
					break;
				}
			}
		}
	}

	/**
	 * Update a hand marking template
	 * <p>
	 * Updates cache and writes everything to file. Pretty much used all the time,
	 * because when you make a new hand marking template, you generate one based
	 * on defaults.
	 * 
	 * @param newHandMarking the new hand marking template
	 */
	public void updateHandMarking(HandMarking newHandMarking) {
		ProjectProperties.getInstance().getHandMarkingDAO().saveOrUpdate(newHandMarking);
	}

	/**
	 * Creates a default hand marking template. Default columns are:
	 * <ul>
	 * <li>Poor : 0%</li>
	 * <li>Acceptable : 50%</li>
	 * <li>Excellent : 100%</li>
	 * </ul>
	 * Default rows are:
	 * <ul>
	 * <li>Formatting : 20%</li>
	 * <li>Code Reuse : 40%</li>
	 * <li>Variable naming : 40%</li>
	 * </ul>
	 * Default descriptions are empty.
	 * 
	 * @param newHandMarking the new hand marking
	 */
	public void newHandMarking(NewHandMarkingForm newHandMarking) {

		HandMarking newMarking = new HandMarking();
		newMarking.setName(newHandMarking.getName());

		newMarking.addColumn(new WeightedField("Poor", 0));
		newMarking.addColumn(new WeightedField("Acceptable", 0.5));
		newMarking.addColumn(new WeightedField("Excellent", 1));

		newMarking.addRow(new WeightedField("Formatting", 0.2));
		newMarking.addRow(new WeightedField("Code Reuse", 0.4));
		newMarking.addRow(new WeightedField("Variable Naming", 0.4));

		for (WeightedField column : newMarking.getColumnHeader()) {
			for (WeightedField row : newMarking.getRowHeader()) {
				newMarking.addData(new HandMarkData(column, row, ""));
			}
		}

		updateHandMarking(newMarking);
	}
	
	@Deprecated
	public void save(Assessment assessment) {
		saveOrUpdate(assessment);
	}
	
	@Deprecated
	public void update(Assessment assessment) {
		saveOrUpdate(assessment);
	}
	
	@Deprecated
	public void merge(Assessment assessment) {
		saveOrUpdate(assessment);
	}
	
	public void delete(Assessment assessment) {
		try {
			sessionFactory.getCurrentSession().delete(assessment);
			logger.info("Deleted assessment " + assessment.getName());
		} catch (Exception e) {
			logger.error("Could not delete assessment " + assessment.getName(), e);
		}
	}
	
	public void saveOrUpdate(Assessment assessment) {
		long id = assessment.getId();
		sessionFactory.getCurrentSession().saveOrUpdate(assessment);
		logger.info((id == assessment.getId() ? "Updated" : "Created") +
				" assessment " + assessment.getName());
	}
	
	@SuppressWarnings("unchecked")
	public List<Assessment> getAllAssessments() {
		return sessionFactory.getCurrentSession().createCriteria(Assessment.class).list();
	}
	
	@SuppressWarnings("unchecked")
	public List<Long> getAssessmentIDList() {
		return sessionFactory.getCurrentSession()
				.createCriteria(Assessment.class)
				.setProjection(Projections.property("id"))
				.list();
	}
	
	public Assessment getAssessment(long id) {
		return (Assessment) sessionFactory.getCurrentSession().get(Assessment.class, id);
	}
}
