package pasta.repository;

import java.util.Collection;
import java.util.HashMap;

import pasta.domain.template.Assessment;
import pasta.domain.template.Competition;
import pasta.domain.template.HandMarking;
import pasta.domain.template.UnitTest;

public class AssessmentDAO {
	
	// assessmentTemplates are cached
	HashMap<String, Assessment> allAssessments;
	HashMap<String, UnitTest> allUnitTests;
	HashMap<String, HandMarking> allHandMarking;
	HashMap<String, Competition> allCompetitions;
	
	public AssessmentDAO(){
		// load up all cached objects
		
		// load up unit tests TODO #44
		
		// load up hand marking TODO #47
		
		// load up competitions TODO #48
		
		// load up all assessments TODO #49

	}
	
	public Assessment getAssessment(String name){
		return allAssessments.get(name);
	}
	
	public Collection getAssessmentList(){
		return allAssessments.values();
	}
	
}
