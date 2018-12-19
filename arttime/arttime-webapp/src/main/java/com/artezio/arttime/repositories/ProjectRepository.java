package com.artezio.arttime.repositories;

import com.artezio.arttime.admin_tool.log.DetailedLogged;
import com.artezio.arttime.admin_tool.log.Log;
import com.artezio.arttime.datamodel.*;
import com.artezio.arttime.filter.Filter;
import com.artezio.arttime.repositories.annotations.CallerCanManage;
import com.artezio.arttime.web.interceptors.FacesMessage;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.criteria.*;
import java.util.*;

import static com.artezio.arttime.datamodel.Project.Status.ACTIVE;

@Stateless
public class ProjectRepository {

    @PersistenceContext
    private EntityManager entityManager;
    @Inject
    private EmployeeRepository employeeRepository;

    @Log(logParams = true)
    @FacesMessage(onCompleteMessageKey = "message.projectIsSaved")
    public Project create(@DetailedLogged Project project) {
        saveEmployees(project);
        entityManager.persist(project);
        return project;
    }

    @Log(logParams = true)
    @FacesMessage(onCompleteMessageKey = "message.projectIsSaved")
    public void create(@DetailedLogged Collection<Project> projects) {
        projects.forEach(this::create);
    }

    @Log(logParams = true)
    @FacesMessage(onCompleteMessageKey = "message.projectIsDeleted")
    public void remove(@CallerCanManage @DetailedLogged Project project) {
        Project managedProject = entityManager.find(Project.class, project.getId());
        entityManager.remove(managedProject);
    }

    @Log(logParams = true)
    @FacesMessage(onCompleteMessageKey = "message.projectIsSaved")
    public Project update(@DetailedLogged @CallerCanManage Project project) {
        saveEmployees(project);
        return entityManager.merge(project);
    }

    @Log(logParams = true)
    @FacesMessage(onCompleteMessageKey = "message.projectIsSaved")
    public void update(@CallerCanManage @DetailedLogged Collection<Project> projects) {
        projects.forEach(this::update);
    }

    public ProjectQuery query() {
        return new ProjectQuery();
    }

    public Project findById(Long id) {
        return query()
                .id(id)
                .getSingleResultOrNull();
    }

    public ProjectQuery query(boolean tupleResult) {
        return new ProjectQuery(tupleResult);
    }

    //Used to prevent from employee's re-updating in the case when the changes come from LDAP while project editing
    private void saveEmployees(Project project) {
        Set<Employee> employees = new HashSet<>(project.getTeam());
        employees.addAll(project.getManagers());
        employees.forEach(employeeRepository::create);
    }

    public class ProjectQuery extends Query<Project> {

        private Root<Hours> hoursRoot;

        private Path<Employee> teamMembersPath;
        private Path<Employee> managersPath;
        private Path<Employee> masterProjectManagersPath;
        private Path<Project> masterProjectPath;
        private Path<HourType> hourTypePath;

        ProjectQuery() {
            super(entityManager);
        }
        
        ProjectQuery(boolean tupleResult) {
            super(entityManager, tupleResult);
        }

        public ProjectQuery teamMember(Collection<Employee> employees) {
            fetchTeam();
            add(teamMembersPath.in(employees));
            return this;
        }

        public ProjectQuery projects(Collection<Project> projects) {
            addInPredicate(getRoot(), projects);
            return this;
        }

        public ProjectQuery id(Long id) {
            add(getCriteriaBuilder().equal(getRoot().get(Project_.id), id));
            return this;
        }

        public ProjectQuery code(String code) {
            add(getCriteriaBuilder().equal(getRoot().get(Project_.code), code));
            return this;
        }

        public ProjectQuery codes(List<String> codes) {
            addInPredicate(getRoot().get(Project_.code), codes);
            return this;
        }

        public ProjectQuery masters(List<Project> projects) {
            addInPredicate(getRoot().get(Project_.master), projects);
            return this;
        }

        public ProjectQuery mastersCodes(List<String> masterCodes) {
            addInPredicate(getRoot().get(Project_.master).get(Project_.code), masterCodes);
            return this;
        }

        public ProjectQuery departments(List<String> departments) {
            fetchTeam();
            addInPredicate(teamMembersPath.get(Employee_.department), departments);
            return this;
        }

        public ProjectQuery withoutMasterOrWithMasterStatus(Project.Status masterStatus) {
            fetchMasterProject();
            add(
                    getCriteriaBuilder().or(
                            getRoot().get(Project_.master).isNull(),
                            getCriteriaBuilder().equal(
                                    masterProjectPath.get(Project_.status),
                                    masterStatus
                            )));
            return this;
        }

        public ProjectQuery filter(Filter filter) {
            if (filter.containsAtLeastOneProject()) {
                projects(filter.getProjects());
            }
            if (filter.containsAtLeastOneHourType()) {
                hourTypes(filter.getHourTypes());
            }
            if (filter.containsAtLeastOneEmployee()) {
                teamMembers(filter.getEmployees());
            }
            if (filter.containsAtLeastOneDepartment()) {
                departments(filter.getDepartments());
            }
            return this;
        }

        public ProjectQuery withMasterStatus(Project.Status masterStatus) {
            fetchMasterProject();
            add(
                    getCriteriaBuilder().equal(
                            masterProjectPath.get(Project_.status),
                            masterStatus
                    )
            );
            return this;
        }

        public ProjectQuery withTeamMembersIn(String department) {
            fetchTeam();
            add(getCriteriaBuilder().equal(teamMembersPath.get(Employee_.department), department));
            return this;
        }

        @SuppressWarnings("unchecked")
        public ProjectQuery managedBy(String username) {
            fetchManagers();
            add(getCriteriaBuilder().or(
                    getCriteriaBuilder().equal(managersPath.get(Employee_.userName), username),
                    getCriteriaBuilder().equal(masterProjectManagersPath.get(Employee_.userName), username)
            ));
            return this;
        }

        public ProjectQuery managedBy(Employee employee) {
            return managedBy(employee.getUserName());
        }

        public ProjectQuery status(Project.Status status) {
            CriteriaBuilder cb = getCriteriaBuilder();
            fetchMasterProject();
            add(cb.or(
                    cb.and(
                            getRoot().get(Project_.master).isNull(),
                            cb.equal(getRoot().get(Project_.status), status)
                    ),
                    cb.and(
                            getRoot().get(Project_.master).isNotNull(),
                            cb.or(
                                    cb.and(cb.notEqual(getRoot().get(Project_.master).get(Project_.status), ACTIVE),
                                            cb.equal(getRoot().get(Project_.master).get(Project_.status), status)),
                                    cb.and(cb.equal(getRoot().get(Project_.master).get(Project_.status), ACTIVE),
                                            cb.equal(getRoot().get(Project_.status), status))
                            )
                    )
            ));
            return this;
        }

        public ProjectQuery teamMember(String userName) {
            fetchTeam();
            add(getCriteriaBuilder().equal(teamMembersPath.get(Employee_.userName), userName));
            return this;
        }

        public ProjectQuery teamMember(Employee employee) {
            if (employee != null) {
                add(getCriteriaBuilder().isMember(employee, getRoot().get(Project_.team)));
            }
            return this;
        }

        public ProjectQuery hourTypes(List<HourType> hourTypes) {
            addInPredicate(getRoot().join(Project_.accountableHours), hourTypes);
            return this;
        }

        public ProjectQuery teamMembers(List<Employee> employees) {
            addInPredicate(getRoot().join(Project_.team), employees);
            return this;
        }

        public ProjectQuery withHoursFrom(Date from) {
            add(getCriteriaBuilder().greaterThanOrEqualTo(getHoursRoot().get(Hours_.date), from));
            return this;
        }

        public ProjectQuery withHoursTill(Date till) {
            add(getCriteriaBuilder().lessThanOrEqualTo(getHoursRoot().get(Hours_.date), till));
            return this;
        }

        public ProjectQuery withHoursFor(Employee employee) {
            add(getCriteriaBuilder().equal(getHoursRoot().get(Hours_.employee), employee));
            return this;
        }

        public ProjectQuery withHoursFor(String userName) {
            add(getCriteriaBuilder().equal(getHoursRoot().get(Hours_.employee).get(Employee_.userName), userName));
            return this;
        }

        public ProjectQuery fetchManagers() {
            if (managersPath == null) {
                managersPath = (Path<Employee>)getRoot().fetch(Project_.managers, JoinType.LEFT);
            }
            if (masterProjectManagersPath == null) {
                fetchMasterProject();
                masterProjectManagersPath = (Path<Employee>) getRoot().fetch(Project_.master, JoinType.LEFT).fetch(Project_.managers, JoinType.LEFT);
            }
            return this;
        }

        public ProjectQuery fetchTeam() {
            if (teamMembersPath == null) {
                teamMembersPath = (Path<Employee>)getRoot().fetch(Project_.team, JoinType.LEFT);
            }
            return this;
        }

        public ProjectQuery fetchMasterProject() {
            if (masterProjectPath == null) {
                masterProjectPath = (Path<Project>)getRoot().fetch(Project_.master, JoinType.LEFT);
            }
            return this;
        }

        private Root<Hours> getHoursRoot() {
            if (hoursRoot == null) {
                hoursRoot = getCriteriaQuery().from(Hours.class);
                add(getCriteriaBuilder().equal(hoursRoot.get(Hours_.project).get(Project_.id), getRoot().get(Project_.id)));
            }
            return hoursRoot;
        }

        public List<Tuple> getManagers() {
            Expression<Set<Employee>> managersSelector = getRoot().get(Project_.managers);
            Expression<Employee> teamSelector = getRoot().join(Project_.team);
            getTupleCriteriaQuery().multiselect(teamSelector, managersSelector).distinct(true);
            addWhereClause(getTupleCriteriaQuery());
            return createQuery(getTupleCriteriaQuery(), false).getResultList();
        }

        public ProjectQuery fetchAccountableHours() {
            if (hourTypePath == null) {
                hourTypePath = (Path<HourType>)getRoot().fetch(Project_.accountableHours, JoinType.LEFT);
            }
            return this;
        }

        public ProjectQuery projectIds(List<Long> projectIds) {
            addInPredicate(getRoot().get(Project_.id), projectIds);
            return this;
        }

    }

}
