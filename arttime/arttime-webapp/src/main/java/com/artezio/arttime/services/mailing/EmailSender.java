package com.artezio.arttime.services.mailing;

import com.artezio.arttime.config.ApplicationSettings;
import com.artezio.arttime.config.Settings;
import com.google.common.base.Strings;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "java:/jms/queue/arttime"),
        @ActivationConfigProperty(propertyName = "maxSession", propertyValue = "1")
})
public class EmailSender implements MessageListener {

    @Inject
    @ApplicationSettings
    private Settings settings;
    @Resource(mappedName="java:jboss/mail/com.artezio.arttime")
    private Session session;

    @PostConstruct
    protected void initSession() {
        if (!Strings.isNullOrEmpty(settings.getSmtpPassword())) {
            session.getProperties().put("mail.smtp.auth", "true");
        } else {
            session.getProperties().put("mail.smtp.auth", "false");
        }
        session.getProperties().put("mail.smtp.host", settings.getSmtpHostName());
        session.getProperties().put("mail.smtp.port", settings.getSmtpPortNumber());
        session.getProperties().put("mail.smtp.user", settings.getSmtpUsername());
        session.setPasswordAuthentication(new URLName("smtp://" + settings.getSmtpUsername() + "@" + settings.getSmtpHostName()),
                new PasswordAuthentication(settings.getSmtpUsername(), settings.getSmtpPassword()));
    }

    @Override
    public void onMessage(Message message) {
        try {
            ObjectMessage objectMessage = (ObjectMessage) message;
            Mail mail = (Mail) objectMessage.getObject();
            sendEmail(mail);
        } catch (Exception exception) {
            throw new MailingException(exception);
        }
    }

    //Thread.sleep was added by the request of the system administrator to avoid bombing of the SMTP server.
    private void sendEmail(Mail mail) throws MessagingException, InterruptedException {
        Transport.send(createMimeMessage(mail));
        TimeUnit.SECONDS.sleep(2);
    }

    private MimeMessage createMimeMessage(Mail mail) throws MessagingException {
        MimeMessage mimeMessage = new MimeMessage(session);
        mimeMessage.setSubject(mail.getSubject(), "UTF-8");
        mimeMessage.setText(mail.getBody(), "UTF-8", "html");
        mimeMessage.setSentDate(new Date());
        InternetAddress[] addressesTo = InternetAddress.parse(mail.getRecipientEmailAddress(), false);
        mimeMessage.setRecipients(javax.mail.Message.RecipientType.TO, addressesTo);
        mimeMessage.setFrom(new InternetAddress(mail.getSenderEmailAddress()));
        return mimeMessage;
    }

}
