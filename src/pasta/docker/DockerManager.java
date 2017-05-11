package pasta.docker;

import org.apache.log4j.Logger;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;

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
	}
}
