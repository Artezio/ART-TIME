package com.artezio.arttime.web;

import javax.enterprise.context.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.ServletException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

@Named
@RequestScoped
public class ErrorPageBean {

    public String getMessage() {
        return (String) getRequestMap().get("javax.servlet.error.message");
    }

    public String getException() {
        Throwable ex = (Throwable) getRequestMap().get("javax.servlet.error.exception");
        StringWriter writer = new StringWriter();
        fillStackTrace(ex, new PrintWriter(writer));

        return writer.toString();
    }

    private void fillStackTrace(Throwable ex, PrintWriter pw) {
        if (ex == null) {
            return;
        }
        ex.printStackTrace(pw);
        Throwable cause = getCause(ex);
        fillStackTrace(cause, pw);
    }

    private Throwable getCause(Throwable ex) {
        return (ex instanceof ServletException)
                ? ((ServletException) ex).getRootCause()
                : ex.getCause();
    }

    private Map<String, Object> getRequestMap() {
        return getExternalContext().getRequestMap();
    }

    private ExternalContext getExternalContext() {
        return FacesContext.getCurrentInstance().getExternalContext();
    }

}
