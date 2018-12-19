package com.artezio.arttime.report.datasource.pojo;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.artezio.arttime.datamodel.Hours;
import com.artezio.arttime.report.datasource.pojo.DataSet;
import com.artezio.arttime.report.repositories.SampleDataRepository;

public class HoursDataSet extends DataSet {
    
    private SampleDataRepository repository = new SampleDataRepository();

    @Override
    public void open(Object appContext, Map<String, Object> parameters) {
        super.open(appContext, parameters);
        List<Hours> hours = repository.getHours();
        Iterator<Hours> iterator = hours.stream()
                .filter(h -> h.getDate().getTime() >= getStartDateParam().getTime())
                .filter(h -> h.getDate().getTime() <= getEndDateParam().getTime())
                .filter(h -> getEmployeeUserNamesParam().contains(h.getEmployee().getUserName()))
                .filter(h -> getProjectIdsParam().contains(h.getProject().getId()))
                .filter(h -> getHourTypeIdsParam().contains(h.getType().getId()))
                .filter(h -> getDepartmentsParam().contains(h.getEmployee().getDepartment()))
                .iterator();
        setDataIterator(iterator);
    }

}
