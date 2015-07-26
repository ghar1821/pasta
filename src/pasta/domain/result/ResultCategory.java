package pasta.domain.result;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.usertype.UserType;

public class ResultCategory implements Serializable, UserType, Comparable<ResultCategory> {

	private static final long serialVersionUID = 5193536327079881946L;

	private String name;
	private boolean studentVisible;
	
	public ResultCategory() {
		name = "";
		studentVisible = false;
	}
	
	public ResultCategory(String base) {
		if(base.startsWith("*")) {
			studentVisible = true;
			base = base.substring(1);
		}
		name = base;
	}
	
	public ResultCategory(String name, boolean studentVisible) {
		this.name = name;
		this.studentVisible = studentVisible;
	}
	
	@Override
	public String toString() {
		return (studentVisible ? "*" : "") + name;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public boolean isStudentVisible() {
		return studentVisible;
	}
	public void setStudentVisible(boolean studentVisible) {
		this.studentVisible = studentVisible;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + (studentVisible ? 1231 : 1237);
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
		ResultCategory other = (ResultCategory) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (studentVisible != other.studentVisible)
			return false;
		return true;
	}

	@Override
	public int compareTo(ResultCategory o) {
		return this.name.compareTo(o.name);
	}

	@Override
	public int[] sqlTypes() {
		return new int[] {Types.VARCHAR};
	}

	@Override
	public Class returnedClass() {
		return ResultCategory.class;
	}

	@Override
	public boolean equals(Object x, Object y) throws HibernateException {
		if(!(x instanceof ResultCategory && y instanceof ResultCategory)) {
			return false;
		}
		return ((ResultCategory)x).equals(y);
	}

	@Override
	public int hashCode(Object x) throws HibernateException {
		return x.hashCode();
	}

	@Override
	public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner)
			throws HibernateException, SQLException {
		String value = StandardBasicTypes.STRING.nullSafeGet(rs, names[0], session);
		return ((value != null) ? new ResultCategory(value) : null);
	}

	@Override
	public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session)
			throws HibernateException, SQLException {
		StandardBasicTypes.STRING.nullSafeSet(st, (value != null) ? value.toString() : null, index, session);
	}

	@Override
	public Object deepCopy(Object value) throws HibernateException {
		return value == null ? null : new ResultCategory(((ResultCategory)value).toString());
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
