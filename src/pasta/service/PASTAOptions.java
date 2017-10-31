package pasta.service;

import java.util.Enumeration;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import pasta.domain.options.Option;
import pasta.repository.OptionsDAO;

@Service("pastaOptions")
@Repository
public class PASTAOptions {

	protected final static Logger logger = Logger.getLogger(PASTAOptions.class);
	
	private static PASTAOptions instance;
	public static PASTAOptions instance() {
		return instance;
	}
	
	@Autowired
	private OptionsDAO optionsDao;
	
	private Properties properties;
	
	private Properties defaultsFromFile;
	
	private PASTAOptions(Properties defaults) {
		this.defaultsFromFile = defaults;
		instance = this;
	}
	
	@PostConstruct
	private void afterInit() {
		defaultsFromFile.forEach((k, v) -> {
			if(!optionsDao.hasOption(k.toString())) {
				optionsDao.saveOrUpdate(new Option(k.toString(), v.toString()));
			}
		});
		this.properties = new Properties(defaultsFromFile);
	}
	
	public Properties getPropertySet(String prefix) {
		Properties props = new Properties();
		Enumeration<?> propertyNames = properties.propertyNames();
		while(propertyNames.hasMoreElements()) {
			String key = (String) propertyNames.nextElement();
			if(key.startsWith(prefix)) {
				props.setProperty(key.substring(prefix.length()), properties.getProperty(key));
			}
		}
		for (Option option : optionsDao.getAllOptionsStartingWith(prefix)) {
			props.setProperty(option.getKey().substring(prefix.length()), option.getValue());
		}
		return props;
	}
	
	public String get(String key) {
		if(properties.containsKey(key)) {
			return properties.getProperty(key);
		}
		Option option = optionsDao.getOption(key);
		if(option != null) {
			properties.put(option.getKey(), option.getValue());
			return option.getValue();
		}
		return properties.getProperty(key);
	}
	
	public boolean clear(String key) {
		if(properties.containsKey(key)) {
			properties.remove(key);
			return true;
		}
		return false;
	}
	
	public boolean hasKey(String key) {
		return get(key) != null;
	}
}
