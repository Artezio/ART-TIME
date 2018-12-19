package com.artezio.arttime.services.synchronization;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Singleton;

import static com.artezio.arttime.security.auth.UserRoles.ADMIN_ROLE;
import static com.artezio.arttime.security.auth.UserRoles.SYSTEM_ROLE;

@Singleton
public class SynchronizerSync extends Synchronizer {

    @Override
    @RolesAllowed({SYSTEM_ROLE, ADMIN_ROLE})
    public void synchronize() {
        super.synchronize();
    }

}
