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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pasta.domain.PASTACompUserResult;
import pasta.domain.result.ArenaResult;
import pasta.domain.result.AssessmentResult;
import pasta.domain.result.CompetitionResult;
import pasta.domain.result.HandMarkingResult;
import pasta.domain.result.UnitTestCaseResult;
import pasta.domain.result.UnitTestResult;
import pasta.domain.template.Assessment;
import pasta.domain.template.Competition;
import pasta.domain.template.WeightedHandMarking;
import pasta.domain.template.WeightedUnitTest;
import pasta.util.ProjectProperties;

public class ResultDAO {
	
	protected final Log logger = LogFactory.getLog(getClass());
	// username, assessment, date
	HashMap<String, HashMap<String, AssessmentResult>> results;
	HashMap<String, CompetitionResult> competitionResults;
	
	/**
	 * loads up all of the latest results
	 * @param assDao - for linking the assessments with the results
	 */
	public ResultDAO(AssessmentDAO assDao){
		loadAssessmentHistoryFromFile(assDao);
		loadCompetitionsFromFile(assDao);
	}

	private void loadCompetitionsFromFile(AssessmentDAO assDao) {
		Collection<Competition> comps = assDao.getCompetitionList();
		competitionResults = new HashMap<String, CompetitionResult>();
		
		for(Competition comp: comps){
			CompetitionResult result = new CompetitionResult();
			List<PASTACompUserResult> compUserResult = new LinkedList<PASTACompUserResult>();
			
			// get latest
			String[] allFiles = (new File(ProjectProperties
					.getInstance().getProjectLocation()
					+ "/competitions/"
					+ comp.getShortName()
					+ "/competition/")).list();
			
			if (allFiles != null) {
				Arrays.sort(allFiles);
				try{
					Scanner in = new Scanner(new File(ProjectProperties
						.getInstance().getProjectLocation()
						+ "/competitions/"
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
	 * @param location - location of the test
	 * @return the result
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
	 * @param username - the username of the user
	 * @return
	 */
	public HashMap<String, AssessmentResult> getLatestResults(String username){
		return results.get(username);
	}
	
	/**
	 * Get the assessment history for a particular user's history
	 * @param username - the username of the user
	 * @param assessment - the assessment (for linking purposes)
	 * @return All of the assessments submitted for this user for this assessment
	 */
	public Collection<AssessmentResult> getAssessmentHistory(String username, Assessment assessment){
		// scan folder
		String[] allFiles = (new File(ProjectProperties
				.getInstance().getProjectLocation()
				+ "/submissions/"
				+ username
				+ "/assessments/"
				+ assessment.getShortName())).list();
		
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
	 * @param assDao - the Assessment Data Access Object
	 */
	private void loadAssessmentHistoryFromFile(AssessmentDAO assDao){
		results = new HashMap<String, HashMap<String, AssessmentResult>>();
		
		// scan all users
		String[] allUsers = (new File(ProjectProperties.getInstance()
				.getProjectLocation() + "/submissions/")).list();
		if (allUsers != null && allUsers.length > 0) {
			for (String currUser : allUsers) {
				// scan all assessments
				Collection<Assessment> allAssessments = assDao
						.getAssessmentList();

				HashMap<String, AssessmentResult> currUserResults = new HashMap<String, AssessmentResult>();
				for (Assessment assessment : allAssessments) {
					// scan all submissions

					// find latest
					String latest = null;
					if(new File(ProjectProperties
							.getInstance().getProjectLocation()
							+ "/submissions/"
							+ currUser
							+ "/assessments/"
							+ assessment.getShortName()
							+ "latestOverride.txt").exists()){
						// overriden latest
						Scanner in;
						try {
							in = new Scanner (new File(ProjectProperties
								.getInstance().getProjectLocation()
								+ "/submissions/"
								+ currUser
								+ "/assessments/"
								+ assessment.getShortName()
								+ "latestOverride.txt"));
							latest = in.nextLine().trim();
							in.close();
						} catch (FileNotFoundException e) {
							// don't care
						}
						
					}
					
					String[] allFiles = (new File(ProjectProperties
							.getInstance().getProjectLocation()
							+ "/submissions/"
							+ currUser
							+ "/assessments/"
							+ assessment.getShortName())).list();
					
					if (allFiles != null) {
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
					
					currUserResults.put(assessment.getShortName(),
							loadAssessmentResultFromDisk(currUser, assessment, latest));
				}
				results.put(currUser, currUserResults);
			}
		}
	}

	/**
	 * Get the hand marking result from a location
	 * @param location - location of the hand marking result
	 * @return null if there is no hand marking result
	 * @return the result otherwise
	 */
	private HandMarkingResult getHandMarkingResult(String location) {
		HandMarkingResult result = new HandMarkingResult();
		
		try {
			// read in the file
			Scanner in = new Scanner(new File(location+"/result.txt"));
			HashMap<String,String> resultMap = new HashMap<String, String>();
			while(in.hasNextLine()){
				String[] currResults = in.nextLine().split(",");
				if(currResults.length >= 2){
					resultMap.put(currResults[0].trim(), currResults[1].trim());
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
	 * @param username - the username of the user
	 * @param assessment - the assessment (needed for lookup and linking)
	 * @param assessmentDate - the date of the submission
	 * @return the assessment or null if it does not exist
	 */
	public AssessmentResult getAsssessmentResult(String username,
			Assessment assessment, String assessmentDate) {
		// check if it's the latest
		try{
			if(results.get(username).get(assessment.getShortName()).getFormattedSubmissionDate().equals(assessmentDate)){
				return results.get(username).get(assessment.getShortName());
			}
		}
		catch(Exception e){
			// do nothing
		}
		return loadAssessmentResultFromDisk(username, assessment, assessmentDate);
	}

	/**
	 * Save the hand marking to a file
	 * @param username - the username of the user
	 * @param assessment - the assessment short name (no whitespace)
	 * @param assessmentDate - the date of the submission
	 * @param handMarkingResults - the results of the hand marking
	 */
	public void saveHandMarkingToFile(String username, String assessmentName,
			String assessmentDate, List<HandMarkingResult> handMarkingResults) {
		 
		for(HandMarkingResult result: handMarkingResults){
			String location = ProjectProperties.getInstance()
					.getProjectLocation()
					+ "/submissions/"
					+ username
					+ "/assessments/"
					+ assessmentName
					+ "/"
					+ assessmentDate
					+ "/handMarking/" + result.getHandMarkingTemplateShortName();
			// create the directory
			(new File(location)).mkdirs();
			// save data
			try {
				PrintWriter out = new PrintWriter(new File(location
									+ "/result.txt"));
				for(Entry<String, String> entry: result.getResult().entrySet()){
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
	 * @param username - the username of the user
	 * @param assessment - the assessment (needed for lookup and linking)
	 * @param assessmentDate - the date of the submission
	 * @return the assessment results
	 * @return null if there is no assessment result for that user, assessment, date combination
	 */
	public AssessmentResult loadAssessmentResultFromDisk(String username, 
			Assessment assessment, String assessmentDate) {

		AssessmentResult assessResult = null;
		if ((new File(ProjectProperties.getInstance().getProjectLocation()
				+ "/submissions/" + username + "/assessments/"
				+ assessment.getShortName() + "/" + assessmentDate)).exists()) {
			assessResult = new AssessmentResult();
			assessResult.setAssessment(assessment);

			// unit tests;
			ArrayList<UnitTestResult> utresults = new ArrayList<UnitTestResult>();
			for (WeightedUnitTest uTest : assessment.getUnitTests()) {
				UnitTestResult result = getUnitTestResult(ProjectProperties
						.getInstance().getProjectLocation()
						+ "/submissions/"
						+ username
						+ "/assessments/"
						+ assessment.getShortName()
						+ "/"
						+ assessmentDate
						+ "/unitTests/" + uTest.getTest().getShortName());
				if (result == null) {
					result = new UnitTestResult();
				}
				result.setTest(uTest.getTest());
				utresults.add(result);

			}

			// handMarking
			ArrayList<HandMarkingResult> handResults = new ArrayList<HandMarkingResult>();
			for (WeightedHandMarking hMarking : assessment.getHandMarking()) {
				HandMarkingResult result = getHandMarkingResult(ProjectProperties
						.getInstance().getProjectLocation()
						+ "/submissions/"
						+ username
						+ "/assessments/"
						+ assessment.getShortName()
						+ "/"
						+ assessmentDate
						+ "/handMarking/"
						+ hMarking.getHandMarking().getShortName());
				if (result == null) {
					result = new HandMarkingResult();
				} 
				result.setHandMarkingTemplateShortName(hMarking
						.getHandMarking().getShortName());
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
						+ username + " - " + assessment.getShortName());
			}
			
			assessResult.setSubmissionsMade(new File(ProjectProperties.getInstance().getProjectLocation()
				+ "/submissions/" + username + "/assessments/"
				+ assessment.getShortName() + "/").list().length);
			
			// comments
			try {
				Scanner in = new Scanner(new File(ProjectProperties
						.getInstance().getProjectLocation()
						+ "/submissions/"
						+ username
						+ "/assessments/"
						+ assessment.getShortName()
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
	 * @param username - the username of the user
	 * @param assessment - the assessment short name (no whitespace)
	 * @param assessmentDate - the date of the submission
	 * @param comments - the comments about the submission
	 */
	public void saveHandMarkingComments(String username, String assessmentName,
			String assessmentDate, String comments) {
		// save to file
		try {
			PrintWriter out = new PrintWriter(new File(ProjectProperties.getInstance().getProjectLocation()
					+ "/submissions/"
					+ username
					+ "/assessments/"
					+ assessmentName
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
			if(results.get(username).get(assessmentName).getFormattedSubmissionDate().equals(assessmentDate)){
				results.get(username).get(assessmentName).setComments(comments);
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
	 * @param username - the username of the user
	 * @param assessment - the assessment (needed for lookup and linking)
	 * @param runDate - the date of the submission
	 */
	public void updateUnitTestResults(String username,
			Assessment assessment, Date runDate) {
		// check if latest
		try{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss");
			if(results.get(username).get(assessment.getShortName()).getSubmissionDate().before(runDate) || 
					results.get(username).get(assessment.getShortName()).getSubmissionDate().equals(runDate)){
				results.get(username).put(assessment.getShortName(), loadAssessmentResultFromDisk(username, assessment, sdf.format(runDate)));
			}
		}
		catch(Exception e){
			// do nothing
		}
	}

	public CompetitionResult getCompetitionResult(String competitionName) {
		return competitionResults.get(competitionName);
	}

	public void updateCompetitionResults(String compName) {
		List<PASTACompUserResult> compUserResult = new LinkedList<PASTACompUserResult>();
		
		// get latest
		String[] allFiles = (new File(ProjectProperties
				.getInstance().getProjectLocation()
				+ "/competitions/"
				+ compName
				+ "/competition/")).list();
		
		if (allFiles != null) {
			Arrays.sort(allFiles);
			try{
				Scanner in = new Scanner(new File(ProjectProperties
					.getInstance().getProjectLocation()
					+ "/competitions/"
					+ compName
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
		
		CompetitionResult result = competitionResults.get(compName);
		if(result == null){
			result = new CompetitionResult();
			competitionResults.put(compName, result);
		}
		result.updatePositions(compUserResult);
	}
	
	public ArenaResult getCalculatedCompetitionResult(String competitionName){
		// get latest
		String[] allFiles = (new File(ProjectProperties
				.getInstance().getProjectLocation()
				+ "/competitions/"
				+ competitionName
				+ "/competition/")).list();
		
		if (allFiles != null) {
			Arrays.sort(allFiles);
			try{
				return loadArenaResult(ProjectProperties
					.getInstance().getProjectLocation()
					+ "/competitions/"
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
	
	public ArenaResult loadArenaResult(String location){
		HashMap<String, HashMap<String, String>> data = new HashMap<String, HashMap<String, String>>();
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
					
					HashMap<String, String> userData = new HashMap<String, String>();
					for(int i=1; i< cat.length; ++i){
						userData.put(cat[i], line[i]);
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
			e.printStackTrace();
			return null;
		}
	}
}
