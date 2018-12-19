package com.artezio.arttime.web.components;

import org.apache.commons.beanutils.PropertyUtils;
import org.primefaces.component.outputlabel.OutputLabelRenderer;
import org.primefaces.el.ValueExpressionAnalyzer;

import javax.el.ELContext;
import javax.el.ValueExpression;
import javax.el.ValueReference;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.inject.Inject;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;

public class CustomOutputLabelRenderer extends OutputLabelRenderer {
	@Inject
	private ELContext elContext;
	@Inject
	private FacesContext facesContext;	
	
	@Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
		super.encodeEnd(context, component);
		if (markAsRequired(component)) {
			ResponseWriter writer = context.getResponseWriter();
			writer.writeText(" *", "value");
		}		
	}
	
	protected boolean markAsRequired(UIComponent outputLabel) {		
		UIComponent targetComponent = findTargetComponent(outputLabel);
		if (targetComponent != null) {			
			return (boolean) (targetComponent.getAttributes().get("required"))
					? false
					: determineRequireness(targetComponent);			
		}
		return false;						
	}
	
	protected boolean determineRequireness(UIComponent input) {
		ValueExpression valueExpression = input.getValueExpression("value");
		if (valueExpression != null) {			
			ValueReference valueReference = ValueExpressionAnalyzer.getReference(elContext, valueExpression);
			if (valueReference != null) {
				Object base = valueReference.getBase();
		        Object property = valueReference.getProperty();
		        Field field = findField(base.getClass(), property); 
		        Method getter = findGetter(base, property);		
		        return (field != null && isRequired(field))
		        		|| (getter != null && isRequired(getter));
			}
		}
		return false;
	}

	protected boolean isRequired(AnnotatedElement annotatedElement) {
		return hasNotNullRestriction(annotatedElement) || hasNotEmptyRestriction(annotatedElement);
	}
	
	protected boolean hasNotEmptyRestriction(AnnotatedElement annotatedElement) {
		for (Annotation annotation : annotatedElement.getAnnotations()) {
        	if ((annotation instanceof NotEmpty)
        	 || (annotation instanceof NotBlank)) {
        		return true;
        	}
        }
		return false;
	}

	protected boolean hasNotNullRestriction(AnnotatedElement annotatedElement) {
		for (Annotation annotation : annotatedElement.getAnnotations()) {
        	if (annotation instanceof NotNull) {
        		return true;
        	}                	
        }
		return false;
	}

	protected Method findGetter(Object base, Object property) {
		try {
			PropertyDescriptor descriptor = PropertyUtils.getPropertyDescriptor(base, property.toString());
			return (descriptor == null)
					? null
					: descriptor.getReadMethod();
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			return null;
		}		
	}		

	protected Field findField(Class<? extends Object> baseClass, Object property) {
		try {
			return baseClass.getDeclaredField((String) property);
		} catch (NoSuchFieldException | SecurityException e) {
			return null;
		}
	}

	protected UIInput findTargetComponent(UIComponent outputLabel) {		
		String inputId = (String) outputLabel.getAttributes().get("for");
		return (inputId == null)
				? null
				: (UIInput)findComponentInRoot(inputId);
	}	
	
	protected UIComponent findComponentInRoot(String id) {
		UIComponent root = facesContext.getViewRoot();
		return findComponent(root, id);	    
	}
	
	protected UIComponent findComponent(UIComponent base, String id) {
		if (id.equals(base.getId())) return base;	  
	    UIComponent child = null;
	    UIComponent result = null;
	    Iterator<UIComponent> childs = base.getFacetsAndChildren();
	    while (childs.hasNext() && result == null) {
	    	child = (UIComponent) childs.next();
	    	result = (id.equals(child.getId())) 
	    		? child
	    		: findComponent(child, id);	    	
	    }
	    return result;
	}			
}
