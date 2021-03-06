package com.artezio.arttime.test.utils.security.runas;

import com.artezio.arttime.security.auth.UserRoles;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RunAs;
import javax.ejb.Stateless;

@Stateless
@RunAs(UserRoles.EXEC_ROLE)
@PermitAll
public class RunAsExec extends RunAsRole {}