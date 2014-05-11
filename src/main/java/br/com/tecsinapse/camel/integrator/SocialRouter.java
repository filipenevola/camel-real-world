package br.com.tecsinapse.camel.integrator;

import br.com.tecsinapse.camel.cdi.EnvProperties;
import br.com.tecsinapse.camel.cdi.annotation.CamelRoute;
import com.google.common.collect.ImmutableList;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.twitter.TwitterComponent;
import org.joda.time.LocalDateTime;

import javax.inject.Inject;
import java.net.URI;

import static com.google.common.base.Strings.nullToEmpty;

@CamelRoute
public class SocialRouter extends RouteBuilder {

    @Inject
    private EnvProperties envProperties;
    @Inject
    private SocialRepository socialRepository;

    private static final ImmutableList<String> TWITTERS = ImmutableList.of("FilipeNevola", "TheDevConf", "globalcode", "tecsinapse");

    @Override
    public void configure() throws Exception {
        TwitterComponent tc = getContext().getComponent("twitter", TwitterComponent.class);
        tc.setAccessToken(envProperties.twitterAccessToken());
        tc.setAccessTokenSecret(envProperties.twitterAccessTokenSecret());
        tc.setConsumerKey(envProperties.twitterConsumerKey());
        tc.setConsumerSecret(envProperties.twitterConsumerSecret());

        //twitter
        for (String user : TWITTERS) {
            fromF("twitter://timeline/user?type=polling&delay=300&user=%s", user)
                    .bean(socialRepository, "arriveTweet");
        }

        //feeds
        for (Feed feed : Feed.values()) {
            final boolean hasQuery = !nullToEmpty(new URI(feed.getUrl()).getQuery()).trim().isEmpty();
            fromF("rss:%ssortEntries=true&throttleEntries=false&consumer.delay=300000&lastUpdate=%s",
                    feed.getUrl() + (hasQuery ? "&" : "?"),
                    LocalDateTime.now().minusDays(feed.getDaysToRead()).toString("yyyy-MM-dd'T'HH:mm:ss"))
                    .setHeader("TsFeed", constant(feed))
                    .bean(socialRepository, "arriveFeed(${in.header.TsFeed}, ${body})");
        }
    }

    public enum Feed {
        SOUJAVA("SouJava", "http://soujava.org.br/feed/", 5),
        JAVAWORLD("Java World", "http://www.javaworld.com/index.rss", 5);

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
