package com.artezio.javax.jpa.abac.hibernate;

import com.artezio.javax.jpa.model.SecuredEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.enterprise.inject.spi.CDI;
import java.util.Collection;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(PowerMockRunner.class)
@PrepareForTest(CDI.class)
public class BatchAccessRightsCheckerTest {

    private AbacEntityManager abacEntityManager;
    private AbacEntityManager.BatchAccessRightsChecker batchAccessRightsChecker;
    private ArgumentCaptor<Collection> entityCollectionCaptor;

    @Before
    public void setUp() throws Exception {
        abacEntityManager = Mockito.mock(AbacEntityManager.class);
        ArgumentCaptor<Class> entityClassCaptor = ArgumentCaptor.forClass(Class.class);
        entityCollectionCaptor = ArgumentCaptor.forClass(Collection.class);
        Mockito.doNothing().when(abacEntityManager)
                .checkEntitiesAccessRights(entityClassCaptor.capture(), entityCollectionCaptor.capture());
        batchAccessRightsChecker = abacEntityManager.new BatchAccessRightsChecker();
    }

    private boolean isEntityAccessChecked(Object entity) {
        return entityCollectionCaptor.getAllValues().stream()
                .flatMap(Collection::stream)
                .anyMatch(object -> object == entity);
    }

    @Test
    public void entityCreated() {
        SecuredEntity newEntity1 = new SecuredEntity("name");
        SecuredEntity newEntity2 = new SecuredEntity("one");

        batchAccessRightsChecker.entityCreated(newEntity1);
        batchAccessRightsChecker.entityCreated(newEntity2);
        assertFalse(isEntityAccessChecked(newEntity1));
        assertFalse(isEntityAccessChecked(newEntity2));

        batchAccessRightsChecker.performAccessCheck();
        assertTrue(isEntityAccessChecked(newEntity1));
        assertTrue(isEntityAccessChecked(newEntity2));
    }

    @Test
    public void entityModified() {
        SecuredEntity modifiedEntity1 = new SecuredEntity("name");
        SecuredEntity modifiedEntity2 = new SecuredEntity("one");

        batchAccessRightsChecker.entityModified(modifiedEntity1);
        batchAccessRightsChecker.entityModified(modifiedEntity2);
        assertFalse(isEntityAccessChecked(modifiedEntity1));
        assertFalse(isEntityAccessChecked(modifiedEntity2));

        batchAccessRightsChecker.performAccessCheck();
        assertTrue(isEntityAccessChecked(modifiedEntity1));
        assertTrue(isEntityAccessChecked(modifiedEntity2));
    }

    @Test
    public void entityLoaded() {
        SecuredEntity loadedEntity1 = new SecuredEntity("name");
        SecuredEntity loadedEntity2 = new SecuredEntity("one");

        batchAccessRightsChecker.entityLoaded(loadedEntity1);
        assertTrue(isEntityAccessChecked(loadedEntity1));
        batchAccessRightsChecker.entityLoaded(loadedEntity2);
        assertTrue(isEntityAccessChecked(loadedEntity2));
    }

    @Test
    public void entityIsAboutToBeRemoved() {
        SecuredEntity entityBeingRemoved1 = new SecuredEntity("name");
        SecuredEntity entityBeingRemoved2 = new SecuredEntity("one");

        batchAccessRightsChecker.entityIsAboutToBeRemoved(entityBeingRemoved1);
        assertTrue(isEntityAccessChecked(entityBeingRemoved1));
        batchAccessRightsChecker.entityIsAboutToBeRemoved(entityBeingRemoved2);
        assertTrue(isEntityAccessChecked(entityBeingRemoved2));
    }
}