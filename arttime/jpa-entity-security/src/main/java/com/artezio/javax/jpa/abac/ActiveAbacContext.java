package com.artezio.javax.jpa.abac;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ActiveAbacContext {

    private ThreadLocal<String> activeContext = ThreadLocal.withInitial(() -> "");

    protected void setName(String name) {
        activeContext.set(name);
    }
    
    public String getName() {
        return activeContext.get();
    }
    
}
