package pasta.archive;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.usertype.UserType;

public class MappableClass implements UserType {
	
	private Class<? extends ArchivableBaseEntity> baseClass;
	
	public MappableClass() {
		this.baseClass = null;
	}
	
	public MappableClass(Class<? extends ArchivableBaseEntity> clazz) {
		this.baseClass = clazz;
	}
	
	public Class<? extends ArchivableBaseEntity> getBaseClass() {
		return baseClass;
	}
	public void setBaseClass(Class<? extends ArchivableBaseEntity> baseClass) {
		this.baseClass = baseClass;
	}

	@Override
	public int[] sqlTypes() {
		return new int[] {Types.VARCHAR};
	}

	@Override
	public Class<?> returnedClass() {
		return Class.class;
	}

	@Override
	public boolean equals(Object x, Object y) throws HibernateException {
		if(!(x instanceof Class<?>) || !(y instanceof Class<?>)) {
			return false;
		}
		return x.equals(y);
	}

	@Override
	public int hashCode(Object x) throws HibernateException {
		return x.hashCode();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner)
			throws HibernateException, SQLException {
		String value = StandardBasicTypes.STRING.nullSafeGet(rs, names[0], session);
		try {
			return ((value != null) ? new MappableClass((Class<? extends ArchivableBaseEntity>) Class.forName(value)) : null);
		} catch (ClassNotFoundException | ClassCastException e) {
			return null;
		}
	}

	@Override
	public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session)
			throws HibernateException, SQLException {
		StandardBasicTypes.STRING.nullSafeSet(st, (value != null) ? ((MappableClass)value).baseClass.getName() : null, index, session);
	}

	@Override
	public Object deepCopy(Object value) throws HibernateException {
		return new MappableClass(((MappableClass)value).baseClass);
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((baseClass == null) ? 0 : baseClass.hashCode());
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
		MappableClass other = (MappableClass) obj;
		if (baseClass == null) {
			if (other.baseClass != null)
				return false;
		} else if (!baseClass.equals(other.baseClass))
			return false;
		return true;
	}
}
