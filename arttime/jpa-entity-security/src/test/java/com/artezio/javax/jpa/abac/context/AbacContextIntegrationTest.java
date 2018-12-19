package com.artezio.javax.jpa.abac.context;

import static org.junit.Assert.assertEquals;

import javax.inject.Inject;

import com.artezio.javax.markers.IntegrationTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import com.artezio.javax.jpa.abac.AbacContext;

@RunWith(Arquillian.class)
@Category(IntegrationTest.class)
public class AbacContextIntegrationTest {

    @Inject
    private TestService service;

    @Deployment
    public static JavaArchive createDeployment() {
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class, "abac-test.jar")
                .addPackage(AbacContext.class.getPackage())
                .addPackage(AbacContextIntegrationTest.class.getPackage())
                .addClass(IntegrationTest.class);
 
        System.out.println(jar.toString(true));
        return jar;
    }

    @Test
    public void testClassRuled() {
        assertEquals("class rule", service.classContextMethod());
    }

    @Test
    public void testSelfRuled() {
        assertEquals("method rule", service.selfContextMethod());
    }

    @Test
    public void testUpperRuleOverwrited() {
        assertEquals("method rule", service.upperContextMethod());
    }

}
