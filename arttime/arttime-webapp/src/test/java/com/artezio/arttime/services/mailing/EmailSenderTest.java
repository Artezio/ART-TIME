package com.artezio.arttime.services.mailing;

import com.artezio.arttime.config.Setting;
import com.artezio.arttime.config.Settings;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import javax.jms.ObjectMessage;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.EnumMap;
import java.util.Properties;

import static junitx.util.PrivateAccessor.setField;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Session.class, Transport.class, EmailSender.class})
public class EmailSenderTest {

    private EmailSender emailSenderSpy;
    private EmailSender emailSender;

    @Before
    public void setUp() throws Exception {
        emailSender = new EmailSender();
        emailSenderSpy = spy(emailSender);
    }

    @Test
    public void testInitSession() throws NoSuchFieldException, NoSuchMethodException {
        Session session = PowerMock.createMock(Session.class);
        Settings settings = new Settings(new EnumMap<>(Setting.Name.class));
        settings.setSmtpHostName("smtp.host");
        settings.setSmtpPortNumber("25");
        settings.setSmtpUsername("TestUser");
        settings.setSmtpPassword("123");
        setField(emailSender, "session", session);
        setField(emailSender, "settings", settings);
        Properties properties = new Properties();
        expect(session.getProperties()).andReturn(properties).anyTimes();
        PowerMock.stub(Session.class.getMethod("setPasswordAuthentication", URLName.class, PasswordAuthentication.class));
        session.setPasswordAuthentication(anyObject(), anyObject());
        PowerMock.replay(session);

        emailSender.initSession();

        verify(session);
        assertEquals(settings.getSmtpHostName(), properties.getProperty("mail.smtp.host"));
        assertEquals(settings.getSmtpPortNumber(), properties.getProperty("mail.smtp.port"));
        assertEquals(settings.getSmtpUsername(), properties.getProperty("mail.smtp.user"));
        assertTrue(Boolean.parseBoolean(properties.getProperty("mail.smtp.auth")));
    }

    @Test
    public void testOnMessage() throws Exception {
        ObjectMessage message = createMock(ObjectMessage.class);
        Mail mail = new Mail();

        expect(message.getObject()).andReturn(mail);
        doNothing().when(emailSenderSpy, "sendEmail", mail);
        replay(message);

        emailSenderSpy.onMessage(message);
    }

    @Test(expected = MailingException.class)
    public void testOnMessage_ErrorDuringEmailSending() throws Exception {
        ObjectMessage message = createMock(ObjectMessage.class);
        Mail mail = new Mail();

        expect(message.getObject()).andReturn(mail);
        doThrow(new MessagingException()).when(emailSenderSpy, "sendEmail", mail);
        replay(message);

        emailSenderSpy.onMessage(message);
    }

    @Test
    public void testSendEMail() throws Exception {
        Mail mail = new Mail();
        mail.setSenderEmailAddress("sender@mail.com");
        mail.setRecipientEmailAddress("recipient@mail.com");
        MimeMessage mimeMessage = PowerMock.createMock(MimeMessage.class);

        doReturn(mimeMessage).when(emailSenderSpy, "createMimeMessage", mail);
        PowerMockito.mockStatic(Transport.class);
        PowerMockito.doNothing().when(Transport.class, "send", mimeMessage);

        Whitebox.invokeMethod(emailSenderSpy, "sendEmail", mail);
    }

    @Test(expected = MessagingException.class)
    public void testSendEMail_ErrorDuringMessageSending() throws Exception {
        Mail mail = new Mail();
        mail.setSenderEmailAddress("sender@mail.com");
        mail.setRecipientEmailAddress("recipient@mail.com");
        MimeMessage mimeMessage = createMock(MimeMessage.class);

        PowerMockito.doReturn(mimeMessage).when(emailSenderSpy, "createMimeMessage", mail);
        PowerMockito.mockStatic(Transport.class);
        PowerMockito.doThrow(new MessagingException()).when(Transport.class, "send", mimeMessage);

        Whitebox.invokeMethod(emailSenderSpy, "sendEmail", mail);
    }

}
