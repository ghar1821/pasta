/*
MIT License

Copyright (c) 2012-2017 PASTA Contributors (see CONTRIBUTORS.txt)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package pasta.testing;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import pasta.docker.CombinedCommandResult;
import pasta.docker.DockerManager;
import pasta.docker.ExecutionContainer;
import pasta.testing.task.Task;

public class AntJob {
	private static final Logger logger = Logger.getLogger(AntJob.class);
	
	private AntResults results;
	private Runner runner;
	private ExecutionContainer container;
	private String[] targets;
	private Map<String, List<String>> dependencies;
	
	private List<Task> setupTasks;
	private List<Task> cleanupTasks;
	
	public AntJob(Runner runner, ExecutionContainer container, String... targets) {
		this(runner, container, new HashMap<String, List<String>>(), targets);
	}
	
	public AntJob(Runner runner, ExecutionContainer container, Map<String, List<String>> dependencies, String... targets) {
		this.targets = targets;
		
		this.results = new AntResults(this.targets);
		this.results.registerExtraLabel("docker");
		
		this.runner = runner;
		this.container = container;
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
		
		File buildFile = runner.createBuildFile(new File(container.getSrcLoc(), "build.xml"));
		logger.debug("Created build file " + buildFile);
		
		DockerManager.instance().runContainer(container);
		if(container.getId() == null) {
			results.setSuccess("docker", false);
			results.append("docker", "Submission already running.");
		} else{
			results.setSuccess("docker", true);
			logger.debug("Executing targets...");
			for(String target : this.targets) {
				doTarget(target);
			}
			logger.debug("Finished executing targets");
		}
		
		logger.debug("Cleaning up...");
		cleanup();
		logger.debug("Cleanup finished");
	}
	
	private void doTarget(String target) {
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
		
		CombinedCommandResult rs = DockerManager.instance().runAntTarget(container, target);
		boolean success = rs.getError().isEmpty();
		
		if(!success) {
			logger.debug("Target not successful.");
			logger.trace("Target error:\n" + rs.getError());
		}
		
		logger.trace("Target output:\n" + rs.getOutput());
		results.append(target, rs.getCombined());
		results.setSuccess(target, success);
	}
	
	private void setup() {
		performTasks(setupTasks);
	}

	private void cleanup() {
		performTasks(cleanupTasks);
		DockerManager.instance().removeContainer(container.getId());
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
}
