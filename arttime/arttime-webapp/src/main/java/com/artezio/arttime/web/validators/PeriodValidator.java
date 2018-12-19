package com.artezio.arttime.web.validators;

import com.artezio.arttime.datamodel.Period;
import org.omnifaces.util.Components;
import org.omnifaces.util.Messages;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.MessageInterpolator;
import javax.validation.ValidatorContext;
import javax.validation.ValidatorFactory;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@FacesValidator
public class PeriodValidator implements Validator {

    @Inject
    private ValidatorFactory validatorFactory;

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        UIInput startComponent = getStartComponent(component);
        Period period = new Period((Date) startComponent.getValue(), (Date) value);

        Set<ConstraintViolation<Object>> violations = createBeanValidator(context).validate(period);

        if (!violations.isEmpty()) {
            List<FacesMessage> messages = violations.stream().map(ConstraintViolation::getMessage)
                    .map(Messages::createError).collect(Collectors.toList());
            startComponent.setValid(false);
            showErrorsForStart(startComponent, component, messages);
            throw new ValidatorException(messages);
        }
    }

    protected void showErrorsForStart(UIComponent start, UIComponent finish, List<FacesMessage> messages) {
        Object showErrors = finish.getAttributes().get("showErrorsForStart");
        if (showErrors == null || Boolean.parseBoolean(showErrors.toString())) {
            messages.forEach(message -> Messages.add(start.getClientId(), message));
        }
    }

    protected javax.validation.Validator createBeanValidator(FacesContext context) {
        ValidatorContext validatorContext = validatorFactory.usingContext();
        MessageInterpolator messageInterpolator = validatorFactory.getMessageInterpolator();
        validatorContext.messageInterpolator(new JsfAwareMessageInterpolator(context, messageInterpolator));
        return validatorContext.getValidator();
    }

    protected UIInput getStartComponent(UIComponent finishComponent) {
        String startId = (String) finishComponent.getAttributes().get("start");
        return Components.findComponentRelatively(finishComponent, startId);
    }

    private static class JsfAwareMessageInterpolator implements MessageInterpolator {
        private FacesContext context;
        private MessageInterpolator delegate;

        public JsfAwareMessageInterpolator(FacesContext context, MessageInterpolator delegate) {
            this.context = context;
            this.delegate = delegate;
        }

        public String interpolate(String message, MessageInterpolator.Context context) {
            Locale locale = this.context.getViewRoot().getLocale();
            if (locale == null) {
                locale = Locale.getDefault();
            }
            return delegate.interpolate(message, context, locale);
        }

        public String interpolate(String message, MessageInterpolator.Context context, Locale locale) {
            return delegate.interpolate(message, context, locale);
        }
    }
}
