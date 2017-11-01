package pasta.domain.form;

import java.util.List;

import org.apache.log4j.Logger;

import pasta.domain.options.Option;

public class UpdateOptionsForm {

	private List<Option> options;
	
	private String addKey;
	private String addValue;
	
	private String addOptions;
	
	public UpdateOptionsForm(List<Option> allOptions) {
		setOptions(allOptions);
		setAddKey(null);
		setAddValue(null);
		setAddOptions(null);
	}

	public List<Option> getOptions() {
		return options;
	}

	public void setOptions(List<Option> options) {
		if(this.options == null || options == null) {
			this.options = options;
		} else {
			this.options.clear();
			this.options.addAll(options);
		}
	}

	public String getAddKey() {
		return addKey;
	}

	public void setAddKey(String addKey) {
		this.addKey = addKey;
	}

	public String getAddValue() {
		return addValue;
	}

	public void setAddValue(String addValue) {
		this.addValue = addValue;
	}

	public String getAddOptions() {
		return addOptions;
	}

	public void setAddOptions(String addOptions) {
		this.addOptions = addOptions;
	}
}
