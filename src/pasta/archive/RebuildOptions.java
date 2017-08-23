package pasta.archive;

import java.util.HashMap;

import pasta.domain.template.Assessment;
import pasta.domain.template.HandMarking;
import pasta.domain.template.UnitTest;

public class RebuildOptions {
	private Assessment parentAssessment;
	private UnitTest parentUnitTest;
	private HandMarking parentHandMarking;
	
	public Assessment getParentAssessment() {
		return parentAssessment;
	}

	public RebuildOptions setParentAssessment(Assessment parentAssessment) {
		this.parentAssessment = parentAssessment;
		return this;
	}

	public UnitTest getParentUnitTest() {
		return parentUnitTest;
	}

	public RebuildOptions setParentUnitTest(UnitTest parentUnitTest) {
		this.parentUnitTest = parentUnitTest;
		return this;
	}

	public HandMarking getParentHandMarking() {
		return parentHandMarking;
	}

	public RebuildOptions setParentHandMarking(HandMarking parentHandMarking) {
		this.parentHandMarking = parentHandMarking;
		return this;
	}
}
