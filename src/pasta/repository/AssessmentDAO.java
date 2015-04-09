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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;

import pasta.domain.result.DueDateComparator;
import pasta.domain.template.Assessment;
import pasta.domain.template.HandMarkData;
import pasta.domain.template.HandMarking;
import pasta.domain.template.WeightedCompetition;
import pasta.domain.template.WeightedField;
import pasta.domain.template.WeightedHandMarking;
import pasta.domain.template.WeightedUnitTest;
import pasta.domain.upload.NewHandMarkingForm;
import pasta.util.ProjectProperties;

/**
 * Data Access Object for Assessments.
 * <p>
 * This class is responsible for all of the interaction between the data layer
 * (disk in this case) and the system for assessments. This includes writing the
 * assessment properties to disk and loading the assessment properties from disk
 * when the system starts. It also handles all of the changes to the objects and
 * holds them cached. There should only be one instance of this object running
 * in the system at any time.
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2012-11-13
 */

@Repository("assessmentDAO")
@DependsOn("projectProperties")
public class AssessmentDAO extends HibernateDaoSupport {

	protected final Log logger = LogFactory.getLog(getClass());

	// Default required by hibernate
	@Autowired
	public void setMySession(SessionFactory sessionFactory) {
		setSessionFactory(sessionFactory);
	}
	
	public AssessmentDAO() {
	}
	
	public Map<String, Set<Assessment>> getAllAssessmentsByCategory() {
		Map<String, Set<Assessment>> assessments = new TreeMap<String, Set<Assessment>>();
		for(Assessment assessment : getAllAssessments()) {
			Set<Assessment> sameCategory = assessments.get(assessment.getCategory());
			if(sameCategory == null) {
				sameCategory = new TreeSet<Assessment>(new DueDateComparator());
				assessments.put(assessment.getCategory(), sameCategory);
			}
			sameCategory.add(assessment);
		}
		return assessments;
	}

	public Collection<HandMarking> getHandMarkingList() {
		return ProjectProperties.getInstance().getHandMarkingDAO().getAllHandMarkings();
	}

	public Collection<Assessment> getAssessmentList() {
		return getAllAssessments();
	}
	
	/**
	 * Go through all assessments and remove test from them.
	 * 
	 * @param unitTestId unit test id to remove.
	 */
	public void unlinkUnitTest(long unitTestId) {
		// go through all assessments and remove the unit test from them
		for(Assessment assessment: getAllAssessments()){
			for(WeightedUnitTest test: assessment.getAllUnitTests()){
				if(test.getTest().getId() == unitTestId){
					assessment.removeUnitTest(test);
				}
			}
		}
	}
	
	/**
	 * Delete an assessment from the system.
	 * <p>
	 * 
	 * Iterates over all competitions and removes itself from them.
	 * Competitions are the only assessment modules that contain
	 * a link to the assessments they are used in.
	 * 
	 * @param id the id of the assessment 
	 */
	public void removeAssessment(long id) {
		Assessment assessmentToRemove = getAssessment(id);
		if(assessmentToRemove == null){
			return;
		}
		
		delete(assessmentToRemove);
	}
	
	/**
	 * Iterates over all assessments and removes itself from them.
	 * 
	 * @param id the id of the competition
	 */
	public void unlinkCompetition(long id) {
		// go through all assessments and remove the competition from them
		for (Assessment assessment : getAllAssessments()) {
			for (WeightedCompetition comp : assessment.getCompetitions()) {
				if (comp.getCompetition().getId() == id) {
					assessment.removeCompetition(comp);
				}
			}
		}
	}
	
	/**
	 * Iterates over all assessments and removes itself from them.
	 * 
	 * @param handMarkingId the id of the hand marking template 
	 */
	public void unlinkHandMarking(long handMarkingId) {
		// remove from assessments
		for(Assessment ass : getAllAssessments()) {
			Set<WeightedHandMarking> marking = ass.getHandMarking();
			for (WeightedHandMarking template : marking) {
				if(template.getHandMarking().getId() == handMarkingId) {
					ass.removeHandMarking(template);
					break;
				}
			}
		}
	}

//	TODO: DELETE
//	/**
//	 * Method to get a competition from a location
//	 * <p>
//	 * Loads the competitionProperties.xml from file into the cache.
//	 * 
//	 * @param location - the location of the competition
//	 * @return null - there is no competition at that location to be retrieved
//	 * @return comp - the competition at that location.
//	 */
//	private Competition getCompetitionFromDisk(String location) {
//		try {
//
//			File fXmlFile = new File(location + "/competitionProperties.xml");
//			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//			DocumentBuilder dBuilder;
//			dBuilder = dbFactory.newDocumentBuilder();
//			Document doc = dBuilder.parse(fXmlFile);
//			doc.getDocumentElement().normalize();
//
//			Competition comp = new Competition();
//
//			// name
//			comp.setName(doc.getElementsByTagName("name").item(0).getChildNodes().item(0).getNodeValue());
//
//			// tested
//			comp.setTested(Boolean.parseBoolean(doc.getElementsByTagName("tested").item(0).getChildNodes().item(0)
//					.getNodeValue()));
//
//			// can students create an arena
//			comp.setStudentCreatableArena(Boolean.parseBoolean(doc.getElementsByTagName("studentCreatableArena")
//					.item(0).getChildNodes().item(0).getNodeValue()));
//
//			// can students create repeatable arenas
//			comp.setStudentCreatableRepeatableArena(Boolean.parseBoolean(doc
//					.getElementsByTagName("studentCreatableRepeatableArena").item(0).getChildNodes().item(0)
//					.getNodeValue()));
//
//			// can tutors create repeatableArenas
//			comp.setTutorCreatableRepeatableArena(Boolean.parseBoolean(doc
//					.getElementsByTagName("tutorCreatableRepeatableArena").item(0).getChildNodes().item(0)
//					.getNodeValue()));
//
//			// is the competition hidden or not
//			if (doc.getElementsByTagName("hidden") != null && doc.getElementsByTagName("hidden").getLength() != 0) {
//				comp.setHidden(Boolean.parseBoolean(doc.getElementsByTagName("hidden").item(0).getChildNodes()
//						.item(0).getNodeValue()));
//			}
//
//			// first start date - only for calculated comps
//			if (doc.getElementsByTagName("firstStartDate") != null
//					&& doc.getElementsByTagName("firstStartDate").getLength() != 0) {
//				comp.setFirstStartDate(PASTAUtil.parseDate(doc.getElementsByTagName("firstStartDate").item(0)
//						.getChildNodes().item(0).getNodeValue()));
//			}
//
//			// frequency - only for calculated comps
//			if (doc.getElementsByTagName("frequency") != null
//					&& doc.getElementsByTagName("frequency").getLength() != 0) {
//				comp.setFrequency(new PASTATime(doc.getElementsByTagName("frequency").item(0).getChildNodes().item(0)
//						.getNodeValue()));
//			}
//
//			// arenas
//			// official
//
//			String[] arenaList = new File(location + "/arenas/").list();
//
//			if (arenaList != null) {
//
//				LinkedList<Arena> completedArenas = new LinkedList<Arena>();
//				LinkedList<Arena> outstandingArenas = new LinkedList<Arena>();
//
//				for (String arenaName : arenaList) {
//					Arena arena = getArenaFromDisk(location + "/arenas/" + arenaName);
//					if (arena != null) {
//						if (arena.getName().replace(" ", "").toLowerCase().equals("officialarena")) {
//							comp.setOfficialArena(arena);
//						} else if (new File(location + "/arenas/" + arenaName + "/results.csv").exists()
//								&& !arena.isRepeatable()) {
//							completedArenas.add(arena);
//						} else {
//							outstandingArenas.add(arena);
//						}
//					}
//				}
//
//				comp.setCompletedArenas(completedArenas);
//				comp.setOutstandingArenas(outstandingArenas);
//			} else {
//				Map<String, Arena> nothing = null;
//				comp.setOutstandingArenas(nothing);
//			}
//
//			return comp;
//		} catch (Exception e) {
//			StringWriter sw = new StringWriter();
//			PrintWriter pw = new PrintWriter(sw);
//			e.printStackTrace(pw);
//			logger.error("Could not read competition " + location + System.getProperty("line.separator")
//					+ sw.toString());
//			return null;
//		}
//	}

	// TODO: DELETE
//	/**
//	 * Method to get an arena from a location
//	 * <p>
//	 * Loads the arenaProperties.xml from file into the cache.
//	 * 
//	 * @param location - the location of the arena
//	 * @return null - there is no arena at that location to be retrieved
//	 * @return arena - the arena at that location.
//	 */
//	private Arena getArenaFromDisk(String location) {
//
//		try {
//			File fXmlFile = new File(location + "/arenaProperties.xml");
//			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//			DocumentBuilder dBuilder;
//			dBuilder = dbFactory.newDocumentBuilder();
//			Document doc = dBuilder.parse(fXmlFile);
//			doc.getDocumentElement().normalize();
//
//			Arena arena = new Arena();
//
//			// name
//			arena.setName(doc.getElementsByTagName("name").item(0).getChildNodes().item(0).getNodeValue());
//
//			// first start date
//			if (doc.getElementsByTagName("firstStartDate") != null
//					&& doc.getElementsByTagName("firstStartDate").getLength() != 0) {
//				arena.setFirstStartDate(PASTAUtil.parseDate(doc.getElementsByTagName("firstStartDate").item(0)
//						.getChildNodes().item(0).getNodeValue()));
//			}
//
//			// frequency
//			if (doc.getElementsByTagName("repeats") != null && doc.getElementsByTagName("repeats").getLength() != 0) {
//				arena.setFrequency(new PASTATime(doc.getElementsByTagName("repeats").item(0).getChildNodes().item(0)
//						.getNodeValue()));
//			}
//
//			// password
//			if (doc.getElementsByTagName("password") != null
//					&& doc.getElementsByTagName("password").getLength() != 0) {
//				arena
//						.setPassword(doc.getElementsByTagName("password").item(0).getChildNodes().item(0).getNodeValue());
//			}
//
//			// players
//			String[] players = new File(location + "/players/").list();
//
//			if (players != null) {
//				for (String player : players) {
//					Scanner in = new Scanner(new File(location + "/players/" + player));
//
//					String username = player.split("\\.")[0];
//					while (in.hasNextLine()) {
//						arena.addPlayer(username, in.nextLine());
//					}
//
//					in.close();
//				}
//			}
//
//			return arena;
//		} catch (Exception e) {
//			StringWriter sw = new StringWriter();
//			PrintWriter pw = new PrintWriter(sw);
//			e.printStackTrace(pw);
//			logger.error("Could not read arena " + location + System.getProperty("line.separator") + sw.toString());
//			return null;
//		}
//	}

	// TODO: DELETE
//	/**
//	 * Method to get an assessment from a location
//	 * <p>
//	 * Loads the assessmentProperties.xml from file into the cache.
//	 * 
//	 * @param location - the location of the assessment
//	 * @return null - there is no assessment at that location to be retrieved
//	 * @return assessment - the assessment at that location.
//	 */
//	private Assessment getAssessmentFromDisk(String location) {
//		try {
//
//			File fXmlFile = new File(location + "/assessmentProperties.xml");
//			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//			DocumentBuilder dBuilder;
//			dBuilder = dbFactory.newDocumentBuilder();
//			Document doc = dBuilder.parse(fXmlFile);
//			doc.getDocumentElement().normalize();
//
//			Assessment currentAssessment = new Assessment();
//
//			currentAssessment.setName(doc.getElementsByTagName("name").item(0).getChildNodes().item(0)
//					.getNodeValue());
//			currentAssessment.setMarks(Double.parseDouble(doc.getElementsByTagName("marks").item(0).getChildNodes()
//					.item(0).getNodeValue()));
//			try {
//				currentAssessment.setReleasedClasses(doc.getElementsByTagName("releasedClasses").item(0)
//						.getChildNodes().item(0).getNodeValue());
//			} catch (Exception e) {
//				// not released
//			}
//
//			try {
//				currentAssessment.setCategory(doc.getElementsByTagName("category").item(0).getChildNodes().item(0)
//						.getNodeValue());
//			} catch (Exception e) {
//				// no category
//			}
//
//			try {
//				currentAssessment.setCountUncompilable(Boolean.parseBoolean(doc
//						.getElementsByTagName("countUncompilable").item(0).getChildNodes().item(0).getNodeValue()));
//			} catch (Exception e) {
//				// no countUncompilable tag - defaults to true
//			}
//
//			try {
//				currentAssessment.setSpecialRelease(doc.getElementsByTagName("specialRelease").item(0)
//						.getChildNodes().item(0).getNodeValue());
//			} catch (Exception e) {
//				// not special released
//			}
//			currentAssessment.setNumSubmissionsAllowed(Integer.parseInt(doc
//					.getElementsByTagName("submissionsAllowed").item(0).getChildNodes().item(0).getNodeValue()));
//
//			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy");
//			currentAssessment.setDueDate(sdf.parse(doc.getElementsByTagName("dueDate").item(0).getChildNodes()
//					.item(0).getNodeValue()));
//
//			// load description from file
//			String description = "";
//			try {
//				Scanner in = new Scanner(new File(location + "/description.html"));
//				while (in.hasNextLine()) {
//					description += in.nextLine() + System.getProperty("line.separator");
//				}
//				in.close();
//			} catch (Exception e) {
//				description = "<pre>Error loading description" + System.getProperty("line.separator") + e + "</pre>";
//			}
//			currentAssessment.setDescription(description);
//
//			// add unit tests
//			NodeList unitTestList = doc.getElementsByTagName("unitTest");
//			if (unitTestList != null && unitTestList.getLength() > 0) {
//				for (int i = 0; i < unitTestList.getLength(); i++) {
//					Node unitTestNode = unitTestList.item(i);
//					if (unitTestNode.getNodeType() == Node.ELEMENT_NODE) {
//						Element unitTestElement = (Element) unitTestNode;
//
//						WeightedUnitTest weightedTest = new WeightedUnitTest();
//						weightedTest.setTest(
//								ProjectProperties.getInstance().getUnitTestDAO().getUnitTest(
//										Long.parseLong(unitTestElement.getAttribute("id"))));
//						weightedTest.setWeight(Double.parseDouble(unitTestElement.getAttribute("weight")));
//						if (unitTestElement.getAttribute("secret") != null
//								&& Boolean.parseBoolean(unitTestElement.getAttribute("secret"))) {
//							currentAssessment.addSecretUnitTest(weightedTest);
//						} else {
//							currentAssessment.addUnitTest(weightedTest);
//						}
//					}
//				}
//			}
//
//			// add hand marking
//			NodeList handMarkingList = doc.getElementsByTagName("handMarks");
//			if (handMarkingList != null && handMarkingList.getLength() > 0) {
//				for (int i = 0; i < handMarkingList.getLength(); i++) {
//					Node handMarkingNode = handMarkingList.item(i);
//					if (handMarkingNode.getNodeType() == Node.ELEMENT_NODE) {
//						Element handMarkingElement = (Element) handMarkingNode;
//
//						WeightedHandMarking weightedHandMarking = new WeightedHandMarking();
//						weightedHandMarking.setHandMarking(
//								ProjectProperties.getInstance().getHandMarkingDAO().getHandMarking(
//										Long.parseLong(handMarkingElement.getAttribute("id"))));
//						weightedHandMarking.setWeight(Double.parseDouble(handMarkingElement.getAttribute("weight")));
//						currentAssessment.addHandMarking(weightedHandMarking);
//					}
//				}
//			}
//
//			// add competitions
//			NodeList competitionList = doc.getElementsByTagName("competition");
//			if (competitionList != null && competitionList.getLength() > 0) {
//				for (int i = 0; i < competitionList.getLength(); i++) {
//					Node competitionNode = competitionList.item(i);
//					if (competitionNode.getNodeType() == Node.ELEMENT_NODE) {
//						Element competitionElement = (Element) competitionNode;
//
//						WeightedCompetition weightedComp = new WeightedCompetition();
//						weightedComp.setCompetition(allCompetitions.get(competitionElement.getAttribute("name")));
//						weightedComp.setWeight(Double.parseDouble(competitionElement.getAttribute("weight")));
//						weightedComp.getCompetition().addAssessment(currentAssessment);
//						currentAssessment.addCompetition(weightedComp);
//					}
//				}
//			}
//
//			return currentAssessment;
//		} catch (Exception e) {
//			StringWriter sw = new StringWriter();
//			PrintWriter pw = new PrintWriter(sw);
//			e.printStackTrace(pw);
//			logger.error("Could not read assessment " + location + System.getProperty("line.separator")
//					+ sw.toString());
//			return null;
//		}
//	}


	/**
	 * Update a hand marking template
	 * <p>
	 * Updates cache and writes everything to file. Pretty much used all the time,
	 * because when you make a new hand marking template, you generate one based
	 * on defaults.
	 * 
	 * @param newHandMarking the new hand marking template
	 */
	public void updateHandMarking(HandMarking newHandMarking) {
		ProjectProperties.getInstance().getHandMarkingDAO().saveOrUpdate(newHandMarking);
	}

	/**
	 * Creates a default hand marking template. Default columns are:
	 * <ul>
	 * <li>Poor : 0%</li>
	 * <li>Acceptable : 50%</li>
	 * <li>Excellent : 100%</li>
	 * </ul>
	 * Default rows are:
	 * <ul>
	 * <li>Formatting : 20%</li>
	 * <li>Code Reuse : 40%</li>
	 * <li>Variable naming : 40%</li>
	 * </ul>
	 * Default descriptions are empty.
	 * 
	 * @param newHandMarking the new hand marking
	 */
	public void newHandMarking(NewHandMarkingForm newHandMarking) {

		HandMarking newMarking = new HandMarking();
		newMarking.setName(newHandMarking.getName());

		newMarking.addColumn(new WeightedField("Poor", 0));
		newMarking.addColumn(new WeightedField("Acceptable", 0.5));
		newMarking.addColumn(new WeightedField("Excellent", 1));

		newMarking.addRow(new WeightedField("Formatting", 0.2));
		newMarking.addRow(new WeightedField("Code Reuse", 0.4));
		newMarking.addRow(new WeightedField("Variable Naming", 0.4));

		for (WeightedField column : newMarking.getColumnHeader()) {
			for (WeightedField row : newMarking.getRowHeader()) {
				newMarking.addData(new HandMarkData(column, row, ""));
			}
		}

		updateHandMarking(newMarking);
	}
	
// TODO: DELETE
//	/**
//	 * Write an arena to disk
//	 * 
//	 * @param arena the arena getting written to disk
//	 * @param comp the comp the arena belongs to
//	 */
//	public void writeArenaToDisk(Arena arena, Competition comp) {
//		if (arena != null && comp != null) {
//			// ensure folder exists for plaers
//			(new File(comp.getFileLocation() + "/arenas/" + arena.getName() + "/players/")).mkdirs();
//
//			// write arena properties
//			try {
//
//				new File(comp.getFileLocation() + "/arenas/" + arena.getName()).mkdirs();
//
//				PrintStream arenaOut = new PrintStream(comp.getFileLocation() + "/arenas/" + arena.getName()
//						+ "/arenaProperties.xml");
//				arenaOut.print(arena);
//				arenaOut.close();
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			}
//
//			// write players
//			for (Entry<String, Set<String>> entry : arena.getPlayers().entrySet()) {
//				updatePlayerInArena(comp, arena, entry.getKey(), entry.getValue());
//			}
//
//		}
//	}

// TODO: DELETE
//	/**
//	 * Update the players taking part in an arena
//	 * <p>
//	 * Updates in both cache and disk. If the set players is empty, all players
//	 * for this user are removed. The players are stored as $username$.players
//	 * files where the name of each player is on a separate line within
//	 * $compLocation$/arenas/$arenaName/players/ This was done to ensure there
//	 * were limited concurrency problems. This could obviously be moved onto the
//	 * database but I didn't want to at the time because I wanted to limit the
//	 * reliance on a database.
//	 * 
//	 * @param comp the competition
//	 * @param arena the arena
//	 * @param username the name of the user updating which of their players are
//	 *          participating in the arena.
//	 * @param players the set of players, if empty, all players will be removed.
//	 */
//	public void updatePlayerInArena(Competition comp, Arena arena, String username, Set<String> players) {
//		if (comp != null && arena != null && username != null && !username.isEmpty() && players != null
//				&& players.isEmpty()) {
//			// if the players set is empty, delete them from the arena.
//			new File(comp.getFileLocation() + "/arenas/" + arena.getName() + "/players/" + username + ".players")
//					.delete();
//		} else if (comp != null && arena != null && username != null && !username.isEmpty() && players != null
//				&& !players.isEmpty()) {
//			try {
//				(new File(comp.getFileLocation() + "/arenas/" + arena.getName() + "/players/")).mkdirs();
//
//				PrintStream out = new PrintStream(comp.getFileLocation() + "/arenas/" + arena.getName() + "/players/"
//						+ username + ".players");
//
//				for (String player : players) {
//					out.println(player);
//				}
//
//				out.close();
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			}
//		}
//	}
	
	@Deprecated
	public void save(Assessment assessment) {
		saveOrUpdate(assessment);
	}
	
	@Deprecated
	public void update(Assessment assessment) {
		saveOrUpdate(assessment);
	}
	
	@Deprecated
	public void merge(Assessment assessment) {
		saveOrUpdate(assessment);
	}
	
	public void delete(Assessment assessment) {
		try {
			getHibernateTemplate().delete(assessment);
			logger.info("Deleted assessment " + assessment.getName());
		} catch (Exception e) {
			logger.error("Could not delete assessment " + assessment.getName(), e);
		}
	}
	
	public void saveOrUpdate(Assessment assessment) {
		long id = assessment.getId();
		getHibernateTemplate().saveOrUpdate(assessment);
		logger.info((id == assessment.getId() ? "Updated" : "Created") +
				" assessment " + assessment.getName());
	}
	
	@SuppressWarnings("unchecked")
	public List<Assessment> getAllAssessments() {
		return getHibernateTemplate().find("FROM Assessment");
	}
	
	public Assessment getAssessment(long id) {
		return getHibernateTemplate().get(Assessment.class, id);
	}
}
