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
		List<Image> images = dockerClient.listImagesCmd().exec();
		Map<String, Long> installed = new HashMap<>();
		for(Image im : images) {
			String key = im.getRepoTags()[0].split(":")[0];
			long modified = (long)im.getCreated()*1000L;
			installed.put(key, modified);
		}
		for(DockerBuildFile buildFile : DockerProperties.getInstance().getBuildFiles()) {
			if(!installed.containsKey(buildFile.getId()) || installed.get(buildFile.getId()) < buildFile.getFile().lastModified()) {
				installImage(buildFile);
			}
		}
	}
	
	private void installImage(DockerBuildFile buildFile) {
		logger.info("Building Docker image: " + buildFile.getId());
		
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
				.withTag(buildFile.getId())
				.exec(callback)
				.awaitImageId();
			buildFile.registerSuccess();
			logger.info("Built: " + id);
		} catch (DockerClientException e) {
			buildFile.registerFailure();
			logger.error("Error building image " + buildFile.getId(), e);
		}
	}
}
