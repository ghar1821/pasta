package pasta.testing;

import java.io.FileNotFoundException;

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

	@Override
	public String extractCompileErrors(AntResults results) {
		// TODO Extract c-specific compile errors and append super errors to those
		return super.extractCompileErrors(results);
	}

	@Override
	public String extractFilesCompiled(AntResults results) {
		// TODO Extract c-specific files compiled and append super files to those
		return super.extractFilesCompiled(results);
	}
}
