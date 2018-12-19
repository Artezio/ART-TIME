package com.artezio.arttime.services.mailing;

/**
 * The Enum MailTemplate.
 */
public enum MailTemplate {

    REQUEST_REPORT_BODY("mail_templates/requestReportBody.ftl"),
    REQUEST_REPORT_SUBJECT("mail_templates/requestReportSubject.ftl"),
    TIME_PROBLEM_SUBJECT("mail_templates/timeProblemNotificationSubject.ftl"),
    TIME_PROBLEM_BODY("mail_templates/timeProblemNotificationBody.ftl"),
    TIME_PROBLEM_BODY_FOR_PM("mail_templates/timeProblemNotificationBodyForPM.ftl"),
    TEAM_SYNCHRONIZATION_SUBJECT("mail_templates/teamSynchronizationSubject.ftl"),
    TEAM_SYNCHRONIZATION_BODY("mail_templates/teamSynchronizationBody.ftl");

    private String fileName;

    /**
     * Instantiates a new mail template.
     *
     * @param fileName the file name
     */
    MailTemplate(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Gets the file name.
     *
     * @return the file name
     */
    public String getFileName() {
        return fileName;
    }

}
