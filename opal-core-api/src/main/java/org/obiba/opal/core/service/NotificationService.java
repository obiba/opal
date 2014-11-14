package org.obiba.opal.core.service;

import java.util.List;

/**
 * Service for notifications
 */
public interface NotificationService {

    public static final String NOTIFICATION_EMAIL_ATTRIBUTE = "notification_email";

    /**
     * Sends an email to the target recipient.
     * @param from from email address
     * @param recipients target email addresses
     * @param subject subject of the email
     * @param text text to be included
     */
    void sendEmail(String from, List<String> recipients, String subject, String text);

    /**
     * Sends a notification email to the configured recipient.
     * @param subject subject of the email
     * @param text text to be included
     */
    void sendNotification(String subject, String text);

    /**
     * @return true if notification is enabled
     */
    boolean isNotificationEnabled();

}
