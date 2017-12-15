/*
MIT License

Copyright (c) 2012-2017 PASTA Contributors (see CONTRIBUTORS.txt)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

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
