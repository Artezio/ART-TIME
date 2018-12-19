package com.artezio.javax.jpa.abac;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class EntityAccessDeniedException extends SecurityException {
    private static final long serialVersionUID = -8364607066630898821L;

    private Object targetEntity;

    public EntityAccessDeniedException(Object targetEntity) {
        super();
        this.targetEntity = targetEntity;
    }

    @Override
    public String getMessage() {
        return "No entity access permissions.";
    }

    public Object getTargetEntity() {
        return targetEntity;
    }

}
