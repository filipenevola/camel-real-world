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
import java.net.URISyntaxException;
import java.util.Arrays;

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

        Arrays.asList(Feed.values()).forEach( feed -> {
            logger.info("starting route {}...", feed.getTitle());

            fromF("rss:%ssortEntries=true&throttleEntries=false&consumer.delay=300000&lastUpdate=%s",
                feed.getUrl() + (feedHasQuery(feed) ? "&" : "?"),
                LocalDateTime.now().minusDays(feed.getDaysToRead()).toString("yyyy-MM-dd'T'HH:mm:ss"))
                    .setHeader("TsFeed", constant(feed))
                    .process(e -> socialRepository.arriveFeed(e.getIn().getHeader("TsFeed", Feed.class), e.getIn().getBody(SyndFeed.class)));
        });
    }

    private boolean feedHasQuery(Feed feed) {
        boolean hasQuery = false;
        try {
            hasQuery = !nullToEmpty(new URI(feed.getUrl()).getQuery()).trim().isEmpty();
        } catch (URISyntaxException e) {
            logger.error("Erro ao criar URI para o feed {}", feed.getTitle(), e);
        }
        return hasQuery;
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
