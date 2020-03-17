package com.artezio.arttime.test.utils;

import java.util.Locale;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class DefaultLocaleRule extends TestWatcher {

    private Locale originalDefault;
    private Locale currentDefault;

    public DefaultLocaleRule() {
        this(null);
    }

    public DefaultLocaleRule(Locale defaultForTests) {
        currentDefault = defaultForTests;
    }

    @Override
    protected void starting(Description description) {
        originalDefault = Locale.getDefault();

        if (null != currentDefault) {
            Locale.setDefault(currentDefault);
        }
    }

    @Override
    protected void finished(Description description) {
        Locale.setDefault(originalDefault);
    }

    public void setDefault(Locale locale) {
        if (null == locale) {
            locale = originalDefault;
        }

        Locale.setDefault(locale);
    }

}