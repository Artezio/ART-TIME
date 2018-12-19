package com.artezio.arttime.web;

import com.artezio.arttime.datamodel.HourType;
import com.artezio.arttime.exceptions.ActualTimeRemovalException;
import com.artezio.arttime.services.HourTypeService;

import javax.annotation.PostConstruct;
import javax.faces.context.ExternalContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Named
@ViewScoped
public class HourTypesBean implements Serializable {
    private static final long serialVersionUID = 6305759075233416778L;

    private HourType hourType;
    @Inject
    private ExternalContext externalContext;
    @Inject
    private HourTypeService hourTypeService;
    private List<HourType> hourTypes;

    @PostConstruct
    public void init() {
        Map<String, String> requestParams = externalContext.getRequestParameterMap();
        String hourTypeId = requestParams.get("hourType");
        this.hourType = (hourTypeId == null)
                ? new HourType()
                : hourTypeService.find(Long.parseLong(hourTypeId));
    }

    public void update(HourType hourType) {
        hourTypeService.update(hourType);
    }

    public void create(HourType hourType) {
        hourTypeService.create(hourType);
    }

    public void remove(HourType hourType) throws ActualTimeRemovalException {
        hourTypeService.remove(hourType);
        hourTypes = null;
    }

    public void addNew() {
        hourType = new HourType();
    }

    public HourType getHourType() {
        return hourType;
    }

    public void setHourType(HourType hourType) {
        this.hourType = hourType;
    }

    public List<HourType> getHourTypes() {
        if (hourTypes == null) {
            hourTypes = hourTypeService.getAll();
            Collections.sort(hourTypes, HourType.ACTUALTIME_TYPE_COMPARATOR);
        }
        return hourTypes;
    }

    public int compare(Object type1, Object type2) {
        return HourType.ACTUALTIME_TYPE_COMPARATOR.compare((HourType) type1, (HourType) type2);
    }

    public void setHourTypes(List<HourType> hourTypes) {
        this.hourTypes = hourTypes;
    }

    public void setActualTime(HourType hourType) {
        hourTypeService.setActualTime(hourType);
        hourTypes = null;
    }
}
