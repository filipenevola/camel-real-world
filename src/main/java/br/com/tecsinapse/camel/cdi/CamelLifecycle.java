package br.com.tecsinapse.camel.cdi;

import br.com.tecsinapse.camel.cdi.annotation.CamelRoute.CamelRouteLiteral;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.servletlistener.CamelContextLifecycle;
import org.apache.camel.component.servletlistener.ServletCamelContext;
import org.apache.camel.impl.SimpleRegistry;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;

public class CamelLifecycle implements CamelContextLifecycle<SimpleRegistry> {

    @Override
    public void beforeStart(ServletCamelContext camelContext, SimpleRegistry registry) throws Exception {
        final Instance<RouteBuilder> routes = CDI.current().select(RouteBuilder.class, new CamelRouteLiteral());

        for (RouteBuilder routeBuilder : routes) {
            camelContext.addRoutes(routeBuilder);
        }
    }

    @Override
    public void afterStart(ServletCamelContext camelContext, SimpleRegistry registry) throws Exception {

    }

    @Override
    public void beforeStop(ServletCamelContext camelContext, SimpleRegistry registry) throws Exception {

    }

    @Override
    public void afterStop(ServletCamelContext camelContext, SimpleRegistry registry) throws Exception {

    }

    @Override
    public void beforeAddRoutes(ServletCamelContext servletCamelContext, SimpleRegistry simpleRegistry) throws Exception {
    }

    @Override
    public void afterAddRoutes(ServletCamelContext servletCamelContext, SimpleRegistry simpleRegistry) throws Exception {
    }
}