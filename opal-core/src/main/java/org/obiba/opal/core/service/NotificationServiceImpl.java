package org.obiba.opal.core.service;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@Component
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private MailSender mailSender;

    @Value("${org.obiba.opal.notification.recipient:}")
    private String notificationRecipient;

    @Value("${org.obiba.opal.notification.from:}")
    private String notificationFrom;

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Override
    public void sendEmail(String from, List<String> recipients, String subject, String text) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setSubject(subject);
        message.setText(text);

        for(String emailAddress : recipients) {
            message.setTo(emailAddress);
            try {
                mailSender.send(message);
            } catch (MailException ex) {
                log.error("Email notification not sent to {}. subject: {} error: {}",
                        recipients.toString(), subject, ex.getMessage());
            }
        }
    }

    @Override
    public void sendNotification(String subject, String text) {
        List<String> recipients = new ArrayList<>();
        recipients.add(notificationRecipient);
        sendEmail(notificationFrom, recipients, subject, text);
    }

    @Override
    public boolean isNotificationEnabled() {
        return StringUtils.isNotBlank(notificationRecipient) && StringUtils.isNotBlank(notificationFrom);
    }
}
