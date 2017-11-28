package pasta.web.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import pasta.service.PASTAOptions;

public class GlobalModelAtributesInterceptor extends HandlerInterceptorAdapter {

	public GlobalModelAtributesInterceptor() {
		Logger.getLogger(getClass()).info("==================================== GlobalModelAtributes");
	}
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		Logger.getLogger(getClass()).info("PREHANDLE");
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		Logger.getLogger(getClass()).info("POSTHANDLE");
		modelAndView.getModel().put("applicationName", PASTAOptions.instance().get("application.name"));
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		Logger.getLogger(getClass()).info("AFTERCOMPLETION");
	}
}
