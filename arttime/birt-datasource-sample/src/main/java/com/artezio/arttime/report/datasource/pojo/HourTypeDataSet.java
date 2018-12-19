package com.artezio.arttime.report.datasource.pojo;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.artezio.arttime.datamodel.HourType;
import com.artezio.arttime.report.datasource.pojo.DataSet;
import com.artezio.arttime.report.repositories.SampleDataRepository;

public class HourTypeDataSet extends DataSet {
    
    private SampleDataRepository repository = new SampleDataRepository();
    
    @Override
    public void open(Object appContext, Map<String, Object> parameters) {
        super.open(appContext, parameters);
        List<HourType> hourTypes = repository.getHourTypes();
        Iterator<HourType> iterator = hourTypes.stream()
                .filter(ht -> getHourTypeIdsParam().contains(ht.getId()))
                .iterator();
        setDataIterator(iterator);
    }

}
