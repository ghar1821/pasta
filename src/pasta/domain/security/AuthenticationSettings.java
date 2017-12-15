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

package pasta.domain.security;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import pasta.domain.BaseEntity;
import pasta.domain.VerboseName;

@Entity
@Table (name = "authentication_settings")
@VerboseName(value = "authentication settings", plural = "authentication settings")
public class AuthenticationSettings extends BaseEntity implements Serializable {
	private static final long serialVersionUID = -3844260322329134309L;

	// auth type (dummy, imap, database), etc
	@Column(name="auth_type")
	private String type;
	
	@Column(name="secret")
	private byte[] key;
	
	@ElementCollection
	@CollectionTable(name = "server_addresses", joinColumns = @JoinColumn(name = "auth_setting_id"))
	@Column(name = "server_address")
	@LazyCollection(LazyCollectionOption.FALSE)
	private List<String> serverAddresses;
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public byte[] getKey() {
		return key;
	}
	public void setKey(byte[] key) {
		this.key = key;
	}
	
	public List<String> getServerAddresses() {
		return serverAddresses;
	}
	public void setServerAddresses(List<String> serverAddresses) {
		this.serverAddresses = serverAddresses;
	}
}
