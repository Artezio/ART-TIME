package com.artezio.arttime.services.integration.spi;

public final class UserInfo {
    private final String username;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String department;

    public UserInfo(String username, String firstName, String lastName, String email, String department) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.department = department;
    }

    public String getUsername() {
        return username;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getDepartment() {
        return department;
    }

}
