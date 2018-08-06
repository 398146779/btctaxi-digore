package com.btctaxi.gate.service;

import genesis.gate.config.DistConfig;
import genesis.gate.config.EmailTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;
import java.util.Locale;

@Service
public class EmailService {
    private Logger log = LoggerFactory.getLogger(EmailService.class);

    private JavaMailSender smtp;
    private MessageSource emailTitle;
    private EmailTemplate emailTemplate;
    private DistConfig distConfig;

    public EmailService(JavaMailSender smtp, @Qualifier("email.title") MessageSource emailTitle, EmailTemplate emailTemplate, DistConfig distConfig) {
        this.smtp = smtp;
        this.emailTitle = emailTitle;
        this.emailTemplate = emailTemplate;
        this.distConfig = distConfig;
    }

    @Async
    public void send(String dest, String title, String content) {
        try {
            MimeMessage message = smtp.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);
            helper.setFrom(distConfig.getEmailSender());
            helper.setTo(dest);
            helper.setSubject(title);
            helper.setText(content, true);
            smtp.send(message);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
    }

    @Async
    @Retryable(value = Throwable.class, backoff = @Backoff(delay = 1000L, multiplier = 1))
    public void sendTemplate(String dest, String name, String locale, Object... params) {
        try {
            String[] lc = locale.split("_");
            String title = emailTitle.getMessage(name, null, lc.length == 2 ? new Locale(lc[0], lc[1]) : new Locale(locale));

            MimeMessage message = smtp.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);
            helper.setFrom(distConfig.getEmailSender());
            helper.setTo(dest);
            helper.setSubject(title);
            helper.setText(emailTemplate.format(name, locale, params), true);
            smtp.send(message);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
    }
}
