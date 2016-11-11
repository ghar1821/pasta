package pasta.domain.result;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import pasta.domain.user.PASTAUser;
import pasta.repository.ResultDAO;
import pasta.service.ResultManager;
import pasta.service.UserManager;
import pasta.util.ProjectProperties;

public class AssessmentResultSummaryCalculator {
	private static Logger logger = Logger.getLogger(AssessmentResultSummaryCalculator.class);
	
	private List<String> output;
	Boolean done = null;
	
	private UserManager userManager;
	private ResultManager resultManager;
	
	public void recacheResultSummaries(UserManager userManager, ResultManager resultManager) {
		if(done != null) {
			return;
		}
		output = Collections.synchronizedList(new LinkedList<String>());
		this.userManager = userManager;
		this.resultManager = resultManager;
		done = false;
		
		doRecache();
		
		done = true;
	}
	
	private void doRecache() {
		Collection<PASTAUser> allUsers = userManager.getUserList();
		
		output.add("Calculating all results...");
		Map<PASTAUser, Map<Long, AssessmentResult>> calculatedResults = resultManager.getLatestResultsIncludingGroupQuick(allUsers);
		
		ResultDAO dao = ProjectProperties.getInstance().getResultDAO();
		
		for(PASTAUser user : allUsers) {
			output.add("Saving results for " + user.getUsername());
			Map<Long, AssessmentResult> results = calculatedResults.get(user);
			for(AssessmentResult result : results.values()) {
				AssessmentResultSummary summary = new AssessmentResultSummary(user, result.getAssessment(), result.getPercentage());
				dao.saveOrUpdate(summary);
			}
		}
	}

	public List<String> getOutputSinceLastCall() {
		List<String> outSinceLastCall = new LinkedList<String>(output);
		output.clear();
		return outSinceLastCall;
	}
	
	public boolean isStarted() {
		return done != null;
	}
	
	public boolean isDone() {
		return done != null && done;
	}
	
	public boolean hasOutput() {
		return !output.isEmpty();
	}
	
	
}
