package com.artezio.javax.jpa.abac.hibernate;

import com.artezio.javax.jpa.abac.AbacRule;
import com.artezio.javax.jpa.abac.EntityAccessDeniedException;
import com.artezio.javax.jpa.abac.ParamValue;
import com.artezio.javax.jpa.abac.testServices.StatelessBean;
import com.artezio.javax.jpa.abac.testServices.TransactionalStatelessBean;
import com.artezio.javax.jpa.model.*;
import com.artezio.javax.markers.IntegrationTest;
import junitx.framework.ListAssert;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.Cleanup;
import org.jboss.arquillian.persistence.CleanupUsingScript;
import org.jboss.arquillian.persistence.TestExecutionPhase;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import javax.ejb.EJBTransactionRolledbackException;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.*;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
@Category(IntegrationTest.class)
@Cleanup(phase = TestExecutionPhase.DEFAULT)
@CleanupUsingScript(value = "SET REFERENTIAL_INTEGRITY FALSE;"
        + "TRUNCATE TABLE SecuredEntity;"
        + "TRUNCATE TABLE SecuredEntity2;"
        + "TRUNCATE TABLE SecuredEntityByContextValue;"
        + "TRUNCATE TABLE Detail;"
        + "TRUNCATE TABLE Master;"
        + "TRUNCATE TABLE MultipleContextSecuredEntity;"
        + "TRUNCATE TABLE MultipleFilterSecuredEntity;"
        + "TRUNCATE TABLE DefaultContextSecuredEntity;"
        + "SET REFERENTIAL_INTEGRITY True;")
public class AbacEntityManagerIntegrationTest {

    @PersistenceContext(unitName = "unsecured-test-pu")
    private EntityManager notSecuredEntityManager;
    @PersistenceContext(unitName = "test-pu")
    private EntityManager abacEntityManager;
    @EJB
    private StatelessBean statelessBean;
    @EJB
    private TransactionalStatelessBean transactionalStatelessBean;

    @Deployment
    public static WebArchive createDeployment() {
        JavaArchive abacJar = ShrinkWrap.create(JavaArchive.class, "abac-test.jar")
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
                .addAsLibrary(abacJar);
    }

    @Test
    public void testSelectAccessibleEntities() throws SecurityException,
            IllegalStateException {
        notSecuredEntityManager.joinTransaction();
        SecuredEntity expected = new SecuredEntity("one");
        SecuredEntity unexpected = new SecuredEntity("two");
        notSecuredEntityManager.persist(expected);
        notSecuredEntityManager.persist(unexpected);
        notSecuredEntityManager.flush();
        notSecuredEntityManager.clear();

        abacEntityManager.joinTransaction();
        List<SecuredEntity> actuals = abacEntityManager.createQuery("SELECT e FROM SecuredEntity e", SecuredEntity.class)
                .getResultList();

        assertEquals(1, actuals.size());
        assertTrue(actuals.contains(expected));
    }

    @Test
    public void testSelectMultipleAccessibleEntities() throws SecurityException,
            IllegalStateException {
        notSecuredEntityManager.joinTransaction();
        SecuredEntity expected1 = new SecuredEntity("one");
        SecuredEntity unexpected1 = new SecuredEntity("two");
        SecuredEntity2 expected2 = new SecuredEntity2("two");
        SecuredEntity2 unexpected2 = new SecuredEntity2("one");
        notSecuredEntityManager.persist(expected1);
        notSecuredEntityManager.persist(expected2);
        notSecuredEntityManager.persist(unexpected1);
        notSecuredEntityManager.persist(unexpected2);
        notSecuredEntityManager.flush();
        notSecuredEntityManager.clear();

        abacEntityManager.joinTransaction();
        List<SecuredEntity> actuals1 = abacEntityManager.createQuery("SELECT e FROM SecuredEntity e", SecuredEntity.class)
                .getResultList();

        assertEquals(1, actuals1.size());
        assertTrue(actuals1.contains(expected1));

        List<SecuredEntity2> actuals2 = abacEntityManager
                .createQuery("SELECT e FROM SecuredEntity2 e", SecuredEntity2.class).getResultList();
        assertEquals(1, actuals2.size());
        assertTrue(actuals2.contains(expected2));
    }

    @Test
    public void testSecuredByContextValue() {
        notSecuredEntityManager.joinTransaction();
        SecuredEntityByContextValue expected = new SecuredEntityByContextValue("one");
        SecuredEntityByContextValue unexpected = new SecuredEntityByContextValue("two");
        notSecuredEntityManager.persist(expected);
        notSecuredEntityManager.persist(unexpected);
        notSecuredEntityManager.flush();
        notSecuredEntityManager.clear();

        abacEntityManager.joinTransaction();
        List<SecuredEntityByContextValue> actuals = abacEntityManager
                .createQuery("SELECT e FROM SecuredEntityByContextValue e", SecuredEntityByContextValue.class)
                .getResultList();
        assertEquals(1, actuals.size());
        assertTrue(actuals.contains(expected));
    }

    @Test
    public void testSecurityForCriteriaBuilder() throws SecurityException,
            IllegalStateException {
        notSecuredEntityManager.joinTransaction();
        SecuredEntity expected = new SecuredEntity("one");
        SecuredEntity unexpected = new SecuredEntity("two");
        notSecuredEntityManager.persist(expected);
        notSecuredEntityManager.persist(unexpected);
        notSecuredEntityManager.flush();
        notSecuredEntityManager.clear();

        abacEntityManager.joinTransaction();
        CriteriaBuilder cb = abacEntityManager.getCriteriaBuilder();
        CriteriaQuery<SecuredEntity> cq = cb.createQuery(SecuredEntity.class);
        Root<SecuredEntity> rootEntry = cq.from(SecuredEntity.class);
        CriteriaQuery<SecuredEntity> all = cq.select(rootEntry);

        List<SecuredEntity> actuals = abacEntityManager.createQuery(all).getResultList();

        assertEquals(1, actuals.size());
        assertTrue(actuals.contains(expected));
    }

    @Test
    public void testAbacFilterSubqueries() {
        notSecuredEntityManager.joinTransaction();
        Master expected = new Master("one");
        Master unexpected = new Master("one");
        Detail detail = new Detail("one", expected);
        notSecuredEntityManager.persist(expected);
        notSecuredEntityManager.persist(unexpected);
        notSecuredEntityManager.persist(detail);
        notSecuredEntityManager.flush();
        notSecuredEntityManager.clear();

        abacEntityManager.joinTransaction();
        List<Master> actuals = abacEntityManager.createQuery("SELECT e FROM Master e", Master.class).getResultList();
        assertEquals(1, actuals.size());
        assertTrue(actuals.contains(expected));
    }

    @Test(expected = EntityAccessDeniedException.class)
    public void testCreateRestricted() {
        abacEntityManager.joinTransaction();
        abacEntityManager.persist(new SecuredEntity("two"));
        abacEntityManager.flush();
    }

    @Test(expected = EntityAccessDeniedException.class)
    public void testCreateRestricted_FilterAlreadyEnabled() {
        abacEntityManager.joinTransaction();
        Filter secured = abacEntityManager.unwrap(Session.class)
                .enableFilter("secured");
        secured.setParameter("name", "one");
        abacEntityManager.persist(new SecuredEntity("two"));
        abacEntityManager.flush();
    }

    @Test(expected = EntityAccessDeniedException.class)
    public void testCreateRestricted_FilterDisabled() {
        abacEntityManager.joinTransaction();
        abacEntityManager.unwrap(Session.class)
                .disableFilter("secured");
        abacEntityManager.persist(new SecuredEntity("two"));
        abacEntityManager.flush();
    }

    @Test(expected = EntityAccessDeniedException.class)
    public void testUpdateRestricted() throws SecurityException,
            IllegalStateException {
        notSecuredEntityManager.joinTransaction();
        SecuredEntity entity = new SecuredEntity("one");
        notSecuredEntityManager.persist(entity);
        notSecuredEntityManager.flush();
        notSecuredEntityManager.clear();
        notSecuredEntityManager.detach(entity);

        abacEntityManager.joinTransaction();
        entity.setName("two");
        abacEntityManager.merge(entity);
        abacEntityManager.flush();
    }

    @Test(expected = EntityAccessDeniedException.class)
    public void testUpdateRestricted_FilterAlreadyEnabled() throws SecurityException,
            IllegalStateException {
        notSecuredEntityManager.joinTransaction();
        SecuredEntity entity = new SecuredEntity("one");
        notSecuredEntityManager.persist(entity);
        notSecuredEntityManager.flush();
        notSecuredEntityManager.clear();
        notSecuredEntityManager.detach(entity);

        abacEntityManager.joinTransaction();
        Filter secured = abacEntityManager.unwrap(Session.class)
                .enableFilter("secured");
        secured.setParameter("name", "one");
        entity.setName("two");
        abacEntityManager.merge(entity);
        abacEntityManager.flush();
    }

    @Test(expected = EntityAccessDeniedException.class)
    public void testUpdateRestricted_FilterDisabled() throws SecurityException,
            IllegalStateException {
        notSecuredEntityManager.joinTransaction();
        SecuredEntity entity = new SecuredEntity("one");
        notSecuredEntityManager.persist(entity);
        notSecuredEntityManager.flush();
        notSecuredEntityManager.clear();
        notSecuredEntityManager.detach(entity);

        abacEntityManager.joinTransaction();
        abacEntityManager.unwrap(Session.class)
                .disableFilter("secured");
        entity.setName("two");
        abacEntityManager.merge(entity);
        abacEntityManager.flush();
    }

    @Test(expected = EntityAccessDeniedException.class)
    public void testDeleteRestricted() throws SecurityException,
            IllegalStateException {
        notSecuredEntityManager.joinTransaction();
        SecuredEntity entity = new SecuredEntity("two");
        notSecuredEntityManager.persist(entity);
        notSecuredEntityManager.flush();
        notSecuredEntityManager.clear();
        notSecuredEntityManager.detach(entity);

        abacEntityManager.joinTransaction();
        abacEntityManager.remove(entity);
        abacEntityManager.flush();
    }

    @Test(expected = EntityAccessDeniedException.class)
    public void testDeleteRestricted_FilterAlreadyEnabled() throws SecurityException,
            IllegalStateException {
        notSecuredEntityManager.joinTransaction();
        SecuredEntity entity = new SecuredEntity("two");
        notSecuredEntityManager.persist(entity);
        notSecuredEntityManager.flush();
        notSecuredEntityManager.clear();
        notSecuredEntityManager.detach(entity);

        abacEntityManager.joinTransaction();
        Filter secured = abacEntityManager.unwrap(Session.class)
                .enableFilter("secured");
        secured.setParameter("name", "one");
        abacEntityManager.remove(entity);
        abacEntityManager.flush();
    }

    @Test(expected = EntityAccessDeniedException.class)
    public void testDeleteRestricted_FilterDisabled() throws SecurityException,
            IllegalStateException {
        notSecuredEntityManager.joinTransaction();
        SecuredEntity entity = new SecuredEntity("two");
        notSecuredEntityManager.persist(entity);
        notSecuredEntityManager.flush();
        notSecuredEntityManager.clear();
        notSecuredEntityManager.detach(entity);

        abacEntityManager.joinTransaction();
        abacEntityManager.unwrap(Session.class)
                .disableFilter("secured");
        abacEntityManager.remove(entity);
        abacEntityManager.flush();
    }

    @Test
    public void testFind() {
        notSecuredEntityManager.joinTransaction();
        SecuredEntity entity = new SecuredEntity("one");
        notSecuredEntityManager.persist(entity);
        notSecuredEntityManager.flush();
        notSecuredEntityManager.clear();
        notSecuredEntityManager.detach(entity);

        abacEntityManager.joinTransaction();
        abacEntityManager.find(SecuredEntity.class, entity.getId());
    }

    @Test
    public void testFind_FilterAlreadyEnabled() {
        notSecuredEntityManager.joinTransaction();
        SecuredEntity entity = new SecuredEntity("one");
        notSecuredEntityManager.persist(entity);
        notSecuredEntityManager.flush();
        notSecuredEntityManager.clear();
        notSecuredEntityManager.detach(entity);

        abacEntityManager.joinTransaction();
        Filter secured = abacEntityManager.unwrap(Session.class)
                .enableFilter("secured");
        secured.setParameter("name", "one");
        abacEntityManager.find(SecuredEntity.class, entity.getId());
    }

    @Test
    public void testFind_FilterDisabled() {
        notSecuredEntityManager.joinTransaction();
        SecuredEntity entity = new SecuredEntity("one");
        notSecuredEntityManager.persist(entity);
        notSecuredEntityManager.flush();
        notSecuredEntityManager.clear();
        notSecuredEntityManager.detach(entity);

        abacEntityManager.joinTransaction();
        abacEntityManager.unwrap(Session.class)
                .disableFilter("secured");
        abacEntityManager.find(SecuredEntity.class, entity.getId());
    }


    @Test
    @Transactional(TransactionMode.COMMIT)
    public void testInjectedInStatelessBean_sessionIsOpen() {
        statelessBean.executeQuery();
    }


    @Test
    @Transactional(TransactionMode.COMMIT)
    public void testMultipleContextSecuredEntity() {
        notSecuredEntityManager.joinTransaction();
        MultipleContextSecuredEntity expected = new MultipleContextSecuredEntity("one");
        MultipleContextSecuredEntity unexpected1 = new MultipleContextSecuredEntity("two");
        MultipleContextSecuredEntity unexpected2 = new MultipleContextSecuredEntity("three");
        notSecuredEntityManager.persist(expected);
        notSecuredEntityManager.persist(unexpected1);
        notSecuredEntityManager.persist(unexpected2);
        notSecuredEntityManager.flush();

        List<MultipleContextSecuredEntity> actuals = statelessBean.getSecuredByContextOne();

        assertEquals(1, actuals.size());
        assertEquals(expected, actuals.get(0));
    }

    @Test(expected = EJBTransactionRolledbackException.class)
    @Transactional(TransactionMode.COMMIT)
    public void testSaveSecuredEntity_batch() {
        SecuredEntity entity1 = new SecuredEntity("two");
        SecuredEntity entity2 = new SecuredEntity("one");
        SecuredEntity entity3 = new SecuredEntity("two");
        SecuredEntity entity4 = new SecuredEntity("one");
        SecuredEntity entity5 = new SecuredEntity("one");
        statelessBean.save(Arrays.asList(entity1, entity2, entity3, entity4, entity5));
    }

    @Test
    public void testListAbacFilters() {
        AbacEntityManager em = createAbacEntityManager(notSecuredEntityManager);
        Set<Class<?>> entitiesToScan = new HashSet<>(Arrays.asList(SecuredEntity.class, MultipleContextSecuredEntity.class));
        Set<String> expectedFilterNames = new HashSet<>(Arrays.asList("secured", "contextSecured1", "contextSecured2"));

        Map<String, ParamValue[]> actuals = em.listAbacFilters(entitiesToScan);

        assertTrue(actuals.keySet().containsAll(expectedFilterNames));
        assertEquals(1, actuals.get("secured").length);
        assertEquals(1, actuals.get("contextSecured1").length);
        assertEquals(1, actuals.get("contextSecured2").length);
    }

    @Test
    public void testGetSecuredEntities() {
        AbacEntityManager em = createAbacEntityManager(notSecuredEntityManager);
        Set<Class<?>> actuals = em.listSecuredEntities();

        assertTrue(actuals.contains(SecuredEntity.class));
        assertTrue(actuals.contains(MultipleContextSecuredEntity.class));
    }

    @Test
    public void testListFiltersInContext() {
        AbacEntityManager em = createAbacEntityManager(notSecuredEntityManager);
        Set<Class<?>> entitiesToScan = new HashSet<>(Arrays.asList(SecuredEntity.class, MultipleContextSecuredEntity.class));
        Set<String> expectedContextNames = new HashSet<>(Arrays.asList("", "contextOne", "contextTwo"));

        Map<String, Set<String>> actuals = em.listFiltersInContext(entitiesToScan);
        assertTrue(actuals.keySet().containsAll(expectedContextNames));
        assertSetsEquals(actuals.get(""), asSet("secured"));
        assertSetsEquals(actuals.get("contextOne"), asSet("contextSecured1"));
        assertSetsEquals(actuals.get("contextTwo"), asSet("contextSecured2"));
    }

    @SuppressWarnings("unchecked")
    private void assertSetsEquals(Set<?> expecteds, Set<?> actuals) {
        ListAssert.assertEquals(new ArrayList(expecteds), new ArrayList(expecteds));
    }

    private static <T> Set<T> asSet(T... a) {
        return new HashSet<>(Arrays.asList(a));
    }

    @Test
    public void testListAbacAnnotations() {
        AbacEntityManager em = createAbacEntityManager(notSecuredEntityManager);

        List<AbacRule> actuals = em.listAbacAnnotations(MultipleContextSecuredEntity.class);

        assertEquals(2, actuals.size());
        actuals.stream().forEach(System.out::println);
    }

    @Test
    public void testRunAMethodInContextNotDeclaredInRules() {
        notSecuredEntityManager.joinTransaction();
        DefaultContextSecuredEntity expected = new DefaultContextSecuredEntity("one");
        DefaultContextSecuredEntity unexpected = new DefaultContextSecuredEntity("two");
        notSecuredEntityManager.persist(expected);
        notSecuredEntityManager.persist(unexpected);
        notSecuredEntityManager.flush();
        List<DefaultContextSecuredEntity> expecteds = Arrays.asList(expected);

        List<DefaultContextSecuredEntity> actuals = statelessBean.getFromNotDeclaredContext();

        ListAssert.assertEquals(expecteds, actuals);
    }

    @Test
    public void testDefaultContextRulesNotEnabledUnderDeclaredContext() {
        notSecuredEntityManager.joinTransaction();
        DefaultContextSecuredEntity defaultSecured = new DefaultContextSecuredEntity("one");
        DefaultContextSecuredEntity ctxTwoSecured = new DefaultContextSecuredEntity("two");
        notSecuredEntityManager.persist(defaultSecured);
        notSecuredEntityManager.persist(ctxTwoSecured);
        notSecuredEntityManager.flush();

        List<DefaultContextSecuredEntity> actualsByDefault = statelessBean.getSecuredByDefault();
        List<DefaultContextSecuredEntity> actualsByDefaultAndContextTwo = statelessBean.getSecuredByContextTwo();

        ListAssert.assertEquals(Arrays.asList(defaultSecured), actualsByDefault);
        ListAssert.assertEquals(Arrays.asList(ctxTwoSecured), actualsByDefaultAndContextTwo);
    }

    @Test
    public void testEnableTheSameFilterTwice() {
        statelessBean.getSecuredByContextOne();
        statelessBean.getSecuredByContextOne();
    }

    @Test
    public void testRuleWithMultipleFiltersWithDifferentParamDefinitions() {
        notSecuredEntityManager.joinTransaction();
        MultipleFilterSecuredEntity expected = new MultipleFilterSecuredEntity("one");
        MultipleFilterSecuredEntity unexpected = new MultipleFilterSecuredEntity("two");
        notSecuredEntityManager.persist(expected);
        notSecuredEntityManager.persist(unexpected);
        notSecuredEntityManager.flush();
        List<MultipleFilterSecuredEntity> expecteds = Arrays.asList(expected);

        abacEntityManager.joinTransaction();
        List<MultipleFilterSecuredEntity> actuals = abacEntityManager.createQuery(
                "SELECT e FROM MultipleFilterSecuredEntity e", MultipleFilterSecuredEntity.class).getResultList();

        ListAssert.assertEquals(expecteds, actuals);
    }

    @Test
    public void testFind_noResult() {
        notSecuredEntityManager.joinTransaction();
        notSecuredEntityManager.createQuery("DELETE FROM SecuredEntity").executeUpdate();
        notSecuredEntityManager.flush();

        abacEntityManager.joinTransaction();

        SecuredEntity actual = abacEntityManager.find(SecuredEntity.class, 77L);
        assertNull(actual);
    }

    @Test
    public void testFind_byIdAndProperties_noResult() {
        notSecuredEntityManager.joinTransaction();
        notSecuredEntityManager.createQuery("DELETE FROM SecuredEntity").executeUpdate();
        notSecuredEntityManager.flush();

        abacEntityManager.joinTransaction();

        SecuredEntity actual = abacEntityManager.find(SecuredEntity.class, 77L, new HashMap<>());
        assertNull(actual);
    }

    @Test
    public void testFind_byIdLockModeType_noResult() {
        notSecuredEntityManager.joinTransaction();
        notSecuredEntityManager.createQuery("DELETE FROM SecuredEntity").executeUpdate();
        notSecuredEntityManager.flush();

        abacEntityManager.joinTransaction();

        SecuredEntity actual = abacEntityManager.find(SecuredEntity.class, 77L, LockModeType.NONE);
        assertNull(actual);
    }

    @Test
    public void testFind_byIdLockModeTypeProperties_noResult() {
        notSecuredEntityManager.joinTransaction();
        notSecuredEntityManager.createQuery("DELETE FROM SecuredEntity").executeUpdate();
        notSecuredEntityManager.flush();

        abacEntityManager.joinTransaction();

        SecuredEntity actual = abacEntityManager.find(SecuredEntity.class, 77L, LockModeType.NONE, new HashMap<>());
        assertNull(actual);
    }

    @Test
    public void testFilterThatUsesNonFlushedJoinTable() {
        AssociationOwningEntity owningEntity = new AssociationOwningEntity();
        owningEntity.setValue("parent");
        AssociationChildEntity childEntity = new AssociationChildEntity();
        childEntity.setValue("child");
        abacEntityManager.joinTransaction();
        abacEntityManager.persist(childEntity);
        owningEntity.getChildEntities().add(childEntity);
        abacEntityManager.persist(owningEntity);
    }

    @Test
    public void testCheckEntitiesAccessRights() {
        AbacEntityManager em = createAbacEntityManager(notSecuredEntityManager);
        SecuredEntity securedEntity1 = new SecuredEntity("one");
        SecuredEntity securedEntity2 = new SecuredEntity("one");
        em.persist(securedEntity1);
        em.persist(securedEntity2);
        em.flush();
        em.checkEntitiesAccessRights(SecuredEntity.class, Arrays.asList(securedEntity1, securedEntity2));
        List<SecuredEntity> actual = em.createQuery("FROM SecuredEntity e", SecuredEntity.class).getResultList();
        assertTrue(actual.contains(securedEntity1));
        assertTrue(actual.contains(securedEntity2));
    }

    @Test(expected = EntityAccessDeniedException.class)
    public void testCheckEntitiesAccessRights_noAccessRightsForOneOfEntities() {
        AbacEntityManager em = createAbacEntityManager(notSecuredEntityManager);
        SecuredEntity securedEntity1 = new SecuredEntity("one");
        SecuredEntity securedEntity2 = new SecuredEntity("two");
        notSecuredEntityManager.persist(securedEntity1);
        notSecuredEntityManager.persist(securedEntity2);
        notSecuredEntityManager.flush();

        em.checkEntitiesAccessRights(SecuredEntity.class, Arrays.asList(securedEntity1, securedEntity2));
    }

    @Test(expected = EntityAccessDeniedException.class)
    public void testBatchIsCheckedBeforeRead() {
        AbacEntityManager em = createAbacEntityManager(notSecuredEntityManager);
        SecuredEntity securedEntity1 = new SecuredEntity("one");
        SecuredEntity securedEntity2 = new SecuredEntity("two");
        em.persist(securedEntity1);
        em.persist(securedEntity2);
        em.find(SecuredEntity.class, securedEntity1.getId());
    }

    @Test(expected = EntityAccessDeniedException.class)
    public void testAccessChecksPerformedOnLeavingContextWhileTransactionIsStillOpen() {
        MultipleContextSecuredEntity entity1 = new MultipleContextSecuredEntity("two");
        MultipleContextSecuredEntity entity2 = new MultipleContextSecuredEntity("one");
        MultipleContextSecuredEntity entity3 = new MultipleContextSecuredEntity("three");
        MultipleContextSecuredEntity entity4 = new MultipleContextSecuredEntity("one");
        MultipleContextSecuredEntity entity5 = new MultipleContextSecuredEntity("one");
        transactionalStatelessBean.openTransactionAndSaveEntitiesSecuredByContextTwo(Arrays.asList(entity1, entity2, entity3, entity4, entity5));
    }

    private AbacEntityManager createAbacEntityManager(EntityManager delegate) {
        return new AbacEntityManager(delegate);
    }
}
