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
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pasta.domain.result.AssessmentResult;
import pasta.domain.result.AssessmentResultSummary;
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
import pasta.domain.user.PASTAGroup;
import pasta.domain.user.PASTAUser;
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
@Transactional
@Repository("resultDAO")
public class ResultDAO{
	
	protected final Log logger = LogFactory.getLog(getClass());
	
	@Autowired
	private SessionFactory sessionFactory;
	
	/**
	 * Delete the assessment result from the database.
	 * 
	 * @param result assessment test result being deleted
	 */
	public void delete(AssessmentResult result) {
		sessionFactory.getCurrentSession().delete(result);
	}

	public void delete(HandMarkingResult result) {
		sessionFactory.getCurrentSession().delete(result);
	}
	
	/**
	 * Delete the unit test result from the database.
	 * 
	 * @param result the unit test result being deleted
	 */
	public void delete(UnitTestResult result) {
		sessionFactory.getCurrentSession().delete(result);
	}
	
	public AssessmentResult getAssessmentResult(long id) {
		return (AssessmentResult) sessionFactory.getCurrentSession().get(AssessmentResult.class, id);
	}
	
	public AssessmentResultSummary getAssessmentResultSummary(PASTAUser user, Assessment assessment) {
		Criteria cr = sessionFactory.getCurrentSession().createCriteria(AssessmentResultSummary.class);
		cr.add(Restrictions.eq("id.assessment", assessment));
		cr.add(Restrictions.eq("id.user", user));

		@SuppressWarnings("unchecked")
		AssessmentResultSummary result = (AssessmentResultSummary) DataAccessUtils.uniqueResult(cr.list());
		return result;
	}

	public int getSubmissionCount(PASTAUser user, long assessmentId, boolean includeGroup, boolean includeCompileErrors) {
		Criteria cr = sessionFactory.getCurrentSession().createCriteria(AssessmentResult.class);
		cr.createCriteria("assessment").add(Restrictions.eq("id", assessmentId));
		restrictCriteriaUser(cr, user, includeGroup, assessmentId);
		
		if(includeCompileErrors) {
			cr.setProjection(Projections.rowCount());
			return DataAccessUtils.intResult(cr.list());
		}
		
		@SuppressWarnings("unchecked")
		List<AssessmentResult> results = cr.list();
		int count = 0;
		for(AssessmentResult result : results) {
			if(!result.isError()) {
				count++;
			}
		}
		return count;
	}
	
	public AssessmentResult getResult(PASTAUser user, long assessmentId, Date submissionDate, boolean includeGroup) {
		Criteria cr = sessionFactory.getCurrentSession().createCriteria(AssessmentResult.class);
		cr.createCriteria("assessment").add(Restrictions.eq("id", assessmentId));
		cr.add(Restrictions.eq("submissionDate", submissionDate));
		restrictCriteriaUser(cr, user, includeGroup, assessmentId);
		
		@SuppressWarnings("unchecked")
		AssessmentResult result = (AssessmentResult) DataAccessUtils.uniqueResult(cr.list());
		if(result != null) {
			refreshHandMarking(result);
		}
		return result;
	}
	
	public AssessmentResult getLatestIndividualResult(PASTAUser user, long assessmentId) {
		Criteria cr = sessionFactory.getCurrentSession().createCriteria(AssessmentResult.class);
		cr.createCriteria("assessment").add(Restrictions.eq("id", assessmentId));
		cr.addOrder(Order.desc("submissionDate"));
		restrictCriteriaUser(cr, user, false, assessmentId);
		cr.setMaxResults(1);
		
		@SuppressWarnings("unchecked")
		AssessmentResult result = (AssessmentResult) DataAccessUtils.uniqueResult(cr.list());
		if(result != null) {
			refreshHandMarking(result);
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public AssessmentResult getLatestGroupResult(PASTAUser user, long assessmentId) {
		Criteria cr = sessionFactory.getCurrentSession().createCriteria(AssessmentResult.class);
		cr.createCriteria("assessment").add(Restrictions.eq("id", assessmentId));
		cr.addOrder(Order.desc("submissionDate"));
		restrictCriteriaUser(cr, user, true, assessmentId);
		
		DetachedCriteria groupCr = DetachedCriteria.forClass(PASTAGroup.class);
		groupCr.setProjection(Projections.property("id"));
		groupCr.createCriteria("assessment").add(Restrictions.eq("id", assessmentId));
		groupCr.createAlias("members", "member");
		groupCr.add(Restrictions.eq("member.id", user.getId()));
		
		cr.add(Subqueries.propertyEq("user.id", groupCr));
		cr.setMaxResults(1);
		
		AssessmentResult result = (AssessmentResult) DataAccessUtils.uniqueResult(cr.list());
		if(result != null) {
			refreshHandMarking(result);
		}
		return result;
	}
	
	public List<AssessmentResult> getAllResults(PASTAUser user, long assessmentId, boolean latestFirst, boolean includeGroup) {
		Criteria cr = sessionFactory.getCurrentSession().createCriteria(AssessmentResult.class);
		if(user != null) {
			restrictCriteriaUser(cr, user, includeGroup, assessmentId);
		}
		if(assessmentId > 0) {
			cr.createCriteria("assessment").add(Restrictions.eq("id", assessmentId));
		}
		if(latestFirst) {
			cr.addOrder(Order.desc("submissionDate"));
		} else {
			cr.addOrder(Order.asc("submissionDate"));
		}
		@SuppressWarnings("unchecked")
		List<AssessmentResult> results = cr.list();
		if(results != null) {
			for(AssessmentResult result : results) {
				refreshHandMarking(result);
			}
		}
		return results;
	}
	
	@SuppressWarnings("unchecked")
	public List<Date> getAllSubmissionDates(PASTAUser user, long assessmentId, boolean latestFirst, boolean includeGroup) {
		Criteria cr = sessionFactory.getCurrentSession().createCriteria(AssessmentResult.class);
		if(user != null) {
			restrictCriteriaUser(cr, user, includeGroup, assessmentId);
		}
		if(assessmentId > 0) {
			cr.createCriteria("assessment").add(Restrictions.eq("id", assessmentId));
		}
		if(latestFirst) {
			cr.addOrder(Order.desc("submissionDate"));
		} else {
			cr.addOrder(Order.asc("submissionDate"));
		}
		cr.setProjection(Projections.property("submissionDate"));
		return cr.list();
	}
	
	public List<AssessmentResult> getResultsForMultiUserAssessment(List<PASTAUser> users,
			long assessmentId, int resultCount, boolean latestFirst) {
		Criteria cr = sessionFactory.getCurrentSession().createCriteria(AssessmentResult.class);
		if(users.isEmpty()) {
			return new LinkedList<AssessmentResult>();
		}
		if(users.size() == 1) {
			cr.add(Restrictions.eq("user", users.get(0)));
		} else {
			cr.add(Restrictions.in("user", users));
		}
		if(assessmentId > 0) {
			cr.createCriteria("assessment").add(Restrictions.eq("id", assessmentId));
		}
		if(latestFirst) {
			cr.addOrder(Order.desc("submissionDate"));
		} else {
			cr.addOrder(Order.asc("submissionDate"));
		}
		cr.setMaxResults(resultCount);
		@SuppressWarnings("unchecked")
		List<AssessmentResult> results = cr.list();
		if(results != null) {
			for(AssessmentResult result : results) {
				refreshHandMarking(result);
			}
		}
		return results;
	}
	
		@SuppressWarnings("unchecked")
		public List<AssessmentResult> getLatestResultsForMultiUser(List<PASTAUser> users) {
			if(users.isEmpty()) {
				return new LinkedList<AssessmentResult>();
			}

			DetachedCriteria latestSub = DetachedCriteria.forClass(AssessmentResult.class);
			latestSub.setProjection(
					Projections.projectionList()
					.add(Projections.groupProperty("user"))
					.add(Projections.groupProperty("assessment"))
					.add(Projections.max("submissionDate"))
					);
			latestSub.add(Restrictions.in("user", users));

			Criteria cr = sessionFactory.getCurrentSession().createCriteria(AssessmentResult.class)
					.add(Subqueries.propertiesIn(new String[] {"user", "assessment", "submissionDate"}, latestSub));

			return cr.list();
		}

		public List<AssessmentResultSummary> getResultsSummaryForMultiUser(Set<PASTAUser> users) {
			if(users.isEmpty()) {
				return new LinkedList<AssessmentResultSummary>();
			}
		
			Criteria cr = sessionFactory.getCurrentSession().createCriteria(AssessmentResultSummary.class)
					.add(Restrictions.in("id.user", users));

			@SuppressWarnings("unchecked")
			List<AssessmentResultSummary> list = (List<AssessmentResultSummary>)cr.list();
			return list;
		}

	private void restrictCriteriaUser(Criteria cr, PASTAUser user, boolean includeGroup, long assessmentId) {
		if(includeGroup) {
			DetachedCriteria groupCr = DetachedCriteria.forClass(PASTAGroup.class);
			groupCr.setProjection(Projections.property("id"));
			if(assessmentId > 0) {
				groupCr.createCriteria("assessment").add(Restrictions.eq("id", assessmentId));
			}
			groupCr.createAlias("members", "member");
			groupCr.add(Restrictions.eq("member.id", user.getId()));
			Criterion userGroupCr = assessmentId > 0 ?
					Subqueries.propertyEq("user.id", groupCr) :
					Subqueries.propertyIn("user.id", groupCr);
			cr.add(Restrictions.or(Restrictions.eq("user", user), userGroupCr));
		} else {
			cr.add(Restrictions.eq("user", user));
		}
	}
	
	public File getLastestSubmission(PASTAUser user, Assessment assessment) {
		Criteria cr = sessionFactory.getCurrentSession().createCriteria(AssessmentResult.class);
		cr.setProjection(Projections.property("submissionDate"));
		cr.createCriteria("user").add(Restrictions.eq("id", user.getId()));
		cr.createCriteria("assessment").add(Restrictions.eq("id", assessment.getId()));
		cr.addOrder(Order.desc("submissionDate"));
		cr.setMaxResults(1);
		@SuppressWarnings("unchecked")
		List<Date> results = cr.list();
		if(results == null || results.isEmpty()) {
			return null;
		}
		Date subDate = results.get(0);
		return new File(ProjectProperties.getInstance().getSubmissionsLocation() + "assessments/" + assessment.getId() + "/" + subDate + "/submission");
	}
	
	/**
	 * Get the results of an arena.
	 * 
	 * @param competitionId the id of a competition
	 * @param arenaId the id of a competition
	 * @return the results of a competition
	 */
	public CompetitionMarks getLatestArenaMarks(long competitionId, long arenaId) {
		Criteria cr = sessionFactory.getCurrentSession().createCriteria(CompetitionResult.class);
		cr.createCriteria("competition").add(Restrictions.eq("id", competitionId));
		cr.createCriteria("arena").add(Restrictions.eq("id", arenaId));
		cr.addOrder(Order.desc("runDate"));
		cr.setMaxResults(1);
		@SuppressWarnings("unchecked")
		List<CompetitionMarks> results = cr.list();
		if(results == null || results.isEmpty()) {
			return null;
		}
		return results.get(0);
	}
	
	public CompetitionResult getLatestArenaResult(long competitionId, long arenaId) {
		Criteria cr = sessionFactory.getCurrentSession().createCriteria(CompetitionResult.class);
		cr.createCriteria("competition").add(Restrictions.eq("id", competitionId));
		cr.createCriteria("arena").add(Restrictions.eq("id", competitionId));
		cr.addOrder(Order.desc("runDate"));
		cr.setMaxResults(1);
		@SuppressWarnings("unchecked")
		List<CompetitionResult> results = cr.list();
		if(results == null || results.isEmpty()) {
			return null;
		}
		return results.get(0);
	}
	
	public CompetitionResult getLatestCalculatedCompetitionResult(long competitionId) {
		Criteria cr = sessionFactory.getCurrentSession().createCriteria(CompetitionResult.class);
		cr.createCriteria("competition").add(Restrictions.eq("id", competitionId));
		cr.add(Restrictions.isNull("arena"));
		cr.addOrder(Order.desc("runDate"));
		cr.setMaxResults(1);
		@SuppressWarnings("unchecked")
		List<CompetitionResult> results = cr.list();
		if(results == null || results.isEmpty()) {
			return null;
		}
		return results.get(0);
	}
	
	/**
	 * Get the results of a calculated competition.
	 * 
	 * @param competitionId the id of a competition
	 * @return the results of a competition
	 */
	public CompetitionMarks getLatestCompetitionMarks(long competitionId) {
		Criteria cr = sessionFactory.getCurrentSession().createCriteria(CompetitionResult.class);
		cr.createCriteria("competition").add(Restrictions.eq("id", competitionId));
		cr.add(Restrictions.isNull("arena"));
		cr.addOrder(Order.desc("runDate"));
		cr.setMaxResults(1);
		@SuppressWarnings("unchecked")
		List<CompetitionMarks> results = cr.list();
		if(results == null || results.isEmpty()) {
			return null;
		}
		return results.get(0);
	}
	
	
	
	/**
	 * Load a unit test result from a location
	 * 
	 * @param location the location of the test
	 * @return the result of the unit test
	 */
	public UnitTestResult getUnitTestResultFromDisk(String location){
		return getUnitTestResultFromDisk(location, null);
	}
	
	/**
	 * Load a unit test result from a location, including error line numbers in
	 * the 'type' if the error occurred in one of the files listed in the given
	 * context.
	 * 
	 * @param location the location of the test
	 * @param errorContext a list of file names to check for error context
	 * @return the result of the unit test
	 */
	public UnitTestResult getUnitTestResultFromDisk(String location, Collection<String> errorContext){
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
							caseResult.setTestResult(UnitTestCaseResult.PASS);
							
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
								if(getText(failedUnitTestElement) != null){
									String message = getText(failedUnitTestElement);
									caseResult.setExtendedMessage(message);
									
									// Include error line number for Java submissions
									if(errorContext != null && caseResult.getType() != null && caseResult.isError()) {
										String find = "(\\(.+?\\.java:[0-9]+\\))";
										Pattern p = Pattern.compile(find);
										Matcher m = p.matcher(message);
										boolean found = false;
										while(m.find() && !found) {
											String line = m.group();
											for(String file : errorContext) {
												if(line.contains(file)) {
													caseResult.setType(caseResult.getType() + " at " + line);
													found = true;
													break;
												}
											}
										}
									}
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
	
	private void refreshHandMarking(AssessmentResult result) {
		List<HandMarkingResult> oldResults = new ArrayList<HandMarkingResult>(result.getHandMarkingResults());
		result.getHandMarkingResults().clear();
				
		int same = 0;
		int initSize = oldResults.size();
		for(WeightedHandMarking template : result.getAssessment().getHandMarking()) {
			if(result.isGroupResult() != template.isGroupWork()) {
				continue;
			}
			
			boolean found = false;
			for(HandMarkingResult currResult : oldResults) {
				if(currResult.getWeightedHandMarking().getId() == template.getId()) {
					found = true;
					result.addHandMarkingResult(currResult);
					same++;
					break;
				}
			}
			if(!found) {
				HandMarkingResult newResult = new HandMarkingResult();
				newResult.setWeightedHandMarking(template);
				saveOrUpdate(newResult);
				result.addHandMarkingResult(newResult);
			}
		}
		
		if(same != initSize || result.getHandMarkingResults().size() != initSize) {
			update(result);
		}
	}

	/**
	 * Save the assessment summary to the database.
	 *
	 * @param result the assessment summary being saved
	 */
	public void saveOrUpdate(AssessmentResultSummary result) {
		sessionFactory.getCurrentSession().saveOrUpdate(result);
	}

	/**
	 * Save the assessment result to the database.
	 * 
	 * @param result the assessment result being saved
	 */
	public void save(AssessmentResult result) {
		sessionFactory.getCurrentSession().save(result);
	}

	/**
	 * Save the unit test result to the database.
	 * 
	 * @param result the unit test result being saved
	 */
	public void save(UnitTestResult result) {
		sessionFactory.getCurrentSession().save(result);
	}
	
	public void saveOrUpdate(CompetitionMarks compMarks) {
		sessionFactory.getCurrentSession().saveOrUpdate(compMarks);
	}

	public void saveOrUpdate(CompetitionResult compResult) {
		sessionFactory.getCurrentSession().saveOrUpdate(compResult);
	}

	public void saveOrUpdate(HandMarkingResult result) {
		sessionFactory.getCurrentSession().saveOrUpdate(result);
	}

	/**
	 * Update the assessment result in the database.
	 * 
	 * @param result the assessment result being updated
	 */
	public void update(AssessmentResult result) {
		Session session = sessionFactory.getCurrentSession();
		session.saveOrUpdate(result);
		session.flush();
		session.clear();
	}

	/**
	 * Update the unit test result in the database.
	 * 
	 * @param result the unit test result being updated
	 */
	public void update(UnitTestResult result) {
		sessionFactory.getCurrentSession().update(result);
	}

	public void unlinkUnitTest(long id) {
		Session session = sessionFactory.getCurrentSession();
		@SuppressWarnings("unchecked")
		List<AssessmentResult> results = session.createCriteria(AssessmentResult.class)
				.createCriteria("unitTests", "utResult")
				.createCriteria("utResult.test", "test")
				.add(Restrictions.eq("test.id", id))
				.list();
		for(AssessmentResult result : results) {
			Iterator<UnitTestResult> utIt = result.getUnitTests().iterator();
			while(utIt.hasNext()) {
				UnitTestResult utResult = utIt.next();
				if(utResult.getTest().getId() == id) {
					utResult.setTest(null);
					session.update(utResult);
					utIt.remove();
				}
			}
			session.update(result);
		}
	}

	@SuppressWarnings("unchecked")
	public List<AssessmentResult> getWaitingResults() {
		return sessionFactory.getCurrentSession()
				.createCriteria(AssessmentResult.class)
				.add(Restrictions.eq("waitingToRun", true))
				.list();
	}

	private static String getText(Element element) {
		StringBuffer stringBuffer = new StringBuffer();
		NodeList elementChildren = element.getChildNodes();
		boolean found = false;
		for (int i = 0; i < elementChildren.getLength(); i++) {
		  Node node = elementChildren.item(i);
		  if (node.getNodeType() == Node.TEXT_NODE) {
		    stringBuffer.append(node.getNodeValue());
		    found = true;
		  }
		}
		return found ? stringBuffer.toString() : null;
	}
}
