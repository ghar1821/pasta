package pasta.archive.convert;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pasta.archive.legacy.Tuple;
import pasta.domain.PASTATime;
import pasta.domain.template.Arena;
import pasta.domain.template.Assessment;
import pasta.domain.template.Competition;
import pasta.domain.template.CompetitionPermissionLevel;
import pasta.domain.template.HandMarkData;
import pasta.domain.template.HandMarking;
import pasta.domain.template.UnitTest;
import pasta.domain.template.WeightedCompetition;
import pasta.domain.template.WeightedField;
import pasta.domain.template.WeightedHandMarking;
import pasta.domain.template.WeightedUnitTest;
import pasta.util.PASTAUtil;
import pasta.util.ProjectProperties;

public class AssessmentConverter {
	private static Logger logger = Logger.getLogger(AssessmentConverter.class);
	
	private Map<String, pasta.archive.legacy.UnitTest> allUnitTests;
	private Map<String, pasta.archive.legacy.HandMarking> allHandMarking;
	private Map<String, pasta.archive.legacy.Competition> allCompetitions;
	private Map<String, pasta.archive.legacy.Assessment> allAssessments;
	
	private Map<String, UnitTest> convertedUnitTests;
	private Map<String, HandMarking> convertedHandMarking;
	private Map<String, Competition> convertedCompetitions;
	private Map<String, Assessment> convertedAssessments;
	
	private List<String> output;
	Boolean done = null;
	
	public void convertLegacyContent() {
		if(done != null) {
			return;
		}
		output = Collections.synchronizedList(new LinkedList<String>());
		done = false;
		doLoad();
		doConvert();
		doSave();
		done = true;
	}
	
	public List<String> getOutputSinceLastCall() {
		List<String> outSinceLastCall = new LinkedList<String>(output);
		output.clear();
		return outSinceLastCall;
	}
	
	public boolean isStarted() {
		return done != null;
	}
	
	public boolean isDone() {
		return done != null && done;
	}
	
	public boolean hasOutput() {
		return !output.isEmpty();
	}
	
	private void doLoad() {
		// load up unit tests
		allUnitTests = new TreeMap<>();
		loadUnitTests();

		// load up hand marking
		allHandMarking = new TreeMap<>();
		loadHandMarking();

		// load up competitions
		allCompetitions = new TreeMap<>();
		loadCompetitions();

		// load up all assessments
		allAssessments = new TreeMap<>();
		loadAssessments();
	}
	
	private void doConvert() {
		convertedUnitTests = new TreeMap<>();
		for(Map.Entry<String, pasta.archive.legacy.UnitTest> oldTestEntry : allUnitTests.entrySet()) {
			UnitTest converted = convertUnitTest(oldTestEntry.getValue());
			if(converted != null) {
				convertedUnitTests.put(oldTestEntry.getKey(), converted);
				output.add("Converted legacy unit test " + converted.getName());
			}
		}
		
		convertedHandMarking = new TreeMap<>();
		for(Map.Entry<String, pasta.archive.legacy.HandMarking> oldHMEntry : allHandMarking.entrySet()) {
			HandMarking converted = convertHandMarking(oldHMEntry.getValue());
			if(converted != null) {
				convertedHandMarking.put(oldHMEntry.getKey(), converted);
				output.add("Converted legacy hand marking " + converted.getName());
			}
		}
		
		convertedCompetitions = new TreeMap<>();
		for(Map.Entry<String, pasta.archive.legacy.Competition> oldCompEntry : allCompetitions.entrySet()) {
			Competition converted = convertCompetition(oldCompEntry.getValue());
			if(converted != null) {
				convertedCompetitions.put(oldCompEntry.getKey(), converted);
				output.add("Converted legacy competition " + converted.getName());
			}
		}
		
		convertedAssessments = new TreeMap<>();
		for(Map.Entry<String, pasta.archive.legacy.Assessment> oldAssEntry : allAssessments.entrySet()) {
			Assessment converted = convertAssessment(oldAssEntry.getValue());
			if(converted != null) {
				convertedAssessments.put(oldAssEntry.getKey(), converted);
				output.add("Converted legacy assessment " + converted.getName());
			}
		}
	}
	
	private void doSave() {
		for(Map.Entry<String, UnitTest> utEntry : convertedUnitTests.entrySet()) {
			try {
				ProjectProperties.getInstance().getUnitTestDAO().save(utEntry.getValue());
				output.add("Saved unit test " + utEntry.getKey());
			} catch(Exception e) {
				String error = "Error saving unit test " + utEntry.getKey();
				output.add(error);
				logger.error(error, e);
			}
		}
		for(Map.Entry<String, HandMarking> hmEntry : convertedHandMarking.entrySet()) {
			try {
				ProjectProperties.getInstance().getHandMarkingDAO().saveOrUpdate(hmEntry.getValue());
				output.add("Saved hand marking " + hmEntry.getKey());
			} catch(Exception e) {
				String error = "Error saving hand marking " + hmEntry.getKey();
				output.add(error);
				logger.error(error, e);
			}
		}
		for(Map.Entry<String, Competition> compEntry : convertedCompetitions.entrySet()) {
			try {
				ProjectProperties.getInstance().getCompetitionDAO().saveOrUpdate(compEntry.getValue());
				output.add("Saved competition " + compEntry.getKey());
			} catch(Exception e) {
				String error = "Error saving competition " + compEntry.getKey();
				output.add(error);
				logger.error(error, e);
			}
		}
		for(Map.Entry<String, Assessment> assEntry : convertedAssessments.entrySet()) {
			try {
				ProjectProperties.getInstance().getAssessmentDAO().saveOrUpdate(assEntry.getValue());
				output.add("Saved assessment " + assEntry.getKey());
			} catch(Exception e) {
				String error = "Error saving assessment " + assEntry.getKey();
				output.add(error);
				logger.error(error, e);
			}
		}
	}
	
	/**
	 * Load all unit tests.
	 * <p>
	 * Calls {@link #getUnitTestFromDisk(String)} multiple times.
	 */
	private void loadUnitTests() {
		// get unit test location
		String allTestLocation = ProjectProperties.getInstance().getProjectLocation() + "legacy/template/unitTest";
		if(!new File(allTestLocation).exists()) {
			allTestLocation = ProjectProperties.getInstance().getProjectLocation() + "legacy/unitTest";
		}
		if(!new File(allTestLocation).exists()) {
			output.add("No unit tests found under " + ProjectProperties.getInstance().getProjectLocation() + "legacy/template/unitTest" +
					" or " + ProjectProperties.getInstance().getProjectLocation() + "legacy/unitTest");
			return;
		}
		String[] allUnitTestNames = (new File(allTestLocation)).list();
		if (allUnitTestNames != null && allUnitTestNames.length > 0) {
			// load properties
			for (String name : allUnitTestNames) {
				pasta.archive.legacy.UnitTest test = getUnitTestFromDisk(allTestLocation + "/" + name);
				if (test != null) {
					allUnitTests.put(name, test);
					output.add("Loaded legacy unit test " + name);
				}
			}
		}
	}

	/**
	 * Load all competitions.
	 * <p>
	 * Calls {@link #getCompetitionFromDisk(String)} multiple times.
	 */
	private void loadCompetitions() {
		// get competition location
		String allCompetitionLocation = ProjectProperties.getInstance().getProjectLocation() + "legacy/template/competition";
		if(!new File(allCompetitionLocation).exists()) {
			allCompetitionLocation = ProjectProperties.getInstance().getProjectLocation() + "legacy/competition";
		}
		if(!new File(allCompetitionLocation).exists()) {
			output.add("No competitions found under " + ProjectProperties.getInstance().getProjectLocation() + "legacy/template/competition" +
					" or " + ProjectProperties.getInstance().getProjectLocation() + "legacy/competition");
			return;
		}
		String[] allCompetitionNames = (new File(allCompetitionLocation))
				.list();
		if (allCompetitionNames != null && allCompetitionNames.length > 0) {
			// load properties
			for (String name : allCompetitionNames) {
				pasta.archive.legacy.Competition comp = getCompetitionFromDisk(allCompetitionLocation + "/" + name);
				if (comp != null) {
					allCompetitions.put(name, comp);
					output.add("Loaded legacy competition " + name);
				}
			}
		}

	}

	/**
	 * Load all assessments
	 * <p>
	 * Calls {@link #getAssessmentFromDisk(String)} multiple times.
	 */
	private void loadAssessments() {
		// get assessment location
		String allTestLocation = ProjectProperties.getInstance().getProjectLocation() + "legacy/template/assessment";
		if(!new File(allTestLocation).exists()) {
			allTestLocation = ProjectProperties.getInstance().getProjectLocation() + "legacy/assessment";
		}
		if(!new File(allTestLocation).exists()) {
			output.add("No assessments found under " + ProjectProperties.getInstance().getProjectLocation() + "legacy/template/assessment" +
					" or " + ProjectProperties.getInstance().getProjectLocation() + "legacy/assessment");
			return;
		}
		String[] allAssessmentNames = (new File(allTestLocation)).list();
		if (allAssessmentNames != null && allAssessmentNames.length > 0) {
			// load properties
			for (String name : allAssessmentNames) {
				pasta.archive.legacy.Assessment assessment = getAssessmentFromDisk(allTestLocation + "/" + name);
				if (assessment != null) {
					allAssessments.put(name, assessment);
					output.add("Loaded legacy assessment " + name);
				}
			}
		}
	}

	/**
	 * Load all handmarkings templates
	 * <p>
	 * Calls {@link #getHandMarkingFromDisk(String)} multiple times.
	 */
	private void loadHandMarking() {
		// get hand marking location
		String allTestLocation = ProjectProperties.getInstance().getProjectLocation() + "legacy/template/handMarking";
		if(!new File(allTestLocation).exists()) {
			allTestLocation = ProjectProperties.getInstance().getProjectLocation() + "legacy/handMarking";
		}
		if(!new File(allTestLocation).exists()) {
			output.add("No hand marking found under " + ProjectProperties.getInstance().getProjectLocation() + "legacy/template/handMarking" +
					" or " + ProjectProperties.getInstance().getProjectLocation() + "legacy/handMarking");
			return;
		}
		String[] allHandMarkingNames = (new File(allTestLocation)).list();
		if (allHandMarkingNames != null && allHandMarkingNames.length > 0) {
			// load properties
			for (String name : allHandMarkingNames) {
				pasta.archive.legacy.HandMarking test = getHandMarkingFromDisk(allTestLocation + "/" + name);
				if (test != null) {
					allHandMarking.put(test.getShortName(), test);
					output.add("Loaded legacy hand marking " + name);
				}
			}
		}
	}
	
	
	private pasta.archive.legacy.UnitTest getUnitTestFromDisk(String location) {
		try {
			File fXmlFile = new File(location + "/unitTestProperties.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder;
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();
			String name = doc.getElementsByTagName("name").item(0)
					.getChildNodes().item(0).getNodeValue();
			boolean tested = Boolean.parseBoolean(doc
					.getElementsByTagName("tested").item(0).getChildNodes()
					.item(0).getNodeValue());
			return new pasta.archive.legacy.UnitTest(name, tested);
		} catch (Exception e) {
			String error = "Could not rebuild legacy unit test from " + location;
			output.add(error);
			logger.error(error, e);
			return null;
		}
	}
	
	/**
	 * Method to get a competition from a location
	 * <p>
	 * Loads the competitionProperties.xml from file into the cache.
	 * 
	 * @param location
	 *            - the location of the competition
	 * @return null - there is no competition at that location to be retrieved
	 * @return comp - the competition at that location.
	 */
	private pasta.archive.legacy.Competition getCompetitionFromDisk(String location) {
		try {

			File fXmlFile = new File(location + "/competitionProperties.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder;
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();

			pasta.archive.legacy.Competition comp = new pasta.archive.legacy.Competition();

			// name
			comp.setName(doc.getElementsByTagName("name").item(0)
					.getChildNodes().item(0).getNodeValue());

			// tested
			comp.setTested(Boolean.parseBoolean(doc
					.getElementsByTagName("tested").item(0).getChildNodes()
					.item(0).getNodeValue()));

			// can students create an arena
			comp.setStudentCreatableArena(Boolean.parseBoolean(doc
					.getElementsByTagName("studentCreatableArena").item(0)
					.getChildNodes().item(0).getNodeValue()));

			// can students create repeatable arenas
			comp.setStudentCreatableRepeatableArena(Boolean.parseBoolean(doc
					.getElementsByTagName("studentCreatableRepeatableArena")
					.item(0).getChildNodes().item(0).getNodeValue()));

			// can tutors create repeatableArenas
			comp.setTutorCreatableRepeatableArena(Boolean.parseBoolean(doc
					.getElementsByTagName("tutorCreatableRepeatableArena")
					.item(0).getChildNodes().item(0).getNodeValue()));
			
			
			// is the competition hidden or not
			if (doc.getElementsByTagName("hidden") != null
					&& doc.getElementsByTagName("hidden").getLength() != 0) {
				comp.setHidden(Boolean.parseBoolean(doc
						.getElementsByTagName("hidden")
						.item(0).getChildNodes().item(0).getNodeValue()));
			}

			// first start date - only for calculated comps
			if (doc.getElementsByTagName("firstStartDate") != null
					&& doc.getElementsByTagName("firstStartDate").getLength() != 0) {
				comp.setFirstStartDate(PASTAUtil.parseDate(doc
						.getElementsByTagName("firstStartDate").item(0)
						.getChildNodes().item(0).getNodeValue()));
			}

			// frequency - only for calculated comps
			if (doc.getElementsByTagName("frequency") != null
					&& doc.getElementsByTagName("frequency").getLength() != 0) {
				comp.setFrequency(new PASTATime(doc
						.getElementsByTagName("frequency").item(0)
						.getChildNodes().item(0).getNodeValue()));
			}

			// arenas
			// official
			
			String[] arenaList = new File(location + "/arenas/").list();
			
			if(arenaList != null){
				
				LinkedList<pasta.archive.legacy.Arena> completedArenas = new LinkedList<>();
				LinkedList<pasta.archive.legacy.Arena> outstandingArenas = new LinkedList<>();
				
				for(String arenaName: arenaList){
					pasta.archive.legacy.Arena arena = getArenaFromDisk(location + "/arenas/" + arenaName);
					if(arena != null){
						if(arena.getName().replace(" ", "").toLowerCase().equals("officialarena")){
							comp.setOfficialArena(arena);
						}
						else if(new File(location + "/arenas/" + arenaName + "/results.csv").exists()
								&& !arena.isRepeatable()){
							completedArenas.add(arena);
						}
						else{
							outstandingArenas.add(arena);
						}
					}
				}
				
				comp.setCompletedArenas(completedArenas);
				comp.setOutstandingArenas(outstandingArenas);
			}
			else{
				Map<String, pasta.archive.legacy.Arena> nothing = null;
				comp.setOutstandingArenas(nothing);
			}
			
			return comp;
		} catch (Exception e) {
			String error = "Could not rebuild legacy competition from " + location;
			output.add(error);
			logger.error(error, e);
			return null;
		}
	}
	
	/**
	 * Method to get an arena from a location
	 * <p>
	 * Loads the arenaProperties.xml from file into the cache.
	 * 
	 * @param location
	 *            - the location of the arena
	 * @return null - there is no arena at that location to be retrieved
	 * @return arena - the arena at that location.
	 */
	private pasta.archive.legacy.Arena getArenaFromDisk(String location) {
		try {
			File fXmlFile = new File(location + "/arenaProperties.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder;
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();
			
			pasta.archive.legacy.Arena arena = new pasta.archive.legacy.Arena();
			
			// name
			arena.setName(doc.getElementsByTagName("name").item(0)
					.getChildNodes().item(0).getNodeValue());
			
			// first start date
			if (doc.getElementsByTagName("firstStartDate") != null
					&& doc.getElementsByTagName("firstStartDate").getLength() != 0) {
				arena.setFirstStartDate(PASTAUtil.parseDate(doc
						.getElementsByTagName("firstStartDate").item(0)
						.getChildNodes().item(0).getNodeValue()));
			}

			// frequency
			if (doc.getElementsByTagName("repeats") != null
					&& doc.getElementsByTagName("repeats").getLength() != 0) {
				arena.setFrequency(new PASTATime(doc
						.getElementsByTagName("repeats").item(0)
						.getChildNodes().item(0).getNodeValue()));
			}
			
			// password
			if (doc.getElementsByTagName("password") != null
					&& doc.getElementsByTagName("password").getLength() != 0) {
				arena.setPassword(doc.getElementsByTagName("password").item(0)
						.getChildNodes().item(0).getNodeValue());
			}
			
			// players
			String[] players = new File(location + "/players/").list();
			
			if(players != null){
				for(String player : players){
					Scanner in = new Scanner(new File(location + "/players/" + player));
					
					String username = player.split("\\.")[0];
					while(in.hasNextLine()){
						arena.addPlayer(username, in.nextLine());
					}
					
					in.close();
				}
			}
			
			return arena;
		} catch (Exception e) {
			String error = "Could not rebuild legacy arena from " + location;
			output.add(error);
			logger.error(error, e);
			return null;
		}
	}
	
	/**
	 * Method to get a handmarking from a location
	 * <p>
	 * Loads the handMarkingProperties.xml from file into the cache. 
	 * Also loads the multiple .html files which are the descriptions
	 * in each box of the hand marking template.
	 * 
	 * @param location
	 *            - the location of the handmarking
	 * @return null - there is no handmarking at that location to be retrieved
	 * @return test - the handmarking at that location.
	 */
	private pasta.archive.legacy.HandMarking getHandMarkingFromDisk(String location) {
		try {

			File fXmlFile = new File(location + "/handMarkingProperties.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder;
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();

			pasta.archive.legacy.HandMarking markingTemplate = new pasta.archive.legacy.HandMarking();

			// load name
			markingTemplate.setName(doc.getElementsByTagName("name").item(0)
					.getChildNodes().item(0).getNodeValue());

			// load column list
			NodeList columnList = doc.getElementsByTagName("column");
			List<Tuple> columnHeaderList = new ArrayList<Tuple>();
			if (columnList != null && columnList.getLength() > 0) {
				for (int i = 0; i < columnList.getLength(); i++) {
					Node columnNode = columnList.item(i);
					if (columnNode.getNodeType() == Node.ELEMENT_NODE) {
						Element columnElement = (Element) columnNode;

						Tuple tuple = new Tuple();
						tuple.setName(columnElement.getAttribute("name"));
						tuple.setWeight(Double.parseDouble(columnElement
								.getAttribute("weight")));

						columnHeaderList.add(tuple);
					}
				}
			}
			markingTemplate.setColumnHeader(columnHeaderList);

			// load row list
			NodeList rowList = doc.getElementsByTagName("row");
			List<Tuple> rowHeaderList = new ArrayList<Tuple>();
			if (rowList != null && rowList.getLength() > 0) {
				for (int i = 0; i < rowList.getLength(); i++) {
					Node rowNode = rowList.item(i);
					if (rowNode.getNodeType() == Node.ELEMENT_NODE) {
						Element rowElement = (Element) rowNode;

						Tuple tuple = new Tuple();
						tuple.setName(rowElement.getAttribute("name"));
						tuple.setWeight(Double.parseDouble(rowElement
								.getAttribute("weight")));

						rowHeaderList.add(tuple);
					}
				}
			}
			markingTemplate.setRowHeader(rowHeaderList);

			// load data
			Map<String, Map<String, String>> descriptionMap = new TreeMap<String, Map<String, String>>();
			for (Tuple column : markingTemplate.getColumnHeader()) {
				Map<String, String> currDescriptionMap = new TreeMap<String, String>();
				for (Tuple row : markingTemplate.getRowHeader()) {
					try {
						Scanner in = new Scanner(new File(location + "/"
								+ column.getName().replace(" ", "") + "-"
								+ row.getName().replace(" ", "") + ".txt"));
						String description = "";
						while (in.hasNextLine()) {
							description += in.nextLine()
									+ System.getProperty("line.separator");
						}
						currDescriptionMap.put(row.getName(), description);
						in.close();
					} catch (Exception e) {
						// do nothing
					}
				}
				descriptionMap.put(column.getName(), currDescriptionMap);
			}

			markingTemplate.setData(descriptionMap);

			return markingTemplate;
		} catch (Exception e) {
			String error = "Could not rebuild legacy hand marking from " + location;
			output.add(error);
			logger.error(error, e);
			return null;
		}
	}
	
	
	/**
	 * Method to get an assessment from a location
	 * <p>
	 * Loads the assessmentProperties.xml from file into the cache. 
	 * 
	 * @param location
	 *            - the location of the assessment
	 * @return null - there is no assessment at that location to be retrieved
	 * @return assessment - the assessment at that location.
	 */
	private pasta.archive.legacy.Assessment getAssessmentFromDisk(String location) {
		try {
			File fXmlFile = new File(location + "/assessmentProperties.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder;
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();

			pasta.archive.legacy.Assessment currentAssessment = new pasta.archive.legacy.Assessment();

			currentAssessment.setName(doc.getElementsByTagName("name").item(0)
					.getChildNodes().item(0).getNodeValue());
			currentAssessment.setMarks(Double.parseDouble(doc
					.getElementsByTagName("marks").item(0).getChildNodes()
					.item(0).getNodeValue()));
			try {
				currentAssessment.setReleasedClasses(doc
						.getElementsByTagName("releasedClasses").item(0)
						.getChildNodes().item(0).getNodeValue());
			} catch (Exception e) {
				// not released
			}

			try {
				currentAssessment.setCategory(doc
						.getElementsByTagName("category").item(0)
						.getChildNodes().item(0).getNodeValue());
			} catch (Exception e) {
				// no category
			}
			
			try {
				currentAssessment.setCountUncompilable(Boolean.parseBoolean(doc
						.getElementsByTagName("countUncompilable").item(0)
						.getChildNodes().item(0).getNodeValue()));
			} catch (Exception e) {
				// no countUncompilable tag - defaults to true
			}

			try {
				currentAssessment.setSpecialRelease(doc
						.getElementsByTagName("specialRelease").item(0)
						.getChildNodes().item(0).getNodeValue());
			} catch (Exception e) {
				// not special released
			}
			currentAssessment.setNumSubmissionsAllowed(Integer.parseInt(doc
					.getElementsByTagName("submissionsAllowed").item(0)
					.getChildNodes().item(0).getNodeValue()));

			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy");
			currentAssessment.setDueDate(sdf.parse(doc
					.getElementsByTagName("dueDate").item(0).getChildNodes()
					.item(0).getNodeValue()));

			// load description from file
			String description = "";
			try {
				Scanner in = new Scanner(new File(location
						+ "/description.html"));
				while (in.hasNextLine()) {
					description += in.nextLine()
							+ System.getProperty("line.separator");
				}
				in.close();
			} catch (Exception e) {
				description = "<pre>Error loading description"
						+ System.getProperty("line.separator") + e + "</pre>";
			}
			currentAssessment.setDescription(description);

			// add unit tests
			NodeList unitTestList = doc.getElementsByTagName("unitTest");
			if (unitTestList != null && unitTestList.getLength() > 0) {
				for (int i = 0; i < unitTestList.getLength(); i++) {
					Node unitTestNode = unitTestList.item(i);
					if (unitTestNode.getNodeType() == Node.ELEMENT_NODE) {
						Element unitTestElement = (Element) unitTestNode;

						pasta.archive.legacy.WeightedUnitTest weightedTest = new pasta.archive.legacy.WeightedUnitTest();
						if(!allUnitTests.containsKey(unitTestElement.getAttribute("name"))) {
							output.add("Skipping unit test " + unitTestElement.getAttribute("name") + " for assessment " + currentAssessment.getName() + " (not found)");
							continue;
						}
						weightedTest.setTest(allUnitTests.get(unitTestElement
								.getAttribute("name")));
						weightedTest.setWeight(Double
								.parseDouble(unitTestElement
										.getAttribute("weight")));
						if (unitTestElement.getAttribute("secret") != null
								&& Boolean.parseBoolean(unitTestElement
										.getAttribute("secret"))) {
							currentAssessment.addSecretUnitTest(weightedTest);
						} else {
							currentAssessment.addUnitTest(weightedTest);
						}
					}
				}
			}

			// add hand marking
			NodeList handMarkingList = doc.getElementsByTagName("handMarks");
			if (handMarkingList != null && handMarkingList.getLength() > 0) {
				for (int i = 0; i < handMarkingList.getLength(); i++) {
					Node handMarkingNode = handMarkingList.item(i);
					if (handMarkingNode.getNodeType() == Node.ELEMENT_NODE) {
						Element handMarkingElement = (Element) handMarkingNode;

						pasta.archive.legacy.WeightedHandMarking weightedHandMarking = new pasta.archive.legacy.WeightedHandMarking();
						if(!allHandMarking.containsKey(handMarkingElement.getAttribute("name"))) {
							output.add("Skipping handMarking " + handMarkingElement.getAttribute("name") + " for assessment " + currentAssessment.getName() + " (not found)");
							continue;
						}
						weightedHandMarking.setHandMarking(allHandMarking
								.get(handMarkingElement.getAttribute("name")));
						weightedHandMarking.setWeight(Double
								.parseDouble(handMarkingElement
										.getAttribute("weight")));
						currentAssessment.addHandMarking(weightedHandMarking);
					}
				}
			}

			// add competitions
			NodeList competitionList = doc.getElementsByTagName("competition");
			if (competitionList != null && competitionList.getLength() > 0) {
				for (int i = 0; i < competitionList.getLength(); i++) {
					Node competitionNode = competitionList.item(i);
					if (competitionNode.getNodeType() == Node.ELEMENT_NODE) {
						Element competitionElement = (Element) competitionNode;

						pasta.archive.legacy.WeightedCompetition weightedComp = new pasta.archive.legacy.WeightedCompetition();
						if(!allCompetitions.containsKey(competitionElement.getAttribute("name"))) {
							output.add("Skipping competition " + competitionElement.getAttribute("name") + " for assessment " + currentAssessment.getName() + " (not found)");
							continue;
						}
						weightedComp.setTest(allCompetitions
								.get(competitionElement.getAttribute("name")));
						weightedComp.setWeight(Double
								.parseDouble(competitionElement
										.getAttribute("weight")));
						weightedComp.getCompetition().addAssessment(currentAssessment);
						currentAssessment.addCompetition(weightedComp);
					}
				}
			}

			return currentAssessment;
		} catch (Exception e) {
			String error = "Could not rebuild legacy assessment from " + location;
			output.add(error);
			logger.error(error, e);
			return null;
		}
	}
	
	
	private UnitTest convertUnitTest(pasta.archive.legacy.UnitTest old) {
		try {
			UnitTest newTest = new UnitTest();
			newTest.setName(old.getName());
			newTest.setTested(false);
			return newTest;
		} catch(Exception e) {
			String error = "Error converting legacy unit test " + old.getName();
			output.add(error);
			logger.error(error, e);
			return null;
		}
	}
	
	private Competition convertCompetition(pasta.archive.legacy.Competition old) {
		try {
			Competition newComp = new Competition();
			newComp.setCalculated(old.isCalculated());
			newComp.setFirstStartDate(old.getFirstStartDate());
			newComp.setFirstStartDateStr(old.getFirstStartDateStr());
			newComp.setFrequency(old.getFrequency());
			newComp.setHidden(old.isHidden());
			newComp.setName(old.getName());
			
			if(old.isStudentCreatableArena()) {
				newComp.setStudentPermissions(CompetitionPermissionLevel.CREATE);
			}
			if(old.isStudentCreatableRepeatableArena()) {
				newComp.setStudentPermissions(CompetitionPermissionLevel.CREATE_REPEATABLE);
			} 
			if(old.isTutorCreatableRepeatableArena()) {
				newComp.setTutorPermissions(CompetitionPermissionLevel.CREATE_REPEATABLE);
			}
			newComp.setTested(false);
			
			Arena newOfficial = convertArena(old.getOfficialArena());
			newOfficial.setCompetition(newComp);
			newComp.setOfficialArena(newOfficial);
			
			for(pasta.archive.legacy.Arena oldArena : old.getCompletedArenas()) {
				Arena newArena = convertArena(oldArena);
				newArena.setCompetition(newComp);
				newComp.getCompletedArenas().add(newArena);
			}
			for(pasta.archive.legacy.Arena oldArena : old.getOutstandingArenas()) {
				Arena newArena = convertArena(oldArena);
				newArena.setCompetition(newComp);
				newComp.getOutstandingArenas().add(newArena);
			}
			return newComp;
		} catch(Exception e) {
			String error = "Error converting legacy competition " + old.getName();
			output.add(error);
			logger.error(error, e);
			return null;
		}
	}
	
	private Arena convertArena(pasta.archive.legacy.Arena old) {
		try {
			Arena newArena = new Arena();
			newArena.setFirstStartDate(old.getFirstStartDate());
			newArena.setFirstStartDateStr(old.getFirstStartDateStr());
			newArena.setFrequency(old.getFrequency());
			newArena.setName(old.getName());
			newArena.setPassword(old.getPassword());
			return newArena;
		} catch(Exception e) {
			String error = "Error converting legacy arena " + old.getName();
			output.add(error);
			logger.error(error, e);
			return null;
		}
	}
	
	private HandMarking convertHandMarking(pasta.archive.legacy.HandMarking old) {
		try {
			HandMarking newHM = new HandMarking();
			newHM.setName(old.getName());
			
			Map<String, WeightedField> rows = new HashMap<>();
			Map<String, WeightedField> cols = new HashMap<>();
			for(Tuple row : old.getRowHeader()) {
				WeightedField newRow = new WeightedField(row.getName(), row.getWeight());
				newHM.addRow(newRow);
				rows.put(row.getName(), newRow);
			}
			for(Tuple column : old.getColumnHeader()) {
				WeightedField newCol = new WeightedField(column.getName(), column.getWeight());
				newHM.addColumn(newCol);
				cols.put(column.getName(), newCol);
			}
			for(String colName : old.getData().keySet()) {
				WeightedField col = cols.get(colName);
				for(String rowName : old.getData().get(colName).keySet()) {
					String data = old.getData().get(colName).get(rowName);
					WeightedField row = rows.get(rowName);
					HandMarkData newData = new HandMarkData(col, row, data);
					newHM.addData(newData);
				}
			}
			return newHM;
		} catch(Exception e) {
			String error = "Error converting legacy hand marking " + old.getName();
			output.add(error);
			logger.error(error, e);
			return null;
		}
	}

	private Assessment convertAssessment(pasta.archive.legacy.Assessment old) {
		try {
			Assessment newAss = new Assessment();
			newAss.setCategory(old.getCategory());
			newAss.setCountUncompilable(old.isCountUncompilable());
			newAss.setDescription(old.getDescription());
			newAss.setDueDate(old.getDueDate());
			newAss.setMarks(old.getMarks());
			newAss.setName(old.getName());
			newAss.setNumSubmissionsAllowed(old.getNumSubmissionsAllowed());
			
			for(pasta.archive.legacy.WeightedUnitTest oldTest : old.getUnitTests()) {
				WeightedUnitTest newTest = new WeightedUnitTest();
				newTest.setSecret(false);
				newTest.setTest(convertedUnitTests.get(oldTest.getTest().getShortName()));
				newTest.setWeight(oldTest.getWeight());
				newAss.addUnitTest(newTest);
			}
			for(pasta.archive.legacy.WeightedUnitTest oldTest : old.getSecretUnitTests()) {
				WeightedUnitTest newTest = new WeightedUnitTest();
				newTest.setSecret(true);
				newTest.setTest(convertedUnitTests.get(oldTest.getTest().getShortName()));
				newTest.setWeight(oldTest.getWeight());
				newAss.addUnitTest(newTest);
			}
			
			for(pasta.archive.legacy.WeightedHandMarking oldHM : old.getHandMarking()) {
				WeightedHandMarking newHM = new WeightedHandMarking();
				newHM.setHandMarking(convertedHandMarking.get(oldHM.getHandMarking().getShortName()));
				newHM.setWeight(oldHM.getWeight());
				newAss.addHandMarking(newHM);
			}
			
			for(pasta.archive.legacy.WeightedCompetition oldComp : old.getCompetitions()) {
				WeightedCompetition newComp = new WeightedCompetition();
				newComp.setCompetition(convertedCompetitions.get(oldComp.getCompetition().getShortName()));
				newComp.setWeight(oldComp.getWeight());
				newAss.addCompetition(newComp);
			}
			
			return newAss;
		} catch(Exception e) {
			String error = "Error converting legacy assessment " + old.getName();
			output.add(error);
			logger.error(error, e);
			return null;
		}
	}

}
