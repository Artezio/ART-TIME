package com.artezio.javax.jpa.abac.testServices;

import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaQuery;

import com.artezio.javax.jpa.abac.AbacContext;
import com.artezio.javax.jpa.model.DefaultContextSecuredEntity;
import com.artezio.javax.jpa.model.Master;
import com.artezio.javax.jpa.model.MultipleContextSecuredEntity;
import com.artezio.javax.jpa.model.SecuredEntity;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class StatelessBean {

    @PersistenceContext(unitName = "test-pu")
    private EntityManager abacEntityManager;

    public void executeQuery() {
        CriteriaQuery<Master> query = abacEntityManager.getCriteriaBuilder().createQuery(Master.class);
        query.select(query.from(Master.class));
        abacEntityManager.createQuery(query).getResultList();
    }
    
    @AbacContext("contextOne")
    public List<MultipleContextSecuredEntity> getSecuredByContextOne() {
        return abacEntityManager
                .createQuery("SELECT e FROM MultipleContextSecuredEntity e", MultipleContextSecuredEntity.class)
                .getResultList();
    }

    @AbacContext("contextTwo")
    public List<DefaultContextSecuredEntity> getSecuredByContextTwo() {
        return abacEntityManager
                .createQuery("SELECT e FROM DefaultContextSecuredEntity e", DefaultContextSecuredEntity.class)
                .getResultList();
    }

    public List<DefaultContextSecuredEntity> getSecuredByDefault() {
        return abacEntityManager
                .createQuery("SELECT e FROM DefaultContextSecuredEntity e", DefaultContextSecuredEntity.class)
                .getResultList();
    }

    public List<SecuredEntity> getSecured() {
        return abacEntityManager
                .createQuery("SELECT e FROM SecuredEntity e", SecuredEntity.class)
                .getResultList();
    }

    @AbacContext("notDeclaredContext")
    public List<DefaultContextSecuredEntity> getFromNotDeclaredContext() {
        return abacEntityManager
                .createQuery("SELECT e FROM DefaultContextSecuredEntity e", DefaultContextSecuredEntity.class)
                .getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void save(List<SecuredEntity> securedEntities) {
        securedEntities.forEach(abacEntityManager::persist);
    }

    @AbacContext("contextTwo")
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void saveInCurrentTransactionSecuredByContextTwo(List<MultipleContextSecuredEntity> securedEntities) {
        securedEntities.forEach(abacEntityManager::persist);
    }

}
