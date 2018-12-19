package com.artezio.arttime.services.integration;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.Project;

import java.util.List;

public interface TeamTrackingSystem extends TrackingSystem {
    List<Employee> getTeamByGroupCode(String groupCode);
    List<Employee> getTeamByDepartment(String department);
}
