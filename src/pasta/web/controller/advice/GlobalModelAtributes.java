package pasta.web.controller.advice;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import pasta.service.PASTAOptions;

@ControllerAdvice
public class GlobalModelAtributes {

	@ModelAttribute("applicationName")
    public String populateInstanceName() {
        return PASTAOptions.instance().get("application.name");
    }
}
