package pasta.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.hibernate.persister.entity.Loadable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import pasta.domain.players.PlayerHistory;
import pasta.domain.players.PlayerResult;
import pasta.domain.result.ArenaResult;
import pasta.domain.result.CompetitionResult;
import pasta.domain.template.Arena;
import pasta.domain.template.Competition;
import pasta.domain.upload.NewCompetition;
import pasta.domain.upload.NewPlayer;
import pasta.repository.AssessmentDAO;
import pasta.repository.PlayerDAO;
import pasta.repository.ResultDAO;
import pasta.scheduler.ExecutionScheduler;
import pasta.scheduler.Job;
import pasta.util.PASTAUtil;
import pasta.util.ProjectProperties;

@Service("competitionManager")
@Repository
/**
 * Competition manager.
 * 
 * Manages interaction between controller and data.
 * 
 * @author Alex
 *
 */
public class CompetitionManager {
	
	private AssessmentDAO assDao = ProjectProperties.getInstance().getAssessmentDAO();
	private ResultDAO resultDAO = ProjectProperties.getInstance().getResultDAO();
	private PlayerDAO playerDAO = ProjectProperties.getInstance().getPlayerDAO();
	
	@Autowired
	private ApplicationContext context;
	
	private ExecutionScheduler scheduler;
	
	@Autowired
	public void setMyScheduler(ExecutionScheduler myScheduler) {
		this.scheduler = myScheduler;
	}

	// Validator for the submission

	public static final Logger logger = Logger
			.getLogger(CompetitionManager.class);
	

	// new 
	public void addCompetition(NewCompetition form) {
		Competition thisComp = new Competition();
		thisComp.setName(form.getName());
		thisComp.setTested(false);
		thisComp.setFirstStartDate(form.getFirstStartDate());
		thisComp.setFrequency(form.getFrequency());
		if(form.getType().equalsIgnoreCase("arena")){
			thisComp.setOutstandingArenas(new LinkedList<Arena>());
			thisComp.setCompletedArenas(new LinkedList<Arena>());
			Arena officialArena = new Arena();
			officialArena.setName("Official Arena");
			officialArena.setFirstStartDate(form.getFirstStartDate());
			officialArena.setFrequency(form.getFrequency());
			thisComp.setOfficialArena(officialArena);
		}

		try {

			// create space on the file system.
			(new File(thisComp.getFileLocation() + "/code/")).mkdirs();

			// generate competitionProperties
			PrintStream out = new PrintStream(thisComp.getFileLocation()
					+ "/competitionProperties.xml");
			out.print(thisComp);
			out.close();

			// unzip the uploaded code into the code folder. (if exists)
			if (form.getFile() != null && !form.getFile().isEmpty()) {
				// unpack
				form.getFile().transferTo(
						new File(thisComp.getFileLocation() + "/code/"
								+ form.getFile().getOriginalFilename()));
				PASTAUtil.extractFolder(thisComp.getFileLocation()
						+ "/code/" + form.getFile().getOriginalFilename());
				form.getFile().getInputStream().close();
				FileUtils.forceDelete(new File(thisComp.getFileLocation()
						+ "/code/" + form.getFile().getOriginalFilename()));
			}

			FileUtils.deleteDirectory((new File(thisComp.getFileLocation()
					+ "/test/")));

			assDao.addCompetition(thisComp);
		} catch (Exception e) {
			(new File(thisComp.getFileLocation())).delete();
			logger.error("Competition " + thisComp.getName()
					+ " could not be created successfully!"
					+ System.getProperty("line.separator") + e);
		}
	}
	
	public void updateCompetition(Competition compForm) {
		Competition thisComp = getCompetition(compForm.getName().replace(" ", ""));
		
		thisComp.setTutorCreatableRepeatableArena(compForm.isTutorCreatableRepeatableArena());
		thisComp.setStudentCreatableArena(compForm.isStudentCreatableArena());
		thisComp.setStudentCreatableRepeatableArena(compForm.isStudentCreatableRepeatableArena());
		
		thisComp.setFrequency(compForm.getFrequency());
		thisComp.setFirstStartDate(compForm.getFirstStartDate());
		
		if(!thisComp.isCalculated()){
			thisComp.getOfficialArena().setFirstStartDate(compForm.getFirstStartDate());
			thisComp.getOfficialArena().setFrequency(compForm.getFrequency());
		}
		
		assDao.addCompetition(thisComp);
	}
	
	public void updateCompetition(NewCompetition form) {
		Competition thisComp = getCompetition(form.getName().replace(" ", ""));
		boolean newComp = false;
		if (thisComp == null){
			thisComp = new Competition();
			newComp = true;
		}
		if(newComp){
			thisComp.setName(form.getName());
			if(!thisComp.isCalculated()){
				thisComp.getOfficialArena().setFrequency(form.getFrequency());
			}
			thisComp.setFirstStartDate(form.getFirstStartDate());
			thisComp.setFrequency(form.getFrequency());
		}
		try {

			if((new File(thisComp.getFileLocation() + "/code/")).exists()){
				FileUtils.deleteDirectory((new File(thisComp.getFileLocation() + "/code/")));
			}
			
			// create space on the file system.
			(new File(thisComp.getFileLocation() + "/code/")).mkdirs();

			// generate competitionProperties
			if(newComp){
				PrintStream out = new PrintStream(thisComp.getFileLocation()
						+ "/competitionProperties.xml");
				out.print(thisComp);
				out.close();
			}

			// unzip the uploaded code into the code folder. (if exists)
			if (form.getFile() != null && !form.getFile().isEmpty()) {
				// unpack
				form.getFile().transferTo(
						new File(thisComp.getFileLocation() + "/code/"
								+ form.getFile().getOriginalFilename()));
				PASTAUtil.extractFolder(thisComp.getFileLocation()
						+ "/code/" + form.getFile().getOriginalFilename());
				form.getFile().getInputStream().close();
				FileUtils.forceDelete(new File(thisComp.getFileLocation()
						+ "/code/" + form.getFile().getOriginalFilename()));
			}

			FileUtils.deleteDirectory((new File(thisComp.getFileLocation()
					+ "/test/")));

			getCompetition(thisComp.getShortName()).setTested(false);
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			(new File(thisComp.getFileLocation())).delete();
			logger.error("Competition " + thisComp.getName()
					+ " could not be created successfully!"
					+ System.getProperty("line.separator") + sw.toString());
		}
	}
	
	public void addCompetition(Competition form) {
		try {

			// create space on the file system.
			(new File(form.getFileLocation() + "/code/")).mkdirs();

			assDao.addCompetition(form);

			// generate unitTestProperties
			PrintStream out = new PrintStream(form.getFileLocation()
					+ "/competitionProperties.xml");
			out.print(getCompetition(form.getShortName()));
			out.close();
			
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			(new File(form.getFileLocation())).delete();
			logger.error("Competition " + form.getName()
					+ " could not be updated successfully!"
					+ System.getProperty("line.separator") + pw);
		}
	}

	public void removeCompetition(String competitionName) {
		assDao.removeCompetition(competitionName);
	}

	public Collection<Competition> getCompetitionList() {
		return assDao.getCompetitionList();
	}

	public Competition getCompetition(String competitionName) {
		return assDao.getCompetition(competitionName);
	}

	public CompetitionResult getCompetitionResult(String competitionName) {
		return resultDAO.getCompetitionResult(competitionName);
	}
	
	public ArenaResult getCalculatedCompetitionResult(String competitionName){
		return resultDAO.getCalculatedCompetitionResult(competitionName);
	}

	public void addArena(Arena arena, Competition currComp) {
		if(currComp.getArena(arena.getName()) == null){
			currComp.addNewArena(arena);
			scheduler.save(new Job("PASTACompetitionRunner", 
					currComp.getShortName()+"#PASTAArena#"+arena.getName(), 
					arena.getNextRunDate()));
			assDao.writeArenaToDisk(arena, currComp);
		}
	}

	public void addPlayer(NewPlayer playerForm, String username, 
			String competitionShortName, BindingResult result) {
		// copy player to temp location
		
		String compLocation = ProjectProperties.getInstance().getProjectLocation()
				+ "/submissions/" + username + "/competitions/"
				+ competitionShortName;
		
		
		if (playerForm.getFile() != null && !playerForm.getFile().isEmpty()) {
			
			if(new File(compLocation + "/temp/").exists()){
				try {
					FileUtils.deleteDirectory(new File(compLocation + "/temp/"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			(new File( compLocation + "/temp/code")).mkdirs();

			// unpack if necessary
			try {
				playerForm.getFile().transferTo(
						new File( compLocation + "/temp/code/"
								+ playerForm.getFile().getOriginalFilename()));
				if(playerForm.getFile().getOriginalFilename().endsWith("zip")){
					PASTAUtil.extractFolder(compLocation + "/temp/code/"
							+ playerForm.getFile().getOriginalFilename());
				}
				playerForm.getFile().getInputStream().close();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else{
			result.rejectValue("file", "Player.noFile");
			return;
		}
		
		// validate player
		try {
			// copy over competition
			FileUtils.copyDirectory(new File(ProjectProperties.getInstance().getProjectLocation()
					+ "/template/competition/" + competitionShortName + "/code/"),
					new File(compLocation + "/temp/test/"));
			
			// copy over player
			FileUtils.copyDirectory(new File(compLocation + "/temp/code"),
					new File(compLocation + "/temp/test/"));
			
			// compile
			File buildFile = new File(compLocation + "/temp/test/build.xml");

			ProjectHelper projectHelper = ProjectHelper.getProjectHelper();
			Project project = new Project();

			project.setUserProperty("ant.file", buildFile.getAbsolutePath());
			project.setBasedir(compLocation + "/temp/test/");
			DefaultLogger consoleLogger = new DefaultLogger();
			PrintStream runErrors = new PrintStream(compLocation + "/temp/run.errors");
			consoleLogger.setOutputPrintStream(runErrors);
			consoleLogger.setMessageOutputLevel(Project.MSG_VERBOSE);
			project.addBuildListener(consoleLogger);
			project.init();

			project.addReference("ant.projectHelper", projectHelper);
			projectHelper.parse(project, buildFile);
			
			// run validate target
			try {
				project.executeTarget("build");
				project.executeTarget("validate");
			} catch (BuildException e) {
//				logger.error("Could not validate player for user: " + username + "\r\n" + e);
//				PrintStream compileErrors = new PrintStream(compLocation + "/temp/validation.errors");
//				compileErrors.print(e.toString().replaceAll(".*/temp/test/" , "folder "));
//				compileErrors.close();
			}
			
			runErrors.flush();
			runErrors.close();
		} catch (IOException e) {
			logger.error("Could not validate player for user: " + username + "\r\n" + e);
		}
		
		// if validation.errors exist, read then report them
		// scrape compiler errors from run.errors
		try{
			Scanner in = new Scanner (new File(compLocation + "/temp/run.errors"));
			boolean containsError = false;
			boolean importantData = false;
			String output = "";
			while(in.hasNextLine()){
				String line = in.nextLine();
				if(line.contains(": error:")){
					containsError = true;
				}
				if(line.contains("[javac] Files to be compiled:")){
					importantData = true;
				}
				if(importantData){
					output += line.replace("[javac]", "").replaceAll(".*temp\\\\test\\\\","") + System.getProperty("line.separator");
				}
			}
			in.close();
			
			if(new File(compLocation + "/temp/validation.errors").exists()){
				if(!containsError){
					output = "";
				}
				Scanner valIn = new Scanner(new File(compLocation + "/temp/validation.errors"));
				while(valIn.hasNextLine()){
					output += valIn.nextLine() + System.getProperty("line.separator");;
				}
				valIn.close();
				containsError=true;
			}
			
			if(containsError){
				PrintStream compileErrors = new PrintStream(compLocation + "/temp/validation.errors");
				compileErrors.print(output);
				compileErrors.close();
				result.rejectValue("file", "Player.errors", output);
			}
		}
		catch (Exception e){
			// do nothing
		}
		if(!result.hasErrors()){
			PlayerResult newPlayer = new PlayerResult();
			
			try {
				Scanner in = new Scanner(new File(compLocation + "/temp/player.info"));
				while(in.hasNextLine()){
					String line = in.nextLine();
					if(line.startsWith("name=")){
						newPlayer.setName(line.replaceFirst("name=", ""));
					}
					else if(line.startsWith("uploadDate=")){
						try {
							newPlayer.setFirstUploaded(PASTAUtil.parseDate(line.replaceFirst("uploadDate=", "")));
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}
				}
				in.close();
			} catch (FileNotFoundException e) {
				logger.error("player.info was not created for user " + username);
				return;
			}
			
			PlayerHistory history = playerDAO.getPlayerHistory(username, competitionShortName, newPlayer.getName());
			if(history == null){
				history = new PlayerHistory(newPlayer.getName());
			}
			
			// if no errors and the player has the same name as an existing player
			// retire the old player.
			retirePlayer(username, competitionShortName, newPlayer.getName());
			history.setActivePlayer(newPlayer);
			(new File(compLocation+"/"+newPlayer.getName()+"/active/")).mkdirs();
			try {
				FileUtils.copyDirectory(new File(compLocation + "/temp/code/"),
						new File(compLocation+"/"+newPlayer.getName()+"/active/code/"));
				FileUtils.copyFile(new File(compLocation+"/temp/player.info"), 
						new File(compLocation+"/"+newPlayer.getName()+"/active/player.info"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			FileUtils.deleteDirectory(new File(compLocation + "/temp/"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void retirePlayer(String username, 
			String competitionName, String playerName){
		if(playerDAO.getPlayerHistory(username, competitionName, playerName) != null){
			PlayerResult oldPlayer = playerDAO.getPlayerHistory(username, competitionName, playerName).getActivePlayer();
			if(oldPlayer != null){
				playerDAO.getPlayerHistory(username, competitionName, playerName).retireActivePlayer();
				
				String compLocation = ProjectProperties.getInstance().getProjectLocation()
						+ "/submissions/" + username + "/competitions/"
						+ competitionName;
				
				try {
					// archive the old player
					FileUtils.moveDirectory(
							new File(compLocation + "/" + playerName
									+ "/active"),
							new File(compLocation
									+ "/"
									+ playerName
									+ "/retired/"
									+ PASTAUtil.formatDate(oldPlayer
											.getFirstUploaded())));
				} catch (IOException e) {
					logger.error("Could not retire player " + playerName 
							+ " of user " + username 
							+ " for competition " + competitionName 
							+ ". Reason : " + e);
				}
			}
		}
	}

	public Collection<PlayerHistory> getPlayers(String username, String competitionName) {
		return playerDAO.getPlayerHistory(username, competitionName);
	}
	
	public Collection<PlayerHistory> getLatestPlayers(String username, String competitionName) {
		return playerDAO.loadPlayerHistory(username, competitionName).values();
	}

	public void addPlayerToArena(String username, String competitionName,
			String arenaName, String playerName) {
		// make sure player is legitimate
		if(playerDAO.getPlayerHistory(username, competitionName, playerName) != null &&
				assDao.getCompetition(competitionName)!= null &&
				assDao.getCompetition(competitionName).getArena(arenaName) != null){
			// add player to arena if arena exists
			assDao.getCompetition(competitionName).getArena(arenaName).addPlayer(username, playerName);
			
			// write to disk
			assDao.updatePlayerInArena(assDao.getCompetition(competitionName)
					, assDao.getCompetition(competitionName).getArena(arenaName)
					, username
					, assDao.getCompetition(competitionName).getArena(arenaName).getPlayers().get(username));
		}
	}
	
	public void removePlayerFromArena(String username, String competitionName,
			String arenaName, String playerName) {
		// make sure player is legitimate
		if(playerDAO.getPlayerHistory(username, competitionName, playerName) != null &&
				assDao.getCompetition(competitionName)!= null &&
				assDao.getCompetition(competitionName).getArena(arenaName) != null){
			// add player to arena if arena exists
			assDao.getCompetition(competitionName).getArena(arenaName).removePlayer(username, playerName);
			
			// if no player
			if(assDao.getCompetition(competitionName).getArena(arenaName).getPlayers() == null ||
					assDao.getCompetition(competitionName).getArena(arenaName).getPlayers().get(username) == null ||
					assDao.getCompetition(competitionName).getArena(arenaName).getPlayers().get(username).isEmpty()){
				// delete file
				new File(assDao.getCompetition(competitionName).getFileLocation() 
						+ "/arenas/" + assDao.getCompetition(competitionName).getArena(arenaName).getName()
						+ "/players/"+ username + ".players").delete();
			}
		}
	}

	public ArenaResult getArenaResults(Competition currComp, Arena arena) {
		if(currComp != null && arena != null){
			return resultDAO.loadArenaResult(currComp.getFileLocation()+"/arenas/"+arena.getName());
		}
		return null;
	}
	
}
