package pasta.repository;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pasta.domain.result.AssessmentResult;
import pasta.domain.result.UnitTestCaseResult;
import pasta.domain.result.UnitTestResult;
import pasta.domain.template.Assessment;
import pasta.domain.template.WeightedUnitTest;
import pasta.util.ProjectProperties;

public class ResultDAO {
	
	protected final Log logger = LogFactory.getLog(getClass());

	public UnitTestResult getUnitTestResult(String location){
		UnitTestResult result = new UnitTestResult();
		
		// check to see if there is a results.xml file
		File testResults = new File(location+"/result.xml");
		if(testResults.exists()){
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
				logger.error("Could not read result.xml" + System.getProperty("line.separator")+e);
			}
		}
		
		// check to see if there is a compile.errors file
		File compileErrors = new File(location+"/compile.errors");
		System.out.println(location+"/compile.errors");
		if(compileErrors.exists()){
			System.out.println("exists");
			try{
				// read in
				Scanner in = new Scanner (compileErrors);
				String input = "";
				while(in.hasNextLine()){
					input+=in.nextLine() + System.getProperty("line.separator");
				}
				// set 
				result.setCompileErrors(input);
				
				// return
				return result;
			}
			catch(Exception e){
				logger.error("Could not read compile.errors" + System.getProperty("line.separator")+e);
			}
		}
		
		// check to see if there is a run.errors file
		File runErrors = new File(location+"/run.errors");
		if(runErrors.exists()){
			try{
				// read in
				Scanner in = new Scanner (runErrors);
				String input = "";
				while(in.hasNextLine()){
					input+=in.nextLine() + System.getProperty("line.separator");
				}
				// set 
				result.setRuntimeErrors(input);
				
				// return
				return result;
			}
			catch(Exception e){
				logger.error("Could not read run.errors" + System.getProperty("line.separator")+e);
			}
		}
		
		return null; // TODO check if in the database
	}
	
	public Collection<AssessmentResult> getAssessmentHistory(String username){
		Collection<AssessmentResult> results = new ArrayList<AssessmentResult>();
		// TODO
		return results;
	}
	
	public Collection<AssessmentResult> getAssessmentHistory(String username, Assessment assessment){
		Collection<AssessmentResult> results = new ArrayList<AssessmentResult>();
		
		String[] allFiles = (new File(ProjectProperties.getInstance()
				.getProjectLocation()
				+ "/submissions/"
				+ username
				+ "/assessments/" + assessment.getShortName())).list();
		
		// TODO 
		if(allFiles == null){
			return results;
		}
		Arrays.sort(allFiles);

		for(int i=0; i< allFiles.length; ++i){
			String latest = allFiles[allFiles.length-1-i];
			AssessmentResult assessResult = new AssessmentResult();
			assessResult.setAssessment(assessment);
	
			ArrayList<UnitTestResult> utresults = new ArrayList<UnitTestResult>();
			for (WeightedUnitTest uTest : assessment.getUnitTests()) {
				UnitTestResult result = getUnitTestResult(ProjectProperties.getInstance()
								.getProjectLocation()
								+ "/submissions/"
								+ username
								+ "/assessments/"
								+ assessment.getShortName()
								+ "/"
								+ latest
								+ "/unitTests/" + uTest.getTest().getShortName());
				if (result == null) {
					result = new UnitTestResult();
				}
				result.setTest(uTest.getTest());
				utresults.add(result);
				
			}
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss");
			try {
				assessResult.setSubmissionDate(sdf.parse(latest));
			} catch (ParseException e) {
				assessResult.setSubmissionDate(new Date());
				logger.error("Submission date " + latest + " - " + username + " - " + assessment.getName());
			}
			assessResult.setUnitTests(utresults);
	
			// add to collection
			results.add(assessResult);
		}
		
		return results;
	}
}
