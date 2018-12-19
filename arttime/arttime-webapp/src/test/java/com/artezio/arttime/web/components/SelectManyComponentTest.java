package com.artezio.arttime.web.components;

import static junitx.util.PrivateAccessor.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import junitx.framework.ListAssert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.artezio.arttime.datamodel.Project;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Locale.class)
public class SelectManyComponentTest {
	private SelectManyComponent<Project> selector;
	
	@Before
	public void setUp() {
		selector = new SelectManyComponent<Project>();
	}
		
	@Test
	@SuppressWarnings("unchecked")
	public void testSelectAll() throws NoSuchFieldException {
		Project project1 = createProject(1L);
		Project project2 = createProject(2L);
		Project project3 = createProject(3L);
		List<Project> availableProjects = Arrays.asList(project1, project2, project3);
		List<Project> selectedProjects = new ArrayList<Project>();
		selectedProjects.add(project1);
		setField(selector, "availableItems", availableProjects);
		setField(selector, "selectedItems", selectedProjects);
		PowerMock.mockStatic(Locale.class);
		expect(Locale.getDefault()).andReturn(Locale.US);
		
		selector.selectAll();
		
		List<Project> actual = (List<Project>) getField(selector, "selectedItems");		
		ListAssert.assertEquals(availableProjects, actual);
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testSelectNone() throws NoSuchFieldException {
		Project project1 = createProject(1L);
		Project project2 = createProject(2L);
		Project project3 = createProject(3L);
		List<Project> availableProjects = Arrays.asList(project1, project2, project3);
		List<Project> selectedProjects = new ArrayList<Project>();
		selectedProjects.add(project1);
		setField(selector, "availableItems", availableProjects);
		setField(selector, "selectedItems", selectedProjects);
		PowerMock.mockStatic(Locale.class);
		expect(Locale.getDefault()).andReturn(Locale.US);
		
		selector.selectNone();
		
		List<Project> actual = (List<Project>) getField(selector, "selectedItems");		
		assertTrue(actual.isEmpty());
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testInvertSelection() throws NoSuchFieldException {
		Project project1 = createProject(1L);
		Project project2 = createProject(2L);
		Project project3 = createProject(3L);
		List<Project> availableProjects = Arrays.asList(project1, project2, project3);
		List<Project> selectedProjects = new ArrayList<Project>();
		selectedProjects.add(project1);
		setField(selector, "availableItems", availableProjects);
		setField(selector, "selectedItems", selectedProjects);
		PowerMock.mockStatic(Locale.class);
		expect(Locale.getDefault()).andReturn(Locale.US);
		List<Project> expected = Arrays.asList(project2, project3);
		
		selector.invertSelection();
		
		List<Project> actual = (List<Project>) getField(selector, "selectedItems");		
		ListAssert.assertEquals(expected, actual);
	}

	private Project createProject(Long id) throws NoSuchFieldException {
		Project project = new Project();
		setField(project, "id", id);
		return project;
	}
	
	@Test
	public void testInit() throws Exception {
		List<Project> availableItems = new ArrayList<Project>();
		List<Project> selectedItems = new ArrayList<Project>();
		Map<String, Object> attrs = new HashMap<String, Object>();
		attrs.put("availableItems", availableItems);
		attrs.put("selectedItems", selectedItems);
		selector = createMockBuilder(SelectManyComponent.class)
				.addMockedMethod("getAttributes")
				.createMock();
		expect(selector.getAttributes()).andReturn(attrs).anyTimes();
		replay(selector);
		
		selector.init();
		
		verify(selector);
		assertSame(availableItems, getField(selector, "availableItems"));
		assertSame(selectedItems, getField(selector, "selectedItems"));
		
	}
}
