package com.artezio.arttime.services.mailing;

import javax.ejb.ApplicationException;

@ApplicationException
public class MailingException extends RuntimeException {

    private static final long serialVersionUID = -4830747303133823475L;

    MailingException(Exception exception) {
        super(exception);
    }

    @Override
    public String getMessage() {
        return "Error during sending e-mail";
    }

}
