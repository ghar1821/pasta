package info1103.project.repository;

import info1103.project.domain.Assessment;
import info1103.project.util.ProjectProperties;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The Data Access Object for Assessments.
 * 
 * Used for all reading/writing of assessment statistics.
 * 
 * @author Alex
 *
 */
public class AssessmentDAO {
	/**
	 * Get the list of all assessment names
	 * @return assessment names
	 */
	public List<String> getAssessmentList() {
		List<String> allAssessments = new ArrayList<String>();

		// get list of files
		File loc = new File(ProjectProperties.getInstance().getTemplateLocation());
		
		String[] assessmentList = loc.list();
		Arrays.sort(assessmentList);

		for (String s : assessmentList) {
			allAssessments.add(s);
		}

		return allAssessments;
	}

	/**
	 * Return a map of assessments for a student.
	 * @param unikey - the unikey of the student
	 * @return a map of assessments <Name of assessment, Assessment>
	 */
	public Map<String, Assessment> getAssessments(String unikey) {
		List<String> allAssessmentsList = getAssessmentList();
		Map<String, Assessment> allAssessments = new TreeMap<String, Assessment>();
		// get list of files

		for (String s : allAssessmentsList) {
			Assessment ass = getAssessment(s, unikey);
			if (ass != null) {
				allAssessments.put(ass.getName(), ass);
			}
		}

		return allAssessments;
	}
	
	/**
	 * Load assessment statistics from the folder.
	 * @param location - location of the assessment
	 * @param assessmentName - name of the assessment
	 * @return
	 */
	public Assessment loadAssessment(String location, String assessmentName) {
		Assessment assess = new Assessment();
		assess.setName(assessmentName);

		// load assessment.config
		try {
			Scanner scan = new Scanner(new File(ProjectProperties.getInstance().getTemplateLocation() + "/"
					+ assessmentName + "/assessment.properties"));
			String in = scan.nextLine();
			DateFormat df = new SimpleDateFormat("HH:mm dd/MM/yyyy");
			assess.setDueDate(df.parse(in.replace("due-date:", "")));
			assess.setWeight(Double.parseDouble(scan.nextLine().replace("weight:", "")));
			String instructions = scan.nextLine().replace("instructions:", "");
			while (scan.hasNextLine()) {
				instructions += "\r\n" + scan.nextLine();
			}
			assess.setInstructions(instructions);
			scan.close();
			
			// load date
			try{
				Scanner dateScan = new Scanner(new File(location + "/submission.info"));
				DateFormat dfDate = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss");
				assess.setSubmissionDate(dfDate.parse(dateScan.nextLine().replace("submission-date:", "")));
				dateScan.close();
			} catch (FileNotFoundException e) {
				// DO NOTHING
			}
			
			/*
			 * load result
			 * 
			 * * "No submission"
			 * * "Did not compile"
			 * * "Number of tests: ##; passed: ##; failed: ##; errors: ##"
			 * * "Processing"
			 */
			
			try{
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				
				// read xml
				Document dom = db.parse(location+"/TEST-AllTests.xml");
				Element root = dom.getDocumentElement();
				
				// get the total number of tests
				int total = Integer.parseInt(root.getAttribute("tests"));
				// get the number of errors
				int errors = Integer.parseInt(root.getAttribute("errors"));
				// get the nuber of failures
				int failures  = Integer.parseInt(root.getAttribute("failures"));
				
				// the date format for the xml
				DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
				
				// set the result
				assess.setResult("Number of tests:"+total+"; passed:" + (total - (errors+failures)) + "; failed: "+failures+"; errors:"+errors);
				DecimalFormat decForm = new DecimalFormat("0.###");
				// set the percentage
				assess.setPercentage(decForm.format((total-(errors+failures))/((double)total)));
				// set the submission date
//				assess.setSubmissionDate(df2.parse(root.getAttribute("timestamp")));
				
				String junitFeedbackHeader = "<table>";
				
				String junitFeedback = "<table>";
				
				NodeList list = root.getElementsByTagName("testcase");
				for(int i=0; i<list.getLength(); ++i){
					Node curr = list.item(i);
					junitFeedback+="\r\n<tr>";
					if(curr.hasChildNodes()){
						junitFeedback+="<td style=\"color:red;font-weight:900;\">FAIL</td>";
						junitFeedbackHeader+="<td style=\"background-color:red;\">&nbsp</td>";
					}
					else{
						junitFeedback+="<td style=\"color:#00CC00;font-weight:900;\">PASS</td>";
						junitFeedbackHeader+="<td style=\"background-color:#00CC00;\">&nbsp</td>";
					}
					junitFeedback+= "<td>"+curr.getAttributes().getNamedItem("name").getNodeValue()+"</td><td><pre style=\"margin:0;\">";
					if(curr.hasChildNodes()){
						if(curr.getChildNodes().item(1).getNodeName().equals("error")){
							if(curr.getChildNodes().item(1).getAttributes().getNamedItem("type") != null){
								junitFeedback += curr.getChildNodes().item(1).getAttributes().getNamedItem("type").getNodeValue().replace("<", "&lt").replace(">", "&gt");
							}
							if(curr.getChildNodes().item(1).getAttributes().getNamedItem("message") != null){
								junitFeedback += ": " + curr.getChildNodes().item(1).getAttributes().item(0).getNodeValue().replace("<", "&lt").replace(">", "&gt");
							}
						}else{
							if(curr.getChildNodes().item(1).getAttributes().getNamedItem("message") != null){
								junitFeedback += curr.getChildNodes().item(1).getAttributes().item(0).getNodeValue().replace("<", "&lt").replace(">", "&gt");
							}
						}
					}
					junitFeedback+="</pre></td></tr>";
				}
				
				junitFeedback += "</table>";
				junitFeedbackHeader += "</table>\r\n";
				
				junitFeedback = junitFeedbackHeader + junitFeedback;
				// set the junit feedback table
				assess.setJunitTable(junitFeedback);
			}
			catch (Exception e){
				// not attemted / DNC / Processing
				
				// not attempted
				File latestFolder = new File(location);
				File compileErrors = new File(location + "/compile.errors");
				if (latestFolder.exists() && compileErrors.exists()){
					Scanner compileErrorIn = new Scanner(new File(location + "/compile.errors"));
					String feedback = "<pre>";
					while(compileErrorIn.hasNextLine()){
						feedback += compileErrorIn.nextLine() + "\r\n";
					}
					feedback+="</pre>";
					
					assess.setResult("Did not compile");
					assess.setFeedback(feedback);
					assess.setPercentage("DNC");
				}
				else if(latestFolder.exists()){
					assess.setResult("Compilation successful, queued for testing.");
					assess.setPercentage("???");
				}
				else{
					assess.setResult("No submission");
					assess.setPercentage("N/A");
				}
			}
		} catch (FileNotFoundException e) {
			return null;
		} catch (ParseException e) {
			return null;
		}

		return assess;
	}

	/**
	 * Return assessment
	 * @param assessmentName - assessment name
	 * @param unikey - unikey
	 * @return the assessment
	 */
	public Assessment getAssessment(String assessmentName, String unikey) {
		return loadAssessment(ProjectProperties.getInstance().getSubmissionsLocation() + "/" + unikey + "/"
				+ assessmentName + "/latest", assessmentName);
	}
	
	/**
	 * Return assessment history.
	 * @param unikey - unikey of the student
	 * @param assessmentName - assessment name.
	 * @return
	 */
	public List<Assessment> getAssessmentHistory(String unikey, String assessmentName){
		List<Assessment> history = new ArrayList<Assessment>();
		Map<Date, Assessment> history2 = new TreeMap<Date, Assessment>();
		File historyFolder = new File(ProjectProperties.getInstance().getSubmissionsLocation() + "/" + unikey + "/"
				+ assessmentName + "/history");
		if(historyFolder.exists()){
			for(File f: historyFolder.listFiles()){
				Assessment ass = loadAssessment(f.getAbsolutePath(), assessmentName);
				if(ass != null){
					history2.put(ass.getSubmissionDate(), ass);
				}
			}
		}
		
		// reverse history
		ArrayList<Assessment> assList = new ArrayList<Assessment>(history2.values());
		for(int i=0; i < assList.size(); ++i){
			history.add(assList.get(assList.size()-1-i));
		}
		
		return history;
	}
}