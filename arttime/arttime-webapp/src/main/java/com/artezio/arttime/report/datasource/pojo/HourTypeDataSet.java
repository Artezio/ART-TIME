package com.artezio.arttime.report.datasource.pojo;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.inject.spi.CDI;

import com.artezio.arttime.datamodel.HourType;
import com.artezio.arttime.services.HourTypeService;

public class HourTypeDataSet extends DataSet {

    private Logger log = Logger.getLogger(HourTypeDataSet.class.getName());

    private HourTypeService hourTypeService;

    @Override
    public void open(Object appContext, Map<String, Object> parameters) {
        super.open(appContext, parameters);
        try {
            hourTypeService = getHourTypeService();
            List<HourType> hourTypes = getHourTypes();
            setDataIterator(hourTypes.iterator());
        } catch (Exception e) {
            log.log(Level.WARNING, "Error opening data source", e);
            throw new RuntimeException(e);
        }
    }

    protected HourTypeService getHourTypeService() {
        return CDI.current().select(HourTypeService.class).get();
    }

    private List<HourType> getHourTypes() {
        return hourTypeService.getAll(getHourTypeIdsParam());
    }

}
