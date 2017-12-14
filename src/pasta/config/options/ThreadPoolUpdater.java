package pasta.config.options;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import pasta.domain.options.Option;
import pasta.scheduler.AssessmentJobExecutor;

@Component
public class ThreadPoolUpdater implements OptionsListener {

	@Autowired
	private AssessmentJobExecutor executor;
	
	@Override
	public boolean actsOn(Option option) {
		String key = option.getKey();
		return key.equals("execution.threads.core.size") || key.equals("execution.threads.max.size");
	}

	@Override
	public void optionUpdated(Option option) {
		String key = option.getKey();
		try {
			int value = new Integer(option.getValue());
			if(key.equals("execution.threads.core.size")) {
				executor.setCorePoolSize(value);
			}
			if(key.equals("execution.threads.max.size")) {
				executor.setMaximumPoolSize(value);
			}
		} catch(NumberFormatException e) {
			return;
		}
	}

}
