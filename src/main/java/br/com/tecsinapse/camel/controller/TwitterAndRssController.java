package br.com.tecsinapse.camel.controller;

import br.com.tecsinapse.camel.data.SocialContent;
import br.com.tecsinapse.camel.integrator.SocialRepository;
import com.google.common.collect.ImmutableList;
import com.ocpsoft.pretty.faces.annotation.URLAction;
import com.ocpsoft.pretty.faces.annotation.URLMapping;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

@Named
@ViewScoped
@URLMapping(id = "twitter-rss", pattern = "/twitter-rss/", viewId = "/jsf/twitter-rss.xhtml")
public class TwitterAndRssController implements Serializable {
    private static final long serialVersionUID = 1L;

    @Inject
    private SocialRepository socialRepository;

    private ImmutableList<SocialContent> latests;

    @URLAction(mappingId = "twitter-rss", onPostback = false)
    public void twitter() {
        latests = socialRepository.getLatests(100);
    }

    public ImmutableList<SocialContent> getLatests() {
        return latests;
    }
}
