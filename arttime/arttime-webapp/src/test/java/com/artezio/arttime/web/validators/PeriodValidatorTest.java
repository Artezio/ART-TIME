package com.artezio.arttime.web.validators;

import static junitx.util.PrivateAccessor.setField;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.validation.ConstraintViolation;
import javax.validation.MessageInterpolator;
import javax.validation.Validator;
import javax.validation.ValidatorContext;
import javax.validation.ValidatorFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.omnifaces.util.Components;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({FacesContext.class, Components.class})
public class PeriodValidatorTest {

    private PeriodValidator periodValidator = new PeriodValidator();
    private UIInput input;
    private Validator validator;
    private UIComponent component;
    private FacesContext facesContext;
    private ValidatorFactory validatorFactory;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
    
    @Before
    public void setUp() throws Exception {
        input = PowerMock.createMock(UIInput.class);
        validator = PowerMock.createMock(Validator.class);
        component = PowerMock.createMock(UIComponent.class);
        facesContext = PowerMock.createMock(FacesContext.class);
        validatorFactory = PowerMock.createMock(ValidatorFactory.class);
        setField(periodValidator, "validatorFactory", validatorFactory);
    }
    
    @Test(expected = ValidatorException.class)
    public void testValidate_ifNotValid() throws Exception {
        Date date1 = sdf.parse("1-01-2015");
        Date date2 = sdf.parse("31-01-2015");
        ConstraintViolation<Object> violation = createMock(ConstraintViolation.class);
        Set<ConstraintViolation<Object>> violations = new HashSet<ConstraintViolation<Object>>();
        violations.add(violation);
        periodValidator = createMockBuilder(PeriodValidator.class)
                .addMockedMethod("getStartComponent", UIComponent.class)
                .addMockedMethod("createBeanValidator", FacesContext.class)
                .addMockedMethod("showErrorsForStart", UIComponent.class, UIComponent.class, List.class)
                .createMock();
        expect(periodValidator.getStartComponent(component)).andReturn(input);
        expect(input.getValue()).andReturn(date1);
        expect(periodValidator.createBeanValidator(facesContext)).andReturn(validator);
        expect(validator.validate(anyObject())).andReturn(violations);
        expect(violation.getMessage()).andReturn("Validation error!!!");
        input.setValid(false);
        periodValidator.showErrorsForStart(eq(input), eq(component), anyObject(List.class));
        PowerMock.replayAll(periodValidator);

        periodValidator.validate(facesContext, component, date2);
        
        PowerMock.verifyAll();
    }
    
    @Test
    public void testValidate_ifValid() throws Exception {
        Date date1 = sdf.parse("1-01-2015");
        Date date2 = sdf.parse("31-01-2015");
        Set<ConstraintViolation<Object>> violations = new HashSet<ConstraintViolation<Object>>();
        periodValidator = createMockBuilder(PeriodValidator.class)
                .addMockedMethod("getStartComponent", UIComponent.class)
                .addMockedMethod("createBeanValidator", FacesContext.class)
                .createMock();
        expect(periodValidator.getStartComponent(component)).andReturn(input);
        expect(input.getValue()).andReturn(date1);
        expect(periodValidator.createBeanValidator(facesContext)).andReturn(validator);
        expect(validator.validate(anyObject())).andReturn(violations);
        PowerMock.replayAll(periodValidator);

        periodValidator.validate(facesContext, component, date2);

        PowerMock.verifyAll();
    }

    @Test
    public void testShowErrorsForStart_ifShowErrorsForStartIsNull() {
        FacesMessage facesMessage = new FacesMessage();
        List<FacesMessage> messages = new ArrayList<FacesMessage>();
        messages.add(facesMessage);
        Map<String, Object> attrs = new HashMap<String, Object>();
        attrs.put("showErrorsForStart", null);
        PowerMock.mockStatic(FacesContext.class);
        expect(component.getAttributes()).andReturn(attrs);
        expect(input.getClientId()).andReturn("id");
        expect(FacesContext.getCurrentInstance()).andReturn(facesContext);
        facesContext.addMessage("id", facesMessage);
        PowerMock.replayAll();

        periodValidator.showErrorsForStart(input, component, messages);

        PowerMock.verifyAll();
    }

    @Test
    public void testShowErrorsForStart_ifShowErrorsForStartIsTrue() {
        FacesMessage facesMessage = new FacesMessage();
        List<FacesMessage> messages = new ArrayList<>();
        messages.add(facesMessage);
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("showErrorsForStart", true);
        expect(component.getAttributes()).andReturn(attrs);
        expect(input.getClientId()).andReturn("id");
        PowerMock.mockStatic(FacesContext.class);
        expect(FacesContext.getCurrentInstance()).andReturn(facesContext);
        facesContext.addMessage("id", facesMessage);
        PowerMock.replayAll(FacesContext.class);

        periodValidator.showErrorsForStart(input, component, messages);

        PowerMock.verifyAll();
    }

    @Test
    public void testShowErrorsForStart_ifShowErrorsForStartIsFalse() {
        FacesMessage facesMessage = new FacesMessage();
        List<FacesMessage> messages = new ArrayList<FacesMessage>();
        messages.add(facesMessage);
        Map<String, Object> attrs = new HashMap<String, Object>();
        attrs.put("showErrorsForStart", false);
        expect(component.getAttributes()).andReturn(attrs);
        PowerMock.replayAll();

        periodValidator.showErrorsForStart(input, component, messages);
        
        PowerMock.verifyAll();
    }

    @Test
    public void testCreateBeanValidator() {
        ValidatorContext validatorContext = createMock(ValidatorContext.class);
        MessageInterpolator messageInterpolator = createMock(MessageInterpolator.class);
        expect(validatorFactory.usingContext()).andReturn(validatorContext);
        expect(validatorFactory.getMessageInterpolator()).andReturn(messageInterpolator);
        expect(validatorContext.messageInterpolator(anyObject())).andReturn(validatorContext);
        expect(validatorContext.getValidator()).andReturn(validator);
        PowerMock.replayAll(validatorContext);

        Validator actual = periodValidator.createBeanValidator(facesContext);
        
        PowerMock.verifyAll();
        assertNotNull(actual);
    }

    @Test
    public void testGetStartComponent() {
        Map<String, Object> attrs = new HashMap<String, Object>();
        attrs.put("start", "startId");
        expect(component.getAttributes()).andReturn(attrs);
        PowerMock.mockStatic(Components.class);
        expect(Components.findComponentRelatively(component, "startId")).andReturn(input);
        PowerMock.replayAll();

        UIInput actual = periodValidator.getStartComponent(component);
        
        PowerMock.verifyAll();
        assertSame(input, actual);
    }
}
