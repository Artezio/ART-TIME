package com.artezio.arttime.utils;

import javax.el.ELContext;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;

public class ContextUtil {
    @Produces
    @RequestScoped
    public static FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    @Produces
    @RequestScoped
    public static ExternalContext getExternalContext() {
        return getFacesContext().getExternalContext();
    }

    @Produces
    @RequestScoped
    public static ELContext getELContext() {
        return getFacesContext().getELContext();
    }

    @Produces
    @RequestScoped
    public static Flash getFlash() {
        return getExternalContext().getFlash();
    }

    public static <T> void putIntoFlash(String key, T value) {
        getFlash().put(key, value);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getFromFlash(String key) {
        return (T) getFlash().get(key);
    }
}
