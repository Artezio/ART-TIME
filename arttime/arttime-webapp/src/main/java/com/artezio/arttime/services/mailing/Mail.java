package com.artezio.arttime.services.mailing;

import java.io.Serializable;

public class Mail implements Serializable {

    private static final long serialVersionUID = 3697539661593550779L;

    private String subject;
    private String body;
    private String senderEmailAddress;
    private String recipientEmailAddress;

    public Mail() {}

    public Mail(String subject, String body, String senderEmailAddress, String recipientEmailAddress) {
        this.subject = subject;
        this.body = body;
        this.senderEmailAddress = senderEmailAddress;
        this.recipientEmailAddress = recipientEmailAddress;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getSenderEmailAddress() {
        return senderEmailAddress;
    }

    public void setSenderEmailAddress(String senderEmailAddress) {
        this.senderEmailAddress = senderEmailAddress;
    }

    public String getRecipientEmailAddress() {
        return recipientEmailAddress;
    }

    public void setRecipientEmailAddress(String recipientEmailAddress) {
        this.recipientEmailAddress = recipientEmailAddress;
    }

    ///CLOVER:OFF
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((subject == null) ? 0 : subject.hashCode());
        result = prime * result + ((body == null) ? 0 : body.hashCode());
        result = prime * result + ((senderEmailAddress == null) ? 0 : senderEmailAddress.hashCode());
        result = prime * result + ((recipientEmailAddress == null) ? 0 : recipientEmailAddress.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Mail other = (Mail) obj;
        if (subject == null) {
            if (other.subject != null)
                return false;
        } else if (!subject.equals(other.subject))
            return false;
        if (body == null) {
            if (other.body != null)
                return false;
        } else if (!body.equals(other.body))
            return false;
        if (senderEmailAddress == null) {
            if (other.senderEmailAddress != null)
                return false;
        } else if (!senderEmailAddress.equals(other.senderEmailAddress)) {
            return false;
        }
        if (recipientEmailAddress == null) {
            if (other.recipientEmailAddress != null)
                return false;
        } else if (!recipientEmailAddress.equals(other.recipientEmailAddress)) {
            return false;
        }
        return true;
    }
    ///CLOVER:ON

}
