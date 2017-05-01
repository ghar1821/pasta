package pasta.testing;

import java.io.FileNotFoundException;

import pasta.util.PASTAUtil;
import pasta.util.WhichProgram;

/**
 * @author Joshua Stretton
 * @version 1.0
 * @since 26 Jun 2015
 */
public class PythonBlackBoxTestRunner extends BlackBoxTestRunner {

	private static String TEMPLATE_FILENAME = "black_box_python_template.xml";
	
	public PythonBlackBoxTestRunner() throws FileNotFoundException {
		super(PASTAUtil.getTemplateResource("build_templates/" + TEMPLATE_FILENAME));
		addOption("pythonPath", WhichProgram.getInstance().path("python3"));
	}

	@Override
	public String extractFilesCompiled(AntResults results) {
		// TODO Extract python-specific files compiled and append super files to those
		return super.extractFilesCompiled(results);
	}
}
