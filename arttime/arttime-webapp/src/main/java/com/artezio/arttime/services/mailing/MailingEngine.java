package com.artezio.arttime.services.mailing;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;

@Stateless
public class MailingEngine {

    @Inject
    private JMSContext jmsContext;
    @Resource(mappedName = "java:/jms/queue/arttime")
    private Queue jmsQueue;

    public void send(Mail mail) {
        try {
            sendOverJms(mail);
        } catch (RuntimeException exception) {
            throw new MailingException(exception);
        }
    }

    private void sendOverJms(Mail mail) {
        ObjectMessage objectMessage = jmsContext.createObjectMessage(mail);
        JMSProducer jmsProducer = jmsContext.createProducer();
        jmsProducer.send(jmsQueue, objectMessage);
    }

}
