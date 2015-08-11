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
