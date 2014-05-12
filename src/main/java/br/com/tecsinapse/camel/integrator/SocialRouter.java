package br.com.tecsinapse.camel.integrator;

import br.com.tecsinapse.camel.cdi.EnvProperties;
import com.google.common.collect.ImmutableList;
import com.sun.syndication.feed.synd.SyndFeed;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.ContextName;
import org.apache.camel.component.twitter.TwitterComponent;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import twitter4j.Status;

import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;

import static com.google.common.base.Strings.nullToEmpty;

@ContextName
public class SocialRouter extends RouteBuilder {

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
        configureTwitter();
        configureFeed();
    }

    private void configureFeed() throws URISyntaxException {
        logger.info("starting configureFeed...");
        if(!envProperties.camelRouteFeed()) {
            logger.info("configureFeed disabled");
            return;
        }

        //feeds
        for (Feed feed : Feed.values()) {
            logger.info("starting route {}...", feed.getTitle());
            final boolean hasQuery = !nullToEmpty(new URI(feed.getUrl()).getQuery()).trim().isEmpty();
            fromF("rss:%ssortEntries=true&throttleEntries=false&consumer.delay=300000&lastUpdate=%s",
                    feed.getUrl() + (hasQuery ? "&" : "?"),
                    LocalDateTime.now().minusDays(feed.getDaysToRead()).toString("yyyy-MM-dd'T'HH:mm:ss"))
                    .setHeader("TsFeed", constant(feed))
                    .process(e -> socialRepository.arriveFeed(e.getIn().getHeader("TsFeed", Feed.class), e.getIn().getBody(SyndFeed.class)));
        }
    }

    private void configureTwitter() {
        logger.info("starting configureTwitter...");
        if(!envProperties.camelRouteTwitter()) {
            logger.info("configureTwitter disabled");
            return;
        }

        TwitterComponent tc = getContext().getComponent("twitter", TwitterComponent.class);
        tc.setAccessToken(envProperties.twitterAccessToken());
        tc.setAccessTokenSecret(envProperties.twitterAccessTokenSecret());
        tc.setConsumerKey(envProperties.twitterConsumerKey());
        tc.setConsumerSecret(envProperties.twitterConsumerSecret());

        //twitter
        for (String user : TWITTERS) {
            logger.info("starting route {}...", user);
            fromF("twitter://timeline/user?type=polling&delay=300&user=%s", user)
                    .process(e -> socialRepository.arriveTweet(e.getIn().getBody(Status.class)));
        }
    }

    public enum Feed {
        SOUJAVA("SouJava", "http://soujava.org.br/feed/", 60),
        JAVAWORLD("Java World", "http://www.javaworld.com/index.rss", 10);

        private final String title;
        private final int daysToRead;
        private final String url;

        Feed(String title, String url, int daysToRead) {
            this.title = title;
            this.url = url;
            this.daysToRead = daysToRead;
        }

        public String getTitle() {
            return title;
        }

        public String getUrl() {
            return url;
        }

        public int getDaysToRead() {
            return daysToRead;
        }
    }
}
