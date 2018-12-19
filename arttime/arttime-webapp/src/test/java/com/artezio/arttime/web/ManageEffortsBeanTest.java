package com.artezio.arttime.web;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.Hours;
import com.artezio.arttime.datamodel.Project;
import com.artezio.arttime.exceptions.SaveApprovedHoursException;
import com.artezio.arttime.filter.Filter;
import com.artezio.arttime.services.EmployeeService;
import com.artezio.arttime.services.FilterService;
import com.artezio.arttime.services.HoursService;
import com.artezio.arttime.services.NotificationManager;
import static com.artezio.arttime.web.EffortsGrouping.BY_EMPLOYEES;
import static com.artezio.arttime.web.EffortsGrouping.BY_PROJECTS;
import com.artezio.arttime.web.spread_sheet.*;
import com.artezio.arttime.web.spread_sheet.strategies.EmployeeEffortsSpreadSheetBuildingStrategy;
import com.artezio.arttime.web.spread_sheet.strategies.ProjectEffortsSpreadSheetBuildingStrategy;
import com.lassitercg.faces.components.sheet.Sheet;

import java.util.*;

import static junit.framework.TestCase.assertEquals;
import static junitx.util.PrivateAccessor.getField;
import static junitx.util.PrivateAccessor.setField;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.omnifaces.util.Faces;
import org.powermock.api.easymock.PowerMock;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.replay;
import static org.powermock.api.easymock.PowerMock.verify;
import org.powermock.api.easymock.annotation.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Faces.class})
public class ManageEffortsBeanTest {

    private ManageEffortsBean manageEffortsBean;
    @Mock
    private FilterBean filterBean;
    @Mock
    private SpreadSheet spreadSheet;
    @Mock
    private HoursService hoursService;
    @Mock
    private FilterService filterService;
    @Mock
    private NotificationManager notificationManager;

    @Before
    public void setUp() throws Exception {
        manageEffortsBean = new ManageEffortsBean();

        setField(manageEffortsBean, "filterBean", filterBean);
        setField(manageEffortsBean, "spreadSheet", spreadSheet);
        setField(manageEffortsBean, "hoursService", hoursService);
        setField(manageEffortsBean, "notificationManager", notificationManager);
    }

    @Test
    public void testGetSpreadSheet_ifNotNull() throws Exception {
        SpreadSheet actual = manageEffortsBean.getSpreadSheet();

        assertSame(spreadSheet, actual);
    }

    @Test
    public void testSaveHours() throws ReflectiveOperationException, SaveApprovedHoursException {
        manageEffortsBean = createMockBuilder(ManageEffortsBean.class).addMockedMethod("resetComponentsTree").createMock();
        Set<Hours> hours = new HashSet<>();

        setField(manageEffortsBean, "spreadSheet", spreadSheet);
        setField(manageEffortsBean, "hoursService", hoursService);
        setField(manageEffortsBean, "updatedStatus", new HashSet<Hours>());

        expect(spreadSheet.getUpdatedHours()).andReturn(hours);
        hoursService.manageHours(hours);
        manageEffortsBean.resetComponentsTree();
        replay(manageEffortsBean, spreadSheet, hoursService);

        manageEffortsBean.saveHours();

        verify(manageEffortsBean, spreadSheet, hoursService);
    }

    @Test
    public void testResetData() throws NoSuchFieldException {
        manageEffortsBean = createMockBuilder(ManageEffortsBean.class).addMockedMethod("resetComponentsTree").createMock();

        setField(manageEffortsBean, "spreadSheet", spreadSheet);

        manageEffortsBean.resetComponentsTree();
        replay(manageEffortsBean);

        manageEffortsBean.resetData();

        verify(manageEffortsBean);

        assertNull(getField(manageEffortsBean, "spreadSheet"));
    }

    @Test
    public void testApproveSelectedHours() throws NoSuchFieldException {
        Hours hours1 = new Hours();
        Hours hours2 = new Hours();
        List<Hours> hours = Arrays.asList(hours1, hours2);

        expect(spreadSheet.getSelectedHours()).andReturn(hours);
        spreadSheet.updateSelectedRows();
        replay(spreadSheet);

        manageEffortsBean.approveSelectedHours();

        verify(spreadSheet);

        assertTrue(hours.get(0).isApproved());
        assertTrue(hours.get(1).isApproved());
    }

    @Test
    public void testApproveAllHours() throws NoSuchFieldException {
        Hours hours1 = new Hours();
        Hours hours2 = new Hours();
        List<Hours> hours = Arrays.asList(hours1, hours2);

        expect(spreadSheet.getHours()).andReturn(hours);
        spreadSheet.updateAllRows();
        replay(spreadSheet);

        manageEffortsBean.approveAllHours();

        verify(spreadSheet);

        assertTrue(hours.get(0).isApproved());
        assertTrue(hours.get(1).isApproved());
    }

    @Test
    public void testDisapproveSelectedHours() throws NoSuchFieldException {
        Hours hours1 = new Hours();
        hours1.setApproved(true);
        Hours hours2 = new Hours();
        hours2.setApproved(true);
        List<Hours> hours = Arrays.asList(hours1, hours2);

        expect(spreadSheet.getSelectedHours()).andReturn(hours);
        spreadSheet.updateSelectedRows();
        replay(spreadSheet);

        manageEffortsBean.disapproveSelectedHours();

        verify(spreadSheet);

        assertFalse(hours.get(0).isApproved());
        assertFalse(hours.get(1).isApproved());
    }

    @Test
    public void testDisapproveAllHours() throws NoSuchFieldException {
        Hours hours1 = new Hours();
        hours1.setApproved(true);
        Hours hours2 = new Hours();
        hours2.setApproved(true);
        List<Hours> hours = Arrays.asList(hours1, hours2);

        expect(spreadSheet.getHours()).andReturn(hours);
        spreadSheet.updateAllRows();
        replay(spreadSheet);

        manageEffortsBean.disapproveAllHours();

        verify(spreadSheet);

        assertFalse(hours.get(0).isApproved());
        assertFalse(hours.get(1).isApproved());
    }

    @Test
    public void testInitProjectEffortsSpreadSheet() throws Exception {
        manageEffortsBean = new ManageEffortsBean();
        ProjectEffortsSpreadSheetBuildingStrategy strategy = createMock(ProjectEffortsSpreadSheetBuildingStrategy.class);
        EmployeeService employeeService = mock(EmployeeService.class);
        setField(strategy, "employeeService", employeeService);
        Filter filter = createMock(Filter.class);

        setField(manageEffortsBean, "grouping", BY_PROJECTS);
        setField(manageEffortsBean, "filterBean", filterBean);
        setField(manageEffortsBean, "employeeService", employeeService);

        EasyMock.expect(filterService.getActiveProjectsFilter()).andReturn(filter);
        EasyMock.expect(filterBean.getCurrentFilter()).andReturn(filter);
        expect(employeeService.getEffortsEmployees()).andReturn(Collections.emptyList());
        replay(employeeService, filterBean, strategy);

        SpreadSheet actual = manageEffortsBean.initSpreadSheet();

        verify(employeeService, filterBean, strategy);
        assertEquals(ProjectEffortsSpreadSheet.class,  actual.getClass());
    }

    @Test
    public void testInitEmployeeEffortsSpreadSheet() throws Exception {
        manageEffortsBean = new ManageEffortsBean();
        EmployeeService employeeService = mock(EmployeeService.class);
        EmployeeEffortsSpreadSheetBuildingStrategy strategy = createMock(EmployeeEffortsSpreadSheetBuildingStrategy.class);
        setField(strategy, "employeeService", employeeService);
        Filter filter = createMock(Filter.class);

        setField(manageEffortsBean, "grouping", BY_EMPLOYEES);
        setField(manageEffortsBean, "filterBean", filterBean);
        setField(manageEffortsBean, "employeeService", employeeService);

        expect(employeeService.getEffortsEmployees()).andReturn(Collections.emptyList());
        EasyMock.expect(filterBean.getCurrentFilter()).andReturn(filter);
        replay(employeeService, filterBean, strategy);

        SpreadSheet actual = manageEffortsBean.initSpreadSheet();

        verify(employeeService, filterBean, strategy);
        assertEquals(EmployeeEffortsSpreadSheet.class,  actual.getClass());
    }

    @Test
    public void testShowMasterProjectTotalsLabel_ifGroupingByEmployee() throws NoSuchFieldException {
        SpreadSheetRow row = createMock(SpreadSheetRow.class);

        setField(manageEffortsBean, "grouping", BY_EMPLOYEES);

        boolean actual = manageEffortsBean.showMasterProjectTotalsLabel(row);

        assertFalse(actual);
    }

    @Test
    public void testShowMasterProjectTotalsLabel_ifGroupingByProject_AndShow() throws NoSuchFieldException {
        Project project1 = new Project();
        Project project2 = new Project();
        Sheet sheet = createMock(Sheet.class);
        SpreadSheet spreadSheet = createMock(SpreadSheet.class);
        HeadSpreadSheetRow row1 = new HeadSpreadSheetRow(project1);
        HeadSpreadSheetRow row2 = new HeadSpreadSheetRow(project2);

        setField(project1, "id", 1L);
        setField(project2, "id", 2L);
        setField(manageEffortsBean, "grouping", BY_PROJECTS);
        setField(manageEffortsBean, "spreadSheet", spreadSheet);

        expect(spreadSheet.getSheet()).andReturn(sheet);
        expect(sheet.getRowIndex()).andReturn(1);
        expect(spreadSheet.getRows()).andReturn(Arrays.asList(row1, row2));
        replay(spreadSheet, sheet);

        boolean actual = manageEffortsBean.showMasterProjectTotalsLabel(row2);

        verify(spreadSheet, sheet);

        assertTrue(actual);
    }

    @Test
    public void testShowMasterProjectTotalsLabel_ifGroupingByProject_AndNotShow() throws NoSuchFieldException {
        Project project = new Project();
        Sheet sheet = createMock(Sheet.class);
        SpreadSheet spreadSheet = createMock(SpreadSheet.class);
        HeadSpreadSheetRow row1 = new HeadSpreadSheetRow(project);
        HeadSpreadSheetRow row2 = new HeadSpreadSheetRow(project);

        setField(manageEffortsBean, "grouping", BY_PROJECTS);
        setField(project, "id", 1L);
        setField(manageEffortsBean, "spreadSheet", spreadSheet);

        expect(spreadSheet.getSheet()).andReturn(sheet);
        expect(sheet.getRowIndex()).andReturn(1);
        expect(spreadSheet.getRows()).andReturn(Arrays.asList(row1, row2));
        replay(spreadSheet, sheet);

        boolean actual = manageEffortsBean.showMasterProjectTotalsLabel(row2);

        verify(spreadSheet, sheet);

        assertFalse(actual);
    }

    @Test
    public void testRequestReport() throws Exception {
        Filter filter = new Filter();
        Employee employee = new Employee();
        PowerMock.mockStatic(Faces.class);

        expect(Faces.getRequestParameter("recipientEmail")).andReturn("iivanov@mail.com");
        expect(filterBean.getCurrentFilter()).andReturn(filter);
        notificationManager.requestWorkTimeReport("iivanov@mail.com", filter.getPeriod());
        PowerMock.replayAll(Faces.class, filterBean, notificationManager);

        manageEffortsBean.requestReport();

        PowerMock.verifyAll();
    }

}
