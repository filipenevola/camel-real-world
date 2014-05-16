package br.com.tecsinapse.camel.controller;

import br.com.tecsinapse.camel.data.SocialContent;
import br.com.tecsinapse.camel.repository.SocialRepository;
import com.ocpsoft.pretty.faces.annotation.URLAction;
import com.ocpsoft.pretty.faces.annotation.URLMapping;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
@URLMapping(id = "rss", pattern = "/rss/", viewId = "/jsf/rss.xhtml")
public class RssController implements Serializable {
    private static final long serialVersionUID = 1L;

    @Inject
    private SocialRepository socialRepository;

    private List<SocialContent> latests;

    @URLAction(mappingId = "rss", onPostback = false)
    public void rss() {
        latests = socialRepository.getLatestsFeeds(10);
    }

    public List<SocialContent> getLatests() {
        return latests;
    }
}
