package com.artezio.arttime.services;

import static junitx.util.PrivateAccessor.setField;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.artezio.arttime.config.Setting;
import com.artezio.arttime.config.Settings;

public class SettingsServiceTest {

    private EntityManager entityManager;
    private EntityManagerFactory entityManagerFactory;
    private SettingsService settingsService;

    @Before
    public void setUp() throws Exception {
        settingsService = new SettingsService();
        Map<String, String> properties = new HashMap<>();
        properties.put("javax.persistence.validation.mode", "none");
        entityManagerFactory = Persistence.createEntityManagerFactory("test-pu", properties);
        entityManager = entityManagerFactory.createEntityManager();
        setField(settingsService, "entityManager", entityManager);
        entityManager.getTransaction().begin();
    }

    @After
    public void tearDown() throws Exception {
        if (entityManager.getTransaction().isActive()) {
            if (entityManager.getTransaction().getRollbackOnly()) {
                entityManager.getTransaction().rollback();
            } else {
                entityManager.getTransaction().commit();
            }
            entityManagerFactory.close();
        }
    }

    @Test
    public void testGetSettings() {
        Setting setting1 = new Setting(Setting.Name.APPLICATION_BASE_URL, "abc");
        Setting setting2 = new Setting(Setting.Name.HELP_PAGE_URL, null);
        entityManager.persist(setting1);
        entityManager.persist(setting2);
        entityManager.flush();
        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++");
        Settings actual = settingsService.getSettings();
        settingsService.getSettings();
        settingsService.getSettings();
        settingsService.getSettings();
        settingsService.getSettings();
        settingsService.getSettings();
        settingsService.getSettings();
        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++");

        assertNotNull(actual);
        assertTrue(actual.getValuesBySettingNames().containsKey(Setting.Name.APPLICATION_BASE_URL));
        assertTrue(actual.getValuesBySettingNames().containsKey(Setting.Name.HELP_PAGE_URL));
        assertEquals(setting1.getValue(), actual.getValuesBySettingNames().get(setting1.getName()));
        assertEquals(setting2.getValue(), actual.getValuesBySettingNames().get(setting2.getName()));
    }

    @Test
    public void testGetSettings_ifNoSettingsExist() {
        Settings actual = settingsService.getSettings();
        assertNotNull(actual);
    }

    @Test
    public void testUpdateSettings() {
        Setting setting1 = new Setting(Setting.Name.LDAP_BIND_CREDENTIALS, "old_value1");
        Setting setting2 = new Setting(Setting.Name.SMTP_HOST_NAME, "old_value2");
        entityManager.persist(setting1);
        entityManager.persist(setting2);
        entityManager.flush();
        Settings settings = settingsService.getSettings();
        settings.setLdapBindCredentials("new_value");
        settings.setSmtpHostName(null);
        settingsService.update(settings);
        Settings newSettings = settingsService.getSettings();
        assertTrue(newSettings.getLdapBindCredentials().equals("new_value"));
        assertNull(newSettings.getSmtpHostName());
    }
}
