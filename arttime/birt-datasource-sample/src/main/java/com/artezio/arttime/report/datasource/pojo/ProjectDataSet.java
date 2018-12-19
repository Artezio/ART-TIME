package com.artezio.arttime.report.datasource.pojo;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.artezio.arttime.datamodel.Project;
import com.artezio.arttime.report.datasource.pojo.DataSet;
import com.artezio.arttime.report.repositories.SampleDataRepository;

public class ProjectDataSet extends DataSet {
    
    private SampleDataRepository repository = new SampleDataRepository();

    @Override
    public void open(Object appContext, Map<String, Object> parameters) {
        super.open(appContext, parameters);
        List<Project> projects = repository.getProjects();
        Iterator<Project> iterator = projects.stream()
                .filter(p -> getProjectIdsParam().contains(p.getId()))
                .iterator();
        setDataIterator(iterator);
    }

}
