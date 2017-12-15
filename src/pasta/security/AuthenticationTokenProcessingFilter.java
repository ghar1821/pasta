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

package pasta.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

public class AuthenticationTokenProcessingFilter extends UsernamePasswordAuthenticationFilter {

    @Autowired 
    private TokenUtils tokenUtils;
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
       
    	String token = ((HttpServletRequest) request).getHeader("Auth-Token");
        if(token != null && !token.isEmpty()) {
            // validate the token
            if (tokenUtils.validate((HttpServletRequest) request, token)) {
                // determine the user based on the (already validated) token
                UserDetails login = tokenUtils.getUserFromToken(token);
                
            	// build an Authentication object with the user's info
            	UsernamePasswordAuthenticationToken authentication = 
            			new UsernamePasswordAuthenticationToken(login.getUsername(), login.getPassword());
            	authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails((HttpServletRequest) request));
            	// set the authentication into the SecurityContext
            	try {
            		SecurityContextHolder.getContext().setAuthentication(getAuthenticationManager().authenticate(authentication));
            	} catch(AuthenticationException e) {
            		if(response instanceof HttpServletResponse) {
            			((HttpServletResponse) response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            		}
            	}
            }
        }
        // continue thru the filter chain
        chain.doFilter(request, response);
    }
}