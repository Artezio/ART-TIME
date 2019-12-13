package com.artezio.arttime.services.synchronization;

import javax.ejb.Local;

@Local
public interface EmployeeSynchronizerLocal {
    void synchronizeEmployees();
}
