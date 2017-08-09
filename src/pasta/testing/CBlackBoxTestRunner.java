package pasta.testing;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import pasta.util.PASTAUtil;
import pasta.util.WhichProgram;

/**
 * @author Joshua Stretton
 * @version 1.0
 * @since 26 Jun 2015
 */
public class CBlackBoxTestRunner extends BlackBoxTestRunner {

	private static String TEMPLATE_FILENAME = "black_box_c_template.xml";
	
	public CBlackBoxTestRunner() throws FileNotFoundException {
		super(PASTAUtil.getTemplateResource("build_templates/" + TEMPLATE_FILENAME));
		addOption("gccPath", WhichProgram.getInstance().path("gcc"));
	}
	
	public void setGCCArguments(String argsLine) {
		addOption("gccCommandLineArgs", argsLine);
	}

	@Override
	public String extractCompileErrors(File compileErrorFile, AntResults results) {
		Scanner scn = new Scanner(PASTAUtil.scrapeFile(compileErrorFile));
		StringBuilder compErrors = new StringBuilder();
		boolean startedJavacErrors = false;
		if(scn.hasNext()) {
			String line = "";
			while(scn.hasNextLine()) {
				line = scn.nextLine();
				String chopped = line.replaceFirst("\\s*\\[javac\\] ", "");
				if(line.length() == chopped.length()) {
					if(startedJavacErrors) break;
				} else {
					startedJavacErrors = true;
				}
				compErrors.append(chopped).append(System.lineSeparator());
			}
			// Chop off the trailing "\n"
			compErrors.deleteCharAt(compErrors.length()-1);
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
