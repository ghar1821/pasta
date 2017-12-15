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

package pasta.docker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.exception.ConflictException;
import com.github.dockerjava.api.exception.DockerClientException;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.BuildResponseItem;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.InternetProtocol;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.BuildImageResultCallback;
import com.github.dockerjava.core.command.ExecStartResultCallback;

import pasta.util.Copy;
import pasta.util.PASTAUtil;
import pasta.util.ProjectProperties;
import pasta.util.WhichProgram;
import pasta.util.io.DualByteArrayOutputStream;

public class DockerManager {
	
	protected static Logger logger = Logger.getLogger(DockerManager.class);
	
	public static final String PASTA_SRC = "/pasta/src";
	public static final String PASTA_OUT = "/pasta/out";
	public static final String PASTA_BIN = "/pasta/bin";
	public static final String PASTA_LIB = "/pasta/lib";
	public static final String WORK_DIR = "/sandbox";
	
	private Object removeLock = new Object();
	private Set<String> deleteLater;
	
	private static DockerManager instance;
	public static DockerManager instance() {
		if(instance == null) {
			return new DockerManager();
		}
		return instance;
	}
	
	private DockerClient dockerClient;
	
	private DockerManager() {
		DefaultDockerClientConfig config = DefaultDockerClientConfig
				.createDefaultConfigBuilder()
				.withDockerHost("unix:///var/run/docker.sock")
				.build();
		dockerClient = DockerClientBuilder
				.getInstance(config)
				.build();
		
		initialiseImages();
		deleteLater = new LinkedHashSet<>();
		instance = this;
	}
	
	private void initialiseImages() {
		for(DockerBuildFile buildFile : LanguageManager.getInstance().getDockerBuildFiles()) {
			Image image = getImage(buildFile.getTag());
			if(image == null || image.getCreated()*1000L < buildFile.getFile().lastModified()) {
				installImage(buildFile);
			} else {
				buildFile.registerSuccess(image.getId());
				logger.debug(String.format("Image %s already exists with ID %s", buildFile.getTag(), buildFile.getId()));
			}
		}
	}
	
	public boolean isImageInstalled(String tag) {
		return getImage(tag) != null;
	}
	public Image getImage(String tag) {
		List<Image> images = dockerClient.listImagesCmd()
				.withImageNameFilter(tag)
				.exec();
		if(images.isEmpty()) {
			return null;
		}
		return images.iterator().next();
	}
	
	public void removeImage(String tag) {
		Image image = getImage(tag);
		if(image == null) {
			return;
		}
		
		Map<String, String> labels = new HashMap<>();
		labels.put("image", tag);
		
		List<Container> containers = dockerClient
				.listContainersCmd()
				.withLabelFilter(labels)
				.exec();
		
		logger.info(String.format("Removing %d containers with label %s.", containers.size(), tag));
		for (Container container : containers) {
			removeContainer(container.getId());
		}
		
		logger.info("Removing image " + tag);
		dockerClient.removeImageCmd(image.getId()).exec();
	}
	
	public void removeContainer(String id) {
		try {
			try { // Container should exist
				dockerClient.inspectContainerCmd(id).exec();
			} catch(NotFoundException e) { return; }
			dockerClient.stopContainerCmd(id).exec();
			dockerClient.removeContainerCmd(id).exec();
			try { // Container should not exist
				dockerClient.inspectContainerCmd(id).exec();
				throw new DockerException("Should have removed container", 400);
			} catch(NotFoundException e) {}
		} catch(DockerException e) {
			synchronized (removeLock) {
				deleteLater.add(id);
			}
		}
	}
	
	@Scheduled(fixedDelay = 30000)
	public void clearBrokenContainers() {
		Set<String> ids;
		synchronized (removeLock) {
			ids = new LinkedHashSet<>(deleteLater);
			deleteLater = new LinkedHashSet<>();
		}
		for(String id : ids) {
			logger.info("Attempting to re-remove container " + id);
			removeContainer(id);
		}
	}
	
	private void installImage(DockerBuildFile buildFile) {
		removeImage(buildFile.getTag());
		
		logger.info("Building Docker image: " + buildFile.getTag());
		
		File baseDir = buildFile.getFile().getParentFile();

		BuildImageResultCallback callback = new BuildImageResultCallback() {
			@Override
			public void onNext(BuildResponseItem item) {
				String content = StringUtils.stripEnd(item.getStream(), "\n");
				if (content != null && !content.isEmpty()) {
					logger.debug(content);
				}
				super.onNext(item);
			}
		};

		try {
			// Load bin files
			try {
				File bin = PASTAUtil.getTemplateResource("bin/");
				Copy.copy(bin.toPath(), baseDir.toPath());
			} catch (IOException e) {}
			
			Set<String> tags = new HashSet<>();
			tags.add(buildFile.getTag());
			String id = dockerClient.buildImageCmd(baseDir)
				.withTags(tags)
				.withBuildArg("workDir", DockerManager.WORK_DIR)
				.withBuildArg("binDir", DockerManager.PASTA_BIN)
				.exec(callback)
				.awaitImageId();
			buildFile.registerSuccess(id);
			logger.info("Built: " + id);
		} catch (DockerClientException e) {
			buildFile.registerFailure();
			logger.error("Error building image " + buildFile.getTag(), e);
		}
	}
	
	private String toHostPath(String dockerPath) {
		String dockerBase = ProjectProperties.getInstance().getProjectLocation();
		String hostBase = ProjectProperties.getInstance().getHostLocation();
		if(dockerBase.endsWith("/")) {
			dockerBase = dockerBase.substring(0, dockerBase.length() - 1);
		}
		if(hostBase.endsWith("/")) {
			hostBase = hostBase.substring(0, hostBase.length() - 1);
		}
		return dockerPath.replaceFirst(dockerBase, hostBase);
	}
	
	//docker run -td --name java -v $(pwd)/src:/pasta/src/ -v $(pwd)/out:/pasta/out/ -v /home/pasta/content/lib/:/pasta/lib java
	public void runContainer(ExecutionContainer container) {
		String libDir = "";
		try {
			libDir = PASTAUtil.getTemplateResource("lib/").getAbsolutePath();
		} catch (FileNotFoundException e) {
			logger.error("Could not load lib directory for Docker container.", e);
		}
		
		Map<String, String> labels = new HashMap<>();
		labels.put("image", container.getImageName());
		
		List<Bind> binds = new LinkedList<>();
		
		String hostSrc = toHostPath(container.getSrcLoc().getAbsolutePath());
		String hostOut = toHostPath(container.getOutLoc().getAbsolutePath());
		String hostLib = toHostPath(libDir);
		
		binds.add(new Bind(hostSrc, new Volume(PASTA_SRC + "/")));
		binds.add(new Bind(hostOut, new Volume(PASTA_OUT + "/")));
		binds.add(new Bind(hostLib, new Volume(PASTA_LIB + "/")));
		
		if(container.getLanguage().getId().equals("matlab")) {
			binds.add(new Bind(WhichProgram.getInstance().path("matlab.install"), new Volume(PASTA_BIN + "/MATLAB/")));
		}
		
		try {
			CreateContainerCmd cmd = dockerClient
					.createContainerCmd(container.getImageName())
					.withName(container.getLabel())
					.withLabels(labels)
					.withTty(true)
					.withBinds(binds);
			
			String macKey = container.getLanguage().getId() + ".hardware-address";
			String mac = LanguageManager.getInstance().getProperty(macKey);
			if(mac != null && !mac.isEmpty()) {
				cmd = cmd.withMacAddress(mac);
			}
			
			String portsKey = container.getLanguage().getId() + ".exposed-ports";
			String ports = LanguageManager.getInstance().getProperty(portsKey);
			if(ports != null && !ports.isEmpty()) {
				cmd = cmd.withExposedPorts(parsePorts(ports));
			}
			
			CreateContainerResponse resp = cmd.exec();
			container.setId(resp.getId());
			
			dockerClient
			.startContainerCmd(container.getId())
			.exec();
			
			runCommand(container, "sh", "-c", "cp -rp " + PASTA_SRC + "/* .");
		} catch (ConflictException e) {
			logger.error("Container " + container.getLabel() + " already running.");
		}
	}
	
	public CombinedCommandResult runCommand(ExecutionContainer container, String... command) {
		return runCommand(container.getId(), container.getLabel(), command);
	}
	
	public CombinedCommandResult runCommand(String containerId, String containerLabel, String... command) {
		ExecCreateCmdResponse cmd = dockerClient.execCreateCmd(containerId)
				.withCmd(command)
				.withAttachStdout(true)
				.withAttachStderr(true)
				.withAttachStdin(false)
				.exec();
		String execId = cmd.getId();
		
		try (
				DualByteArrayOutputStream streams = new DualByteArrayOutputStream();
			) {
			ExecStartResultCallback callback = new ExecStartResultCallback(streams.getOutputStream(), streams.getErrorStream());
			logger.trace("Start running command " + Arrays.toString(command) + " on " + containerLabel);
			dockerClient
					.execStartCmd(execId)
					.exec(callback)
					.awaitCompletion();
			logger.trace("Finished running command " + Arrays.toString(command) + " on " + containerLabel);
			streams.flush();
			String combinedStr = streams.toString(StandardCharsets.UTF_8.name());
			String outStr = streams.getOutputStream().toString(StandardCharsets.UTF_8.name());
			String errStr = streams.getErrorStream().toString(StandardCharsets.UTF_8.name());
			return new CombinedCommandResult(combinedStr, outStr, errStr);
		} catch (InterruptedException e) {
			logger.error("Error waiting for command to run.", e);
		} catch (IOException e) {
			logger.error("Error getting command output streams.", e);
		}
		
		return null;
	}
	
	public CombinedCommandResult runAntTarget(ExecutionContainer container, String target) {
		return runCommand(container, "ant", "-v", "-f", "build.xml", target);
	}
	
	/**
	 * Parse a comma-separated list of ports for a container to expose, 
	 * ignoring any invalid ones.
	 * 
	 * @param portsList a string of the format <code>[protocol:]port{,[protocol2:]port2}</code>.
	 * e.g. "tcp:22,80,443". If the protocol is not included, it will default to 
	 * {@link com.github.dockerjava.api.model.InternetProtocol.DEFAULT}.
	 * @return a List of {@link com.github.dockerjava.api.model.ExposedPort} which 
	 * will be empty if no valid ports can be parsed.
	 */
	private List<ExposedPort> parsePorts(String portsList) {
		List<ExposedPort> eps = new LinkedList<>();
		if(portsList == null || portsList.isEmpty()) {
			return eps;
		}
		for(String port : portsList.split(",")) {
			String[] parts = port.split(":");
			InternetProtocol protocol = InternetProtocol.DEFAULT;
			Integer portNumber = null;
			try {
				if(parts.length > 1) {
					try {
						protocol = InternetProtocol.parse(parts[0]);
						portNumber = Integer.parseInt(parts[1]);
					} catch(IllegalArgumentException e) {
						portNumber = Integer.parseInt(parts[0]);
					}
				} else {
					portNumber = Integer.parseInt(parts[0]);
				}
				eps.add(new ExposedPort(portNumber, protocol));
			} catch(NumberFormatException e) {
				logger.warn(String.format("Invalid exposed-port: \"%s\". Expected format: \"[protocol:]number\"", port));
				continue;
			}
		}
		return eps;
	}
	
	public CommandResult executeDatabaseDump(List<String> command) {
		List<Container> containers = dockerClient.listContainersCmd().withLabelFilter("pasta", "mysql").exec();
		if(containers.size() == 0) {
			throw new IllegalStateException("No Docker container running PASTA's MySQL found.");
		}
		
		Container container = containers.get(0);
		if(containers.size() > 1) {
			logger.warn("Found more than one container for running mysqldump. Executing on first one: " + container.getId());
		}
		
		String[] commandArray = command.toArray(new String[command.size()]);
		CombinedCommandResult runCommand = runCommand(container.getId(), container.getNames()[0], commandArray);
		logger.info("OUTPUT:" + runCommand.getOutput());
		logger.info("ERROR:" + runCommand.getError());
		return runCommand;
	}
}
