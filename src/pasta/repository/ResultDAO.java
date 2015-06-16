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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pasta.domain.PASTAUser;
import pasta.domain.result.AssessmentResult;
import pasta.domain.result.CompetitionMarks;
import pasta.domain.result.CompetitionResult;
import pasta.domain.result.CompetitionResultData;
import pasta.domain.result.CompetitionUserMark;
import pasta.domain.result.HandMarkingResult;
import pasta.domain.result.ResultCategory;
import pasta.domain.result.UnitTestCaseResult;
import pasta.domain.result.UnitTestResult;
import pasta.domain.template.Assessment;
import pasta.domain.template.WeightedHandMarking;
import pasta.util.ProjectProperties;

/**
 * Data Access Object for Results.
 * <p>
 * 
 * This class is responsible for all of the interaction
 * between the data layer (disk in this case) and the system
 * for assessment results.
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2012-11-16
  *
 */
@Repository("resultDAO")
public class ResultDAO extends HibernateDaoSupport{
	
	protected final Log logger = LogFactory.getLog(getClass());
	
	// Default required by hibernate
	@Autowired
	public void setMySession(SessionFactory sessionFactory) {
		setSessionFactory(sessionFactory);
	}

	/**
	 * Load a unit test result from a location
	 * 
	 * @param location the location of the test
	 * @return the result of the unit test
	 */
	public UnitTestResult getUnitTestResultFromDisk(String location){
		UnitTestResult result = new UnitTestResult();
		
		//TODO: replace with generic file
		// check to see if there is a results.xml file
		File testResults = new File(location+"/result.xml");
		if(testResults.exists() && testResults.length() != 0){
			try{	
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder;
				dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(testResults);
				doc.getDocumentElement().normalize();
				
				ArrayList<UnitTestCaseResult> testCases = new ArrayList<UnitTestCaseResult>();
				
				NodeList unitTestList = doc.getElementsByTagName("testcase");
				if (unitTestList != null && unitTestList.getLength() > 0) {
					for (int i = 0; i < unitTestList.getLength(); i++) {
						Node unitTestNode = unitTestList.item(i);
						if (unitTestNode.getNodeType() == Node.ELEMENT_NODE) {
							UnitTestCaseResult caseResult = new UnitTestCaseResult();
							
							Element unitTestElement = (Element) unitTestNode;
							// name
							caseResult.setTestName(unitTestElement.getAttribute("name"));
							// time
							caseResult.setTime(Double.parseDouble(unitTestElement.getAttribute("time")));
							// test case - assume it is a pass.
							caseResult.setTestResult("pass");
							
							// if failed
							if(unitTestElement.hasChildNodes()){
								Element failedUnitTestElement = (Element) unitTestNode.getChildNodes().item(1);
								
								// new result
								caseResult.setTestResult(failedUnitTestElement.getNodeName());
								// message
								if(failedUnitTestElement.hasAttribute("message")){
									caseResult.setTestMessage(failedUnitTestElement.getAttribute("message"));
								}
								// type
								if(failedUnitTestElement.hasAttribute("type")){
									caseResult.setType((failedUnitTestElement.getAttribute("type")));
								}
								// extended message
								if(failedUnitTestElement.getTextContent() != null){
									caseResult.setExtendedMessage(failedUnitTestElement.getTextContent());
								}
							}
							testCases.add(caseResult);
						}
					}
				}
				result.setTestCases(testCases);
				return result;
			} 
			catch (Exception e){
				logger.error("Could not read result.xml", e);
			}
		}
		
		return null;
	}

	
	/**
	 * Get the assessment history for a particular user and assessment
	 * 
	 * @param username the name of the user
	 * @param assessment the assessment (for linking purposes)
	 * @return All of the assessments submitted for this user for this assessment
	 * @deprecated until no more reading from files
	 */
	@Deprecated public Collection<AssessmentResult> getAssessmentHistory(String username, Assessment assessment){
		// scan folder
		String[] allFiles = (new File(ProjectProperties
				.getInstance().getSubmissionsLocation()
				+ username
				+ "/assessments/"
				+ assessment.getId())).list();
		
		Collection<AssessmentResult> results = new LinkedList<AssessmentResult>();
		
		// if it exists
		if (allFiles != null) {
			// sort it so it's easyer to read by the user
			Arrays.sort(allFiles);
			
			// inverted order (latest should be at the top
			for(int i=allFiles.length -1; i>=0; --i){
				// load assessment result
				//results.add(loadAssessmentResultFromDisk(username, assessment, allFiles[i]));
			}
		}
		
		return results;
	}

	/**
	 * Get the hand marking result from a location
	 * 
	 * @param location the location of the hand marking result
	 * @return null if there is no hand marking result
	 * @return the result otherwise
	 */
	private HandMarkingResult getHandMarkingResult(String location) {
		HandMarkingResult result = new HandMarkingResult();
		
		try {
			// read in the file
			Scanner in = new Scanner(new File(location+"/result.txt"));
			Map<Long,Long> resultMap = new TreeMap<Long, Long>();
			while(in.hasNextLine()){
				String[] currResults = in.nextLine().split(",");
				if(currResults.length >= 2){
					resultMap.put(Long.parseLong(currResults[0]),
							Long.parseLong(currResults[1]));
				}
			}
			in.close();
			result.setResult(resultMap);
			
			return result;
		} catch (FileNotFoundException e) {
			// return null if the file doesn't exist
		}
		return null;	
	}

	
	/**
	 * Get the results of a calculated competition.
	 * 
	 * @param competitionId the id of a competition
	 * @return the results of a competition
	 */
	public CompetitionMarks getLatestCompetitionMarks(long competitionId) {
		DetachedCriteria cr = DetachedCriteria.forClass(CompetitionResult.class);
		cr.createCriteria("competition").add(Restrictions.eq("id", competitionId));
		cr.add(Restrictions.isNull("arena"));
		cr.addOrder(Order.desc("runDate"));
		@SuppressWarnings("unchecked")
		List<CompetitionMarks> results = getHibernateTemplate().findByCriteria(cr, 0, 1);
		if(results == null || results.isEmpty()) {
			return null;
		}
		return results.get(0);
	}
	
	/**
	 * Get the results of an arena.
	 * 
	 * @param competitionId the id of a competition
	 * @param arenaId the id of a competition
	 * @return the results of a competition
	 */
	public CompetitionMarks getLatestArenaMarks(long competitionId, long arenaId) {
		DetachedCriteria cr = DetachedCriteria.forClass(CompetitionResult.class);
		cr.createCriteria("competition").add(Restrictions.eq("id", competitionId));
		cr.createCriteria("arena").add(Restrictions.eq("id", arenaId));
		cr.addOrder(Order.desc("runDate"));
		@SuppressWarnings("unchecked")
		List<CompetitionMarks> results = getHibernateTemplate().findByCriteria(cr, 0, 1);
		if(results == null || results.isEmpty()) {
			return null;
		}
		return results.get(0);
	}
	
	public CompetitionResult getLatestCalculatedCompetitionResult(long competitionId) {
		DetachedCriteria cr = DetachedCriteria.forClass(CompetitionResult.class);
		cr.createCriteria("competition").add(Restrictions.eq("id", competitionId));
		cr.add(Restrictions.isNull("arena"));
		cr.addOrder(Order.desc("runDate"));
		@SuppressWarnings("unchecked")
		List<CompetitionResult> results = getHibernateTemplate().findByCriteria(cr, 0, 1);
		if(results == null || results.isEmpty()) {
			return null;
		}
		return results.get(0);
	}
	
	public CompetitionResult getLatestArenaResult(long competitionId, long arenaId) {
		DetachedCriteria cr = DetachedCriteria.forClass(CompetitionResult.class);
		cr.createCriteria("competition").add(Restrictions.eq("id", competitionId));
		cr.createCriteria("arena").add(Restrictions.eq("id", competitionId));
		cr.addOrder(Order.desc("runDate"));
		@SuppressWarnings("unchecked")
		List<CompetitionResult> results = getHibernateTemplate().findByCriteria(cr, 0, 1);
		if(results == null || results.isEmpty()) {
			return null;
		}
		return results.get(0);
	}
	
	public AssessmentResult getAssessmentResult(long id) {
		return getHibernateTemplate().get(AssessmentResult.class, id);
	}
	
	/**
	 * Save the unit test result to the database.
	 * 
	 * @param result the unit test result being saved
	 */
	public void save(UnitTestResult result) {
		getHibernateTemplate().save(result);
	}

	/**
	 * Update the unit test result in the database.
	 * 
	 * @param result the unit test result being updated
	 */
	public void update(UnitTestResult result) {
		getHibernateTemplate().update(result);
	}
	
	/**
	 * Delete the unit test result from the database.
	 * 
	 * @param result the unit test result being deleted
	 */
	public void delete(UnitTestResult result) {
		getHibernateTemplate().delete(result);
	}
	
	
	/**
	 * Save the assessment result to the database.
	 * 
	 * @param result the assessment result being saved
	 */
	public void save(AssessmentResult result) {
		getHibernateTemplate().save(result);
	}

	/**
	 * Update the assessment result in the database.
	 * 
	 * @param result the assessment result being updated
	 */
	public void update(AssessmentResult result) {
		getHibernateTemplate().saveOrUpdate(result);
	}
	
	/**
	 * Delete the assessment result from the database.
	 * 
	 * @param result assessment test result being deleted
	 */
	public void delete(AssessmentResult result) {
		getHibernateTemplate().delete(result);
	}
	
	public AssessmentResult getLatestResultsForUserAssessment(PASTAUser user, long assessmentId) {
		List<AssessmentResult> results = getResultsForUserAssessment(user, assessmentId, 1, true);
		if (results == null || results.isEmpty()) {
			return null;
		}
		return results.get(0);
	}

	public List<AssessmentResult> getAllResultsForUserAssessment(PASTAUser user, long assessmentId) {
		return getResultsForUserAssessment(user, assessmentId, -1, true);
	}
	
	public List<AssessmentResult> getAllResultsForUserAssessment(PASTAUser user, long assessmentId, boolean latestFirst) {
		return getResultsForUserAssessment(user, assessmentId, -1, latestFirst);
	}
	
	private List<AssessmentResult> getResultsForUserAssessment(PASTAUser user,
			long assessmentId, int resultCount, boolean latestFirst) {
		DetachedCriteria cr = DetachedCriteria.forClass(AssessmentResult.class);
		cr.add(Restrictions.eq("user", user));
		cr.createCriteria("assessment").add(Restrictions.eq("id", assessmentId));
		if(latestFirst) {
			cr.addOrder(Order.desc("submissionDate"));
		} else {
			cr.addOrder(Order.asc("submissionDate"));
		}
		@SuppressWarnings("unchecked")
		List<AssessmentResult> results = getHibernateTemplate().findByCriteria(cr, 0, resultCount);
		for(AssessmentResult result : results) {
			refreshHandMarking(result);
		}
		return results;
	}


	public AssessmentResult getAssessmentResult(PASTAUser user, long assessmentId, Date submissionDate) {
		DetachedCriteria cr = DetachedCriteria.forClass(AssessmentResult.class);
		cr.add(Restrictions.eq("user", user));
		cr.createCriteria("assessment").add(Restrictions.eq("id", assessmentId));
		cr.add(Restrictions.eq("submissionDate", submissionDate));
		@SuppressWarnings("unchecked")
		List<AssessmentResult> results = getHibernateTemplate().findByCriteria(cr, 0, 1);
		if(results == null || results.isEmpty()) {
			return null;
		}
		AssessmentResult result = results.get(0);
		refreshHandMarking(result);
		return result;
	}
	
	
	/**
	 * Get the latest result for a user.
	 * 
	 * @param user the user
	 * @return the map which holds the assessment results with a key which is the assessment name
	 */
	public Map<Long, AssessmentResult> getLatestResults(PASTAUser user){
		Map<Long, AssessmentResult> results = new HashMap<Long, AssessmentResult>();
		
		DetachedCriteria cr = DetachedCriteria.forClass(AssessmentResult.class);
		cr.add(Restrictions.eq("user", user));
		cr.addOrder(Order.desc("submissionDate"));
		
		@SuppressWarnings("unchecked")
		List<AssessmentResult> allResults = getHibernateTemplate().findByCriteria(cr);
		
		for(AssessmentResult result : allResults) {
			if(results.containsKey(result.getAssessment().getId())) {
				continue;
			}
			refreshHandMarking(result);
			results.put(result.getAssessment().getId(), result);
		}
		
		return results;
	}
	
	private void refreshHandMarking(AssessmentResult result) {
		List<HandMarkingResult> oldResults = new ArrayList<HandMarkingResult>(result.getHandMarkingResults());
		result.getHandMarkingResults().clear();
				
		for(WeightedHandMarking template : result.getAssessment().getHandMarking()) {
			boolean found = false;
			for(HandMarkingResult currResult : oldResults) {
				if(currResult.getWeightedHandMarking().getId() == template.getId()) {
					found = true;
					result.addHandMarkingResult(currResult);
					break;
				}
			}
			if(!found) {
				HandMarkingResult newResult = new HandMarkingResult();
				newResult.setWeightedHandMarking(template);
				ProjectProperties.getInstance().getResultDAO().saveOrUpdate(newResult);
				result.addHandMarkingResult(newResult);
			}
		}
	}

	public void delete(HandMarkingResult result) {
		getHibernateTemplate().delete(result);
	}

	public void saveOrUpdate(HandMarkingResult result) {
		getHibernateTemplate().saveOrUpdate(result);
	}
	
	public int getSubmissionCount(PASTAUser user, long assessmentId) {
		DetachedCriteria cr = DetachedCriteria.forClass(AssessmentResult.class);
		cr.setProjection(Projections.rowCount())
		.createCriteria("user").add(Restrictions.eq("id", assessmentId))
		.createCriteria("assessment").add(Restrictions.eq("id", assessmentId));
		return ((Number)getHibernateTemplate().findByCriteria(cr).get(0)).intValue();
	}

	public void loadCompetitionResults(CompetitionResult result, File file) {
		Set<CompetitionResultData> data = new TreeSet<CompetitionResultData>();
		List<ResultCategory> categories = new ArrayList<ResultCategory>();
		
		try {
			Scanner in = new Scanner(file);
			
			// adding the categories (first line)
			String[] cat = in.nextLine().split(",");
			for(int i=1; i< cat.length; ++i){
				categories.add(new ResultCategory(cat[i]));
			}
			
			while(in.hasNextLine()){
				try{
					String[] line = in.nextLine().split(",");
					for(int i=1; i< cat.length; ++i){
						data.add(new CompetitionResultData(line[0], categories.get(i).getName(), line[i]));
					}
				}
				catch(Exception e){}
			}
			
			in.close();

			result.setCategories(categories);
			result.setData(data);
		} catch (FileNotFoundException e) {
			logger.error("Could not load competition results - " + file + " did not exist.");
		}
	}

	public void loadCompetitionMarks(CompetitionMarks marks, File file) {
		List<CompetitionUserMark> userMarks = new LinkedList<CompetitionUserMark>();
		
		try {
			Scanner in = new Scanner(file);
			if(in.hasNext()) {
				String[] parts = in.nextLine().split(",");
				if(parts.length != 2) {
					logger.error("Incorrect number of columns in marks file " + file);
					in.close();
					return;
				}
				
				// ignore header if it exists
				try {
					double mark = Double.parseDouble(parts[1]);
					userMarks.add(new CompetitionUserMark(parts[0], mark));
				} catch (NumberFormatException nfe) {
				}
			}
			
			while(in.hasNextLine()){
				String[] parts = in.nextLine().split(",");
				try {
					double mark = Double.parseDouble(parts[1]);
					userMarks.add(new CompetitionUserMark(parts[0], mark));
				} catch (NumberFormatException nfe) {
				}
			}
			
			in.close();

			marks.updatePositions(userMarks);
		} catch (FileNotFoundException e) {
			logger.error("Could not load competition marks - " + file + " did not exist.");
		}
	}

	public void saveOrUpdate(CompetitionResult compResult) {
		getHibernateTemplate().saveOrUpdate(compResult);
	}

	public void saveOrUpdate(CompetitionMarks compMarks) {
		getHibernateTemplate().saveOrUpdate(compMarks);
	}

	public File getLastestSubmission(PASTAUser user, Assessment assessment) {
		DetachedCriteria cr = DetachedCriteria.forClass(AssessmentResult.class);
		cr.setProjection(Projections.property("submissionDate"));
		cr.add(Restrictions.eq("username", user.getUsername()));
		cr.createCriteria("assessment").add(Restrictions.eq("id", assessment.getId()));
		cr.addOrder(Order.desc("submissionDate"));
		@SuppressWarnings("unchecked")
		List<Date> results = getHibernateTemplate().findByCriteria(cr, 0, 1);
		if(results == null || results.isEmpty()) {
			return null;
		}
		Date subDate = results.get(0);
		return new File(ProjectProperties.getInstance().getSubmissionsLocation() + "assessments/" + assessment.getId() + "/" + subDate + "/submission");
	}
}
