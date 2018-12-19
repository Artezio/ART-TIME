package com.artezio.arttime.web.converters;

import com.artezio.arttime.datamodel.Project;
import com.artezio.arttime.repositories.ProjectRepository;
import com.artezio.arttime.services.ProjectService;
import org.apache.commons.lang.StringUtils;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;

@FacesConverter(value = "projectConverter", forClass = Project.class)
public class ProjectConverter implements Converter {

    @Inject
    private ProjectRepository projectRepository;

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        Long id = Long.parseLong(value);
        return projectRepository.findById(id);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value instanceof Project) {
            return ((Project) value).getId().toString();
        }
        return null;
    }

}
