package br.com.tecsinapse.camel.repository;

import br.com.tecsinapse.camel.data.SocialContent;
import br.com.tecsinapse.camel.data.SocialContentType;
import br.com.tecsinapse.camel.integrator.FeedRouter;
import com.google.common.base.Function;
import com.google.common.collect.*;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import twitter4j.Status;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@ApplicationScoped
public class SocialRepository {
    private static final int KEEP = 100;

    private final ConcurrentMap<String, SortedSet<Status>> tweetByUser = new ConcurrentHashMap<>();
    private static final Ordering<Status> TWEET_COMPARATOR = Ordering.natural().reverse();

    private final ConcurrentMap<FeedRouter.Feed, SortedSet<SyndEntry>> rssByFeed = new ConcurrentHashMap<>();
    private static final Comparator<SyndEntry> SYND_COMPARATOR = (o1, o2) -> ComparisonChain.start()
            .compare(o2.getPublishedDate(), o1.getPublishedDate())
            .compare(o2, o1, Ordering.arbitrary())
            .result();

    private final SortedSet<Status> tweetsSearch = new TreeSet<>(TWEET_COMPARATOR);

    private String keyword = "tdc2014";

    @Inject
    private transient Logger logger;

    private <K, V> void arrive(K key, V value, Map<K, SortedSet<V>> map, Comparator<V> comparator) {

        SortedSet<V> values = map.get(key);
        if (values == null) {
            map.put(key, values = new TreeSet<>(comparator));
        }
        values.add(value);

        while (values.size() > KEEP) {
            final V oldest = values.last();
            values.remove(oldest);
        }
    }

    public void arriveTwitterSearchResult(Status tweet) {
        logger.info("arriveTwitterSearchResult from {}", tweet.getUser().getScreenName());
        tweetsSearch.add(tweet);
    }

    public void arriveTweet(Status tweet) {
        logger.info("arriveTweet from {}", tweet.getUser().getScreenName());
        arrive(tweet.getUser().getScreenName().toLowerCase(), tweet, tweetByUser, TWEET_COMPARATOR);
    }

    public void arriveFeed(FeedRouter.Feed feed, SyndFeed syndFeed) {
        logger.info("arriveFeed from {}", feed.getTitle());

        @SuppressWarnings("unchecked")
        final List<SyndEntry> entries = (List<SyndEntry>) syndFeed.getEntries();
        for (SyndEntry syndEntry : entries) {

            arrive(feed, syndEntry, rssByFeed, SYND_COMPARATOR);
        }
    }

    private <K, V> ImmutableList<SocialContent> getLatests(
            int quantity, Map<K, SortedSet<V>> map, final Comparator<V> comparator, Function<Entry<K, V>, SocialContent> contentFunction) {
        final ImmutableList<Entry<K, V>> values = FluentIterable.from(map.entrySet())
                .transformAndConcat(entry -> Collections2.transform(entry.getValue(), (V value) -> Maps.immutableEntry(entry.getKey(), value))).toSortedList(new Comparator<Entry<K, V>>() {
                    @Override
                    public int compare(Entry<K, V> o1, Entry<K, V> o2) {
                        return comparator.compare(o1.getValue(), o2.getValue());
                    }
                });

        return FluentIterable.from(values.subList(0, Math.min(quantity, values.size())))
                .transform(contentFunction).toList();
    }

    public ImmutableList<SocialContent> getLatests(int quantity) {
        SortedSet<SocialContent> contents = new TreeSet<>(Ordering.natural().<SocialContent>reverse());

        contents.addAll(getLatestsTweets(quantity));

        contents.addAll(getLatestsFeeds(quantity));

        return FluentIterable.from(contents).limit(quantity).toList();
    }

    public ImmutableList<SocialContent> getLatestsFeeds(int quantity) {
        return getLatests(quantity, rssByFeed, SYND_COMPARATOR, entry -> {
            final SyndEntry feed = entry.getValue();
            return new SocialContent(feed.getTitle(), entry.getKey().getTitle(),
                    feed.getDescription().getValue(), localDateTimeFromDate(feed.getPublishedDate()), SocialContentType.from(feed.getDescription().getType()));
        });
    }

    public ImmutableList<SocialContent> getLatestsTweets(int quantity) {
        return getLatests(quantity, tweetByUser, TWEET_COMPARATOR, entry -> {
            final Status tweet = entry.getValue();
            return new SocialContent(tweet.getUser().getName(), "@" + tweet.getUser().getScreenName(),
                    tweet.getText(), localDateTimeFromDate(tweet.getCreatedAt()), SocialContentType.TEXT);
        });
    }

    public static LocalDateTime localDateTimeFromDate(Date date) {
        if (date != null) {
            return LocalDateTime.fromDateFields(date);
        }
        return null;
    }

    public String getKeyword() {
        return keyword;
    }

    public void changeKeyword(String keyword) {
        this.keyword = keyword;
        this.tweetsSearch.clear();
    }

    public ImmutableList<SocialContent> getTweetsSearch(int quantity) {
        return FluentIterable.from(tweetsSearch).limit(quantity).transform(tweet -> new SocialContent(tweet.getUser().getName(), "@" + tweet.getUser().getScreenName(),
                tweet.getText(), localDateTimeFromDate(tweet.getCreatedAt()), SocialContentType.TEXT)).toList();
    }
}
