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
