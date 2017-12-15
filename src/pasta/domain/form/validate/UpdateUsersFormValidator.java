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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import pasta.domain.form.UpdateUsersForm;
import pasta.util.ProjectProperties;

/**
 * @author Joshua Stretton
 * @version 1.0
 * @since 9 Jul 2015
 *
 */
@Component
public class UpdateUsersFormValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return UpdateUsersForm.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		UpdateUsersForm form = (UpdateUsersForm) target;
		
		if(!form.isReplace() && (form.getFile() == null || form.getFile().isEmpty()) && form.getUpdateContents().isEmpty()) {
			errors.reject("NoChange");
			return;
		}
		
		StringBuilder newData = new StringBuilder();
		if(form.getUpdateContents() != null) {
			newData.append(form.getUpdateContents()).append(System.lineSeparator());
		}
		
		Scanner content = null;
		if(form.getFile() != null && !form.getFile().isEmpty()) {
			File updateFile = new File(ProjectProperties.getInstance().getSandboxLocation() + form.getFile().getOriginalFilename());
			try {
				form.getFile().transferTo(updateFile);
				content = new Scanner(new FileInputStream(updateFile));
				while(content.hasNextLine()) {
					newData.append(content.nextLine()).append(System.lineSeparator());
				}
				content.close();
				if(updateFile != null) {
					updateFile.delete();
				}
			} catch (IllegalStateException | IOException e) {
				Logger.getLogger(getClass()).error("Problem saving uploaded file to sandbox: " + updateFile, e);
			}
		}
		
		form.setFile(null);
		form.setUpdateContents(newData.toString());
		
		content = new Scanner(form.getUpdateContents());
		while(content.hasNext()) {
			String line = content.nextLine().replaceAll("\\s+", "");
			if(line.isEmpty()) {
				continue;
			}
			if(form.isUpdateTutors()) {
				String[] parts = line.split(",", 3); 
				if(parts.length < 2) {
					errors.rejectValue("updateContents", "MissingPermission", new Object[]{parts[0]}, "");
				}
			} 
		}
		content.close();
	}

}
