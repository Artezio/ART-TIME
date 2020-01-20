package com.artezio.arttime.web;

import com.artezio.arttime.datamodel.*;
import com.artezio.arttime.filter.Filter;
import com.artezio.arttime.report.OutputFormat;
import com.artezio.arttime.report.ReportEngine;
import com.artezio.arttime.repositories.EmployeeRepository;
import com.artezio.arttime.services.*;
import com.artezio.arttime.web.criteria.RangePeriodSelector;
import junitx.framework.ArrayAssert;
import junitx.framework.ListAssert;
import org.apache.commons.io.IOUtils;
import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.primefaces.model.StreamedContent;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static junitx.util.PrivateAccessor.setField;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(EasyMockRunner.class)
public class ReportsBeanTest {
    
    @TestSubject
    private ReportsBean reportsBean = new ReportsBean();
    @Mock
    private FilterBean filterBean;
    @Mock
    private ReportEngine reportEngine;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private HourTypeService hourTypeService;
    @Mock
    private ProjectService projectService;
    @Mock
    private HoursService hoursService;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private EmployeeService employeeService;

    @Test
    public void testGetReportFile() throws Exception {
        reportsBean = createMockBuilder(ReportsBean.class).addMockedMethod("generateReport").createMock();
        String templateName = "testReport";
        setField(reportsBean, "outputFormat", OutputFormat.EXCEL);
        setField(reportsBean, "templateName", templateName);

        expect(reportsBean.generateReport()).andReturn(new ByteArrayInputStream("report".getBytes()));
        replay(reportsBean);

        StreamedContent actual = reportsBean.getReportFile();

        verify(reportsBean);
        assertNotNull(actual);
        assertEquals(actual.getContentType(), OutputFormat.EXCEL.getContentType());
        assertEquals(actual.getName(), templateName + "." + OutputFormat.EXCEL.getFileExtension());
    }
    
    @Test
    public void testGenerateReport() throws Exception {
        reportsBean = createMockBuilder(ReportsBean.class).addMockedMethod("getReportParams").createMock();
        String templateName = "testReport";
        String report = "report";
        setField(reportsBean, "reportEngine", reportEngine);
        setField(reportsBean, "outputFormat", OutputFormat.EXCEL);
        setField(reportsBean, "templateName", templateName);
        Map<String, Object> params = new HashMap<>();


        expect(reportsBean.getReportParams()).andReturn(params);
        expect(reportEngine.generate(templateName, OutputFormat.EXCEL, params)).andReturn(report.getBytes());
        replay(reportsBean, reportEngine);

        InputStream actual = reportsBean.generateReport();

        verify(reportsBean, reportEngine);
        assertNotNull(actual);
        assertEquals(report, IOUtils.toString(actual, StandardCharsets.UTF_8));
    }
    
    @Test
    public void testGetReportParams() {
        reportsBean = createMockBuilder(ReportsBean.class)
            .addMockedMethods("getSelectedStartDate", "getSelectedEndDate", "getSelectedEmployeeUserNames",
                "getSelectedDepartments", "getSelectedProjectIds", "getSelectedHourTypeIds")
            .createMock();

        Map<String, Object> expected = new HashMap<>();
        expected.put("startDate", new Date(1));
        expected.put("endDate", new Date(2));
        expected.put("employeeUserNames", new String[] {"user"});
        expected.put("departments",  new String[] {"department"});
        expected.put("projectIds", new Long[]{1L});
        expected.put("hourTypeIds", new Long[]{2L});

        expect(reportsBean.getSelectedStartDate()).andReturn((Date) expected.get("startDate"));
        expect(reportsBean.getSelectedEndDate()).andReturn((Date) expected.get("endDate"));
        expect(reportsBean.getSelectedEmployeeUserNames()).andReturn((String[]) expected.get("employeeUserNames"));
        expect(reportsBean.getSelectedDepartments()).andReturn((String[]) expected.get("departments"));
        expect(reportsBean.getSelectedProjectIds()).andReturn((Long[]) expected.get("projectIds"));
        expect(reportsBean.getSelectedHourTypeIds()).andReturn((Long[]) expected.get("hourTypeIds"));
        replay(reportsBean);

        Map<String, Object> actual = reportsBean.getReportParams();

        verify(reportsBean);
        assertEquals(expected, actual);
    }
    
    @Test
    public void testGetSelectedStartDate() throws Exception {
        Filter filter = new Filter();
        Date expected = new Date(1000);
        filter.setRangePeriodSelector(new RangePeriodSelector(new Period(expected, new Date(2000))));

        expect(filterBean.getCurrentFilter()).andReturn(filter);
        replay(filterBean);

        Date actual = reportsBean.getSelectedStartDate();

        verify(filterBean);
        assertEquals(expected, actual);
    }
    
    
    @Test
    public void testGetSelectedEndDate() throws Exception {
        Filter filter = new Filter();
        Date expected = new Date(2000);
        filter.setRangePeriodSelector(new RangePeriodSelector(new Period(new Date(1000), expected)));

        expect(filterBean.getCurrentFilter()).andReturn(filter);
        replay(filterBean);

        Date actual = reportsBean.getSelectedEndDate();

        verify(filterBean);
        assertEquals(expected, actual);
    }
    
    @Test
    public void testGetSelectedEmployeeUserNames() throws NoSuchFieldException {
        List<Employee> employees = asList(new Employee("username1"), new Employee("username2"));
        Filter filter = new Filter();
        filter.setEmployees(employees);

        expect(employeeService.getEffortsEmployees(filter)).andReturn(employees);
        expect(filterBean.getCurrentFilter()).andReturn(filter).anyTimes();
        replay(filterBean, employeeService);
        
        String[] actual = reportsBean.getSelectedEmployeeUserNames();

        verify(filterBean, employeeService);
        ListAssert.assertEquals(Arrays.asList("username1", "username2"), Arrays.asList(actual));
    }

    @Test
    public void testGetSelectedEmployeeUserNames_emptyParam() {
        List<Employee> employees = asList(new Employee("username1"), new Employee("username2"));
        Filter filter = new Filter();
        filter.setEmployees(Collections.emptyList());

        expect(employeeService.getEffortsEmployees(filter)).andReturn(employees);
        expect(filterBean.getCurrentFilter()).andReturn(filter).anyTimes();
        replay(filterBean, employeeService);

        String[] actual = reportsBean.getSelectedEmployeeUserNames();

        verify(filterBean, employeeService);
        Assert.assertArrayEquals(new String[]{"username1", "username2"}, actual);
    }
    
    @Test
    public void testGetSelectedDepartmens() throws NoSuchFieldException {
        String[] expected = new String[]{"Minsk"};
        Filter filter = new Filter();
        filter.setDepartments(asList(expected));
        
        expect(filterBean.getCurrentFilter()).andReturn(filter).times(2);
        filter.getDepartments();
        replay(filterBean);

        String[] actual = reportsBean.getSelectedDepartments();

        verify(filterBean);
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void testGetSelectedDepartmens_emptyParam() throws NoSuchFieldException {
        String[] expected = new String[]{"Minsk", "Vitebsk", ""};
        Filter filter = new Filter();
        filter.setDepartments(Collections.emptyList());
        
        expect(filterBean.getCurrentFilter()).andReturn(filter).times(1);
        expect(departmentService.getAll()).andReturn(asList(expected));
        filter.getDepartments();
        replay(filterBean, departmentService);
        

        String[] actual = reportsBean.getSelectedDepartments();

        verify(filterBean, departmentService);
        ArrayAssert.assertEquivalenceArrays(expected, actual);
    }

    @Test
    public void testGetSelectedProjectIds() throws NoSuchFieldException {
        Project selectedProject = new Project();
        setField(selectedProject, "id", 1L);
        List<Project> projects = asList(selectedProject);
        Filter filter = new Filter();
        filter.setProjects(asList(selectedProject));

        EasyMock.expect(filterBean.getCurrentFilter()).andReturn(filter).anyTimes();
        EasyMock.expect(hoursService.getHours(filter)).andReturn(emptyList());
        EasyMock.expect(projectService.getEffortsProjects(filter)).andReturn(projects);
        EasyMock.replay(hoursService, projectService, filterBean);

        Long[] actual = reportsBean.getSelectedProjectIds();

        EasyMock.verify(hoursService, projectService, filterBean);
        Assert.assertArrayEquals(new Long[]{1L}, actual);
    }

    @Test
    public void testGetSelectedProjectIds_emptyParam() throws NoSuchFieldException {
        Project project1 = new Project();
        setField(project1, "id", 1L);
        Project project2 = new Project();
        setField(project2, "id", 2L);
        List<Project> projects = asList(project1, project2);
        Filter filter = new Filter();
        
        EasyMock.expect(filterBean.getCurrentFilter()).andReturn(filter).anyTimes();
        EasyMock.expect(hoursService.getHours(filter)).andReturn(emptyList());
        EasyMock.expect(projectService.getEffortsProjects(filter)).andReturn(projects);
        EasyMock.replay(hoursService, filterBean, projectService);

        Long[] actual = reportsBean.getSelectedProjectIds();

        verify(hoursService, filterBean, projectService);
        Assert.assertArrayEquals(new Long[]{1L, 2L}, actual);
    }

    @Test
    public void testGetSelectedProjectIds_noProjectsSelected() throws NoSuchFieldException {
        Project activeProject = new Project();
        activeProject.setStatus(Project.Status.ACTIVE);
        setField(activeProject, "id", 1L);
        Project inactiveProject = new Project();
        inactiveProject.setStatus(Project.Status.CLOSED);
        setField(inactiveProject, "id", 2L);
        Project inactiveProjectWithReportedHours = new Project();
        inactiveProjectWithReportedHours.setStatus(Project.Status.CLOSED);
        setField(inactiveProjectWithReportedHours, "id", 3L);
        List<Project> projects = asList(activeProject, inactiveProject, inactiveProjectWithReportedHours);
        Hours reportedHours = new Hours(inactiveProjectWithReportedHours, null, null, null);
        Filter filter = new Filter();

        EasyMock.expect(filterBean.getCurrentFilter()).andReturn(filter).anyTimes();
        EasyMock.expect(hoursService.getHours(filter)).andReturn(asList(reportedHours));
        EasyMock.expect(projectService.getEffortsProjects(filter)).andReturn(projects);
        EasyMock.replay(hoursService, projectService, filterBean);

        Long[] actual = reportsBean.getSelectedProjectIds();

        EasyMock.verify(hoursService, projectService, filterBean);
        Assert.assertArrayEquals(new Long[]{1L, 3L}, actual);
    }

    @Test
    public void testGetSelectedProjectIds_savedFilterWithInactiveProjects() throws NoSuchFieldException {
        Project selectedInactiveProject = new Project();
        setField(selectedInactiveProject, "id", 1L);
        selectedInactiveProject.setStatus(Project.Status.CLOSED);
        Project nonSelectedActiveProject = new Project();
        setField(nonSelectedActiveProject, "id", 2L);
        nonSelectedActiveProject.setStatus(Project.Status.ACTIVE);
        List<Project> selectedProjects = asList(selectedInactiveProject);
        Filter filter = new Filter();
        filter.setName("Filter Name");
        filter.setProjects(asList(selectedInactiveProject));

        EasyMock.expect(filterBean.getCurrentFilter()).andReturn(filter).anyTimes();
        EasyMock.expect(hoursService.getHours(filter)).andReturn(emptyList());
        EasyMock.expect(projectService.getEffortsProjects(filter)).andReturn(selectedProjects);
        EasyMock.replay(hoursService, projectService, filterBean);

        Long[] actual = reportsBean.getSelectedProjectIds();

        EasyMock.verify(hoursService, projectService, filterBean);
        Assert.assertArrayEquals(new Long[]{selectedInactiveProject.getId()}, actual);
    }

    @Test
    public void testGetSelectedHourTypeIds() throws NoSuchFieldException {
        HourType hourType1 = new HourType();
        setField(hourType1, "id",1L);
        HourType hourType2 = new HourType();
        setField(hourType2, "id",2L);
        List<HourType> hourTypes = asList(hourType1, hourType2);
        Filter filter = new Filter();
        filter.setHourTypes(hourTypes);
        
        expect(filterBean.getCurrentFilter()).andReturn(filter).times(2);
        filter.getHourTypes();
        replay(filterBean);

        Long[] actual = reportsBean.getSelectedHourTypeIds();

        verify(filterBean);

        Assert.assertArrayEquals(new Long[]{1L, 2L}, actual);
    }

    @Test
    public void testGetSelectedHourTypeIds_emptyParam_expectAllHourtypes() throws NoSuchFieldException {
        HourType hourType1 = new HourType();
        setField(hourType1, "id",1L);
        HourType hourType2 = new HourType();
        setField(hourType2, "id",2L);
        Filter filter = new Filter();
        filter.setHourTypes(Collections.emptyList());
        
        expect(filterBean.getCurrentFilter()).andReturn(filter).times(1);
        expect(hourTypeService.getAll()).andReturn(asList(hourType1, hourType2));
        filter.getHourTypes();
        replay(filterBean, hourTypeService);

        Long[] actual = reportsBean.getSelectedHourTypeIds();

        verify(filterBean, hourTypeService);

        Assert.assertArrayEquals(new Long[]{1L, 2L}, actual);
    }
    
}
