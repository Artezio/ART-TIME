package com.artezio.arttime.web.converters;

import static junitx.util.PrivateAccessor.setField;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.artezio.arttime.datamodel.HourType;
import com.artezio.arttime.datamodel.Project;
import com.artezio.arttime.repositories.HourTypeRepository;
import org.mockito.Mockito;

@RunWith(EasyMockRunner.class)
public class HourTypeConverterTest {
	private HourTypeConverter converter;
	@Mock
	private UIComponent component;
	@Mock
	private FacesContext facesContext;
	@Mock
	private HourTypeRepository hourTypeRepository;	
	
	@Before
	public void setUp() {
		converter = new HourTypeConverter();
	}
	
	@Test
	public void testGetAsObject_ifFoundInRepository() throws Exception {
		setField(converter, "hourTypeRepository", hourTypeRepository);
		HourType hourType = new HourType();

		expect(hourTypeRepository.find(1L)).andReturn(hourType);
		replay(hourTypeRepository);

		Object actual = converter.getAsObject(facesContext, component, "1");
		
		EasyMock.verify(hourTypeRepository);

		assertSame(hourType, actual);
	}	
	
	@Test
	public void testGetAsObject_ifNotFound() throws Exception {
		setField(converter, "hourTypeRepository", hourTypeRepository);

		EasyMock.expect(hourTypeRepository.find(1L)).andReturn(null);
		replay(hourTypeRepository);
		
		Object actual = converter.getAsObject(facesContext, component, "1");
		
		EasyMock.verify(hourTypeRepository);

		assertNull(actual);
	}
	
	@Test
	public void testGetAsObject_ifNull() {
		Object actual = converter.getAsObject(facesContext, component, null);
		
		assertNull(actual);
	}
	
	@Test
	public void testGetAsString_ifHourType() throws Exception {
		HourType hourType = new HourType();
		setField(hourType, "id", 1L);
		
		String actual = converter.getAsString(facesContext, component, hourType);
		
		assertEquals("1", actual);
	}
	
	@Test
	public void testGetAsString_ifNotHourType() {
		Project project = new Project();
		
		String actual = converter.getAsString(facesContext, component, project);
		
		assertNull(actual);
	}
}
