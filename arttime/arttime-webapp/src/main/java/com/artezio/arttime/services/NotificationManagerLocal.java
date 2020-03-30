package com.artezio.arttime.services;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.Period;
import com.artezio.arttime.datamodel.Project;

import javax.mail.MessagingException;
import java.util.List;

public interface NotificationManagerLocal {

    void notifyAboutWorkTimeProblems(List<Employee> employees, Period period, String notificationComment);
    void requestWorkTimeReport(String recipientEmail, Period period) throws MessagingException;
    void notifyAboutTeamChanges(Project project, List<Employee> formerTeamMembers, List<Employee> newTeamMembers);
    void notifyAboutIncorrectTimesheet(List<Employee> employees, Period period);
    void notifyAboutUnapprovedHours(List<Employee> managers, Period period);

}
