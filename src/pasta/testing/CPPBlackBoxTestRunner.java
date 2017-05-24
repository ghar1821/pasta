package pasta.testing;

import java.io.File;
import java.io.FileNotFoundException;

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
		// TODO Extract cpp-specific compile errors and append super errors to those
		return super.extractCompileErrors(compileErrorsFile, results);
	}

	@Override
	public String extractFilesCompiled(AntResults results) {
		// TODO Extract cpp-specific files compiled and append super files to those
		return super.extractFilesCompiled(results);
	}
}
