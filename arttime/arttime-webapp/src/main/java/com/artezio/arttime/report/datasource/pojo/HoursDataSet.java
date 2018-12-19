package com.artezio.arttime.report.datasource.pojo;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.inject.spi.CDI;

import com.artezio.arttime.datamodel.Hours;
import com.artezio.arttime.datamodel.Period;
import com.artezio.arttime.services.HoursService;

public class HoursDataSet extends DataSet {

    private Logger log = Logger.getLogger(HoursDataSet.class.getName());

    private HoursService hoursService;

    @Override
    public void open(Object appContext, Map<String, Object> parameters) {
        super.open(appContext, parameters);
        try {
            hoursService = getHoursService();
            List<Hours> hours = getHours();
            setDataIterator(hours.iterator());
        } catch (Exception e) {
            log.log(Level.WARNING, "Error opening data source", e);
            throw new RuntimeException(e);
        }
    }

    protected HoursService getHoursService() {
        return CDI.current().select(HoursService.class).get();
    }

    private List<Hours> getHours() {
        Period period = new Period(getStartDateParam(), getEndDateParam());
        return hoursService.getHours(period, getEmployeeUserNamesParam(),
                getProjectIdsParam(), getHourTypeIdsParam(), getDepartmentsParam());
    }

}
