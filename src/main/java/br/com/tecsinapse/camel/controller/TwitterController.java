package br.com.tecsinapse.camel.controller;

import br.com.tecsinapse.camel.data.SocialContent;
import br.com.tecsinapse.camel.repository.SocialRepository;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.ocpsoft.pretty.faces.annotation.URLAction;
import com.ocpsoft.pretty.faces.annotation.URLMapping;
import com.ocpsoft.pretty.faces.annotation.URLMappings;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.cdi.Uri;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

@Named
@ViewScoped
@URLMappings(mappings = {
        @URLMapping(id = "twitter", pattern = "/twitter/", viewId = "/jsf/twitter.xhtml"),
        @URLMapping(id = "twitter-search", pattern = "/twitter/search/", viewId = "/jsf/twitter-search.xhtml")
})
public class TwitterController implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final int MAX_TWEETS = 100;

    @Inject
    private SocialRepository socialRepository;

    @Inject
    @Uri("direct:twitterSearch")
    private transient ProducerTemplate twitterSearchProducer;

    private ImmutableList<SocialContent> latests;
    private ImmutableList<SocialContent> results;
    private String keyword;

    @URLAction(mappingId = "twitter", onPostback = false)
    public void twitter() {
        latests = socialRepository.getLatestsTweets(10);
    }

    public void search() {
        socialRepository.clearResults();
        twitterSearchProducer.sendBodyAndHeaders(null, ImmutableMap.of("CamelTwitterKeywords", keyword, "CamelTwitterCount", MAX_TWEETS));
        results = socialRepository.getTweetsResult(MAX_TWEETS);
    }

    public ImmutableList<SocialContent> getLatests() {
        return latests;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public ImmutableList<SocialContent> getResults() {
        return results;
    }
}
