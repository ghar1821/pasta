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

package pasta.domain.form.validate;

import java.util.Scanner;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import pasta.domain.form.UpdateOptionsForm;
import pasta.domain.options.Option;

/**
 * @author Joshua Stretton
 * @version 1.0
 * @since 1 Nov 2017
 */
@Component
public class UpdateOptionsFormValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return UpdateOptionsForm.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		UpdateOptionsForm form = (UpdateOptionsForm) target;
		
		boolean addKey = form.getAddKey() != null && !form.getAddKey().isEmpty();
		boolean addValue = form.getAddValue() != null && !form.getAddValue().isEmpty();
		if(addKey && !addValue) {
			errors.rejectValue("addValue", "NeedBoth");
		}
		if(!addKey && addValue) {
			errors.rejectValue("addKey", "NeedBoth");
		}
		
		String keyRegex = "[a-z][-a-z0-9._]*";
		
		if(addKey) {
			form.setAddKey(form.getAddKey().toLowerCase());
			if(!form.getAddKey().matches(keyRegex)) {
				errors.rejectValue("addKey", "InvalidCharacters");
			}
		}
		
		int lineNum = 0;
		if(form.getAddOptions() != null && !form.getAddOptions().isEmpty()) {
			form.setAddOptions(form.getAddOptions().toLowerCase());
			Scanner lineScn = new Scanner(form.getAddOptions());
			while(lineScn.hasNext()) {
				String line = lineScn.nextLine();
				lineNum++;
				if(line.trim().isEmpty()) {
					continue;
				}
				String[] parts = line.split("=", 2);
				if(parts.length != 2) {
					errors.rejectValue("addOptions", "MissingValue", new Object[] {lineNum, line}, "");
				}
				String key = parts[0];
				if(!key.matches(keyRegex)) {
					errors.rejectValue("addOptions", "InvalidCharacters", new Object[] {lineNum, key}, "");
				}
			}
			lineScn.close();
		}
		
		for(int i = 0; i < form.getOptions().size(); i++) {
			Option opt = form.getOptions().get(i);
			opt.setKey(opt.getKey().toLowerCase());
			if(opt.getKey().isEmpty()) {
				continue;
			}
			if(!opt.getKey().matches(keyRegex)) {
				errors.rejectValue("options[" + i + "].key", "InvalidCharacters");
			}
		}
	}

}
