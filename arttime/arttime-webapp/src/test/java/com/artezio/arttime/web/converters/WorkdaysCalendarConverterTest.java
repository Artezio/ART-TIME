package com.artezio.arttime.web.converters;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.WorkdaysCalendar;
import com.artezio.arttime.repositories.WorkdaysCalendarRepository;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import static junitx.util.PrivateAccessor.setField;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

@RunWith(EasyMockRunner.class)
public class WorkdaysCalendarConverterTest {
    @TestSubject
    private WorkdaysCalendarConverter converter = new WorkdaysCalendarConverter();
    @Mock
    private UIComponent component;
    @Mock
    private FacesContext facesContext;
    @Mock
    private WorkdaysCalendarRepository workdaysCalendarRepository;

    @Test
    public void testGetAsObject_ifFoundInRepository() throws Exception {
        WorkdaysCalendar calendar = new WorkdaysCalendar();
        expect(workdaysCalendarRepository.findWorkdaysCalendar(1L)).andReturn(calendar);
        replay(workdaysCalendarRepository);

        Object actual = converter.getAsObject(facesContext, component, "1");

        verify(workdaysCalendarRepository);
        assertSame(calendar, actual);
    }

    @Test
    public void testGetAsObject_ifNotFound() throws Exception {
        expect(workdaysCalendarRepository.findWorkdaysCalendar(1L)).andReturn(null);
        replay(workdaysCalendarRepository);

        Object actual = converter.getAsObject(facesContext, component, "1");

        verify(workdaysCalendarRepository);
        assertNull(actual);
    }

    @Test
    public void testGetAsObject_ifNull() throws Exception {
        Object actual = converter.getAsObject(facesContext, component, null);
        assertNull(actual);
    }

    @Test
    public void testGetAsString_ifWorkdaysCalendar() throws Exception {
        WorkdaysCalendar workdaysCalendar = new WorkdaysCalendar();
        setField(workdaysCalendar, "id", 1L);

        String actual = converter.getAsString(facesContext, component, workdaysCalendar);

        assertEquals("1", actual);
    }

    @Test
    public void testGetAsString_ifNotWorkdaysCalendar() {
        Employee employee = new Employee();

        String actual = converter.getAsString(facesContext, component, employee);

        assertNull(actual);
    }

    @Test
    public void testGetAsString_ifNull() {
        String actual = converter.getAsString(facesContext, component, null);
        assertEquals("", actual);
    }
}
