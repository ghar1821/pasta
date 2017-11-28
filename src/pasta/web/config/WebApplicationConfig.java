package pasta.web.config;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import pasta.repository.LoginDAO;
import pasta.util.ProjectProperties;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages={"pasta.repository", "pasta.login", "pasta.service", "pasta.scheduler", "pasta.domain", "pasta.util", "pasta.docker"})
@ImportResource({"classpath:applicationContext.xml", "classpath:applicationContext-security.xml"})
@PropertySource("classpath:project.properties")
@PropertySource("classpath:database.properties")
public class WebApplicationConfig extends WebMvcConfigurerAdapter {

	protected static Logger logger = Logger.getLogger(WebApplicationConfig.class);
	
	@Autowired
	private GlobalModelAtributesInterceptor interceptor;
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(interceptor);
	}
	
	@Bean
	public GlobalModelAtributesInterceptor createInterceptor() {
		return new GlobalModelAtributesInterceptor();
	}
	
	/*
	<bean 
	class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer">
		<property name="locations">
		<list>
				<value>WEB-INF/classes/database.properties</value>
				<value>WEB-INF/classes/project.properties</value>
			</list>
		</property>
	</bean>
	*/
	
	@Bean
	public static PropertySourcesPlaceholderConfigurer createPropertyPlaceholderConfigurer() {
		PropertySourcesPlaceholderConfigurer config = new PropertySourcesPlaceholderConfigurer();
		config.setLocations(
				new ClassPathResource("database.properties"),
				new ClassPathResource("project.properties")
		);
		return config;
	}
	
	@Autowired
	private LoginDAO loginDAO;
	
	@Value("${project.name}")
	private String projectName;
	
	@Bean(name="projectProperties")
	@DependsOn("loginDAO")
	public ProjectProperties createProjectProperties() {
		Map<String, String> props = new HashMap<>();
		props.put("name", projectName);
		logger.info("---------=-=-=-=-=-=-=-=-=-= Project Name: " + projectName);
		
		try {
			Constructor<ProjectProperties> constructor = ProjectProperties.class.getConstructor(Map.class);
			ProjectProperties instance = constructor.newInstance(props);
			return instance;
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			logger.error("Cannot get contructor for ProjectProperties", e);
		}
		
		return null;
	}
	
	/*
	 * 
	 * <bean id="loginDAO" class="pasta.repository.LoginDAO" />
    <bean id="projectProperties" class="pasta.util.ProjectProperties" depends-on="loginDAO">
		<constructor-arg>
			<map key-type="java.lang.String" value-type="java.lang.String">
					<entry key="name" value="${project.name}" />
					<entry key="location" value="${project.location}" />
					<entry key="hostLocation" value="${project.hostLocation}" />
					<entry key="authentication" value="${project.authentication:ldap}" />
					<entry key="createAccountOnSuccessfulLogin" value="${project.createAccountOnSuccessfulLogin:true}" />
					<entry key="pathUnitTests" value="${project.pathUnitTests:}" />
					<entry key="pathSubmissions" value="${project.pathSubmissions:}" />
					<entry key="proxydomain" value="${project.proxydomain:}" />
					<entry key="proxyport" value="${project.proxyport:}" />
					<entry key="initialInstructor" value="${project.initialInstructor:}" />
			</map>
		</constructor-arg>
	</bean>
	 */
}
