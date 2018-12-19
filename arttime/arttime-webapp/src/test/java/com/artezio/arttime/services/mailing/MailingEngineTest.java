package com.artezio.arttime.services.mailing;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.jms.*;

import static junitx.util.PrivateAccessor.setField;
import static org.easymock.EasyMock.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Session.class})
public class MailingEngineTest {

    private MailingEngine mailingEngine;
    private JMSContext jmsContext;

    @Before
    public void setUp() throws Exception {
        jmsContext = createMock(JMSContext.class);
        mailingEngine = new MailingEngine();
    }

    @Test
    public void testSend() throws Exception {
        Queue emailJmsQueue = createMock(Queue.class);
        JMSProducer jmsProducer = createMock(JMSProducer.class);
        ObjectMessage objectMessage = createMock(ObjectMessage.class);
        Mail mail = new Mail("subj", "body", "sender@mail.com", "recipient@mail.com");

        setField(mailingEngine, "jmsQueue", emailJmsQueue);
        setField(mailingEngine, "jmsContext", jmsContext);
        expect(jmsContext.createProducer()).andReturn(jmsProducer);
        expect(jmsContext.createObjectMessage(mail)).andReturn(objectMessage);
        expect(jmsProducer.send(emailJmsQueue, objectMessage)).andReturn(jmsProducer);
        replay(jmsContext, jmsProducer);

        mailingEngine.send(mail);
    }

    @Test(expected = MailingException.class)
    public void testSend_ErrorDuringSendingEmail() throws Exception {
        Queue emailJmsQueue = createMock(Queue.class);
        JMSProducer jmsProducer = createMock(JMSProducer.class);
        ObjectMessage objectMessage = createMock(ObjectMessage.class);
        Mail mail = new Mail("subj", "body", "sender@mail.com", "recipient@mail.com");

        setField(mailingEngine, "jmsQueue", emailJmsQueue);
        setField(mailingEngine, "jmsContext", jmsContext);
        expect(jmsContext.createProducer()).andReturn(jmsProducer);
        expect(jmsContext.createObjectMessage(mail)).andReturn(objectMessage);
        expect(jmsProducer.send(emailJmsQueue, objectMessage)).andThrow(new RuntimeException("Test"));
        replay(jmsContext, jmsProducer);

        mailingEngine.send(mail);
    }

}
