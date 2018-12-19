package com.artezio.arttime.test.utils.security.runas;

import com.artezio.arttime.security.auth.UserRoles;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RunAs;
import javax.ejb.Stateless;

@Stateless
@RunAs(UserRoles.OFFICE_MANAGER)
@PermitAll
public class RunAsOfficeManager extends RunAsRole {}