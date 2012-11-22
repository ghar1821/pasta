package pasta.repository;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pasta.domain.template.Assessment;
import pasta.domain.template.Competition;
import pasta.domain.template.HandMarking;
import pasta.domain.template.Tuple;

import pasta.domain.template.UnitTest;
import pasta.domain.template.WeightedUnitTest;
import pasta.util.ProjectProperties;

public class AssessmentDAO {

	// assessmentTemplates are cached
	Map<String, Assessment> allAssessments;
	Map<String, UnitTest> allUnitTests;
	Map<String, HandMarking> allHandMarking;
	Map<String, Competition> allCompetitions;
	
	protected final Log logger = LogFactory.getLog(getClass());

	public AssessmentDAO() {
		// load up all cached objects

		// load up unit tests
		allUnitTests = new TreeMap<String, UnitTest>();
		loadUnitTests();

		// load up hand marking TODO #47
		allHandMarking = new TreeMap<String, HandMarking>();
		loadHandMarking();
		// load up competitions TODO #48

		// load up all assessments TODO #49
		allAssessments = new TreeMap<String, Assessment>();
		loadAssessments();
	}

	public Map<String, UnitTest> getAllUnitTests() {
		return allUnitTests;
	}

	public Assessment getAssessment(String name) {
		return allAssessments.get(name);
	}
	public HandMarking getHandMarking(String name) {
		return allHandMarking.get(name);
	}

	public Collection<Assessment> getAssessmentList() {
		return allAssessments.values();
	}
	
	public void addUnitTest(UnitTest newUnitTest){
		allUnitTests.put(newUnitTest.getShortName(), newUnitTest);
	}
	
	public void addAssessment(Assessment newAssessment){
		allAssessments.put(newAssessment.getShortName(), newAssessment);
	}
	
	public void removeUnitTest(String unitTestName){
		allUnitTests.remove(unitTestName);
		try {
			FileUtils.deleteDirectory(new File(ProjectProperties.getInstance().getProjectLocation()+"/template/unitTest/"+unitTestName));
		} catch (Exception e) {
			logger.error("Could not delete the folder for " + unitTestName +"\r\n"+e);
		}
	}
	
	public void removeAssessment(String assessmentName){
		allAssessments.remove(assessmentName);
		try {
			FileUtils.deleteDirectory(new File(ProjectProperties.getInstance().getProjectLocation()+"/template/assessment/"+assessmentName));
		} catch (Exception e) {
			logger.error("Could not delete the folder for " + assessmentName +"\r\n"+e);
		}
	}

	/**
	 * Load all unit tests.
	 */
	private void loadUnitTests() {
		// get unit test location
		String allTestLocation = ProjectProperties.getInstance().getProjectLocation() + "/template/unitTest";
		String[] allUnitTestNames = (new File(allTestLocation)).list();
		Arrays.sort(allUnitTestNames);

		// load properties
		for (String name : allUnitTestNames) {
			UnitTest test = getUnitTestFromDisk(allTestLocation + "/" + name);
			if (test != null) {
				allUnitTests.put(name, test);
			}
		}

	}

	/**
	 * Load all assessments.
	 */
	private void loadAssessments() {
		// get unit test location
		String allTestLocation = ProjectProperties.getInstance().getProjectLocation() + "/template/assessment";
		String[] allAssessmentNames = (new File(allTestLocation)).list();
		Arrays.sort(allAssessmentNames);

		// load properties
		for (String name : allAssessmentNames) {
			Assessment test = getAssessmentFromDisk(allTestLocation + "/" + name);
			if (test != null) {
				allAssessments.put(name, test);
			}
		}

	}
	/**
	 * Load all handmarkings.
	 */
	private void loadHandMarking() {
		// get unit test location
		String allTestLocation = ProjectProperties.getInstance().getProjectLocation() + "/template/handmarking";
		String[] allHandMarkingNames = (new File(allTestLocation)).list();
		Arrays.sort(allHandMarkingNames);

		// load properties
		for (String name : allHandMarkingNames) {
			HandMarking test = getHandMarkingFromDisk(allTestLocation + "/" + name);
			if (test != null) {
				allHandMarking.put(name, test);
			}
		}

	}

	/**
	 * Method to get a unit test from a location
	 * 
	 * @param location
	 *            - the location of the unit test
	 * @return null - there is no unit test at that location to be retrieved
	 * @return test - the unit test at that location.
	 */
	private UnitTest getUnitTestFromDisk(String location) {
		try {

			File fXmlFile = new File(location + "/unitTestProperties.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder;
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();
			String name = doc.getElementsByTagName("name").item(0).getChildNodes().item(0).getNodeValue();
			boolean tested = Boolean.parseBoolean(doc.getElementsByTagName("tested").item(0).getChildNodes().item(0)
					.getNodeValue());

			return new UnitTest(name, tested);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Method to get an assessment from a location
	 * 
	 * @param location
	 *            - the location of the assessment
	 * @return null - there is no assessment at that location to be retrieved
	 * @return test - the assessment at that location.
	 */
	private Assessment getAssessmentFromDisk(String location) {
		try {

			File fXmlFile = new File(location + "/assessmentProperties.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder;
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();

			Assessment currentAssessment = new Assessment();

			currentAssessment.setName(doc.getElementsByTagName("name").item(0).getChildNodes().item(0).getNodeValue());
			currentAssessment.setMarks(Double.parseDouble(doc.getElementsByTagName("marks").item(0).getChildNodes()
					.item(0).getNodeValue()));
			currentAssessment.setReleased(Boolean.parseBoolean(doc.getElementsByTagName("released").item(0).getChildNodes()
					.item(0).getNodeValue()));
			currentAssessment.setNumSubmissionsAllowed(Integer.parseInt(doc.getElementsByTagName("submissionsAllowed").item(0).getChildNodes()
					.item(0).getNodeValue()));

			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy");
			currentAssessment.setDueDate(sdf.parse(doc.getElementsByTagName("dueDate").item(0).getChildNodes().item(0)
					.getNodeValue()));
			currentAssessment.setDescription(doc.getElementsByTagName("description").item(0).getTextContent());

			NodeList unitTestList = doc.getElementsByTagName("unitTest");
			if (unitTestList != null && unitTestList.getLength() > 0) {
				for (int i = 0; i < unitTestList.getLength(); i++) {
					Node unitTestNode = unitTestList.item(i);
					if (unitTestNode.getNodeType() == Node.ELEMENT_NODE) {
						Element unitTestElement = (Element) unitTestNode;
						
						WeightedUnitTest weightedTest = new WeightedUnitTest();
						weightedTest.setTest(allUnitTests.get(unitTestElement.getAttribute("name")));
						weightedTest.setWeight(Double.parseDouble(unitTestElement.getAttribute("weight")));
						if (unitTestElement.getAttribute("secret") != null
								&& Boolean.parseBoolean(unitTestElement.getAttribute("secret"))) {
							currentAssessment.addSecretUnitTest(weightedTest);
						} else {
							currentAssessment.addUnitTest(weightedTest);
						}
					}
				}
			}

			// TODO add hand marking

			// TODO add competitions

			return currentAssessment;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * Method to get a handmarking from a location
	 * 
	 * @param location
	 *            - the location of the handmarking
	 * @return null - there is no handmarking at that location to be retrieved
	 * @return test - the handmarking at that location.
	 */
	private HandMarking getHandMarkingFromDisk(String location) {
		try {
			//Find and parse the XML file
			File fXmlFile = new File(location + "/handMarkingProperties.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder;
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();
			
			//Get the Rubric name and description
			HandMarking currentHandMarking = new HandMarking();
			currentHandMarking.setName(doc.getElementsByTagName("name").item(0).getChildNodes().item(0).getNodeValue());
			//System.out.println("FUCKYOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOU" + doc.getElementsByTagName("columnName").item(0).getChildNodes().item(0).getNodeValue());
			currentHandMarking.setDescription(doc.getElementsByTagName("description").item(0).getChildNodes().item(0).getNodeValue());
			
			//HashMap Key rowName, value HashMap(key columnName, value description text)
			//HashMap<String, HashMap<String, String>> rowNameAndHashMap = new HashMap();
			ArrayList<ArrayList<String[]>> dataGrid = new ArrayList();
			
			
			NodeList columns = doc.getElementsByTagName("columnName");
			NodeList rows = doc.getElementsByTagName("rowName");
			
			
			String[] row = new String[rows.getLength()];
			String[] column = new String[columns.getLength()];
			int i = 0;
			for(int k = 0; k < rows.getLength(); k++){
				Node rowNode = rows.item(k); 
				row[i] = rowNode.getChildNodes().item(0).getNodeValue();
				i++;
			}
			i = 0;
			for(int l = 0; l < columns.getLength(); l++){
				Node columnNode = columns.item(l); 
				column[i] = columnNode.getChildNodes().item(0).getNodeValue();
				i++;
			}
			//text in a row, one for each column
			NodeList text = doc.getElementsByTagName("text");
			NodeList weightNodes = doc.getElementsByTagName("weight");
			Double[] weightList = new Double[weightNodes.getLength()];
			//HashMap<String, String> columnNameAndText = new HashMap();
			ArrayList<String[]> rowElementsPerColumn = new ArrayList();
					int columnCounter = 0;
					int rowCounter = 0;
					for(int j = 0; j < text.getLength(); j++)
					{
					
						Node theNode = text.item(j);
						Node weightNode = weightNodes.item(j);
						
						//System.out.println(column[0]);
						Double d = Double.parseDouble(weightNode.getChildNodes().item(0).getNodeValue());
						
						String[] elem = new String[4];
						elem[0]=weightNode.getChildNodes().item(0).getNodeValue();
						weightList[j] = Double.parseDouble(weightNode.getChildNodes().item(0).getNodeValue());
						elem[1]=theNode.getChildNodes().item(0).getNodeValue();
						//System.out.println("FUCKYOUOOOOOOOOUOUOOu" + theNode.getChildNodes().item(0).getNodeValue());
						elem[2]=column[columnCounter];
						elem[3]=row[rowCounter];
						//Tuple elem = new Tuple(d, theNode.getNodeValue(), column[columnCounter], row[rowCounter]);
						rowElementsPerColumn.add(elem);
						columnCounter++;
						if(columnCounter >= column.length)
						{
							//System.out.println("GAH");
							columnCounter = 0;
							rowCounter++;
							//may need to duplicate to fix error
							dataGrid.add(rowElementsPerColumn);
							rowElementsPerColumn = new ArrayList();
						}
					}
	
					currentHandMarking.setWeights(weightList);
					currentHandMarking.setRows(row);
					currentHandMarking.setColumns(column);
			currentHandMarking.setData(dataGrid);
			return currentHandMarking;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
