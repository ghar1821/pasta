package pasta.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import pasta.domain.result.ArenaResult;
import pasta.domain.result.CompetitionResult;
import pasta.domain.template.Arena;
import pasta.domain.template.Competition;
import pasta.domain.upload.NewCompetition;
import pasta.repository.AssessmentDAO;
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
	
	private ExecutionScheduler scheduler;
	
	@Autowired
	public void setMyScheduler(ExecutionScheduler myScheduler) {
		this.scheduler = myScheduler;
	}
	
	@Autowired
	private ApplicationContext context;

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
	
	public void updateCompetition(NewCompetition form) {
		Competition thisComp = getCompetition(form.getName().replace(" ", ""));
		if (thisComp == null){
			thisComp = new Competition();
		}
		thisComp.setName(form.getName());
		if(!thisComp.isCalculated()){
			thisComp.getOfficialArena().setFrequency(form.getFrequency());
		}
		thisComp.setFirstStartDate(form.getFirstStartDate());
		thisComp.setFrequency(form.getFrequency());
		try {

			// create space on the file system.
			(new File(thisComp.getFileLocation() + "/codse/")).mkdirs();

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
		try {
			currComp.getOutstandingArenas().add(arena);
			assDao.addCompetition(currComp);
			
			PrintStream out = new PrintStream(currComp.getFileLocation()
					+ "/competitionProperties.xml");
			out.print(currComp);
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// schedule it for execution
		if(currComp.isLive()){
			scheduler.save(new Job("PASTACompetitionRunner", currComp.getShortName()+"#PASTAArena#"+arena.getName(), arena.getFirstStartDate()));
		}
	}
	
}
