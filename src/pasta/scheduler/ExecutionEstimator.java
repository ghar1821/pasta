package pasta.scheduler;

import java.io.File;
import java.util.Map;

import pasta.docker.Language;
import pasta.docker.LanguageManager;
import pasta.domain.template.Assessment;
import pasta.domain.template.BlackBoxTestCase;
import pasta.domain.template.UnitTest;
import pasta.util.PASTAUtil;

public class ExecutionEstimator {

	public static long estimateTime(AssessmentJob job) {
		return estimateTime(job.getResults().getAssessment(), job.getLanguage());
	}
	
	public static long estimateTime(Assessment assessment) {
		return estimateTime(assessment, null);
	}
	public static long estimateTime(Assessment assessment, Language language) {
		return assessment.getAllUnitTests().stream()
				.reduce(0L, 
						(t, wut) -> t + estimateTime(wut.getTest(), language), 
						Long::sum);
	}
	
	public static long estimateTime(UnitTest unitTest) {
		return estimateTime(unitTest, null);
	}
	public static long estimateTime(UnitTest unitTest, Language language) {
		long time = 0;
		if(!unitTest.getTestCases().isEmpty()) {
			if(unitTest.getBlackBoxTimeout() == null) {
				time += unitTest.getTestCases().stream()
						.reduce(0L, 
								(t, tc) -> t + estimateTime(tc, language), 
								Long::sum);
			} else {
				time += unitTest.getBlackBoxTimeout();
			}
			if(language != null) {
				time += language.getTestSuiteExecutionOverhead();
			}
		}
		
		Language java = LanguageManager.getInstance().getLanguage("java");
		if(unitTest.getAdvancedTimeout() == null) {
			File main = unitTest.getMainSourceFile();
			if(main != null) {
				Map<String, Long> timeouts = PASTAUtil.extractTestTimeouts(main);
				time += timeouts.entrySet().stream()
						.reduce(0L, 
								(total, e) -> total + e.getValue(), 
								Long::sum);
				time += java.getTestSuiteExecutionOverhead();
				time += timeouts.size() * java.getTestCaseExecutionOverhead();
			}
		} else {
			time += unitTest.getAdvancedTimeout();
			time += java.getTestSuiteExecutionOverhead();
		}
		
		return time;
	}
	
	public static long estimateTime(BlackBoxTestCase testCase) {
		return estimateTime(testCase, null);
	}
	public static long estimateTime(BlackBoxTestCase testCase, Language language) {
		long time = testCase.getTimeout();
		if(language != null) {
			time += language.getTestCaseExecutionOverhead();
		}
		return time;
	}
}
