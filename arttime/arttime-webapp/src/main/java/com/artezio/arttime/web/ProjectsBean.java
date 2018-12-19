package com.artezio.arttime.web;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.Project;
import com.artezio.arttime.datamodel.Project.Status;
import com.artezio.arttime.repositories.ProjectRepository;
import com.artezio.arttime.services.ProjectService;
import org.primefaces.component.datatable.DataTable;

import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class ProjectsBean implements Serializable {
    private static final long serialVersionUID = 6385561178974142829L;
    @Inject
    private FacesContext facesContext;
    @Inject
    private ProjectRepository projectRepository;
    @Inject
    private ProjectService projectService;
    private List<Project> filteredProjects;
    private List<Project> projects;
    private UIInput hourTypesCheckbox;
    private Status defaultStatus = Status.ACTIVE;

    public List<Project> getProjects() {
        if (projects == null) {
            projects = projectService.fetchComplete(projectService.getManagedProjects());
        }
        return projects;
    }

    public void remove(Project project) {
        projectService.remove(project);
    }

    public List<Project> getFilteredProjects() {
        return filteredProjects;
    }

    public void setFilteredProjects(List<Project> filteredProjects) {
        this.filteredProjects = filteredProjects;
    }

    @SuppressWarnings("unchecked")
    public boolean filterByManager(Object value, Object filterValue, Locale locale) {
        String stringFilterValue = ((String) filterValue).toLowerCase();
        List<Employee> managers = (List<Employee>) value;
        return managers.parallelStream().
                anyMatch(manager -> manager.getFullName().toLowerCase().contains(stringFilterValue));
    }

    public UIInput getHourTypesCheckbox() {
        return hourTypesCheckbox;
    }

    public void setHourTypesCheckbox(UIInput hourTypesCheckbox) {
        this.hourTypesCheckbox = hourTypesCheckbox;
    }

    public boolean filterBySubprojects(Object value, Object filterValue, Locale locale) {
        boolean showSubprojects = (boolean) filterValue;
        boolean hasMaster = (boolean) value;
        return showSubprojects ? true : !hasMaster;
    }

    public Status getDefaultStatus() {
        return defaultStatus;
    }

    public void setDefaultStatus(Status defaultStatus) {
        this.defaultStatus = defaultStatus;
    }

    public void setInitialFilteredValue(ComponentSystemEvent event) {
        if (!facesContext.isPostback()) {
            filteredProjects = getProjects().parallelStream()
                    .filter(project -> project.getStatus() == defaultStatus)
                    .collect(Collectors.toList());
            DataTable table = (DataTable) event.getComponent();
            table.updateValue(filteredProjects);
        }
    }

}
