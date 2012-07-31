package security;

import java.security.Permission;
import java.util.PropertyPermission;

public class CustomSecurityManager extends SecurityManager{
	// block EVERYTHING
	public CustomSecurityManager(){
		System.out.println("SecurityManager replaced");
	}
	
	@Override
	public void checkPermission(Permission perm) {

		if(perm.implies(new RuntimePermission("setIO")) || 
				perm.implies(new PropertyPermission("user.dir", "read")) ||
				perm.implies(new PropertyPermission("line.separator", "read")) ||
				perm.implies(new RuntimePermission("setContextClassLoader"))||
				perm.implies(new RuntimePermission("accessDeclaredMembers"))||
				perm.implies(new RuntimePermission("getProtectionDomain"))){
				// do nothing
			return;
		}
		super.checkPermission(perm);
	}
	
	@Override
	public void checkExit(int status) {
		// do nothing
	}
	
	@Override
	public void checkRead(String file) {
		// TODO Auto-generated method stub
		if(file.matches(".*/other/ant/.*/lib/.*\\.jar") ||
				file.matches(System.getProperty("user.dir")+".*")){
			// do nothing
			return;
		}
		super.checkRead(file);
	}
	
	@Override
	public void checkDelete(String file) {
		if(file.matches(System.getProperty("user.dir")+"/bin.*")){
			// do nothing
			return;
		}
		super.checkDelete(file);
	}
	
	@Override
	public void checkWrite(String file) {
		if(file.endsWith("TEST-AllTests.xml")){
			// do nothing
			return;
		}
		super.checkWrite(file);
	}	
}