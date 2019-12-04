package com.artezio.javax.jpa.abac.testServices;

import com.artezio.javax.jpa.model.MultipleContextSecuredEntity;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.util.List;

@Stateless
public class TransactionalStatelessBean {

    @Inject
    private StatelessBean statelessBean;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void openTransactionAndSaveEntitiesSecuredByContextTwo(List<MultipleContextSecuredEntity> entities) {
        statelessBean.saveInCurrentTransactionSecuredByContextTwo(entities);
    }

}
