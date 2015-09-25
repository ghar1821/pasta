package pasta.testing;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

import pasta.testing.task.Task;

public class AntJob {
	private static final Logger logger = Logger.getLogger(AntJob.class);
	
	private AntResults results;
	private File homeDirectory;
	private Runner runner;
	private String[] targets;
	private Map<String, List<String>> dependencies;
	
	private List<Task> setupTasks;
	private List<Task> cleanupTasks;
	
	public AntJob(File homeDirectory, Runner runner, String... targets) {
		this(homeDirectory, runner, new HashMap<String, List<String>>(), targets);
	}
	
	public AntJob(File homeDirectory, Runner runner, Map<String, List<String>> dependencies, String... targets) {
		this.targets = targets;
		this.results = new AntResults(targets);
		this.homeDirectory = homeDirectory;
		this.runner = runner;
		this.dependencies = dependencies;
		
		this.setupTasks = new LinkedList<Task>();
		this.cleanupTasks = new LinkedList<Task>();
	}
	
	public void addDependency(String target, String dependsOn) {
		List<String> existing = dependencies.get(target);
		if(existing == null) {
			existing = new LinkedList<String>();
			dependencies.put(target, existing);
		}
		existing.add(dependsOn);
	}
	
	public void addSetupTask(Task task) {
		this.setupTasks.add(task);
	}
	
	public void addCleanupTask(Task task) {
		this.cleanupTasks.add(task);
	}
	
	public final void run() {
		setup();
		
		File buildFile = runner.createBuildFile(new File(homeDirectory, "build.xml"));
		logger.debug("Created build file " + buildFile);
		
		ProjectHelper projectHelper = ProjectHelper.getProjectHelper();
		Project project = new Project();
		
		project.setUserProperty("ant.file", buildFile.getAbsolutePath());
		project.setBasedir(homeDirectory.getAbsolutePath());
		DefaultLogger consoleLogger = new DefaultLogger();
		
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		PrintStream runErrors = new PrintStream(output);
		consoleLogger.setOutputPrintStream(runErrors);
		consoleLogger.setErrorPrintStream(runErrors);
		consoleLogger.setMessageOutputLevel(Project.MSG_VERBOSE);
		project.addBuildListener(consoleLogger);
		project.init();
		
		project.addReference("ant.projectHelper", projectHelper);
		projectHelper.parse(project, buildFile);			
		
		logger.debug("Executing targets...");
		for(String target : this.targets) {
			doTarget(project, target, output);
		}
		logger.debug("Finished executing targets");
		
		runErrors.close();
		
		logger.debug("Cleaning up...");
		cleanup();
		logger.debug("Cleanup finished");
	}
	
	private void doTarget(Project project, String target, ByteArrayOutputStream output) {
		logger.debug("Executing target \"" + target + "\"");
		List<String> dependsOn = dependencies.get(target);
		if(dependsOn != null) {
			for(String other : dependsOn) {
				if(!results.isSuccess(other)) {
					logger.debug("Skipping target due to failure of \"" + other + "\"");
					return;
				}
			}
		}
		
		boolean success = true;
		try {
			project.executeTarget(target);
			logger.debug("Target finished successfully.");
		} catch (BuildException e) {
			logger.debug("Target not successful.");
			if(!e.getMessage().contains("Compile failed") && !e.getMessage().contains("apply returned: 1")) {
				logger.error("Error on task " + target + " at " + homeDirectory, e);
			}
			success = false;
		} catch (Exception e) {
			logger.debug("Target not successful - unknown error");
			logger.error("Error on task " + target + " at " + homeDirectory, e);
			success = false;
		}
		
		String contents = output.toString();
		logger.trace("Target content:\n" + contents);
		results.append(target, contents);
		results.setSuccess(target, success);
		output.reset();
	}
	
	private void setup() {
		performTasks(setupTasks);
	}

	private void cleanup() {
		performTasks(cleanupTasks);
	}
	
	private void performTasks(List<Task> tasks) {
		for(Task task : tasks) {
			if(!task.go()) {
				logger.warn("Task " + task.toString() + " not completed successfully.");
			}
		}
	}

	public AntResults getResults() {
		return results;
	}

	public File getHomeDirectory() {
		return homeDirectory;
	}
}
