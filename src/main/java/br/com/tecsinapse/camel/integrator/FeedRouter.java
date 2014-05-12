package br.com.tecsinapse.camel.integrator;

import br.com.tecsinapse.camel.cdi.EnvProperties;
import br.com.tecsinapse.camel.repository.SocialRepository;
import com.sun.syndication.feed.synd.SyndFeed;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.ContextName;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.net.URI;

import static com.google.common.base.Strings.nullToEmpty;

@ContextName
public class FeedRouter extends RouteBuilder {

    @Inject
    private transient Logger logger;
    @Inject
    private EnvProperties envProperties;
    @Inject
    private SocialRepository socialRepository;

    @Override
    public void configure() throws Exception {
        logger.info("starting configure...");
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
