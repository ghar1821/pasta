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
}
