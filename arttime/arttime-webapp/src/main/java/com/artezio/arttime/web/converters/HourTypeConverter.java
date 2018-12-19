package com.artezio.arttime.web.converters;

import com.artezio.arttime.datamodel.HourType;
import com.artezio.arttime.repositories.HourTypeRepository;
import org.apache.commons.lang.StringUtils;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;

@FacesConverter(value = "hourTypeConverter", forClass = HourType.class)
public class HourTypeConverter implements Converter {

    @Inject
    private HourTypeRepository hourTypeRepository;

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        Long id = Long.valueOf(value);
        return hourTypeRepository.find(id);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value instanceof HourType) {
            return ((HourType) value).getId().toString();
        }
        return null;
    }
}
