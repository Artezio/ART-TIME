package com.artezio.javax.jpa.abac.testServices;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CdiTestService {

    public String getValue() {
        return "one";
    }

}
