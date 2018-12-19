package com.artezio.arttime.web.components;

import javax.faces.component.FacesComponent;
import javax.faces.component.UINamingContainer;

@FacesComponent("checkUnsavedModificationComponent")
public class CheckUnsavedModificationComponent extends UINamingContainer {
	private boolean showNotification;
	
	public boolean isShowNotification() {
		return showNotification;
	}

	public void setShowNotification(boolean showNotification) {
		this.showNotification = showNotification;
	}
	
	
}
