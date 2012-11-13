package pasta.repository;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import pasta.domain.template.Assessment;
import pasta.domain.template.Competition;
import pasta.domain.template.HandMarking;
import pasta.domain.template.UnitTest;
import pasta.util.ProjectProperties;

public class AssessmentDAO {

	// assessmentTemplates are cached
	Map<String, Assessment> allAssessments;
	Map<String, UnitTest> allUnitTests;
	Map<String, HandMarking> allHandMarking;
	Map<String, Competition> allCompetitions;
	
	public AssessmentDAO(){
		// load up all cached objects

		// load up unit tests
		allUnitTests = new TreeMap<String, UnitTest>();
		loadUnitTests();

		// load up hand marking TODO #47

		// load up competitions TODO #48

		// load up all assessments TODO #49
	}

	public Map<String, UnitTest> getAllUnitTests() {
		return allUnitTests;
	}

	public Assessment getAssessment(String name) {
		return allAssessments.get(name);
	}

	public Collection getAssessmentList() {
		return allAssessments.values();
	}

	private void loadUnitTests() {
		// get unit test location
		String allTestLocation = ProjectProperties.getInstance().getProjectLocation() + "/template/unitTest";
		String[] allUnitTestNames = (new File(allTestLocation)).list();
		Arrays.sort(allUnitTestNames);

		// load properties
		for (String name : allUnitTestNames) {
			UnitTest test = getUnitTest(allTestLocation+"/"+name);
			if(test != null){
				allUnitTests.put(name, test);
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
	private UnitTest getUnitTest(String location) {
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

}
