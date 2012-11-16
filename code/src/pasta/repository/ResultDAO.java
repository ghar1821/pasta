package pasta.repository;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pasta.domain.result.UnitTestCaseResult;
import pasta.domain.result.UnitTestResult;

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
				logger.error("Could not read result.xml\r\n"+e);
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
					input+=in.nextLine()+"\r\n";
				}
				// set 
				result.setRuntimeErrors(input);
				
				// return
				return result;
			}
			catch(Exception e){
				logger.error("Could not read run.errors\r\n"+e);
			}
		}
				
		// check to see if there is a compile.errors file
		File compileErrors = new File(location+"/compile.errors");
		if(compileErrors.exists()){
			try{
				// read in
				Scanner in = new Scanner (compileErrors);
				String input = "";
				while(in.hasNextLine()){
					input+=in.nextLine()+"\r\n";
				}
				// set 
				result.setCompileErrors(input);
				
				// return
				return result;
			}
			catch(Exception e){
				logger.error("Could not read compile.errors\r\n"+e);
			}
		}
		
		return null; // TODO check if in the database
	}
}
