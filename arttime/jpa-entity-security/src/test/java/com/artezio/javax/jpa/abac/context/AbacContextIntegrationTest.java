package com.artezio.javax.jpa.abac.context;

import com.artezio.javax.markers.IntegrationTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
@Category(IntegrationTest.class)
public class AbacContextIntegrationTest {

    @Inject
    private TestService service;

    @Deployment
    public static WebArchive createDeployment() {
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class, "abac-test.jar")
                .addPackage(AbacContextIntegrationTest.class.getPackage())
                .addPackages(true, "com/google")
                .addPackage("com.artezio.javax.el")
                .addPackage("com.artezio.javax.jpa.abac")
                .addPackage("com.artezio.javax.jpa.abac.testServices")
                .addPackage("com.artezio.javax.jpa.abac.hibernate")
                .addPackage("com.artezio.javax.jpa.abac.hibernate.spi")
                .addPackage("com.artezio.javax.jpa.model")
                .addClass(IntegrationTest.class)
                .addAsResource("META-INF/services/javax.persistence.spi.PersistenceProvider", "META-INF/services/javax.persistence.spi.PersistenceProvider")
                .setManifest(new StringAsset("Manifest-Version: 1.0\nDependencies: org.hibernate"))
                .addAsResource("META-INF/jboss-deployment-structure.xml", "META-INF/jboss-deployment-structure.xml")
                .addAsResource("META-INF/persistence.xml", "META-INF/persistence.xml")
                .addAsResource("META-INF/beans.xml", "META-INF/beans.xml")
                .addPackages(true, "junitx.framework");

        return ShrinkWrap.create(WebArchive.class)
                .addAsWebInfResource("META-INF/jboss-deployment-structure.xml", "jboss-deployment-structure.xml")
                .addAsWebInfResource("META-INF/persistence.xml", "persistence.xml")
                .addAsLibrary(jar);
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
