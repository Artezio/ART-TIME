package com.artezio.arttime.report.datasource.pojo;

import static com.artezio.arttime.report.datasource.pojo.DataSet.*;
import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.easymock.Mock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.artezio.arttime.datamodel.Period;
import com.artezio.arttime.services.HoursService;

@RunWith(PowerMockRunner.class)
@PrepareForTest(HoursDataSetTest.class)
public class HoursDataSetTest {

    private HoursDataSet hoursDataSet;
    @Mock
    private HoursService hoursService;

    @Before
    public void setUp() throws Exception {
        hoursDataSet = PowerMock.createPartialMock(HoursDataSet.class, "getHoursService");
    }

    @Test
    public void testOpen_dataByPeriod() throws NamingException, NoSuchFieldException {
        Date start = new GregorianCalendar(2018, 9, 29).getTime();
        Date finish = new GregorianCalendar(2018, 9, 31).getTime();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(REPORT_PARAM_NAME_START_DATE, start);
        parameters.put(REPORT_PARAM_NAME_END_DATE, finish);
        parameters.put(REPORT_PARAM_NAME_EMPLOYEE_USERNAMES, new String[] { "user1", "user2" });
        parameters.put(REPORT_PARAM_NAME_PROJECT_IDS, new Long[] { 1L, 2L });
        parameters.put(REPORT_PARAM_NAME_HOUR_TYPE_IDS, new Long[] { 1L, 3L });
        parameters.put(REPORT_PARAM_NAME_DEPARTMENTS, new String[] { "dep1", "dep2" });

        expect(hoursDataSet.getHoursService()).andReturn(hoursService);
        expect(hoursService.getHours(new Period(start, finish), Arrays.asList("user1", "user2"),
                Arrays.asList(1L, 2L), Arrays.asList(1L, 3L), Arrays.asList("dep1", "dep2")))
                        .andReturn(Collections.emptyList());
        replayAll();

        hoursDataSet.open(null, parameters);

        verifyAll();
    }

    @Test(expected = RuntimeException.class)
    public void testOpen_noService() {
        PowerMock.mockStatic(InitialContext.class);
        expect(hoursDataSet.getHoursService()).andThrow(new RuntimeException());

        replayAll();

        hoursDataSet.open(null, new HashMap<>());

        verifyAll();
    }

}
