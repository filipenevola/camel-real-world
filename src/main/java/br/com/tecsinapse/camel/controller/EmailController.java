package br.com.tecsinapse.camel.controller;

import br.com.tecsinapse.camel.integrator.EmailRepository;
import com.ocpsoft.pretty.faces.annotation.URLAction;
import com.ocpsoft.pretty.faces.annotation.URLMapping;
import org.apache.camel.component.mail.MailMessage;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
@URLMapping(id = "email", pattern = "/email/", viewId = "/jsf/email.xhtml")
public class EmailController implements Serializable {
    private static final long serialVersionUID = 1L;

    @Inject
    private EmailRepository emailRepository;

    private List<MailMessage> latests;

    @URLAction(mappingId = "email", onPostback = false)
    public void rss() {
        latests = emailRepository.getMessages();
    }

    public List<MailMessage> getLatests() {
        return latests;
    }
}
