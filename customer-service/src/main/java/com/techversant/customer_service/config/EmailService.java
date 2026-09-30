/**
 * @file EmailService.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @version 1.0
 * @description This service handles sending HTML emails using JavaMailSender.
 */

package com.techversant.customer_service.config;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender javaMailSender;

    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    /**
     * Sends an HTML email to the specified recipient.
     * This method constructs a MIME message, sets the recipient, subject, and HTML content,
     * and sends it using the {@link JavaMailSender}.
     * Any exceptions during email creation or sending are caught and logged.
     *
     * @param to the recipient's email address
     * @param subject the subject of the email
     * @param htmlContent the HTML content to be sent in the email body
     */
    public void sendHtml(String to, String subject, String htmlContent) {
        try{
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = HTML content
            javaMailSender.send(message);
        }
        catch (MessagingException | MailException e) {
            logger.error("Failed to send email.",e);
        }
    }
}
