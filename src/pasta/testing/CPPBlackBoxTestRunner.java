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
public class CPPBlackBoxTestRunner extends BlackBoxTestRunner {

	private static String TEMPLATE_FILENAME = "black_box_cpp_template.xml";
	
	public CPPBlackBoxTestRunner() throws FileNotFoundException {
		super(PASTAUtil.getTemplateResource("build_templates/" + TEMPLATE_FILENAME));
	}
	
	@Override
	public String extractCompileErrors(File compileErrorsFile, AntResults results) {
		Scanner scn = new Scanner(PASTAUtil.scrapeFile(compileErrorsFile));
		StringBuilder compErrors = new StringBuilder();
		if(scn.hasNext()) {
			String line = "";
			while(scn.hasNextLine()) {
				line = scn.nextLine();
				String chopped = line.replaceFirst("\\s*\\[javac\\] ", "").replaceFirst("\\s*\\[cc\\] ", "");
				if(line.length() == chopped.length()) {
					break;
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
		// TODO Extract cpp-specific files compiled and append super files to those
		return super.extractFilesCompiled(results);
	}
}
