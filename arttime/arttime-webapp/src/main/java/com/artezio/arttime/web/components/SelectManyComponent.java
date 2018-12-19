package com.artezio.arttime.web.components;

import javax.faces.component.FacesComponent;
import javax.faces.component.UINamingContainer;
import java.util.ArrayList;
import java.util.List;

@FacesComponent("selectManyComponent")
public class SelectManyComponent<T> extends UINamingContainer {
	private List<T> availableItems;	
	private List<T> selectedItems;
	private String searchValue;
	
	@SuppressWarnings("unchecked")
	public void init() {
		availableItems = (List<T>) getAttributes().get("availableItems");
		selectedItems = (List<T>) getAttributes().get("selectedItems");
	}
	
	public void selectAll() {		
		selectedItems.clear();
		selectedItems.addAll(availableItems);
	}
	
	public void selectNone() {				
		selectedItems.clear();		
	}
	
	public void invertSelection() {
		List<T> previouslySelected = new ArrayList<T>(selectedItems);
		selectedItems.clear();
		selectedItems.addAll(availableItems);
		selectedItems.removeAll(previouslySelected);
	}

	public String getSearchValue() {
		return searchValue;
	}

	public void setSearchValue(String searchValue) {
		this.searchValue = searchValue;
	}
}
