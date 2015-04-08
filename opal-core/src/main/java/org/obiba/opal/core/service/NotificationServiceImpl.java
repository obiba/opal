package org.obiba.opal.core.service;

import org.apache.commons.lang.StringUtils;
import org.obiba.opal.core.cfg.InvalidConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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

    @Value("${OPAL_HOME}/conf/opal-notification.properties")
    private File notificationRecipientsFile;

    private Map<String,List<String>> projectToRecipientsMap = new HashMap<>();

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);
    private static final String PROJECT_RECIPIENT_PREFIX = "notification.recipient.project.";

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
    public void sendProjectNotification(String project, String subject, String text) {
        sendEmail(notificationFrom, getProjectRecipients(project), subject, text);
    }

    private List<String> getProjectRecipients(String project) {
        List<String> recipients = projectToRecipientsMap.get(project);
        if (recipients == null) {
            //fallback recipient
            return Collections.singletonList(notificationRecipient);
        }

        return recipients;
    }

    @Override
    public boolean isNotificationEnabled() {
        return StringUtils.isNotBlank(notificationRecipient) && StringUtils.isNotBlank(notificationFrom);
    }

    @PostConstruct
    public void initFileRecipients() {
        if (notificationRecipientsFile.exists()) {
            log.debug("Reading notification recipients file");
            Properties props = new Properties();
            try (InputStream in = new FileInputStream(notificationRecipientsFile)) {
                props.load(in);
            } catch(IOException e) {
                throw new InvalidConfigurationException("Error reading Opal configuration file.", e);
            }

            int prefixLength = PROJECT_RECIPIENT_PREFIX.length();

            for (String key: props.stringPropertyNames()) {
                if (key.length() > prefixLength && key.startsWith(PROJECT_RECIPIENT_PREFIX)) {
                    String project = key.substring(prefixLength);
                    List<String> recipients = Arrays.asList(props.getProperty(key).split(" "));
                    if (!recipients.isEmpty()) {
                        projectToRecipientsMap.put(project, recipients);
                    }
                }
            }
        }
    }

}
