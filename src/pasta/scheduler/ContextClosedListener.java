package pasta.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

@Component
public class ContextClosedListener implements ApplicationListener<ContextClosedEvent> {
    @Autowired
    AssessmentJobExecutor assessmentJobExecutor;

    @Override
	public void onApplicationEvent(ContextClosedEvent event) {
        assessmentJobExecutor.shutdown();
    }  
}