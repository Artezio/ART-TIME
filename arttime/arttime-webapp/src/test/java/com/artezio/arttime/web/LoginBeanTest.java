package com.artezio.arttime.web;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.repositories.EmployeeRepository;
import com.artezio.arttime.security.auth.UserRoles;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import javax.faces.context.ExternalContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

import static junitx.util.PrivateAccessor.setField;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
public class LoginBeanTest {

    private LoginBean loginBean;

    private Employee loggedEmployee;

    private ExternalContext externalContext;

    @Before
    public void setUp() {
        loginBean = new LoginBean();
        externalContext = createMock(ExternalContext.class);
        Whitebox.setInternalState(loginBean, externalContext);
    }

    @Test
    public void testLogout() throws NoSuchFieldException, ServletException {
        externalContext.invalidateSession();
        HttpServletRequest request = createMock(HttpServletRequest.class);
        expect(externalContext.getRequest()).andReturn(request);
        request.logout();
        PowerMock.replayAll(externalContext, request);

        String actual = loginBean.logout();

        PowerMock.verifyAll();
        assertEquals("logout", actual);
    }

}
