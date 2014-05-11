package br.com.tecsinapse.camel.filter;

import com.google.common.base.Charsets;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;

@WebFilter(filterName = "characterEncodingFilter", urlPatterns = {"/*"})
public class CharacterEncodingFilter implements Filter {

	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		
		request.setCharacterEncoding(Charsets.UTF_8.displayName());
		
		response.setCharacterEncoding(Charsets.UTF_8.displayName());
		
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
		
	}
	
}
