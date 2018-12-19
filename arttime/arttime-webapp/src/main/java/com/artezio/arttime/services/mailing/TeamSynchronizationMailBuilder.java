package com.artezio.arttime.services.mailing;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.Project;

import java.util.List;

import static com.artezio.arttime.services.mailing.MailTemplate.TEAM_SYNCHRONIZATION_BODY;
import static com.artezio.arttime.services.mailing.MailTemplate.TEAM_SYNCHRONIZATION_SUBJECT;

public class TeamSynchronizationMailBuilder extends MailBuilder {

    public TeamSynchronizationMailBuilder(MailTemplateManager mailTemplateManager) {
        this.mailTemplateManager = mailTemplateManager;
    }

    public TeamSynchronizationMailBuilder setManagedProject(Project managedProject) {
        putParameter("managedProject", managedProject);
        return this;
    }

    public TeamSynchronizationMailBuilder setNewEmployees(List<Employee> newEmployees) {
        putParameter("newEmployees", newEmployees);
        return this;
    }

    public TeamSynchronizationMailBuilder setClosedEmployees(List<Employee> closedEmployees) {
        putParameter("closedEmployees", closedEmployees);
        return this;
    }

    public TeamSynchronizationMailBuilder setAppUrl(String appUrl) {
        putParameter("appUrl", appUrl);
        return this;
    }

    public TeamSynchronizationMailBuilder setSenderEmailAddress(String senderEmailAddress) {
        this.senderEmailAddress = senderEmailAddress;
        return this;
    }

    public TeamSynchronizationMailBuilder setRecipientEmailAddress(String recipientEmailAddress) {
        this.recipientEmailAddress = recipientEmailAddress;
        return this;
    }

    public Mail build() {
        return super.build(TEAM_SYNCHRONIZATION_SUBJECT.getFileName(), TEAM_SYNCHRONIZATION_BODY.getFileName());
    }

}
