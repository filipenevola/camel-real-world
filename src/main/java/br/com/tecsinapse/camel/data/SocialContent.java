package br.com.tecsinapse.camel.data;

import br.com.tecsinapse.camel.integrator.FeedRouter;
import com.google.common.collect.ComparisonChain;
import com.sun.syndication.feed.synd.SyndEntry;
import org.joda.time.LocalDateTime;
import org.jsoup.Jsoup;
import twitter4j.Status;

import java.util.Date;
import java.util.Map;

public class SocialContent implements Comparable<SocialContent> {
    private final String title;
    private final String complement;
    private final String content;
    private final LocalDateTime dateTime;
    private final SocialContentType type;

    public SocialContent(String title, String complement, String content, LocalDateTime dateTime, SocialContentType type) {
        this.title = title;
        this.complement = complement;
        this.content = content;
        this.dateTime = dateTime;
        this.type = type;
    }

    public static SocialContent from(Status tweet) {
        return new SocialContent(tweet.getUser().getName(), "@" + tweet.getUser().getScreenName(),
                tweet.getText(), localDateTimeFromDate(tweet.getCreatedAt()), SocialContentType.TEXT);
    }

    public static SocialContent from(Map.Entry<FeedRouter.Feed, SyndEntry> entry) {
        SyndEntry feed = entry.getValue();
        return new SocialContent(feed.getTitle(), entry.getKey().getTitle(),
                feed.getDescription().getValue(), localDateTimeFromDate(feed.getPublishedDate()), SocialContentType.from(feed.getDescription().getType()));
    }

    public static LocalDateTime localDateTimeFromDate(Date date) {
        if (date != null) {
            return LocalDateTime.fromDateFields(date);
        }
        return null;
    }


    public String getTitle() {
        return title;
    }

    public String getComplement() {
        return complement;
    }

    public String getContent() {
        return content;
    }

    public String getContentWithoutTags() {
        return Jsoup.parse(getContent()).text();
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public SocialContentType getType() {
        return type;
    }

    @Override
    public int compareTo(SocialContent o) {
        return ComparisonChain.start()
                .compare(dateTime, o.dateTime)
                .compare(title, o.title)
                .compare(complement, o.complement).result();
    }
}
