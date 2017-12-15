/*
MIT License

Copyright (c) 2012-2017 PASTA Contributors (see CONTRIBUTORS.txt)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package pasta.domain.result;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.regex.Pattern;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.usertype.UserType;

public class ResultFeedback implements Serializable, UserType, Comparable<ResultFeedback> {
	private static final long serialVersionUID = 7432673816932250602L;
	
	private String category;
	private String feedback;
	
	public ResultFeedback() {
		category = "";
		feedback = "";
	}
	
	public ResultFeedback(String feedback) {
		this.category = "";
		this.feedback = feedback;
	}
	
	public ResultFeedback(String category, String feedback) {
		this.category = category;
		this.feedback = feedback;
	}
	
	private ResultFeedback(String base, char separator) {
		String[] parts = base.split(Pattern.quote(String.valueOf(separator)), 2);
		category = parts[0];
		feedback = parts[1];
	}

	@Override
	public String toString() {
		return category + "|" + feedback;
	}
	
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}

	public String getFeedback() {
		return feedback;
	}
	public void setFeedback(String feedback) {
		this.feedback = feedback;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((category == null) ? 0 : category.hashCode());
		result = prime * result + ((feedback == null) ? 0 : feedback.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ResultFeedback other = (ResultFeedback) obj;
		if (category == null) {
			if (other.category != null)
				return false;
		} else if (!category.equals(other.category))
			return false;
		if (feedback == null) {
			if (other.feedback != null)
				return false;
		} else if (!feedback.equals(other.feedback))
			return false;
		return true;
	}

	@Override
	public int compareTo(ResultFeedback o) {
		int diff = this.category.compareTo(o.category);
		if(diff != 0) {
			return diff;
		}
		return this.feedback.compareTo(o.feedback);
	}

	@Override
	public int[] sqlTypes() {
		return new int[] {Types.VARCHAR};
	}

	@Override
	public Class returnedClass() {
		return ResultFeedback.class;
	}

	@Override
	public boolean equals(Object x, Object y) throws HibernateException {
		if(!(x instanceof ResultFeedback && y instanceof ResultFeedback)) {
			return false;
		}
		return ((ResultFeedback)x).equals(y);
	}

	@Override
	public int hashCode(Object x) throws HibernateException {
		return x.hashCode();
	}

	@Override
	public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner)
			throws HibernateException, SQLException {
		String value = StandardBasicTypes.STRING.nullSafeGet(rs, names[0], session);
		return ((value != null) ? new ResultFeedback(value, '|') : null);
	}

	@Override
	public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session)
			throws HibernateException, SQLException {
		StandardBasicTypes.STRING.nullSafeSet(st, (value != null) ? value.toString() : null, index, session);
	}

	@Override
	public Object deepCopy(Object value) throws HibernateException {
		return value == null ? null : new ResultFeedback(((ResultFeedback)value).toString(), '|');
	}

	@Override
	public boolean isMutable() {
		return true;
	}

	@Override
	public Serializable disassemble(Object value) throws HibernateException {
		return (Serializable) deepCopy(value);
	}

	@Override
	public Object assemble(Serializable cached, Object owner) throws HibernateException {
		return deepCopy(cached);
	}

	@Override
	public Object replace(Object original, Object target, Object owner) throws HibernateException {
		return deepCopy(original);
	}
}
