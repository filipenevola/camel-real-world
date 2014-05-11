package br.com.tecsinapse.camel.integrator;

import org.apache.camel.component.mail.MailMessage;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.mail.MessagingException;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class EmailRepository {
    private final List<MailMessage> messages = new ArrayList<>();

    @Inject
    private transient Logger logger;

    public void arriveEmail(MailMessage message) throws MessagingException {
        logger.info("arriveEmail {}...", message.getMessage().getSubject());
        messages.add(message);
    }

    public List<MailMessage> getMessages() {
        return messages;
    }
}
