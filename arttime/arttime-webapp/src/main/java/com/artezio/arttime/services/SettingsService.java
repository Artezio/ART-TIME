package com.artezio.arttime.services;

import com.artezio.arttime.admin_tool.log.DetailedLogged;
import com.artezio.arttime.admin_tool.log.Log;
import com.artezio.arttime.config.ApplicationSettings;
import com.artezio.arttime.config.Setting;
import com.artezio.arttime.config.Settings;
import com.artezio.arttime.web.interceptors.FacesMessage;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.enterprise.inject.Produces;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.constraints.NotNull;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static com.artezio.arttime.security.auth.UserRoles.ADMIN_ROLE;

@Named
@Stateless
public class SettingsService {

    @PersistenceContext
    private EntityManager entityManager;

    @PermitAll
    @Produces
    @ApplicationSettings
    public Settings getSettings() {
        List<Setting> storedSettings = entityManager
                .createQuery("SELECT s FROM Setting s", Setting.class)
                .setHint(org.hibernate.jpa.QueryHints.HINT_CACHEABLE, "true")
                .getResultList();
        Map<Setting.Name, String> valuesByNames = new EnumMap<>(Setting.Name.class);
        storedSettings.forEach(setting -> valuesByNames.put(setting.getName(), setting.getValue()));
        return new Settings(valuesByNames);
    }

    @RolesAllowed(ADMIN_ROLE)
    @Log(logParams = true)
    @FacesMessage(onCompleteMessageKey = "message.settingsAreSaved")
    public Settings update(@DetailedLogged Settings settings) {
        settings.getValuesBySettingNames().entrySet().stream()
                .map(entry -> new Setting(entry.getKey(), entry.getValue()))
                .forEach(entityManager::merge);
        return settings;
    }
}
