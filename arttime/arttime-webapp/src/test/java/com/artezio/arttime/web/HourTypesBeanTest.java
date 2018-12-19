package com.artezio.arttime.web;

import static junitx.util.PrivateAccessor.getField;
import static junitx.util.PrivateAccessor.setField;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.ExternalContext;

import com.artezio.arttime.services.HourTypeService;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.artezio.arttime.datamodel.HourType;
import com.artezio.arttime.exceptions.ActualTimeRemovalException;
import com.artezio.arttime.repositories.HourTypeRepository;

@RunWith(EasyMockRunner.class)
public class HourTypesBeanTest {
	private HourTypesBean bean;
	@Mock
	private ExternalContext externalContext;
	@Mock
	private HourTypeService hourTypeService;

	@Before
	public void setUp() throws NoSuchFieldException {
		bean = new HourTypesBean();				
	}

	@Test
	public void testCreate() throws NoSuchFieldException {
		setField(bean, "hourTypeService", hourTypeService);
		HourType hourType = new HourType();
		expect(hourTypeService.create(hourType)).andReturn(hourType);
		replay(hourTypeService);

		bean.create(hourType);

		verify(hourTypeService);
	}

	@Test
	public void testUpdate() throws NoSuchFieldException {
		setField(bean, "hourTypeService", hourTypeService);
		HourType hourType = new HourType();
		expect(hourTypeService.update(hourType)).andReturn(hourType);
		replay(hourTypeService);

		bean.update(hourType);

		verify(hourTypeService);
	}

	@Test
	public void testRemove() throws ActualTimeRemovalException, NoSuchFieldException {
		setField(bean, "hourTypeService", hourTypeService);
		HourType hourType = new HourType();
		hourTypeService.remove(hourType);
		replay(hourTypeService);

		bean.remove(hourType);

		verify(hourTypeService);
	}

	@Test
	public void testAddNew() throws NoSuchFieldException {
		setField(bean, "hourType", null);

		bean.addNew();

		HourType actual = (HourType) getField(bean, "hourType");
		assertNotNull(actual);
	}
	
	@Test 
    public void testGetHourTypes_ifNotNull() throws NoSuchFieldException {
    	List<HourType> hourTypes = new ArrayList<HourType>(); 
    	setField(bean, "hourTypes", hourTypes);
    	
    	List<HourType> actual = bean.getHourTypes();
    	
    	assertSame(hourTypes, actual);
    }
    
    @Test 
    public void testGetHourTypes_ifNull() throws NoSuchFieldException {    	
    	setField(bean, "hourTypes", null);
    	setField(bean, "hourTypeService", hourTypeService);
    	List<HourType> calendars = new ArrayList<>();
    	expect(hourTypeService.getAll()).andReturn(calendars);
		replay(hourTypeService);
    	
    	List<HourType> actual = bean.getHourTypes();
    	
    	assertSame(calendars, actual);
    }
    
    @Test
    public void testInit() throws NoSuchFieldException {
    	setField(bean, "externalContext", externalContext);
    	setField(bean, "hourTypeService", hourTypeService);
    	HourType hourType = new HourType();
    	Map<String, String> requestParams = new HashMap<String, String>();
    	requestParams.put("hourType", "1");
    	expect(externalContext.getRequestParameterMap()).andReturn(requestParams);
    	expect(hourTypeService.find(1L)).andReturn(hourType);
    	replay(externalContext, hourTypeService);
    	
    	bean.init();
    	
    	HourType actual = (HourType) getField(bean, "hourType");
    	verify(externalContext, hourTypeService);
    	assertSame(hourType, actual);
    }
    
    @Test
    public void testInit_ifHourTypeNotPassAsParam() throws NoSuchFieldException {
    	setField(bean, "externalContext", externalContext);
    	Map<String, String> requestParams = new HashMap<String, String>();
    	expect(externalContext.getRequestParameterMap()).andReturn(requestParams);
    	replay(externalContext);
    	
    	bean.init();
    	
    	HourType actual = (HourType) getField(bean, "hourType");
    	verify(externalContext);
    	assertNotNull(actual);
    }
    
    @Test
    public void testSetActualTime() throws NoSuchFieldException {
    	HourType hourType = new HourType();
    	setField(bean, "hourTypes", new ArrayList<HourType>());
    	setField(bean, "hourTypeService", hourTypeService);
    	expect(hourTypeService.setActualTime(hourType)).andReturn(hourType);
    	replay(hourTypeService);
    	
    	bean.setActualTime(hourType);
    	
    	verify(hourTypeService);
    	assertNull(getField(bean, "hourTypes"));
    }
    
    @Test
    public void testCompare() {
    	HourType hourType1 = createHourType("Regular time", 10, true);
    	HourType hourType2 = createHourType("Workday overtime", 5, false);
    	HourType hourType3 = createHourType("Weekend overtime", 6, false);
    	
    	assertEquals(0, bean.compare(hourType1, hourType1));
    	assertEquals(-1, bean.compare(hourType1, hourType2));
    	assertEquals(-1, bean.compare(hourType1, hourType3));
    	assertEquals(1, bean.compare(hourType2, hourType1));
    	assertEquals(0, bean.compare(hourType2, hourType2));
    	assertEquals(1, bean.compare(hourType2, hourType3));
    	assertEquals(1, bean.compare(hourType3, hourType1));
    	assertEquals(-1, bean.compare(hourType3, hourType2));
    	assertEquals(0, bean.compare(hourType3, hourType3));
    }

	private HourType createHourType(String type, int priority, boolean actualTime) {
		HourType hourType = new HourType();
		hourType.setType(type);
		hourType.setPriority(priority);
		hourType.setActualTime(actualTime);
		return hourType;
	}
}
