package pasta.docker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.exception.DockerClientException;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.BuildResponseItem;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.BuildImageResultCallback;

import pasta.util.Copy;
import pasta.util.PASTAUtil;

public class DockerManager {
	
	protected static Logger logger = Logger.getLogger(DockerManager.class);
	
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
	
	private void installImage(DockerBuildFile buildFile) {
		logger.info("Building Docker image: " + buildFile.getTag());
		
		File baseDir = buildFile.getFile().getParentFile();

		BuildImageResultCallback callback = new BuildImageResultCallback() {
		    @Override
		    public void onNext(BuildResponseItem item) {
		       logger.debug(StringUtils.stripEnd(item.getStream(), " \n"));
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
				.exec(callback)
				.awaitImageId();
			buildFile.registerSuccess(id);
			logger.info("Built: " + id);
		} catch (DockerClientException e) {
			buildFile.registerFailure();
			logger.error("Error building image " + buildFile.getTag(), e);
		}
	}
}
