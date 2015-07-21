package pasta.tag;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * @author Joshua Stretton
 * @version 1.0
 * @since 13 Jun 2015
 */
public class EscapeNewLineTagHandler extends TagSupport{
	private static final long serialVersionUID = -251115527745415978L;
	
	private String value;
	
	@Override
    public int doStartTag() throws JspException {
        try {
        	String escaped = value.replaceAll("\\r?\\n", "\\\\n");
            JspWriter out = pageContext.getOut();
            out.print(escaped);
        } catch (IOException e) {}
        return SKIP_BODY;
    }

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
