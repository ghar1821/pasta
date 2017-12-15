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

package pasta.config.options;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import pasta.domain.options.Option;
import pasta.scheduler.AssessmentJobExecutor;

@Component
public class ThreadPoolUpdater implements OptionsListener {

	@Autowired
	private AssessmentJobExecutor executor;
	
	@Override
	public boolean actsOn(Option option) {
		String key = option.getKey();
		return key.equals("execution.threads.core.size") || key.equals("execution.threads.max.size");
	}

	@Override
	public void optionUpdated(Option option) {
		String key = option.getKey();
		try {
			int value = new Integer(option.getValue());
			if(key.equals("execution.threads.core.size")) {
				executor.setCorePoolSize(value);
			}
			if(key.equals("execution.threads.max.size")) {
				executor.setMaximumPoolSize(value);
			}
		} catch(NumberFormatException e) {
			return;
		}
	}

}
