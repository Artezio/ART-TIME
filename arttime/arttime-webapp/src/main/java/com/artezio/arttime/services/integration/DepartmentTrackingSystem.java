package com.artezio.arttime.services.integration;

import java.util.Collection;

public interface DepartmentTrackingSystem extends TrackingSystem {
    Collection<String> getDepartments();
}
