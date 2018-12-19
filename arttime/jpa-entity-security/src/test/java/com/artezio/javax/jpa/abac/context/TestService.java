package com.artezio.javax.jpa.abac.context;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.artezio.javax.jpa.abac.ActiveAbacContext;
import com.artezio.javax.jpa.abac.AbacContext;

@Stateless
@AbacContext(value = "class rule")
public class TestService {
    
    @Inject 
    private ActiveAbacContext activeRuleName;
    @Inject
    private TestService self;
    
    public String classContextMethod() {
        return activeRuleName.getName();
    }

    @AbacContext(value = "method rule")
    public String selfContextMethod() {
        return activeRuleName.getName();
    }

    @AbacContext(value = "upper rule")
    public String upperContextMethod() {
        return self.selfContextMethod();
    }

    
}
