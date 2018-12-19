package com.artezio.arttime.datamodel;

import org.junit.BeforeClass;
import org.junit.Test;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import static org.junit.Assert.*;

public class TeamFilterTest {
	private static Validator validator;

    @BeforeClass
    public static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testValidation() {
        TeamFilter teamFilter1 = new TeamFilter(TeamFilter.Type.DISABLED, null);
        TeamFilter teamFilter2 = new TeamFilter(TeamFilter.Type.DEPARTMENTS, "");
        TeamFilter teamFilter3 = new TeamFilter(TeamFilter.Type.PROJECT_CODES, null);
        TeamFilter teamFilter4 = new TeamFilter(TeamFilter.Type.DEPARTMENTS, "value");

        assertEquals(0, validator.validate(teamFilter1).size());
        assertEquals(0, validator.validate(teamFilter2).size());
        assertEquals(1, validator.validate(teamFilter3).size());
        assertEquals(0, validator.validate(teamFilter4).size());        
    }

    @Test
    public void testIsValid() {
    	TeamFilter teamFilter1 = new TeamFilter(TeamFilter.Type.DISABLED, null);
        TeamFilter teamFilter2 = new TeamFilter(TeamFilter.Type.DEPARTMENTS, "");
        TeamFilter teamFilter3 = new TeamFilter(TeamFilter.Type.PROJECT_CODES, null);
        TeamFilter teamFilter4 = new TeamFilter(TeamFilter.Type.DEPARTMENTS, "value");
        TeamFilter teamFilter5 = new TeamFilter(TeamFilter.Type.BASED_ON_MASTER, null);

        assertTrue(teamFilter1.isValid());
        assertTrue(teamFilter2.isValid());
        assertFalse(teamFilter3.isValid());
        assertTrue(teamFilter4.isValid());
        assertTrue(teamFilter5.isValid());
    }
    
    @Test
    public void testToString() {
    	TeamFilter teamFilter = new TeamFilter(TeamFilter.Type.PROJECT_CODES, "ANY_LDAP_GROUP");
    	String expected = "TeamFilter {type=PROJECT_CODES, value=ANY_LDAP_GROUP}";
    	
    	String actual = teamFilter.toString();
    	
    	assertEquals(expected, actual);
    }
}
