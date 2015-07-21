package pasta.tag;

import java.util.Collection;

import pasta.util.PASTAUtil;

/**
 * @author Joshua Stretton
 * @version 1.0
 * @since 21 Jul 2015
 */
public class ELPASTAFunctions {
	public static String escapeNewLines(String value) {
		return value.replaceAll("\\r?\\n", "\\\\n");
	}
	
	public static boolean isPlainText(String fileLocation) {
		return PASTAUtil.canDisplayFile(fileLocation);
	}
	
	public static boolean contains(Collection<?> collection, Object item) {
		if(collection == null) {
			return false;
		}
		return collection.contains(item);
	}
}
