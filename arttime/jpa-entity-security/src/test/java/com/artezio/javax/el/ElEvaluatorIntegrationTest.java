package com.artezio.javax.el;

import static org.junit.Assert.assertEquals;

import javax.enterprise.deploy.spi.exceptions.BeanNotFoundException;
import javax.inject.Inject;

import com.artezio.javax.markers.IntegrationTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import com.artezio.javax.jpa.abac.testServices.CdiTestService;
import com.artezio.javax.jpa.abac.testServices.EjbTestService;

@RunWith(Arquillian.class)
@Category(IntegrationTest.class)
public class ElEvaluatorIntegrationTest {

    @Inject
    private ElEvaluator testSubject;
    
    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class, "el-evaluator-test.jar")
                .addPackage("com.artezio.javax.el")
                .addClass(EjbTestService.class)
                .addClass(CdiTestService.class)
                .addClass(BeanNotFoundException.class)
                .addClass(IntegrationTest.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void testEvaluateSimpleEl() {
        Object actual = testSubject.evaluate("#{'simpleString'}");
        
        assertEquals("simpleString", actual);
    }

    @Test
    public void testEvaluateWithEjbBean() {
        Object actual = testSubject.evaluate("#{ejbTestService.value}");
        
        assertEquals("one", actual);
    }

    @Test
    public void testEvaluateWithCdiBean() {
        Object actual = testSubject.evaluate("#{cdiTestService.value}");
        
        assertEquals("one", actual);
    }

}
