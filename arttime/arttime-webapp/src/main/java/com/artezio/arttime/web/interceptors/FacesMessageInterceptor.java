package com.artezio.arttime.web.interceptors;

import java.util.Locale;
import java.util.ResourceBundle;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@FacesMessage
@Interceptor
public class FacesMessageInterceptor {

    @AroundInvoke
    public Object aroundInvoke(InvocationContext ic) throws Exception {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Object result = ic.proceed();
        if (facesContext == null) {
            return result;
        }
        ExternalContext externalContext = facesContext.getExternalContext();
        externalContext.getFlash().setKeepMessages(true);

        FacesMessage annotation = ic.getMethod().getAnnotation(FacesMessage.class);
        String onCompleteMessageKey = annotation.onCompleteMessageKey();
        Locale locale = externalContext.getRequestLocale();
        String messageBundleName = facesContext.getApplication().getMessageBundle();
        ResourceBundle messageBundle = ResourceBundle.getBundle(messageBundleName, locale);
        facesContext.addMessage(null, new javax.faces.application.FacesMessage(messageBundle.getString(onCompleteMessageKey)));

        return result;
    }

}
