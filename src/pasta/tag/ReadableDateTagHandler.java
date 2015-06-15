package pasta.tag;

import java.io.IOException;
import java.util.Date;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import pasta.util.PASTAUtil;

/**
 * @author Joshua Stretton
 * @version 1.0
 * @since 13 Jun 2015
 */
public class ReadableDateTagHandler extends TagSupport{
	private Date inputDate;
	
	@Override
    public int doStartTag() throws JspException {
        try {
        	String readable = PASTAUtil.formatDateReadable(inputDate);
            JspWriter out = pageContext.getOut();
            out.print(readable);
        } catch (IOException e) {}
        return SKIP_BODY;
    }

	public Date getDate() {
		return inputDate;
	}

	public void setDate(Date inputDate) {
		this.inputDate = inputDate;
	}
}
