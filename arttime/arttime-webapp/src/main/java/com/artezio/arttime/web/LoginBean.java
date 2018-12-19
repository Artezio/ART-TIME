package com.artezio.arttime.web;

import com.artezio.arttime.security.auth.UserRoles;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Named
@SessionScoped
public class LoginBean implements Serializable {
    private static final long serialVersionUID = -7055525369625427016L;

    @Inject
    private ExternalContext externalContext;
    private static Map<String, String> outcomes;

    static {
        outcomes = new LinkedHashMap<>();
        outcomes.put(UserRoles.EXEC_ROLE, "manageEfforts");
        outcomes.put(UserRoles.PM_ROLE, "manageEfforts");
        outcomes.put(UserRoles.EMPLOYEE_ROLE, "timesheet");
        outcomes.put(UserRoles.ADMIN_ROLE, "settings");
    }

    public String logout() throws ServletException {
        HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
        request.logout();
        externalContext.invalidateSession();
        return "logout";
    }

    protected String calculateOutcome() {
        for (String role : outcomes.keySet()) {
            if (externalContext.isUserInRole(role)) {
                return outcomes.get(role);
            }
        }
        return null;
    }

}
