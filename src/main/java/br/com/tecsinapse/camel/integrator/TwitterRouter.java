package br.com.tecsinapse.camel.integrator;

import br.com.tecsinapse.camel.cdi.EnvProperties;
import br.com.tecsinapse.camel.repository.SocialRepository;
import com.google.common.collect.ImmutableList;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.ContextName;
import org.apache.camel.component.twitter.TwitterComponent;
import org.apache.camel.support.ExpressionAdapter;
import org.slf4j.Logger;
import twitter4j.Status;

import javax.inject.Inject;
import java.util.List;

@ContextName
public class TwitterRouter extends RouteBuilder {

    @Inject
    private transient Logger logger;
    @Inject
    private EnvProperties envProperties;
    @Inject
    private SocialRepository socialRepository;

    private static final ImmutableList<String> TWITTERS = ImmutableList.of("FilipeNevola", "TheDevConf", "globalcode", "tecsinapse");

    @Override
    public void configure() throws Exception {
        logger.info("starting configure...");
        configureTwitterComponent();
        configureTimelines();
        configureSearch();
    }

    private void configureTwitterComponent() {
        TwitterComponent tc = getContext().getComponent("twitter", TwitterComponent.class);
        tc.setAccessToken(envProperties.twitterAccessToken());
        tc.setAccessTokenSecret(envProperties.twitterAccessTokenSecret());
        tc.setConsumerKey(envProperties.twitterConsumerKey());
        tc.setConsumerSecret(envProperties.twitterConsumerSecret());
    }

    private void configureTimelines() {
        if (!envProperties.camelRouteTwitter()) {
            logger.info("configureTimelines disabled");
            return;
        }

        //twitter
        for (String user : TWITTERS) {
            logger.info("starting route {}...", user);
            fromF("twitter://timeline/user?type=polling&delay=300&user=%s", user)
                    .process(e -> socialRepository.arriveTweet(e.getIn().getBody(Status.class)));
        }
    }


    private void configureSearch() {
        if (!envProperties.camelRouteTwitter()) {
            logger.info("configureSearch disabled");
            return;
        }

        //twitter
        logger.info("starting route search {}...");
        from("direct:twitterSearch")
            .to("twitter://search")
                .process(e -> socialRepository.arriveTwitterSearchResult(e.getIn().getBody(List.class)));
    }

    private Expression getKeyword() {
        return new ExpressionAdapter() {
            @Override
            public Object evaluate(Exchange exchange) {
                return socialRepository.getKeyword();
            }
        };
    }

}
