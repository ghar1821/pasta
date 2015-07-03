package pasta.testing;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import pasta.domain.PASTAUser;
import pasta.util.ProjectProperties;

public class PlayerValidationRunner extends Runner {

	private static String TEMPLATE_FILENAME = ProjectProperties.getInstance().getProjectLocation() + "build_templates" + File.separator + "player_validate_template.xml";
	private static String VALIDATE_BASE_FILENAME = ProjectProperties.getInstance().getProjectLocation() + "build_templates" + File.separator + "ValidatePlayerBase.java";
	private static String VALIDATE_INTERFACE_FILENAME = ProjectProperties.getInstance().getProjectLocation() + "build_templates" + File.separator + "PlayerValidator.java";
	
	public static final String playerNameFilename = "playername.out";
	public static final String validationErrorFilename = "validate.out";
	
	private PASTAUser user;
	private String validationClassname;
	private String codeDirectory = "src";
	private String playerDirectory = "player";
	
	public PlayerValidationRunner() {
		super(new File(TEMPLATE_FILENAME));
		addOption("validateTimeout", "30000");
		setValidationDirectory("validate");
	}
	
	public PlayerValidationRunner(PASTAUser user, String validationClassname, String validateCodeLocation) {
		this();
		this.user = user;
		this.validationClassname = validationClassname;
		setValidationDirectory(validateCodeLocation);
		refreshArgs();
	}
	
	public void setUser(PASTAUser user) {
		this.user = user;
		refreshArgs();
	}

	public void setValidationClassname(String name) {
		name = name.replaceAll("[/\\\\]", ".");
		this.validationClassname = name;
		refreshArgs();
	}
	
	public void setPlayerDirectory(String directory) {
		this.playerDirectory = directory;
		refreshArgs();
	}
	
	private void refreshArgs() {
		addArgsOption("validationArgs", user.getUsername(), "${basedir}/" + playerDirectory, validationClassname, "${basedir}/" + playerNameFilename, "${basedir}/" + validationErrorFilename);
	}
	
	public void setMaxRunTime(long milliseconds) {
		if(milliseconds >= 0) {
			addOption("maxTimeAllowed", String.valueOf(milliseconds));
		}
	}
	
	public void setValidationDirectory(String directory) {
		this.codeDirectory = directory;
		addOption("validateDirectory", directory);
	}
	
	@Override
	public File createBuildFile(File file) {
		File toReturn = super.createBuildFile(file);
		try {
			FileUtils.copyFile(new File(VALIDATE_BASE_FILENAME), new File(new File(file.getParentFile(), codeDirectory), new File(VALIDATE_BASE_FILENAME).getName()));
			FileUtils.copyFile(new File(VALIDATE_INTERFACE_FILENAME), new File(new File(file.getParentFile(), codeDirectory), new File(VALIDATE_INTERFACE_FILENAME).getName()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return toReturn;
	}

	@Override
	public String extractCompileErrors(AntResults results) {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public String extractFilesCompiled(AntResults results) {
		// TODO Auto-generated method stub
		return "";
	}
}
