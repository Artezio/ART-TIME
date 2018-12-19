package com.artezio.arttime.web.converters;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.Project;
import com.artezio.arttime.repositories.ProjectRepository;
import com.artezio.arttime.services.ProjectService;
import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import static junitx.util.PrivateAccessor.setField;
import static org.junit.Assert.*;

@RunWith(EasyMockRunner.class)
public class ProjectConverterTest {

    @TestSubject
    private ProjectConverter converter = new ProjectConverter();
    @Mock
    private UIComponent component;
    @Mock
    private FacesContext facesContext;
    @Mock
    private ProjectService projectService;
    @Mock
    private ProjectRepository projectRepository;

    @Test
    public void testGetAsObject() {
        final Long id = 1L;
        Project project = new Project();

        EasyMock.expect(projectRepository.findById(id)).andReturn(project);
        EasyMock.replay(projectRepository);

        Object actual = converter.getAsObject(facesContext, component, id.toString());

        EasyMock.verify(projectRepository);
        assertSame(project, actual);
    }

    @Test
    public void testGetAsObject_ifNotFound() {
        final Long id = 1L;

        EasyMock.expect(projectRepository.findById(id)).andReturn(null);
        EasyMock.replay(projectRepository);

        Object actual = converter.getAsObject(facesContext, component, id.toString());

        EasyMock.verify(projectRepository);
        assertNull(actual);
    }

    @Test
    public void testGetAsObject_ifNull() {
        Object actual = converter.getAsObject(facesContext, component, null);

        assertNull(actual);
    }

    @Test
    public void testGetAsString_ifProject() throws Exception {
        Project project = new Project();
        setField(project, "id", 1L);

        String actual = converter.getAsString(facesContext, component, project);

        assertEquals("1", actual);
    }

    @Test
    public void testGetAsString_ifNotProject() {
        Employee employee = new Employee();

        String actual = converter.getAsString(facesContext, component, employee);

        assertNull(actual);
    }

}
