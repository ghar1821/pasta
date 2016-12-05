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
	private String nullString = "";
	
	@Override
    public int doStartTag() throws JspException {
        try {
        	JspWriter out = pageContext.getOut();
        	if(inputDate == null) {
        		out.print(nullString);
        	} else {
        		String readable = PASTAUtil.formatDateReadable(inputDate);
        		out.print(readable);
        	}
        } catch (IOException e) {}
        return SKIP_BODY;
    }

	public Date getDate() {
		return inputDate;
	}

	public void setDate(Date inputDate) {
		this.inputDate = inputDate;
	}

	public String getNullString() {
		return nullString;
	}

	public void setNullString(String nullString) {
		this.nullString = nullString;
	}
}
