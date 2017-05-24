package pasta.docker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.exception.ConflictException;
import com.github.dockerjava.api.exception.DockerClientException;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.BuildResponseItem;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.BuildImageResultCallback;
import com.github.dockerjava.core.command.ExecStartResultCallback;

import pasta.util.Copy;
import pasta.util.PASTAUtil;
import pasta.util.WhichProgram;
import pasta.util.io.DualByteArrayOutputStream;

public class DockerManager {
	
	protected static Logger logger = Logger.getLogger(DockerManager.class);
	
	public static final String PASTA_SRC = "/pasta/src";
	public static final String PASTA_OUT = "/pasta/out";
	public static final String PASTA_BIN = "/pasta/bin";
	public static final String PASTA_LIB = "/pasta/lib";
	public static final String WORK_DIR = "/sandbox";
	
	private static DockerManager instance;
	public static DockerManager instance() {
		if(instance == null) {
			instance = new DockerManager();
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
		dockerClient.stopContainerCmd(id).exec();
		dockerClient.removeContainerCmd(id).exec();
	}
	
	private void installImage(DockerBuildFile buildFile) {
		removeImage(buildFile.getTag());
		
		logger.info("Building Docker image: " + buildFile.getTag());
		
		File baseDir = buildFile.getFile().getParentFile();

		BuildImageResultCallback callback = new BuildImageResultCallback() {
		    @Override
		    public void onNext(BuildResponseItem item) {
		       logger.debug(StringUtils.stripEnd(item.getStream(), "\n"));
		       super.onNext(item);
		    }
		};

		try {
			// Load bin files
			try {
				File bin = PASTAUtil.getTemplateResource("bin/");
				Copy.copy(bin.toPath(), baseDir.toPath());
			} catch (IOException e) {}
			
			String id = dockerClient.buildImageCmd(baseDir)
				.withTag(buildFile.getTag())
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
		binds.add(new Bind(container.getSrcLoc().getAbsolutePath(), new Volume(PASTA_SRC + "/")));
		binds.add(new Bind(container.getOutLoc().getAbsolutePath(), new Volume(PASTA_OUT + "/")));
		binds.add(new Bind(libDir, new Volume(PASTA_LIB + "/")));
		
		if(container.getLanguage().getId().equals("matlab")) {
			binds.add(new Bind(WhichProgram.getInstance().path("matlab.install"), new Volume(PASTA_BIN + "/MATLAB/")));
		}
		
		try {
			CreateContainerResponse resp = dockerClient
					.createContainerCmd(container.getImageName())
					.withName(container.getLabel())
					.withLabels(labels)
					.withTty(true)
					.withBinds(binds)
					.withMacAddress("00:0c:29:92:b6:e8") //TODO
					.exec();
			container.setId(resp.getId());
			
			dockerClient
			.startContainerCmd(container.getId())
			.exec();
			
			runCommand(container, "sh", "-c", "cp -rp " + PASTA_SRC + "/* .");
		} catch (ConflictException e) {
			logger.error("Container " + container.getLabel() + " already running.");
		}
	}
	
	public CommandResult runCommand(ExecutionContainer container, String... command) {
		ExecCreateCmdResponse cmd = dockerClient.execCreateCmd(container.getId())
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
			logger.trace("Start running command " + Arrays.toString(command) + " on " + container.getLabel());
			dockerClient
					.execStartCmd(execId)
					.exec(callback)
					.awaitCompletion();
			logger.trace("Finished running command " + Arrays.toString(command) + " on " + container.getLabel());
			streams.flush();
			String combinedStr = streams.toString(StandardCharsets.UTF_8.name());
			String outStr = streams.getOutputStream().toString(StandardCharsets.UTF_8.name());
			String errStr = streams.getErrorStream().toString(StandardCharsets.UTF_8.name());
			return new CommandResult(combinedStr, outStr, errStr);
		} catch (InterruptedException e) {
			logger.error("Error waiting for command to run.", e);
		} catch (IOException e) {
			logger.error("Error getting command output streams.", e);
		}
		
		return null;
	}
	
	public CommandResult runAntTarget(ExecutionContainer container, String target) {
		return runCommand(container, "ant", "-v", "-f", "build.xml", target);
	}
}
