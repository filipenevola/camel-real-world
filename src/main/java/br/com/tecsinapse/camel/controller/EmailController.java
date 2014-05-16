package br.com.tecsinapse.camel.controller;

import br.com.tecsinapse.camel.repository.EmailRepository;
import com.google.common.base.Joiner;
import com.ocpsoft.pretty.faces.annotation.URLAction;
import com.ocpsoft.pretty.faces.annotation.URLMapping;
import org.apache.camel.component.mail.MailMessage;
import org.joda.time.LocalDateTime;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.Address;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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

    public String getFrom(Address[] addresses) {
        return Joiner.on(",").join(Arrays.asList(addresses).stream().map(a -> a.toString()).collect(Collectors.toList()));
    }

    public String getDate(Date date) {
        return LocalDateTime.fromDateFields(date).toString("dd/MM/yyyy HH:mm");
    }
}
