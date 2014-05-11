package br.com.tecsinapse.camel.filter;

import org.slf4j.MDC;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@WebFilter(filterName = "MDCLoggingFilter", urlPatterns = {"/*"})
public class MDCLoggingFilter implements Filter {

    private static final String REQUEST_URL = "requestURL";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;

        String requestURL = req.getRequestURL().toString();
        MDC.put(REQUEST_URL, requestURL);

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(REQUEST_URL);
        }
    }

    @Override
    public void init(FilterConfig config) throws ServletException {
    }

    @Override
    public void destroy() {
    }

}
