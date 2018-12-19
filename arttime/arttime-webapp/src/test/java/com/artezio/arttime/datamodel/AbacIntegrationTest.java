package com.artezio.arttime.datamodel;

import com.artezio.arttime.markers.IntegrationTest;
import com.artezio.arttime.test.utils.CallUsingAbacContext;
import com.artezio.arttime.test.utils.RunInNewTx;
import com.artezio.arttime.test.utils.security.runas.*;
import org.junit.experimental.categories.Category;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.HashSet;

@Category(IntegrationTest.class)
public abstract class AbacIntegrationTest {
    @Inject
    protected RunInNewTx runInNewTx;
    @PersistenceContext(unitName = "test-pu")
    protected EntityManager unsecuredEntityManager;
    @Inject
    protected RunAsProjectManager runAsProjectManager;
    @Inject
    protected RunAsAccountant runAsAccountant;
    @Inject
    protected RunAsOfficeManager runAsOfficeManager;
    @Inject
    protected RunAsIntegrationClient runAsIntegrationClient;
    @Inject
    protected RunAsExec runAsExec;
    @Inject
    protected RunAsAdmin runAsAdmin;
    @Inject
    protected RunAsSystem runAsSystem;
    @Inject
    protected CallUsingAbacContext callUsingAbacContext;

    protected Employee createEmployee(String username, String department, String[] accessibleDepartments) throws Exception {
        Employee caller = new Employee(username, "anon", "non", "non", department);
        caller.setAccessibleDepartments(new HashSet<>(Arrays.asList(accessibleDepartments)));
        runInNewTx.run(() -> {
            unsecuredEntityManager.joinTransaction();
            unsecuredEntityManager.persist(caller);
        });
        return caller;
    }

    protected Employee createAnonymousEmployee(String department, String[] accessibleDepartments) throws Exception {
        return createEmployee("anonymous", department, accessibleDepartments);
    }

    protected Employee createAnonymousEmployee(String department) throws Exception {
        return createAnonymousEmployee(department, new String[]{});
    }
}
