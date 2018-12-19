package com.artezio.arttime.filter;

import com.artezio.arttime.datamodel.Period;
import com.artezio.arttime.datamodel.Project;
import com.artezio.arttime.web.criteria.RangePeriodSelector;
import com.artezio.arttime.web.criteria.RangePeriodSelector.Range;
import com.ibm.icu.text.SimpleDateFormat;
import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class FilterTest {
	private Filter filter;
	private SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
	
	@Before
	public void setUp() {
		filter = new Filter();		
	}
	
	@Test
	public void testSetCustomPeriod() throws ParseException {
		RangePeriodSelector rangePeriodSelector = new RangePeriodSelector();
		rangePeriodSelector.setMonthPeriod();
		filter.setRangePeriodSelector(rangePeriodSelector);
		Period period = new Period(sdf.parse("13-01-2015"), sdf.parse("15-01-2015"));
		
		filter.setCustomPeriod(period);
		
		assertEquals(Range.CUSTOM, rangePeriodSelector.getRange());		
		assertSame(period.getStart(), rangePeriodSelector.getPeriod().getStart());
		assertSame(period.getFinish(), rangePeriodSelector.getPeriod().getFinish());
	}
	
	@Test
	public void testToString() {
		filter.setOwner("ownerName");
		filter.setName("favorite filter");
		String expected = "Filter {name=favorite filter, owner=ownerName}";
		
		String actual = filter.toString();
				
		assertEquals(expected, actual);
	}
	
	@Test
	public void testGetMasterProjects() {
	    Project master = createProject(1L);
	    Project sub = createProject(2L);
	    sub.setMaster(master);
	    filter.setProjects(Arrays.asList(master, sub));
	    
	    List<Project> actual = filter.getMasterProjects();
	    
	    assertTrue(actual.contains(master));
	    assertFalse(actual.contains(sub));
	}

    private Project createProject(Long id) {
        Project p = new Project();
        Whitebox.setInternalState(p, "id", id);
        return p;
    }
}
