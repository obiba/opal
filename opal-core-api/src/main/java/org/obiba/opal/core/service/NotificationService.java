package org.obiba.opal.core.service;

import java.util.List;

/**
 * Service for notifications
 */
public interface NotificationService {

    /**
     * Sends an email to the target recipient.
     * @param from from email address
     * @param recipients target email addresses
     * @param subject subject of the email
     * @param text text to be included
     */
    void sendEmail(String from, List<String> recipients, String subject, String text);

    /**
     * Sends a notification email about a project to the configured recipients.
     * @param project project that the email is about
     * @param subject subject of the email
     * @param text text to be included
     */
    void sendProjectNotification(String project, String subject, String text);

    /**
     * @return true if notification is enabled
     */
    boolean isNotificationEnabled();

}
