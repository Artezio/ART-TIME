package com.artezio.arttime.web.exception_handler;

import com.artezio.arttime.utils.MessagesUtil;
import com.artezio.arttime.utils.OrderedProperties;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;
import javax.servlet.ServletContext;
import javax.validation.ConstraintViolation;
import org.hibernate.exception.ConstraintViolationException;
import org.primefaces.component.datatable.DataTable;

public class ExceptionHandler extends ExceptionHandlerWrapper {

    private final static String ERROR_MESSAGES_PROPERTIES_PATH = "/WEB-INF/errorMessages.xml";

    private javax.faces.context.ExceptionHandler wrappedExceptionHandler;
    private static OrderedProperties errorMessages;

    static {
        loadErrorMessages();
    }

    protected static void loadErrorMessages() {
        errorMessages = new OrderedProperties();
        try {
            ServletContext servletContext = getServletContext();
            InputStream input = servletContext.getResourceAsStream(ERROR_MESSAGES_PROPERTIES_PATH);
            errorMessages.loadFromXML(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected static ServletContext getServletContext() {
        return (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
    }

    ExceptionHandler(javax.faces.context.ExceptionHandler exceptionHandler) {
        this.wrappedExceptionHandler = exceptionHandler;
    }

    @Override
    public void handle() throws FacesException {
        final Iterator<ExceptionQueuedEvent> iterator = getUnhandledExceptionQueuedEvents().iterator();
        List<String> errorMessages = new ArrayList<String>();
        while (iterator.hasNext()) {
            ExceptionQueuedEvent exceptionEvent = iterator.next();
            ExceptionQueuedEventContext exceptionEventContext = (ExceptionQueuedEventContext) exceptionEvent.getSource();
            Throwable incomingException = exceptionEventContext.getException();
            List<String> messages = findErrorMessages(incomingException);
            if (!messages.isEmpty()) {
                errorMessages.addAll(messages);
                iterator.remove();
            }
        }
        if (!errorMessages.isEmpty()) {
            MessagesUtil.removeMessagesLessThan(FacesMessage.SEVERITY_ERROR);
            for (String message : errorMessages) {
                MessagesUtil.addError(null, message);
            }
            resetDataTablesRowIndex(getViewRoot());
        }
        getWrapped().handle();
    }

    private void resetDataTablesRowIndex(UIComponent parent) {
        if (parent == null) {
            return;
        }
        for (UIComponent child : parent.getChildren()) {
            if (child instanceof DataTable) {
                DataTable table = ((DataTable) child);
                if (table.getValueExpression("sortBy") != null || table.getSortBy() != null) {
                    table.setRowIndex(-1);
                }
            }
            resetDataTablesRowIndex(child);
        }
    }

    private UIViewRoot getViewRoot() {
        return FacesContext.getCurrentInstance().getViewRoot();
    }

    @Override
    public javax.faces.context.ExceptionHandler getWrapped() {
        return wrappedExceptionHandler;
    }

    protected List<String> findErrorMessages(Throwable exceptionForSearch) {
        List<Throwable> exceptionPath = getCauses(exceptionForSearch);
        for (String exceptionClassName : errorMessages.stringPropertyNames()) {
            try {
                Class<?> exceptionClass = Class.forName(exceptionClassName);
                for (Throwable exception : exceptionPath) {
                    if (exception instanceof ConstraintViolationException) {
                        return Arrays.asList(getHibernateConstraintViolationMessage((ConstraintViolationException)exception));
                    }
                    if (exception instanceof javax.validation.ConstraintViolationException) {
                        return getConstraintsViolationMessages(exception);
                    } else if (exceptionClass.isInstance(exception)) {
                        return Arrays.asList(errorMessages.get(exceptionClassName));
                    }
                }
            } catch (ClassNotFoundException e) {
                // e.printStackTrace();
            }
        }
        return new ArrayList<String>();
    }

    private List<String> getConstraintsViolationMessages(Throwable throwable) {
        javax.validation.ConstraintViolationException cve = (javax.validation.ConstraintViolationException) throwable;
        List<String> result = new ArrayList<String>();
        for (ConstraintViolation<?> cv : cve.getConstraintViolations()) {
            result.add(cv.getMessage());
        }
        return result;
    }

    private String getHibernateConstraintViolationMessage(ConstraintViolationException constraintViolationException) {
        String exceptionClass = constraintViolationException.getClass().getName();
        String messageKey = exceptionClass + "." + constraintViolationException.getConstraintName().toLowerCase();
        return errorMessages.containsKey(messageKey)
                ? errorMessages.get(messageKey)
                : errorMessages.get(exceptionClass);
    }

    protected List<Throwable> getCauses(Throwable throwable) {
        List<Throwable> result = new ArrayList<Throwable>();
        while (throwable != null) {
            result.add(throwable);
            throwable = throwable.getCause();
        }
        return result;
    }
}
