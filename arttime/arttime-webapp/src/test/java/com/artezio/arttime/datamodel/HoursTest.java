package com.artezio.arttime.datamodel;

import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import static org.junit.Assert.*;

public class HoursTest {
    @Test
    public void testSetQuantity_ifNewQuantityZero() {
        Hours hours = new Hours();
        hours.setQuantity(BigDecimal.ZERO);

        assertNull(hours.getQuantity());
        
        hours = new Hours();
        hours.setQuantity(BigDecimal.valueOf(0.0));
        
        assertNull(hours.getQuantity());
    }
    
    @Test
    public void testSetQuantity_ifNewQuantityIsNotZero() {
        Hours hours = new Hours();

        hours.setQuantity(BigDecimal.ONE);

        assertEquals(BigDecimal.ONE, hours.getQuantity());
    }
    
    @Test
    public void testSetQuantity_ifNewQuantityIsNull() {
        Hours hours = new Hours();

        hours.setQuantity(null);

        assertNull(hours.getQuantity());
    }

    @Test
    public void testToString() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
        Project project = new Project();
        project.setCode("prj");
        Date date = sdf.parse("1-01-2014");
        Employee employee = new Employee("iivanov");
        HourType hourType = new HourType("regular time");
        Hours hours = new Hours(project, date, employee, hourType);
        String expected = "Hours {date=" + df.format(hours.getDate()) + ", employee=iivanov, project=prj, hourType=regular time, quantity=null, approved=false, comment=null}";

        String actual = hours.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void testValidatePositiveQuantity() {
        ValidatorFactory vf = Validation.buildDefaultValidatorFactory();
        Validator validator = vf.getValidator();
        Hours h = new Hours(new Project(), new Date(), new Employee("user"), new HourType("type1"));
        h.setQuantity(BigDecimal.valueOf(1.0));
        Set<ConstraintViolation<Hours>> violations = validator.validate(h);
        vf.close();
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testValidateNegativeQuantity() {
        ValidatorFactory vf = Validation.buildDefaultValidatorFactory();
        Validator validator = vf.getValidator();
        Hours h = new Hours(new Project(), new Date(), new Employee("user"), new HourType("type1"));
        h.setQuantity(BigDecimal.valueOf(-1.0));
        Set<ConstraintViolation<Hours>> violations = validator.validate(h);
        vf.close();
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testAdd_whenInitialQuantityNotNull() throws Exception {
        Hours h = new Hours();
        h.setQuantity(BigDecimal.valueOf(0.2));
        h.add(BigDecimal.valueOf(1.0));
        assertEquals(h.getQuantity(), BigDecimal.valueOf(1.2));
    }

    @Test
    public void testAdd_whenInitialQuantityNull() throws Exception {
        Hours h = new Hours();
        h.add(BigDecimal.valueOf(1.0));
        assertEquals(h.getQuantity(), BigDecimal.valueOf(1.0));
    }

    @Test(expected = Exception.class)
    public void testAdd_whenFinalQuantityIsNegative_expectException() throws Exception {
        Hours h = new Hours();
        h.add(BigDecimal.valueOf(-1.0));
    }

    @Test(expected = Exception.class)
    public void testAdd_whenAlreadyApproved_expectException() throws Exception {
        Hours h = new Hours();
        h.setApproved(true);
        h.add(BigDecimal.valueOf(1.0));
    }

}
