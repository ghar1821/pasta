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
		if(unitTest.getBlackBoxTimeout() == null) {
			time += unitTest.getTestCases().stream()
					.reduce(0L, 
							(t, tc) -> t + estimateTime(tc), 
							Long::sum);
		} else {
			time += unitTest.getBlackBoxTimeout();
		}
		
		if(unitTest.getAdvancedTimeout() == null) {
			File main = unitTest.getMainSourceFile();
			if(main != null) {
				Map<String, Long> timeouts = PASTAUtil.extractTestTimeouts(main);
				time += timeouts.entrySet().stream()
						.reduce(0L, 
								(total, e) -> total + e.getValue(), 
								Long::sum);
			}
		} else {
			time += unitTest.getAdvancedTimeout();
		}
		
		time += overhead(unitTest, language);
		
		return time;
	}
	
	public static long estimateTime(BlackBoxTestCase testCase) {
		return testCase.getTimeout();
	}
	
	public static long overhead(UnitTest unitTest, Language language) {
		if(language == null) {
			return 0;
		}
		
		long overhead = 0;
		if(!unitTest.getTestCases().isEmpty()) {
			overhead += unitTest.getTestCases().size() * language.getTestCaseExecutionOverhead(); 
			overhead += language.getTestSuiteExecutionOverhead();
		}
		
		if(unitTest.hasCode()) {
			File main = unitTest.getMainSourceFile();
			if(main != null) {
				Language java = LanguageManager.getInstance().getLanguage("java");
				Map<String, Long> timeouts = PASTAUtil.extractTestTimeouts(main);
				overhead += java.getTestSuiteExecutionOverhead();
				overhead += timeouts.size() * java.getTestCaseExecutionOverhead();
			}
		}
		
		return overhead;
	}
}
