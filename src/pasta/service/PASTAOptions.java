package pasta.service;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import pasta.domain.form.UpdateOptionsForm;
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
	
	@Autowired
	@Qualifier("defaultProperties")
	private Properties defaultsFromFile;
	
	private PASTAOptions() {
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
	
	public List<Option> getAllOptions() {
		return optionsDao.getAllOptions();
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
	
	public void delete(String key) {
		clear(key);
		optionsDao.delete(key);
	}

	public void updateOptions(UpdateOptionsForm form) {
		Set<Option> formOptions = new HashSet<>(form.getOptions());
		if(form.getAddKey() != null && !form.getAddKey().isEmpty()) {
			Option opt = new Option(form.getAddKey(), form.getAddValue());
			if(!formOptions.add(opt)) {
				formOptions.remove(opt);
				formOptions.add(opt);
				clear(opt.getKey());
			}
		}
		
		if(form.getAddOptions() != null && !form.getAddOptions().isEmpty()) {
			Scanner lineScn = new Scanner(form.getAddOptions());
			while(lineScn.hasNext()) {
				String line = lineScn.nextLine();
				if(line.trim().isEmpty()) {
					continue;
				}
				String[] parts = line.split("=", 2);
				Option opt = new Option(parts[0], parts[1]);
				if(!formOptions.add(opt)) {
					formOptions.remove(opt);
					formOptions.add(opt);
					clear(opt.getKey());
				}
			}
			lineScn.close();
		}
		
		Set<String> allKeys = getAllOptions().stream()
					.map(Option::getKey)
					.collect(Collectors.toSet());
		for(Option option : formOptions) {
			if(!option.getKey().isEmpty()) {
				if(!allKeys.contains(option.getKey()) || 
						!get(option.getKey()).equals(option.getValue())) {
					optionsDao.saveOrUpdate(option);
					clear(option.getKey());
				}
				allKeys.remove(option.getKey());
			}
		}
		allKeys.forEach(this::delete);
	}
}
