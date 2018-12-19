package com.artezio.arttime.web;

import static junitx.util.PrivateAccessor.setField;
import static org.junit.Assert.*;

import com.artezio.arttime.config.Setting;
import com.artezio.arttime.config.Settings;
import org.junit.Before;
import org.junit.Test;

import com.artezio.arttime.services.SettingsService;

import java.util.EnumMap;

public class SettingsBeanTest {
    private SettingsBean settingsBean;
    private SettingsService settingsService;
    private Settings settings;

    @Before
    public void setUp() throws NoSuchFieldException {
        settingsBean = new SettingsBean();
    }

    @Test
    public void testGetSettings_ifNotNull() throws Exception {
        settings = new Settings(new EnumMap<>(Setting.Name.class));
        setField(settingsBean, "settings", settings);

        Settings actual = settingsBean.getSettings();

        assertSame(settings, actual);
    }

}
