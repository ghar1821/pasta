package security;

import java.security.Permission;
import java.util.PropertyPermission;

public class CustomSecurityManager extends SecurityManager{
	// block EVERYTHING
	public CustomSecurityManager(){
	}
	
	@Override
	public void checkPermission(Permission perm) {

		if(perm.implies(new RuntimePermission("setIO")) || 
				perm.implies(new PropertyPermission("user.dir", "read")) ||
				perm.implies(new PropertyPermission("line.separator", "read")) ||
				perm.implies(new PropertyPermission("java.home", "read")) ||
				perm.implies(new PropertyPermission("java.security.debug", "read")) ||
				perm.implies(new PropertyPermission("ibm.system.encoding", "read")) ||
				perm.implies(new PropertyPermission("jdk.lang.Process.launchMechanism", "read")) ||
				perm.implies(new java.util.logging.LoggingPermission("control", null))||
				perm.implies(new RuntimePermission("setContextClassLoader"))||
				perm.implies(new RuntimePermission("shutdownHooks"))||
				perm.implies(new RuntimePermission("accessDeclaredMembers"))||
				perm.implies(new RuntimePermission("createClassLoader"))||
				perm.implies(new RuntimePermission("modifyThreadGroup"))||
				perm.implies(new RuntimePermission("writeFileDescriptor"))||
				perm.implies(new RuntimePermission("readFileDescriptor"))||
				perm.implies(new RuntimePermission("loadLibrary.net"))||
				perm.implies(new RuntimePermission("loadLibrary.nio"))||
				perm.implies(new RuntimePermission("modifyThread"))||
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
		// allow ant jars and the files in the working folder be read
		if(file.matches(".*/lib/.*\\.jar") ||
				file.matches(System.getProperty("user.dir")+".*") || 
				file.endsWith("StrongInversion") || 
				file.endsWith("so") || 
				file.endsWith("out.output") || 
				file.endsWith("python") ||
				file.endsWith("/usr/local/bin/pastarun") || 
				file.endsWith("java") || 
				file.endsWith("Test1.txt") || 
				file.endsWith("Test2.txt") || 
				file.endsWith("Test3.txt") || 
				file.endsWith("Test4.txt") || 
				file.endsWith("Test5.txt") || 
				file.endsWith("Test6.txt") || 				
				file.endsWith("py")){
			// do nothing
			return;
		}
		super.checkRead(file);
	}
	
	@Override
	public void checkExec(String file) {
		// allow ant jars and the files in the working folder be read
		if(file.matches(".*/lib/.*\\.jar") ||
				file.matches(System.getProperty("user.dir")+".*") || 
				file.endsWith("StrongInversion") || 
				file.endsWith("/usr/local/bin/pastarun") || 
				file.endsWith("python") || 
				file.endsWith("StrongInversion.java") || 
				file.endsWith("StrongInversion.py")){
			// do nothing
			return;
		}
		super.checkRead(file);
	}
	
	@Override
	public void checkDelete(String file) {
		// allow the bin folder to be deleted
		if(file.matches(System.getProperty("user.dir")+"/bin.*")||
			file.endsWith("result.xml")){
			// do nothing
			return;
		}
		super.checkDelete(file);
	}
	
	@Override
	public void checkWrite(String file) {
		// allow results to be written
		if(file.endsWith("results.xml") ||
			file.endsWith("compile.errors") ||
			file.matches(".*/junitvmwatcher\\d*\\.properties")){
			// do nothing
			return;
		}
		super.checkWrite(file);
	}	
}
