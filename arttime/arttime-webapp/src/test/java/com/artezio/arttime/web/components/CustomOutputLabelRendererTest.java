package com.artezio.arttime.web.components;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import static junitx.util.PrivateAccessor.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.el.ELContext;
import javax.el.ValueExpression;
import javax.el.ValueReference;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.primefaces.el.ValueExpressionAnalyzer;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ValueExpressionAnalyzer.class})
public class CustomOutputLabelRendererTest {
	private CustomOutputLabelRenderer renderer;
	
	@Before
	public void setUp() {
		renderer = new CustomOutputLabelRenderer();
	}
	
	@Test
	public void testMarkAsRequired_ifTargetComponentHasRequiredAttribute() throws Exception {		
		UIComponent component = createMock(UIComponent.class);
		UIInput targetComponent = createMock(UIInput.class);
		renderer = createMockBuilder(CustomOutputLabelRenderer.class)
				.addMockedMethod("findTargetComponent", UIComponent.class)
				.createMock();
		Map<String, Object> attrs = new HashMap<String, Object>();
		attrs.put("required", true);
		expect(renderer.findTargetComponent(component)).andReturn(targetComponent);
		expect(targetComponent.getAttributes()).andReturn(attrs);
		replay(renderer, targetComponent);
		
		boolean actual = renderer.markAsRequired(component);
		
		verify(renderer, targetComponent);
		assertFalse(actual);
	}
	
	@Test
	public void testMarkAsRequired_ifTargetComponentDefaultRequireness() throws Exception {		
		UIComponent component = createMock(UIComponent.class);
		UIInput targetComponent = createMock(UIInput.class);
		renderer = createMockBuilder(CustomOutputLabelRenderer.class)
				.addMockedMethod("findTargetComponent", UIComponent.class)
				.addMockedMethod("determineRequireness", UIComponent.class)
				.createMock();
		Map<String, Object> attrs = new HashMap<String, Object>();
		attrs.put("required", false);
		expect(renderer.findTargetComponent(component)).andReturn(targetComponent);
		expect(renderer.determineRequireness(targetComponent)).andReturn(true);
		expect(targetComponent.getAttributes()).andReturn(attrs);
		replay(renderer, targetComponent);
		
		boolean actual = renderer.markAsRequired(component);
		
		verify(renderer, targetComponent);
		assertTrue(actual);
	}
	
	@Test
	public void testMarkAsRequired_ifTargetComponentNotFound() throws Exception {		
		UIComponent component = createMock(UIComponent.class);
		renderer = createMockBuilder(CustomOutputLabelRenderer.class)
				.addMockedMethod("findTargetComponent", UIComponent.class)
				.createMock();		
		expect(renderer.findTargetComponent(component)).andReturn(null);
		replay(renderer);
		
		boolean actual = renderer.markAsRequired(component);
		
		verify(renderer);
		assertFalse(actual);
	}
	
	@Test
	public void testDetermineRequireness() throws Exception {
		TestObject testObject = new TestObject();
		ELContext elContext = createMock(ELContext.class);
		setField(renderer, "elContext", elContext);
		UIComponent component = createMock(UIComponent.class);
		ValueExpression valueExpression = createMock(ValueExpression.class);
		ValueReference valueReference = createMock(ValueReference.class);
		PowerMock.mockStatic(ValueExpressionAnalyzer.class);
		expect(component.getValueExpression("value")).andReturn(valueExpression);
		expect(ValueExpressionAnalyzer.getReference(elContext, valueExpression)).andReturn(valueReference);
		expect(valueReference.getBase()).andReturn(testObject);
		expect(valueReference.getProperty()).andReturn("attr_1");
		PowerMock.replayAll(ValueExpressionAnalyzer.class, component, valueReference);
		
		boolean actual = renderer.determineRequireness(component);
		
		PowerMock.verifyAll();
		assertFalse(actual);
	}
	
	@Test
	public void testDetermineRequireness_ifFieldIsNull() throws Exception {
		TestObject testObject = new TestObject();
		ELContext elContext = createMock(ELContext.class);
		setField(renderer, "elContext", elContext);
		UIComponent component = createMock(UIComponent.class);
		ValueExpression valueExpression = createMock(ValueExpression.class);
		ValueReference valueReference = createMock(ValueReference.class);
		PowerMock.mockStatic(ValueExpressionAnalyzer.class);
		expect(component.getValueExpression("value")).andReturn(valueExpression);
		expect(ValueExpressionAnalyzer.getReference(elContext, valueExpression)).andReturn(valueReference);
		expect(valueReference.getBase()).andReturn(testObject);
		expect(valueReference.getProperty()).andReturn("not_existed_attr");
		PowerMock.replayAll(ValueExpressionAnalyzer.class, component, valueReference);
		
		boolean actual = renderer.determineRequireness(component);
		
		PowerMock.verifyAll();
		assertFalse(actual);
	}
	
	@Test
	public void testDetermineRequireness_ifFieldMarkNotNull() throws Exception {
		TestObject testObject = new TestObject();
		ELContext elContext = createMock(ELContext.class);
		setField(renderer, "elContext", elContext);
		UIComponent component = createMock(UIComponent.class);
		ValueExpression valueExpression = createMock(ValueExpression.class);
		ValueReference valueReference = createMock(ValueReference.class);
		PowerMock.mockStatic(ValueExpressionAnalyzer.class);
		expect(component.getValueExpression("value")).andReturn(valueExpression);
		expect(ValueExpressionAnalyzer.getReference(elContext, valueExpression)).andReturn(valueReference);
		expect(valueReference.getBase()).andReturn(testObject);
		expect(valueReference.getProperty()).andReturn("notNullAttr");
		PowerMock.replayAll(ValueExpressionAnalyzer.class, component, valueReference);
		
		boolean actual = renderer.determineRequireness(component);
		
		PowerMock.verifyAll();
		assertTrue(actual);
	}
	
	@Test
	public void testDetermineRequireness_ifGetterNotFound() throws Exception {
		TestObject testObject = new TestObject();
		ELContext elContext = createMock(ELContext.class);
		setField(renderer, "elContext", elContext);
		UIComponent component = createMock(UIComponent.class);
		ValueExpression valueExpression = createMock(ValueExpression.class);
		ValueReference valueReference = createMock(ValueReference.class);
		PowerMock.mockStatic(ValueExpressionAnalyzer.class);
		expect(component.getValueExpression("value")).andReturn(valueExpression);
		expect(ValueExpressionAnalyzer.getReference(elContext, valueExpression)).andReturn(valueReference);
		expect(valueReference.getBase()).andReturn(testObject);
		expect(valueReference.getProperty()).andReturn("attr_2");
		PowerMock.replayAll(ValueExpressionAnalyzer.class, component, valueReference);
		
		boolean actual = renderer.determineRequireness(component);
		
		PowerMock.verifyAll();
		assertFalse(actual);
	}
	
	@Test
	public void testDetermineRequireness_ifGetterMarkNotBlank() throws Exception {
		TestObject testObject = new TestObject();
		ELContext elContext = createMock(ELContext.class);
		setField(renderer, "elContext", elContext);
		UIComponent component = createMock(UIComponent.class);
		ValueExpression valueExpression = createMock(ValueExpression.class);
		ValueReference valueReference = createMock(ValueReference.class);
		PowerMock.mockStatic(ValueExpressionAnalyzer.class);
		expect(component.getValueExpression("value")).andReturn(valueExpression);
		expect(ValueExpressionAnalyzer.getReference(elContext, valueExpression)).andReturn(valueReference);
		expect(valueReference.getBase()).andReturn(testObject);
		expect(valueReference.getProperty()).andReturn("notBlankAttr");
		PowerMock.replayAll(ValueExpressionAnalyzer.class, component, valueReference);
		
		boolean actual = renderer.determineRequireness(component);
		
		PowerMock.verifyAll();
		assertTrue(actual);
	}
	
	@Test
	public void testDetermineRequireness_ifValueReferenceIsNull() throws Exception {
		ELContext elContext = createMock(ELContext.class);
		setField(renderer, "elContext", elContext);
		UIComponent component = createMock(UIComponent.class);
		ValueExpression valueExpression = createMock(ValueExpression.class);
		PowerMock.mockStatic(ValueExpressionAnalyzer.class);
		expect(component.getValueExpression("value")).andReturn(valueExpression);
		expect(ValueExpressionAnalyzer.getReference(elContext, valueExpression)).andReturn(null);		
		PowerMock.replayAll(ValueExpressionAnalyzer.class, component);
		
		boolean actual = renderer.determineRequireness(component);
		
		PowerMock.verifyAll();
		assertFalse(actual);
	}
	
	@Test
	public void testDetermineRequireness_ifValueExpressionIsNull() throws Exception {
		ELContext elContext = createMock(ELContext.class);
		setField(renderer, "elContext", elContext);
		UIComponent component = createMock(UIComponent.class);		
		expect(component.getValueExpression("value")).andReturn(null);				
		replay(component);
		
		boolean actual = renderer.determineRequireness(component);
		
		verify(component);
		assertFalse(actual);
	}
	
	@Test
	public void testHasNotEmptyRestriction_ifNotEmpty() throws Exception {
		Field field = TestObject.class.getDeclaredField("notEmptyAttr");
		
		boolean actual = renderer.hasNotEmptyRestriction(field);
		
		assertTrue(actual);
	}
	
	@Test
	public void testHasNotEmptyRestriction_ifNotBlank() throws Exception {
		Method method = TestObject.class.getMethod("getNotBlankAttr");
		
		boolean actual = renderer.hasNotEmptyRestriction(method);
		
		assertTrue(actual);
	}
	
	@Test
	public void testHasNotEmptyRestriction_ifNotNull() throws Exception {
		Field field = TestObject.class.getDeclaredField("notNullAttr");
		
		boolean actual = renderer.hasNotEmptyRestriction(field);
		
		assertFalse(actual);
	}
	
	@Test
	public void testFindGetter() throws Exception {
		TestObject testObject = new TestObject();
		Method expected = TestObject.class.getDeclaredMethod("getNotBlankAttr");
		
		Method actual = renderer.findGetter(testObject, "notBlankAttr");
		
		assertEquals(expected, actual);
	}
	
	@Test
	public void testFindTargetComponent() {
		renderer = createMockBuilder(CustomOutputLabelRenderer.class)
				.addMockedMethod("findComponentInRoot", String.class)
				.createMock();
		UIInput input = createMock(UIInput.class);
		UIComponent component = createMock(UIComponent.class);
		Map<String, Object> attrs = new HashMap<String, Object>();
		attrs.put("for", "inputId");
		expect(component.getAttributes()).andReturn(attrs);
		expect(renderer.findComponentInRoot("inputId")).andReturn(input);
		replay(component, renderer);
		
		UIInput actual = renderer.findTargetComponent(component);
		
		verify(component, renderer);
		assertSame(input, actual);
	}
	
	@Test
	public void testFindTargetComponent_ifForAttributeIsNull() {		
		UIComponent component = createMock(UIComponent.class);
		Map<String, Object> attrs = new HashMap<String, Object>();
		attrs.put("for", null);
		expect(component.getAttributes()).andReturn(attrs);		
		replay(component);
		
		UIInput actual = renderer.findTargetComponent(component);
		
		verify(component);
		assertNull(actual);
	}
	
	@Test
	public void testFindComponentInRoot() throws Exception {
		renderer = createMockBuilder(CustomOutputLabelRenderer.class)
				.addMockedMethod("findComponent", UIComponent.class, String.class)
				.createMock();
		FacesContext facesContext = createMock(FacesContext.class);
		UIComponent component = createMock(UIComponent.class);
		UIViewRoot viewRoot = createMock(UIViewRoot.class);
		setField(renderer, "facesContext", facesContext);
		expect(facesContext.getViewRoot()).andReturn(viewRoot);
		expect(renderer.findComponent(viewRoot, "id")).andReturn(component);
		replay(facesContext, renderer);
		
		UIComponent actual = renderer.findComponentInRoot("id");
		
		verify(facesContext, renderer);
		assertSame(component, actual);
	}
	
	@Test
	public void testFindComponent() {
		UIComponent component = createMock(UIComponent.class);
		UIComponent result = createMock(UIComponent.class);
		Iterator<UIComponent> iterator = createMock(Iterator.class);
		expect(component.getId()).andReturn("rootId");
		expect(component.getFacetsAndChildren()).andReturn(iterator);
		expect(iterator.hasNext()).andReturn(true).anyTimes();
		expect(iterator.next()).andReturn(result);
		expect(result.getId()).andReturn("id");
		replay(component, iterator, result);
		
		UIComponent actual = renderer.findComponent(component, "id");
		
		verify(component, iterator, result);
		assertSame(result, actual);
	}		
	
	class TestObject {
		private String attr_1;
		private String attr_2;
		@NotNull
		private String notNullAttr;
		private String notBlankAttr;
		@NotEmpty
		private String notEmptyAttr;

		public String getAttr_1() {
			return attr_1;
		}

		public void setAttr_1(String attr_1) {
			this.attr_1 = attr_1;
		}

		@NotBlank
		public String getNotBlankAttr() {
			return notBlankAttr;
		}

		public void setNotBlankAttr(String notBlankAttr) {
			this.notBlankAttr = notBlankAttr;
		}
		
		
	}
}
