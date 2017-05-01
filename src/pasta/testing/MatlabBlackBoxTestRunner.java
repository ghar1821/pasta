package pasta.testing;

import java.io.FileNotFoundException;
import java.util.Scanner;

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
		addOption("mccPath", WhichProgram.getInstance().path("mcc"));
		addOption("matlabInstall", WhichProgram.getInstance().path("matlab.install"));
	}
	
	@Override
	public String extractCompileErrors(AntResults results) {
		Scanner scn = new Scanner(results.getOutput("build"));
		StringBuilder compErrors = new StringBuilder();
		String line = "";
		boolean appending = false;
		if(scn.hasNext()) {
			while(scn.hasNextLine()) {
				line = scn.nextLine().trim();
				if(line.startsWith("[exec] ")) {
					line = line.substring(7);
				} else {
					continue;
				}
				if(!appending && line.toLowerCase().replaceAll("[^a-z]", "").startsWith("error")) {
					appending = true;
				}
				if(appending && line.toLowerCase().contains("error in")) {
					appending = false;
				}
				if(appending) {
					compErrors.append(line).append('\n');
				}
			}
			// Chop off the trailing "\n"
			compErrors.deleteCharAt(compErrors.length()-1);
		}
		scn.close();
		return compErrors.toString();
	}
}
