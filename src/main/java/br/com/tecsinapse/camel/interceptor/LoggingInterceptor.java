package br.com.tecsinapse.camel.interceptor;

import java.io.Serializable;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Interceptor
@Logging
public class LoggingInterceptor implements Serializable {
	private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);

	@AroundInvoke
	public Object log(InvocationContext context) throws Exception {
		logger.info(string(context));
		return context.proceed();
	}

	private String string(InvocationContext context) {
		final String clazz = context.getMethod().getName();
		final Object[] parameters = context.getParameters();
		StringBuilder sb = new StringBuilder();
		sb.append("call ");
		sb.append(clazz);
		if (parameters != null && parameters.length > 0) {
			sb.append(": params=[");
			for (Object o : parameters) {
				if(o != null) {
					sb.append(o.toString());
				}
			}
			sb.append("]");
		}
		return sb.toString();
	}
}