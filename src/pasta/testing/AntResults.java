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

package pasta.testing;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class AntResults {

	private Map<String, StringBuilder> outputs;
	private Map<String, Boolean> success;
	
	public AntResults() {
		outputs = new LinkedHashMap<String, StringBuilder>();
		success = new HashMap<String, Boolean>();
	}
	
	public AntResults(String... targets) {
		this();
		addTargets(targets);
	}
	
	public void addTargets(String... targets) {
		for(String target : targets) {
			outputs.put(target, new StringBuilder());
		}
	}
	
	public boolean append(String target, String contents) {
		StringBuilder sb = outputs.get(target);
		if(sb == null) {
			return false;
		}
		sb.append(contents).append(System.lineSeparator());
		return true;
	}
	
	public String getOutput(String target) {
		if(!outputs.containsKey(target)) {
			return "";
		}
		return outputs.get(target).toString();
	}
	
	public boolean isSuccess(String target) {
		return hasRun(target) && success.get(target);
	}
	
	public boolean hasRun(String target) {
		return success.get(target) != null;
	}
	
	public void setSuccess(String target, boolean success) {
		this.success.put(target, success);
	}
	
	public String getFullOutput() {
		StringBuilder sb = new StringBuilder();
		for(Map.Entry<String, StringBuilder> entry : outputs.entrySet()) {
			sb.append(entry.getValue().toString()).append(System.lineSeparator());
		}
		return sb.toString();
	}
	
	protected void registerExtraLabel(String label) {
		outputs.put(label, new StringBuilder());
	}
}
