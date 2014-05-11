package br.com.tecsinapse.camel.data;

import com.google.common.collect.ComparisonChain;
import org.joda.time.LocalDateTime;
import org.jsoup.Jsoup;

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
