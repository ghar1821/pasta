package pasta.web.config;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages={"pasta.web"})
public class PASTAServletConfig extends WebMvcConfigurerAdapter {

	protected static Logger logger = Logger.getLogger(PASTAServletConfig.class);
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(createInterceptor());
	}
	
	@Bean
	public GlobalModelAtributesInterceptor createInterceptor() {
		return new GlobalModelAtributesInterceptor();
	}
	
	@Bean
	public InternalResourceViewResolver createInternalResourceViewResolver() {
		InternalResourceViewResolver resolver = new InternalResourceViewResolver();
		resolver.setViewClass(JstlView.class);
		resolver.setPrefix("/WEB-INF/jsp/");
		resolver.setSuffix(".jsp");
		return resolver;
	}
	
	@Bean
	public CommonsMultipartResolver createCommonsMultipartResolver() {
		CommonsMultipartResolver resolver = new CommonsMultipartResolver();
		resolver.setMaxUploadSize(10000000);
		return resolver;
	}
}
