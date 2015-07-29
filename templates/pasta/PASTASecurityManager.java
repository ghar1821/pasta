package pasta;

import java.security.Permission;
import java.util.PropertyPermission;
import java.io.File;
import java.io.IOException;

public class PASTASecurityManager extends SecurityManager{
	// block EVERYTHING
	public PASTASecurityManager(){
	}

	@Override
	public void checkPermission(Permission perm) {
		if(new RuntimePermission("loadLibrary.net").implies(perm) ||
				new RuntimePermission("loadLibrary.nio").implies(perm)) {
			return;
		}

		if(perm.implies(new RuntimePermission("setIO")) ||
				perm.implies(new PropertyPermission("user.dir", "read")) ||
				perm.implies(new PropertyPermission("java.home", "read")) ||
				perm.implies(new PropertyPermission("line.separator", "read")) ||
				perm.implies(new RuntimePermission("setContextClassLoader"))||
				perm.implies(new RuntimePermission("accessDeclaredMembers"))||
				perm.implies(new RuntimePermission("createClassLoader"))||
				perm.implies(new java.lang.reflect.ReflectPermission("suppressAccessChecks"))||
				perm.implies(new RuntimePermission("getStackTrace"))||
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
		// allow the working folder to be read
		try {
			String fullPath = new File(file).getCanonicalPath();
			if(fullPath.matches(System.getProperty("user.dir")+"/.*")) {
				return;
			}
		} catch(IOException e) {}

		// allow ant jars to be read
		if(file.matches(".*/lib/.*\\.jar") ||
			file.matches(System.getProperty("java.home")+"/.*")){
			// do nothing
			return;
		}
		super.checkRead(file);
	}

	@Override
	public void checkDelete(String file) {
		// allow the bin folder to be deleted
		if(file.matches(System.getProperty("user.dir")+"bin.*")){
			// do nothing
			return;
		}
		super.checkDelete(file);
	}

	@Override
	public void checkWrite(String file) {
		// allow results to be written
		if(file.endsWith("results.xml") ||
			file.matches(".*/junitvmwatcher\\d*\\.properties")){
			// do nothing
			return;
		}
		try {
			String fullPath = new File(file).getCanonicalPath();
			if(fullPath.matches(System.getProperty("user.dir")+"/.*")) {
				return;
			}
		} catch(IOException e) {}
		super.checkWrite(file);
	}
}
