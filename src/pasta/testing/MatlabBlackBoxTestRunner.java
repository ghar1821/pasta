package pasta.testing;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import pasta.docker.DockerManager;
import pasta.util.PASTAUtil;
import pasta.util.WhichProgram;

/**
 * @author Joshua Stretton
 * @version 1.0
 * @since 09 Mar 2017
 */
public class MatlabBlackBoxTestRunner extends BlackBoxTestRunner {

	private static String TEMPLATE_FILENAME = "black_box_matlab_template.xml";
	
	public MatlabBlackBoxTestRunner() throws FileNotFoundException {
		super(PASTAUtil.getTemplateResource("build_templates/" + TEMPLATE_FILENAME));
		addOption("mccPath", convertToExecPath(WhichProgram.getInstance().path("mcc")));
		addOption("matlabInstall", convertToExecPath(WhichProgram.getInstance().path("matlab.install")));
	}
	
	private String convertToExecPath(String file) {
		String matlab = WhichProgram.getInstance().path("matlab.install");
		String important = file.substring(matlab.length());
		return DockerManager.PASTA_BIN + "/MATLAB/" + important;
	}
	
	@Override
	public String extractCompileErrors(File compileErrorsFile, AntResults results) {
		Scanner scn = new Scanner(PASTAUtil.scrapeFile(compileErrorsFile));
		StringBuilder compErrors = new StringBuilder();
		String line = "";
		if(scn.hasNext()) {
			while(scn.hasNextLine()) {
				line = scn.nextLine().trim();
				if(line.toLowerCase().startsWith("error in")) {
					break;
				}
				compErrors.append(line).append('\n');
			}
			// Chop off the trailing "\n"
			compErrors.deleteCharAt(compErrors.length()-1);
		}
		scn.close();
		return compErrors.toString();
	}
}
