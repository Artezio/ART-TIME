package com.artezio.arttime.repositories;

import com.artezio.arttime.admin_tool.cache.WebCached;
import com.artezio.arttime.admin_tool.cache.WebCached.Scope;
import org.hibernate.annotations.QueryHints;

import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Named
@Stateless
public class DepartmentRepository {
	@PersistenceContext
    private EntityManager entityManager;
	
	@WebCached(scope = Scope.REQUEST_SCOPED)
	public List<String> getDepartments() {
        List<String> result = entityManager
        		.createQuery("SELECT DISTINCT e.department FROM Employee e", String.class)
                .setHint(QueryHints.CACHEABLE, "true")
                .getResultList();
        result.sort(String::compareToIgnoreCase);
        return result;
    }
	
}
