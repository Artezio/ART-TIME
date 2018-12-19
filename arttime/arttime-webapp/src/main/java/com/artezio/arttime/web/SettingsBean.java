package com.artezio.arttime.web;

import com.artezio.arttime.config.ApplicationSettings;
import com.artezio.arttime.config.Settings;
import org.primefaces.component.tabview.TabView;
import org.primefaces.event.TabChangeEvent;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

@Named
@ViewScoped
public class SettingsBean implements Serializable {
    private static final long serialVersionUID = -4663864281011566866L;

    @Inject
    @ApplicationSettings
    private Settings settings;
    private String adminPassword;
    private Integer selectedTabIndex = 0;

    public Settings getSettings() {
        return settings;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public Integer getSelectedTabIndex() {
        return selectedTabIndex;
    }

    public void setSelectedTabIndex(Integer selectedTabIndex) {
        this.selectedTabIndex = selectedTabIndex;
    }

    public void onDataProvidersTabChange(TabChangeEvent event) {
        TabView tabView = (TabView)event.getComponent();
        selectedTabIndex = tabView.getChildren().indexOf(event.getTab());
    }
    
}
