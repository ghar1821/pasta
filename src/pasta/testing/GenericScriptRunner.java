package pasta.testing;

import java.io.File;
import java.io.FileNotFoundException;

import pasta.testing.options.ScriptOptions;
import pasta.util.PASTAUtil;

public class GenericScriptRunner extends Runner {

	private static String TEMPLATE_FILENAME = "generic_script.xml";

	public GenericScriptRunner() throws FileNotFoundException {
		super(PASTAUtil.getTemplateResource("build_templates/" + TEMPLATE_FILENAME));
	}
	
	public void setBuildScript(ScriptOptions script) {
		addOption("hasbuild", "1");
		addOption("buildscriptfilename", script.scriptFilename);
		addOption("builderrorfilename", script.errorFilename);
		addOption("buildoutputfilename", script.outputFilename);
		addOption("buildinputfilename", script.inputFilename);
		setBuildTimeout(script.timeout);
		addArgsOption("buildargs", script.getArguments());
		addEnvOption("buildenv", script.getEnvironment());
	}
	
	public void setRunScript(ScriptOptions script) {
		addOption("hasrun", "1");
		addOption("runscriptfilename", script.scriptFilename);
		addOption("runerrorfilename", script.errorFilename);
		addOption("runoutputfilename", script.outputFilename);
		addOption("runinputfilename", script.inputFilename);
		setRunTimeout(script.timeout);
		addArgsOption("runargs", script.getArguments());
		addEnvOption("runenv", script.getEnvironment());
	}
	
	public void setBuildTimeout(long timeout) {
		if(timeout > 0) {
			addOption("buildtimeout", String.valueOf(timeout));
		}
	}
	
	public void setRunTimeout(long timeout) {
		if(timeout > 0) {
			addOption("runtimeout", String.valueOf(timeout));
		}
	}

	@Override
	public String extractCompileErrors(File compileErrorFile, AntResults results) {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public String extractFilesCompiled(AntResults results) {
		// TODO Auto-generated method stub
		return "";
	}
}
