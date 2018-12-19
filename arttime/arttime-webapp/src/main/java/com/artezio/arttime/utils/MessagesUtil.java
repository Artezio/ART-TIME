package com.artezio.arttime.utils;

import org.hibernate.exception.ConstraintViolationException;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;

public class MessagesUtil {
    
    private static final String MESSAGE_BUNDLE = "com.artezio.arttime.i18n.messages";
    private static final Locale DEFAULT_LOCALE = new Locale("en", "US");

    public static void addInfo(String id, String key) {
        String message = getLocalizedString(key);
        getFacesContext().addMessage(id, new FacesMessage(FacesMessage.SEVERITY_INFO, message, message));
    }

    public static void addError(String id, String key) {
        String message = getLocalizedString(key);
        getFacesContext().addMessage(id, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, message));
    }
    
    public static void addError(String id, String key, Object... arguments) {
        String message = getLocalizedString(key, arguments);
        getFacesContext().addMessage(id, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, message));
    }

    public static String getLocalizedString(String key) {
        ResourceBundle bundle = ResourceBundle.getBundle(getMessageBundle(), getCurrentLocale());
        return bundle.containsKey(key) ? bundle.getString(key) : key;
    }
    
    public static String getLocalizedString(String key, Object... arguments){
    	ResourceBundle bundle = ResourceBundle.getBundle(getMessageBundle(), getCurrentLocale());    	
    	return bundle.containsKey(key) ? MessageFormat.format(bundle.getString(key), arguments) : key;
    }

    private static String getMessageBundle() {
	if(getFacesContext() == null){
	    return MESSAGE_BUNDLE;
	}
        return getFacesContext().getApplication().getMessageBundle(); 
    }

    private static Locale getCurrentLocale() {
		if(getFacesContext() == null){
		    return  DEFAULT_LOCALE;
		}
		UIViewRoot viewRoot = getFacesContext().getViewRoot();
        return (viewRoot == null)
        		? DEFAULT_LOCALE
        		: viewRoot.getLocale();
    }

    private static FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

	public static void addError(Throwable throwable) {
		String key = null;
		ResourceBundle resourceBundle = ResourceBundle.getBundle(getMessageBundle(), getCurrentLocale()); 
		while (throwable != null) {			
			key = (throwable instanceof ConstraintViolationException)
					? getConstraintViolationExceptionKey((ConstraintViolationException)throwable)				
					: throwable.getClass().getName();							
			if (resourceBundle.containsKey(key)) break;
			throwable = throwable.getCause();
		}		
		addError(null, key);				
	}

	private static String getConstraintViolationExceptionKey(ConstraintViolationException exception) {		
		return (exception.getConstraintName() == null)
				? exception.getClass().getName()
				: exception.getConstraintName();
	}

	public static void removeMessagesLessThan(Severity severity) {
		Iterator<FacesMessage> iterator = getFacesContext().getMessages();
		while ( iterator.hasNext() ) {
		    iterator.next();
		    iterator.remove();
		}
		
	}
}

