package pasta.docker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.usertype.UserType;

import pasta.service.PASTAOptions;
import pasta.testing.BlackBoxTestRunner;
import pasta.util.PASTAUtil;

public class Language implements UserType, Comparable<Language> {
	protected static Logger logger = Logger.getLogger(Language.class);
	
	private String id;
	private File templateFile;
	private File dockerFile;
	private Class<? extends BlackBoxTestRunner> runnerClass;
	
	private DockerBuildFile dockerBuildFile;
	
	//For Hibernate
	public Language() {}
	
	@SuppressWarnings("unchecked")
	public Language(String id, String templateFile, String dockerFile, String runnerClass) {
		this.id = id;
		try {
			this.templateFile = PASTAUtil.getTemplateResource("build_templates/" + templateFile);
		} catch (FileNotFoundException e) {
			logger.error("Cannot find template file for " + this.id + ": " + templateFile);
		}
		try {
			this.dockerFile = PASTAUtil.getTemplateResource("docker/" + dockerFile);
		} catch (FileNotFoundException e) {
			logger.error("Cannot find docker file for " + this.id + ": " + dockerFile);
		}
		this.dockerBuildFile = new DockerBuildFile(id + "-exec", getDockerFile());
		try {
			this.runnerClass = (Class<? extends BlackBoxTestRunner>) Class.forName(runnerClass);
		} catch (ClassNotFoundException e) {
			logger.error("Class not found: " + runnerClass + " for " + getName(), e);
		}
	}

	public String getId() {
		return id;
	}
	public String getName() {
		String value = PASTAOptions.instance()
				.get("languages." + this.getId() + ".name");
		if(value == null) {
			logger.warn("Option \"languages." + this.getId() + ".name\" not found");
			return "Unknown";
		}
		return value;
	}
	public List<String> getExtensions() {
		String value = PASTAOptions.instance()
				.get("languages." + this.getId() + ".extensions");
		if(value == null) {
			logger.warn("Option \"languages." + this.getId() + ".extensions\" not found");
			return new LinkedList<>();
		}
		return Arrays.asList(value.split(","));
	}
	public File getTemplateFile() {
		return templateFile;
	}
	public File getDockerFile() {
		return dockerFile;
	}
	public DockerBuildFile getDockerBuildFile() {
		return dockerBuildFile;
	}
	public BlackBoxTestRunner getRunner() {
		try {
			return runnerClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			logger.error("Error creating runner for " + getName(), e);
		}
		return null;
	}
	
	public long getTestCaseExecutionOverhead() {
		String value = PASTAOptions.instance()
				.get("languages." + this.getId() + ".test-case-overhead");
		if(value == null) {
			logger.warn("Option \"languages." + this.getId() + ".test-case-overhead\" not found");
			return 0;
		}
		try {
			return Long.valueOf(value);
		} catch(NumberFormatException e) {
			logger.warn("\"" + value + "\" is not a valid value for test-case-overhead. Must be a whole number (in milliseconds)");
		}
		return 0;
	}
	public long getTestSuiteExecutionOverhead() {
		String value = PASTAOptions.instance()
				.get("languages." + this.getId() + ".test-suite-overhead");
		if(value == null) {
			logger.warn("Option \"languages." + this.getId() + ".test-suite-overhead\" not found");
			return 0;
		}
		try {
			return Long.valueOf(value);
		} catch(NumberFormatException e) {
			logger.warn("\"" + value + "\" is not a valid value for test-suite-overhead. Must be a whole number (in milliseconds)");
		}
		return 0;
	}

	public String getImageName() {
		return dockerBuildFile.getTag();
	}
	
	public boolean isLanguage(File file) {
		return file != null && !file.isDirectory() && isLanguage(file.getName());
	}
	public boolean isLanguage(String filename) {
		return filename != null &&
				getExtensions().contains(filename.substring(filename.lastIndexOf('.') + 1).toLowerCase());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		Language other = (Language) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public Object assemble(Serializable cached, Object owner) throws HibernateException {
		return deepCopy(cached);
	}

	@Override
	public Object deepCopy(Object value) throws HibernateException {
		return value == null ? null : LanguageManager.getInstance().getLanguage(((Language)value).getId());
	}

	@Override
	public Serializable disassemble(Object value) throws HibernateException {
		return (Serializable) deepCopy(value);
	}

	@Override
	public boolean equals(Object x, Object y) throws HibernateException {
		if(!(x instanceof Language) || !(y instanceof Language)) {
			return false;
		}
		return x.equals(y);
	}

	@Override
	public int hashCode(Object x) throws HibernateException {
		return x.hashCode();
	}

	@Override
	public boolean isMutable() {
		return true;
	}

	@Override
	public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner)
			throws HibernateException, SQLException {
		String value = StandardBasicTypes.STRING.nullSafeGet(rs, names[0], session);
		return ((value != null) ? LanguageManager.getInstance().getLanguage(value) : null);
	}

	@Override
	public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session)
			throws HibernateException, SQLException {
		StandardBasicTypes.STRING.nullSafeSet(st, (value != null) ? ((Language)value).getId() : null, index, session);
	}

	@Override
	public Object replace(Object original, Object target, Object owner) throws HibernateException {
		return deepCopy(original);
	}

	@Override
	public Class<?> returnedClass() {
		return Language.class;
	}

	@Override
	public int[] sqlTypes() {
		return new int[] {Types.VARCHAR};
	}

	@Override
	public int compareTo(Language o) {
		return this.getName().compareTo(o.getName());
	}

	@Override
	public String toString() {
		return getName();
	}
}
