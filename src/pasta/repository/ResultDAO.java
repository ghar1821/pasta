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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pasta.domain.result.ArenaResult;
import pasta.domain.result.AssessmentResult;
import pasta.domain.result.CompetitionResult;
import pasta.domain.result.HandMarkingResult;
import pasta.domain.result.PASTACompUserResult;
import pasta.domain.result.UnitTestCaseResult;
import pasta.domain.result.UnitTestResult;
import pasta.domain.template.Assessment;
import pasta.domain.template.Competition;
import pasta.domain.template.WeightedHandMarking;
import pasta.domain.template.WeightedUnitTest;
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
	// username, assessment, date
	Map<String, Map<Long, AssessmentResult>> results;
	Map<String, CompetitionResult> competitionResults;
	
	// Default required by hibernate
	@Autowired
	public void setMySession(SessionFactory sessionFactory) {
		setSessionFactory(sessionFactory);
	}

	
	/**
	 * loads up all of the latest results
	 * @param assDao for linking the results with their respective
	 * assessments. This is done to allow modifications to the assessment
	 * to dynamically change the marks.
	 */
	public void init(AssessmentDAO assDao){
		loadAssessmentHistoryFromFile(assDao);
		loadCompetitionsFromFile(assDao);
	}

	/**
	 * Load a the results of a competition from disk.
	 * 
	 * @param  assDao for linking the results with their respective
	 * assessments. This is done to allow modifications to the assessment
	 * to dynamically change the marks.
	 */
	private void loadCompetitionsFromFile(AssessmentDAO assDao) {
		Collection<Competition> comps = assDao.getCompetitionList();
		competitionResults = new TreeMap<String, CompetitionResult>();
		
		for(Competition comp: comps){
			CompetitionResult result = new CompetitionResult();
			List<PASTACompUserResult> compUserResult = new LinkedList<PASTACompUserResult>();
			
			// get latest
			String[] allFiles = (new File(ProjectProperties
					.getInstance().getCompetitionsLocation()
					+ comp.getShortName()
					+ "/competition/")).list();
			
			if (allFiles != null) {
				Arrays.sort(allFiles);
				try{
					Scanner in = new Scanner(new File(ProjectProperties
						.getInstance().getCompetitionsLocation()
						+ comp.getShortName()
						+ "/competition/"
						+ allFiles[allFiles.length -1]
						+ "/marks.csv"));
					
					while(in.hasNextLine()){
						try{
							String line = in.nextLine();
							PASTACompUserResult user = new PASTACompUserResult(line.split(",")[0].trim(), Double.parseDouble(line.split(",")[1].trim()));
							compUserResult.add(user);
						}
						catch(Exception e){
							logger.error(e);
						}
					}
					in.close();
				}
				catch(Exception e){
					logger.error(e);
				}
			}
			
			result.updatePositions(compUserResult);
			competitionResults.put(comp.getShortName(), result);
		}
	}

	/**
	 * Load a unit test result from a location
	 * 
	 * @param location the location of the test
	 * @return the result of the unit test
	 */
	public UnitTestResult getUnitTestResult(String location){
		UnitTestResult result = new UnitTestResult();
		
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
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				logger.error("Could not read result.xml" + System.getProperty("line.separator")+sw.toString());
			}
		}
		
		// check to see if there is a compile.errors file
		File compileErrors = new File(location+"/compile.errors");
		if(compileErrors.exists() && compileErrors.length() != 0){
			try{
				// read in
				Scanner in = new Scanner (compileErrors);
				String input = "";
				while(in.hasNextLine()){
					input+=in.nextLine() + System.getProperty("line.separator");
				}
				// set 
				result.setCompileErrors(input);
				in.close();
				
				// return
				return result;
			}
			catch(Exception e){
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				logger.error("Could not read compile.errors" + System.getProperty("line.separator")+sw.toString());
			}
		}
		
		// check to see if there is a run.errors file
		File runErrors = new File(location+"/run.errors");
		if(runErrors.exists() && runErrors.length() != 0){
			try{
				// read in
				Scanner in = new Scanner (runErrors);
				String input = "";
				while(in.hasNextLine()){
					input+=in.nextLine() + System.getProperty("line.separator");
				}
				// set 
				result.setRuntimeErrors(input);
				in.close();
				
				// return
				return result;
			}
			catch(Exception e){
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				logger.error("Could not read run.errors" + System.getProperty("line.separator")+sw.toString());
			}
		}
		return null; // TODO check if in the database
	}
	
	/**
	 * Get the latest result for a user.
	 * 
	 * @param username the name of the user
	 * @return the map which holds the assessment results with a key which is the assessment name
	 */
	public Map<Long, AssessmentResult> getLatestResults(String username){
		return results.get(username);
	}
	
	/**
	 * Get the assessment history for a particular user and assessment
	 * 
	 * @param username the name of the user
	 * @param assessment the assessment (for linking purposes)
	 * @return All of the assessments submitted for this user for this assessment
	 */
	public Collection<AssessmentResult> getAssessmentHistory(String username, Assessment assessment){
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
				results.add(loadAssessmentResultFromDisk(username, assessment, allFiles[i]));
			}
		}
		
		return results;
	}
	
	/**
	 * Load the assessment history from file.
	 * 
	 * Done at startup.
	 * @param assDao the Assessment Data Access Object
	 */
	private void loadAssessmentHistoryFromFile(AssessmentDAO assDao){
		results = new TreeMap<String, Map<Long, AssessmentResult>>();
		
		// scan all users
		String[] allUsers = (new File(ProjectProperties.getInstance()
				.getSubmissionsLocation())).list();
		if (allUsers != null && allUsers.length > 0) {
			for (String currUser : allUsers) {
				// scan all assessments
				Collection<Assessment> allAssessments = assDao
						.getAssessmentList();

				Map<Long, AssessmentResult> currUserResults = new TreeMap<Long, AssessmentResult>();
				for (Assessment assessment : allAssessments) {
					// scan all submissions

					// find latest
					String latest = null;
					if(new File(ProjectProperties
							.getInstance().getSubmissionsLocation()
							+ currUser
							+ "/assessments/"
							+ assessment.getId()
							+ "latestOverride.txt").exists()){
						// overriden latest
						Scanner in;
						try {
							in = new Scanner (new File(ProjectProperties
								.getInstance().getSubmissionsLocation()
								+ currUser
								+ "/assessments/"
								+ assessment.getId()
								+ "latestOverride.txt"));
							latest = in.nextLine().trim();
							in.close();
						} catch (FileNotFoundException e) {
							// don't care
						}
						
					}
					
					String[] allFiles = (new File(ProjectProperties
							.getInstance().getSubmissionsLocation()
							+ currUser
							+ "/assessments/"
							+ assessment.getId())).list();
					
					if (allFiles != null && allFiles.length > 0) {
						Arrays.sort(allFiles);
						
						String temporalLatest = allFiles[allFiles.length-1];

						// ensure that the latest is in the list
						if(latest != null){
							for(String file: allFiles){
								if(file.trim().equals(latest.trim())){
									temporalLatest = null;
									break;
								}
							}
						}
						
						if(temporalLatest != null){
							latest = temporalLatest;
						}
					}
					else{
						latest = null;
					}
					
					currUserResults.put(assessment.getId(),
							loadAssessmentResultFromDisk(currUser, assessment, latest));
				}
				results.put(currUser, currUserResults);
			}
		}
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
	 * Get the assessment result for a particular user, for a particular assessment, for a particular date
	 * 
	 * @param username the name of the user
	 * @param assessment the assessment (needed for lookup and linking)
	 * @param assessmentDate the date of the submission
	 * @return the assessment or null if it does not exist
	 */
	public AssessmentResult getAsssessmentResult(String username,
			Assessment assessment, String assessmentDate) {
		// check if it's the latest
		try{
			if(results.get(username).get(assessment.getId()).getFormattedSubmissionDate().equals(assessmentDate)){
				return results.get(username).get(assessment.getId());
			}
		}
		catch(Exception e){
			// do nothing
		}
		return loadAssessmentResultFromDisk(username, assessment, assessmentDate);
	}

	/**
	 * Save the hand marking to a file
	 * 
	 * @param username the name of the user
	 * @param assessmentId the assessment id
	 * @param assessmentDate the date of the submission (format yyyy-MM-dd'T'HH-mm-ss)
	 * @param handMarkingResults the results of the hand marking
	 */
	public void saveHandMarkingToFile(String username, long assessmentId,
			String assessmentDate, List<HandMarkingResult> handMarkingResults) {
		 
		for(HandMarkingResult result: handMarkingResults){
			String location = ProjectProperties.getInstance()
					.getSubmissionsLocation()
					+ username
					+ "/assessments/"
					+ assessmentId
					+ "/"
					+ assessmentDate
					+ "/handMarking/" + result.getId();
			// create the directory
			(new File(location)).mkdirs();
			// save data
			try {
				PrintWriter out = new PrintWriter(new File(location
									+ "/result.txt"));
				for(Entry<Long, Long> entry: result.getResult().entrySet()){
					out.println(entry.getKey() + "," + entry.getValue());
				}
				out.close();
			} catch (FileNotFoundException e) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				logger.error("Could not save hand marking submission " + location + System.getProperty("line.separator")+ sw.toString());
			}
		}
	}
	
	/**
	 * Load a particular assessment form disk
	 * 
	 * @param username the name of the user
	 * @param assessment the assessment (needed for lookup and linking)
	 * @param assessmentDate the date of the submission
	 * @return the assessment results
	 * @return null if there is no assessment result for that user, assessment, date combination
	 */
	public AssessmentResult loadAssessmentResultFromDisk(String username, 
			Assessment assessment, String assessmentDate) {

		AssessmentResult assessResult = null;
		if ((new File(ProjectProperties.getInstance().getSubmissionsLocation() + username + "/assessments/"
				+ assessment.getId() + "/" + assessmentDate)).exists()) {
			assessResult = new AssessmentResult();
			assessResult.setAssessment(assessment);

			// unit tests;
			ArrayList<UnitTestResult> utresults = new ArrayList<UnitTestResult>();
			for (WeightedUnitTest uTest : assessment.getUnitTests()) {
				UnitTestResult result = getUnitTestResult(ProjectProperties
						.getInstance().getSubmissionsLocation()
						+ username
						+ "/assessments/"
						+ assessment.getId()
						+ "/"
						+ assessmentDate
						+ "/unitTests/" + uTest.getTest().getId());
				if (result == null) {
					result = new UnitTestResult();
				}
				result.setTest(uTest.getTest());
				utresults.add(result);

			}
			
			// secret unit tests;
			for (WeightedUnitTest uTest : assessment.getSecretUnitTests()) {
				UnitTestResult result = getUnitTestResult(ProjectProperties
						.getInstance().getSubmissionsLocation()
						+ username
						+ "/assessments/"
						+ assessment.getId()
						+ "/"
						+ assessmentDate
						+ "/unitTests/" + uTest.getTest().getId());
				if (result == null) {
					result = new UnitTestResult();
				}
				result.setSecret(true);
				result.setTest(uTest.getTest());
				utresults.add(result);
			}

			// handMarking
			ArrayList<HandMarkingResult> handResults = new ArrayList<HandMarkingResult>();
			for (WeightedHandMarking hMarking : assessment.getHandMarking()) {
				HandMarkingResult result = getHandMarkingResult(ProjectProperties
						.getInstance().getSubmissionsLocation()
						+ username
						+ "/assessments/"
						+ assessment.getId()
						+ "/"
						+ assessmentDate
						+ "/handMarking/"
						+ hMarking.getHandMarking().getId());
				if (result == null) {
					result = new HandMarkingResult();
				} 
				result.setMarkingTemplate(hMarking.getHandMarking());
				handResults.add(result);
			}

			// submission date
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss");
			try {
				assessResult.setSubmissionDate(sdf.parse(assessmentDate));
			} catch (ParseException e) {
				assessResult.setSubmissionDate(new Date());
				logger.error("Submission date " + assessmentDate + " - "
						+ username + " - " + assessment.getName());
			}
			
			if(assessment.isCountUncompilable() || assessment.getUnitTests().isEmpty()){
				assessResult.setSubmissionsMade(new File(ProjectProperties.getInstance().getSubmissionsLocation() + username + "/assessments/"
					+ assessment.getId() + "/").list().length);
			}else{
				File[] allSubmissions = new File(ProjectProperties.getInstance().getSubmissionsLocation() + username + "/assessments/"
						+ assessment.getId()).listFiles();
				
				int numSubmissionsMade = 0;
				if(allSubmissions!=null)for(File submission: allSubmissions){
					File[] tests = new File(submission.getAbsolutePath()+"/unitTests/").listFiles();
					if(tests != null)for(File test: tests){
						File[] files = test.listFiles();
						boolean found = false;
						if(files!=null)for(File file: files){
							if(file.getAbsolutePath().endsWith("result.xml")){
								++numSubmissionsMade;
								found = true;
								break;
							}
						}
						if(found){
							break;
						}
					}
				}
				assessResult.setSubmissionsMade(numSubmissionsMade);
			}
			
			// comments
			try {
				Scanner in = new Scanner(new File(ProjectProperties
						.getInstance().getSubmissionsLocation()
						+ username
						+ "/assessments/"
						+ assessment.getId()
						+ "/"
						+ assessmentDate
						+ "/comments.txt"));
				String comments = "";
				while (in.hasNextLine()) {
					comments += in.nextLine()
							+ System.getProperty("line.separator");
				}
				in.close();
				assessResult.setComments(comments);
			} catch (FileNotFoundException e) {
			}

			assessResult.setUnitTests(utresults);
			assessResult.setHandMarkingResults(handResults);
		}
		return assessResult;
	}

	/**
	 * Method to save the hand marking comments
	 * 
	 * @param username the name of the user
	 * @param assessmentId the assessment id
	 * @param assessmentDate the date of the submission
	 * @param comments the comments about the submission
	 */
	public void saveHandMarkingComments(String username, long assessmentId,
			String assessmentDate, String comments) {
		// save to file
		try {
			PrintWriter out = new PrintWriter(new File(ProjectProperties.getInstance().getSubmissionsLocation()
					+ username
					+ "/assessments/"
					+ assessmentId
					+ "/"
					+ assessmentDate
					+ "/comments.txt"));
			out.print(comments);
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// update if latest
		try{
			if(results.get(username).get(assessmentId).getFormattedSubmissionDate().equals(assessmentDate)){
				results.get(username).get(assessmentId).setComments(comments);
			}
		}
		catch(Exception e){
			// do nothing
		}
	}

	/**
	 * Update unit test results
	 * 
	 * If the result is the latest, reload it from file. Otherwise ignore it (it will get loaded later)
	 * 
	 * @param username the name of the user
	 * @param assessment the assessment (needed for lookup and linking)
	 * @param runDate the date of the submission
	 */
	public void updateUnitTestResults(String username,
			Assessment assessment, Date runDate) {
		// check if latest
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss");
		if(results.get(username)==null){
			results.put(username, new TreeMap<Long, AssessmentResult>());
		}
		if(results.get(username).get(assessment.getId()) == null ||
				results.get(username).get(assessment.getId()).getSubmissionDate().before(runDate) || 
				results.get(username).get(assessment.getId()).getSubmissionDate().equals(runDate)){
			results.get(username).put(assessment.getId(), loadAssessmentResultFromDisk(username, assessment, sdf.format(runDate)));
		}
	}

	/**
	 * Get the results of a competition from cache.
	 * 
	 * @param competitionName the short name (no whitespace) of a competition
	 * @return the results of a competition
	 */
	public CompetitionResult getCompetitionResult(String competitionName) {
		return competitionResults.get(competitionName);
	}
	
	/**
	 * Update the results of a competition
	 * <p>
	 * Read from file into cache.
	 * 
	 * @param compName the name of the competition
	 * @param filename the name of the file being written
	 */
	private void updateCompetitionResults(String compName, String filename){
		List<PASTACompUserResult> compUserResult = new LinkedList<PASTACompUserResult>();

		try{
			Scanner in = new Scanner(new File(filename));
			
			while(in.hasNextLine()){
				try{
					String line = in.nextLine();
					PASTACompUserResult user = new PASTACompUserResult(line.split(",")[0].trim(), Double.parseDouble(line.split(",")[1].trim()));
					compUserResult.add(user);
				}
				catch(Exception e){
					logger.error(e);
				}
			}
			in.close();
			
			CompetitionResult result = competitionResults.get(compName);
			if(result == null){
				result = new CompetitionResult();
				competitionResults.put(compName, result);
			}
			result.updatePositions(compUserResult);
		}
		catch(Exception e){
			logger.error(e);
		}
	}

	/**
	 * Updated the results of a calculated competition
	 * <p>
	 * Read from file into cache.
	 * Calls {@link #updateCompetitionResults(String, String)}
	 * 
	 * @param compName the name of the competition
	 */
	public void updateCalculatedCompetitionResults(String compName) {
		
		// get latest
		String[] allFiles = (new File(ProjectProperties
				.getInstance().getCompetitionsLocation()
				+ compName
				+ "/competition/")).list();
		
		if (allFiles != null) {
			Arrays.sort(allFiles);
				
			updateCompetitionResults(compName, ProjectProperties
				.getInstance().getCompetitionsLocation()
				+ compName
				+ "/competition/"
				+ allFiles[allFiles.length -1]
				+ "/marks.csv");
		}
	}
	
	/**
	 * Updated the results of an arena based competition
	 * <p>
	 * Read from file into cache.
	 * Calls {@link #updateCompetitionResults(String, String)}
	 * 
	 * @param compName the name of the competition
	 */
	public void updateArenaCompetitionResults(String compName) {
		
		// get latest
		String[] allFiles = (new File(ProjectProperties
				.getInstance().getCompetitionsLocation()
				+ compName
				+ "/arenas/Official Arena/")).list();
		
		if (allFiles != null) {
			Arrays.sort(allFiles);
				
			updateCompetitionResults(compName, ProjectProperties
				.getInstance().getCompetitionsLocation()
				+ compName
				+ "/arenas/Official Arena/"
				+ allFiles[allFiles.length -1]
				+ "/marks.csv");
		}
	}
	
	/**
	 * Get the results of a calculated competition.
	 * <p>
	 * Not sure if this method returns the proper thing since 
	 * calculated competitions should't reutnr arena results
	 * 
	 * @param competitionName the name of the competition
	 * @return the results of the competition, null if no results
	 * are available
	 */
	public ArenaResult getCalculatedCompetitionResult(String competitionName){
		// get latest
		String[] allFiles = (new File(ProjectProperties
				.getInstance().getCompetitionsLocation()
				+ competitionName
				+ "/competition/")).list();
		
		if (allFiles != null) {
			Arrays.sort(allFiles);
			try{
				return loadArenaResult(ProjectProperties
					.getInstance().getCompetitionsLocation()
					+ competitionName
					+ "/competition/"
					+ allFiles[allFiles.length -1]);
				
			}
			catch(Exception e){
				logger.error(e);
			}
		}
		return null;
	}
	
	/**
	 * Load arena results from disk.
	 * 
	 * @param location the location of the arena results.csv
	 * @return the arena result
	 */
	public ArenaResult loadArenaResult(String location){
		Map<String, Map<String, String>> data = new TreeMap<String, Map<String, String>>();
		Collection<String> categories = new LinkedList<String>();
		
		try {
			Scanner in = new Scanner(new File(location + "/results.csv"));
			
			// adding the categories (first line)
			String[] cat = in.nextLine().split(",");
			for(int i=1; i< cat.length; ++i){
				categories.add(cat[i]);
			}
			
			while(in.hasNextLine()){
				try{
					String[] line = in.nextLine().split(",");
					
					Map<String, String> userData = new TreeMap<String, String>();
					for(int i=1; i< cat.length; ++i){
						String category = cat[i];
						if(category.startsWith("*")){
							category = category.replaceFirst("\\*", "");
						}
						userData.put(category, line[i]);
					}
					
					data.put(line[0], userData);
				}
				catch(Exception e){}
			}
			
			in.close();
			
			ArenaResult result = new ArenaResult();
			result.setCategories(categories);
			result.setData(data);
			
			return result;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			return null;
		}
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
}
