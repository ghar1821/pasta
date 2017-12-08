package pasta.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import pasta.service.PASTAOptions;

public class GlobalModelAtributesInterceptor extends HandlerInterceptorAdapter {

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		if(request != null) {
			request.setAttribute("applicationName", PASTAOptions.instance().get("application.name"));
		}
	}
}
