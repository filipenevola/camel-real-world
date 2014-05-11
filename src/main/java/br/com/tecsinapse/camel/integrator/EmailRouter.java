package br.com.tecsinapse.camel.integrator;

import br.com.tecsinapse.camel.cdi.EnvProperties;
import br.com.tecsinapse.camel.cdi.annotation.CamelRoute;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;

import javax.inject.Inject;

@CamelRoute
public class EmailRouter extends RouteBuilder {
    // 1 minute
    private final int DELAY = 60000;

    @Inject
    private transient Logger logger;
    @Inject
    private EnvProperties envProperties;
    @Inject
    private EmailRepository emailRepository;

    @Override
    public void configure() throws Exception {
        logger.info("starting configure...");

        if (!envProperties.camelRouteEmail()) {
            logger.info("configureEmail disabled");
            return;
        }

        fromF("imaps://imap.gmail.com?username=%s&password=%s&delete=%s&unseen=%s&consumer.delay=%s",
                envProperties.emailUsername(), envProperties.emailPassword(), false, true, DELAY)
                .process(exchange -> emailRepository.arriveEmail((org.apache.camel.component.mail.MailMessage) exchange.getIn()));

    }
}
