package br.com.tecsinapse.camel.integrator;

import br.com.tecsinapse.camel.data.SocialContent;
import br.com.tecsinapse.camel.data.SocialContentType;
import com.google.common.base.Function;
import com.google.common.collect.*;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import org.joda.time.LocalDateTime;
import twitter4j.Status;

import javax.enterprise.context.ApplicationScoped;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@ApplicationScoped
public class SocialRepository {
    private static final int KEEP = 10;

    private final ConcurrentMap<String, SortedSet<Status>> tweetByUser = new ConcurrentHashMap<>();
    private static final Ordering<Status> TWEET_COMPARATOR = Ordering.natural().reverse();

    private final ConcurrentMap<SocialRouter.Feed, SortedSet<SyndEntry>> rssByFeed = new ConcurrentHashMap<>();
    private static final Comparator<SyndEntry> SYND_COMPARATOR = (o1, o2) -> ComparisonChain.start()
            .compare(o2.getPublishedDate(), o1.getPublishedDate())
            .compare(o2, o1, Ordering.arbitrary())
            .result();

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

    public void arriveTweet(Status tweet) {
        arrive(tweet.getUser().getScreenName().toLowerCase(), tweet, tweetByUser, TWEET_COMPARATOR);
    }

    public void arriveFeed(SocialRouter.Feed feed, SyndFeed syndFeed) {
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

        contents.addAll(getLatests(quantity, tweetByUser, TWEET_COMPARATOR, entry -> {
            final Status tweet = entry.getValue();
            return new SocialContent(tweet.getUser().getName(), "@" + tweet.getUser().getScreenName(),
                    tweet.getText(), localDateTimeFromDate(tweet.getCreatedAt()), SocialContentType.TEXT);
        }));

        contents.addAll(getLatests(quantity, rssByFeed, SYND_COMPARATOR, entry -> {
            final SyndEntry feed = entry.getValue();
            return new SocialContent(feed.getTitle(), entry.getKey().getTitle(),
                    feed.getDescription().getValue(), localDateTimeFromDate(feed.getPublishedDate()), SocialContentType.from(feed.getDescription().getType()));
        }));


        return FluentIterable.from(contents).limit(quantity).toList();
    }

    public static LocalDateTime localDateTimeFromDate(Date date) {
        if (date != null) {
            return LocalDateTime.fromDateFields(date);
        }
        return null;
    }
}
