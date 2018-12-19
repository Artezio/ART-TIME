package com.artezio.arttime.web.converters;

import com.artezio.arttime.datamodel.WorkdaysCalendar;
import com.artezio.arttime.repositories.WorkdaysCalendarRepository;
import org.apache.commons.lang.StringUtils;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;

@FacesConverter(value = "workdaysCalendarConverter", forClass = WorkdaysCalendar.class)
public class WorkdaysCalendarConverter implements Converter {

    @Inject
    private WorkdaysCalendarRepository workdaysCalendarRepository;

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        Long id = Long.parseLong(value);
        return workdaysCalendarRepository.findWorkdaysCalendar(id);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value == null) {
            return ""; // Required by spec.
        }
        if (value instanceof WorkdaysCalendar) {
            return ((WorkdaysCalendar) value).getId().toString();
        }
        return null;
    }

}
