package com.artezio.javax.jpa.abac.testServices;

import javax.ejb.Stateless;

@Stateless
public class EjbTestService {

    public String getValue() {
        return "one";
    }

}
