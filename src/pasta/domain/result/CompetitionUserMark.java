/*
Copyright (c) 2014, Alex Radu
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer. 
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

The views and conclusions contained in the software and documentation are those
of the authors and should not be interpreted as representing official policies, 
either expressed or implied, of the PASTA Project.
 */

package pasta.domain.result;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.usertype.UserType;

/**
 * Container class to hold the result of a user for a competition. 
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2014-01-29
 * 
 */
public class CompetitionUserMark implements Serializable, UserType, Comparable<CompetitionUserMark> {

	private static final long serialVersionUID = 4210605261504407008L;
	
	private String username;
	private Double percentage;
	
	public CompetitionUserMark() {
		username = "";
		percentage = 0.0;
	}
	
	public CompetitionUserMark(String base) {
		int index = base.lastIndexOf('_');
		if(index == -1) {
			username = base;
			percentage = 0.0;
		} else {
			username = base.substring(0, index);
			percentage = Double.parseDouble(base.substring(index+1));
		}
	}
	
	public CompetitionUserMark(String username, double percentage){
		this.username = username;
		this.percentage = percentage;
	}
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}

	public Double getPercentage() {
		return percentage;
	}
	public void setPercentage(Double percentage) {
		this.percentage = percentage;
	}
	
	public String toString() {
		return username + "_" + percentage;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((percentage == null) ? 0 : percentage.hashCode());
		result = prime * result + ((username == null) ? 0 : username.hashCode());
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
		CompetitionUserMark other = (CompetitionUserMark) obj;
		if (percentage == null) {
			if (other.percentage != null)
				return false;
		} else if (!percentage.equals(other.percentage))
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}

	@Override
	public int compareTo(CompetitionUserMark target) {
		return target.getPercentage().compareTo(percentage); 
	}

	@Override
	public int[] sqlTypes() {
		return new int[] {Types.VARCHAR};
	}

	@Override
	public Class returnedClass() {
		return CompetitionUserMark.class;
	}

	@Override
	public boolean equals(Object x, Object y) throws HibernateException {
		if(!(x instanceof CompetitionUserMark) || !(y instanceof CompetitionUserMark)) {
			return false;
		}
		return x.equals(y);
	}

	@Override
	public int hashCode(Object x) throws HibernateException {
		return x.hashCode();
	}

	@Override
	public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException,
			SQLException {
		String value = (String) StandardBasicTypes.STRING.nullSafeGet(rs, names[0]);
        return ((value != null) ? new CompetitionUserMark(value) : null);
	}

	@Override
	public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException,
			SQLException {
		StandardBasicTypes.STRING.nullSafeSet(st, (value != null) ? value.toString() : null, index);
	}

	@Override
	public Object deepCopy(Object value) throws HibernateException {
		return value == null ? null : new CompetitionUserMark(((CompetitionUserMark)value).toString());
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
