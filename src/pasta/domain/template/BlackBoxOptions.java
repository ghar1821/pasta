package pasta.domain.template;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import pasta.archive.Archivable;
import pasta.archive.RebuildOptions;

@Entity
@Table(name = "black_box_options")
public class BlackBoxOptions implements Archivable<BlackBoxOptions> {
	private static final long serialVersionUID = 1094750990939205404L;
	
	@Id @GeneratedValue
	private Long id;
	
	@Column(name = "detailed_errors")
	private boolean detailedErrors;
	
	@Column(name = "gcc_command_line_args")
	private String gccCommandLineArgs;
	
	public BlackBoxOptions() {
		detailedErrors = true;
		gccCommandLineArgs = "-w -std=c99";
	}
	
	public BlackBoxOptions(BlackBoxOptions copy) {
		update(copy);
	}
	
	public void update(BlackBoxOptions copy) {
		if(copy == null) {
			copy = new BlackBoxOptions();
		}
		this.detailedErrors = copy.detailedErrors;
		this.gccCommandLineArgs = copy.gccCommandLineArgs;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public boolean isDetailedErrors() {
		return detailedErrors;
	}

	public void setDetailedErrors(boolean detailedErrors) {
		this.detailedErrors = detailedErrors;
	}

	public String getGccCommandLineArgs() {
		return gccCommandLineArgs;
	}

	public void setGccCommandLineArgs(String gccCommandLineArgs) {
		this.gccCommandLineArgs = gccCommandLineArgs;
	}

	@Override
	public BlackBoxOptions rebuild(RebuildOptions options) {
		return new BlackBoxOptions(this);
	}
}
