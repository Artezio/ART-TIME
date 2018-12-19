package com.artezio.arttime.services.integration;

public class ProjectNotFoundException extends Exception {
    public ProjectNotFoundException() {
        super("Project not found");
    }
}
