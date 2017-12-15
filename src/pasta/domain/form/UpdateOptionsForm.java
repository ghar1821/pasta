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
