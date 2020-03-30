package com.artezio.arttime.report.datasource.pojo;

import static com.artezio.arttime.report.datasource.pojo.DataSet.REPORT_PARAM_NAME_HOUR_TYPE_IDS;
import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.artezio.arttime.services.HourTypeService;

@RunWith(PowerMockRunner.class)
@PrepareForTest(HourTypeDataSet.class)
public class HourTypeDataSetTest {

    private HourTypeDataSet hourTypeDataSet;
    private HourTypeService hourTypeService;

    @Before
    public void setUp() throws Exception {
        hourTypeDataSet = PowerMock.createPartialMock(HourTypeDataSet.class, "getHourTypeService");
        hourTypeService = PowerMock.createMock(HourTypeService.class);
    }

    @Test
    public void testOpen() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(REPORT_PARAM_NAME_HOUR_TYPE_IDS, new Long[] { 1L, 2L });

        expect(hourTypeDataSet.getHourTypeService()).andReturn(hourTypeService);
        expect(hourTypeService.getAll(Arrays.asList(1L, 2L))).andReturn(Collections.emptyList());
        replayAll();

        hourTypeDataSet.open(null, parameters);

        verifyAll();
    }

    @Test(expected = RuntimeException.class)
    public void testOpen_noService() {
        expect(hourTypeDataSet.getHourTypeService()).andThrow(new RuntimeException());
        replayAll();

        hourTypeDataSet.open(null, new HashMap<>());

        verifyAll();
    }

}
