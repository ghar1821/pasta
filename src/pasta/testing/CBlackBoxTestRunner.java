package pasta.testing;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import pasta.util.PASTAUtil;

/**
 * @author Joshua Stretton
 * @version 1.0
 * @since 26 Jun 2015
 */
public class CBlackBoxTestRunner extends BlackBoxTestRunner {

	private static String TEMPLATE_FILENAME = "black_box_c_template.xml";
	
	public CBlackBoxTestRunner() throws FileNotFoundException {
		super(PASTAUtil.getTemplateResource("build_templates/" + TEMPLATE_FILENAME));
	}
	
	public void setGCCArguments(String argsLine) {
		addOption("gccCommandLineArgs", argsLine);
	}

	@Override
	public String extractCompileErrors(File compileErrorFile, AntResults results) {
		Scanner scn = new Scanner(results.getOutput("build"));
		StringBuilder compErrors = new StringBuilder();
		String line = "";
		if(scn.hasNext()) {
			while(scn.hasNextLine()) {
				line = scn.nextLine();
				if(line.contains("not part of the command.")) {
					break;
				}
			}
		}
		while(scn.hasNextLine()) {
			line = scn.nextLine();
			if(!line.contains("[apply]")) {
				break;
			}
			compErrors.append(line.replaceFirst("\\s*\\[apply\\]\\s*", "")).append('\n');
		}
		if(compErrors.length() > 0) {
			// Chop off the trailing "\n"
			compErrors.replace(compErrors.length()-1, compErrors.length(), "");
		}
		scn.close();
		return compErrors.toString();
	}

	@Override
	public String extractFilesCompiled(AntResults results) {
		// TODO Extract c-specific files compiled and append super files to those
		return super.extractFilesCompiled(results);
	}
}
