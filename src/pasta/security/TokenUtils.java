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

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import pasta.util.ProjectProperties;

@Component
public class TokenUtils {
	public final static int EXP_TIME = 30; // minutes
	
	public String generateToken(HttpServletRequest request, String username, String password) {
		String ipAddress = request.getHeader("X-FORWARDED-FOR");
		if(ipAddress == null) {
			ipAddress = request.getRemoteAddr();
		}
		Date now = new Date();
		Calendar exp = Calendar.getInstance();
		exp.setTime(now);
		exp.add(Calendar.MINUTE, EXP_TIME);
		byte[] key = ProjectProperties.getInstance().getAuthenticationSettings().getKey();
		return Jwts.builder()
				.setIssuer(ProjectProperties.getInstance().getName())
				.setIssuedAt(now)
				.setAudience(ipAddress)
				.setExpiration(exp.getTime())
				.claim("username", username)
				.claim("password", password)
				.signWith(SignatureAlgorithm.HS256, key)
				.compact();
	}
	
	public boolean validate(HttpServletRequest request, String token) {
		byte[] key = ProjectProperties.getInstance().getAuthenticationSettings().getKey();
		try {
			Jws<Claims> jws = Jwts.parser().setSigningKey(key).parseClaimsJws(token);
			if(jws.getBody().getExpiration().before(new Date())) {
				return false;
			}
			Calendar exp = Calendar.getInstance();
			exp.setTime(jws.getBody().getIssuedAt());
			exp.add(Calendar.MINUTE, EXP_TIME);
			if(exp.getTime().before(new Date())) {
				return false;
			}
			if(!jws.getBody().getIssuer().equals(ProjectProperties.getInstance().getName())) {
				return false;
			}
			String ipAddress = request.getHeader("X-FORWARDED-FOR");
			if(ipAddress == null) {
				ipAddress = request.getRemoteAddr();
			}
			if(jws.getBody().getAudience() == null) {
				if(ipAddress != null) {
					return false;
				}
			} else if(!jws.getBody().getAudience().equals(ipAddress)) {
				return false;
			}
		} catch (SignatureException e) {
			return false;
		}
		return true;
	}

	public UserDetails getUserFromToken(String token) {
		byte[] key = ProjectProperties.getInstance().getAuthenticationSettings().getKey();
		try {
			Jws<Claims> jws = Jwts.parser().setSigningKey(key).parseClaimsJws(token);
			User user = new User(jws.getBody().get("username").toString(), jws.getBody().get("password").toString(), new LinkedList<GrantedAuthority>());
			return user;
		} catch (SignatureException e) {
			return null;
		}
	}
}
