package com.artezio.arttime.test.utils;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.concurrent.Callable;

@Stateless
public class RunInNewTx {
    
    @PersistenceContext(unitName = "secured-test-pu")
    private EntityManager abacEntityManager;
    
    public interface AbacEntityManagerAwareCallable<T> {
        T call(EntityManager abacEntityManager);
    };
    
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void run(Runnable runnable) throws Exception {
        runnable.run();
    }
    
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public <T> T call(Callable<T> callable) throws Exception {
        return callable.call();
    }
    
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public <T> T call(AbacEntityManagerAwareCallable<T> callable) throws Exception {
        return callable.call(abacEntityManager);
    }
    
}
