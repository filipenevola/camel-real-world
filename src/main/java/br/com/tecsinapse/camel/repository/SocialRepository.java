package br.com.tecsinapse.camel.repository;

import br.com.tecsinapse.camel.data.SocialContent;
import br.com.tecsinapse.camel.integrator.FeedRouter;
import com.google.common.collect.*;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import org.slf4j.Logger;
import twitter4j.Status;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    private final SortedSet<Status> tweetsResults = new TreeSet<>(TWEET_COMPARATOR);

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

    public void arriveTwitterSearchResult(List<Status> tweets) {
        logger.info("arriveTwitterSearchResult number of results {}", tweets.size());
        tweetsResults.addAll(tweets);
    }

    public void arriveTweet(Status tweet) {
        logger.info("arriveTweet from {}", tweet.getUser().getScreenName());
        arrive(tweet.getUser().getScreenName().toLowerCase(), tweet, tweetByUser, TWEET_COMPARATOR);
    }

    public void arriveFeed(FeedRouter.Feed feed, SyndFeed syndFeed) {
        logger.info("arriveFeed from {}", feed.getTitle());

        @SuppressWarnings("unchecked")
        final List<SyndEntry> entries = (List<SyndEntry>) syndFeed.getEntries();

        entries.forEach(syndEntry ->
                arrive(feed, syndEntry, rssByFeed, SYND_COMPARATOR));
    }

    private <K, V> List<SocialContent> getLatests(
            int quantity, Map<K, SortedSet<V>> map, final Comparator<V> comparator, Function<Entry<K, V>, SocialContent> contentFunction) {
        final List<Entry<K, V>> values = FluentIterable.from(map.entrySet())
                .transformAndConcat(entry -> Collections2
                        .transform(entry.getValue(), (V value) -> Maps.immutableEntry(entry.getKey(), value)))
                .toSortedList(new Comparator<Entry<K, V>>() {
                    @Override
                    public int compare(Entry<K, V> o1, Entry<K, V> o2) {
                        return comparator.compare(o1.getValue(), o2.getValue());
                    }
                });

        return values.subList(0, Math.min(quantity, values.size())).stream().map(contentFunction).collect(Collectors.toList());
    }

    public List<SocialContent> getLatests(int quantity) {
        SortedSet<SocialContent> contents = new TreeSet<>(Ordering.natural().<SocialContent>reverse());

        contents.addAll(getLatestsTweets(quantity));

        contents.addAll(getLatestsFeeds(quantity));

        return contents.stream().limit(quantity).collect(Collectors.toList());
    }

    public List<SocialContent> getLatestsFeeds(int quantity) {
        return getLatests(quantity, rssByFeed, SYND_COMPARATOR, entry -> SocialContent.from(entry));
    }

    public List<SocialContent> getLatestsTweets(int quantity) {
        return getLatests(quantity, tweetByUser, TWEET_COMPARATOR, entry -> SocialContent.from(entry.getValue()));
    }

    public List<SocialContent> getTweetsResult(int quantity) {
        return tweetsResults
                .stream()
                .limit(quantity)
                .map(tweet -> SocialContent.from(tweet)).collect(Collectors.toList());
    }

    public void clearResults() {
        tweetsResults.clear();
    }
}
