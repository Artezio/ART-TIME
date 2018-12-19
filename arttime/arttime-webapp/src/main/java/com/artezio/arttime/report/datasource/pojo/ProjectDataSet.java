package com.artezio.arttime.report.datasource.pojo;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.inject.spi.CDI;

import com.artezio.arttime.datamodel.Project;
import com.artezio.arttime.services.ProjectService;

public class ProjectDataSet extends DataSet {

    private Logger log = Logger.getLogger(ProjectDataSet.class.getName());

    private ProjectService projectService;

    @Override
    public void open(Object appContext, Map<String, Object> parameters) {
        super.open(appContext, parameters);
        try {
            projectService = getProjectService();
            List<Project> projects = getProjects();
            setDataIterator(projects.iterator());
        } catch (Exception e) {
            log.log(Level.WARNING, "Error opening data source", e);
            throw new RuntimeException(e);
        }
    }

    protected ProjectService getProjectService() {
        return CDI.current().select(ProjectService.class).get();
    }

    private List<Project> getProjects() {
        return projectService.fetchComplete(projectService.getProjects(getProjectIdsParam()));
    }

}
