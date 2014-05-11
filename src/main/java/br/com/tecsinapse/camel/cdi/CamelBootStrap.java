package br.com.tecsinapse.camel.cdi;

import br.com.tecsinapse.camel.cdi.annotation.CamelRoute;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.CdiCamelContext;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

@Startup
@Singleton
public class CamelBootStrap {
	@Inject
	private Logger logger;
//	@Inject
	private CdiCamelContext camelCtx;
	@Inject
	@CamelRoute
	private Instance<RouteBuilder> routes;

	@PostConstruct
	public void init() {
        //FIXME @Inject CdiCamelContext está lançando exeção:
        // WELD-001409: Ambiguous dependencies for type CdiCamelContext with qualifiers @Default
        //Managed Bean [class org.apache.camel.cdi.CdiCamelContext] with qualifiers [@Any @Default],
        //- org.apache.camel.cdi.internal.CamelContextBean@26a1f00f
        //Verificar motivo e retornar inicialização por aqui
        if(true) {
            return;
        }

		logger.info("Iniciando camel");

		for (RouteBuilder routeBuilder : routes) {
			logger.info("Registrando rota: {}", routeBuilder);
            try {
                camelCtx.addRoutes(routeBuilder);
            } catch (Exception e) {
                throw Throwables.propagate(e);
            }
        }

		if (!Iterables.isEmpty(routes)) {
			camelCtx.getShutdownStrategy().setTimeout(120);
			camelCtx.start();
		}
	}

	@PreDestroy
	public void stop() {
		//camelCtx.stop();
	}
}
