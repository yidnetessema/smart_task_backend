package com.inkomoko.smarttask.emailservice.email.services;


import com.inkomoko.smarttask.emailservice.email.enums.EmailStatus;
import com.inkomoko.smarttask.emailservice.email.models.EmailModel;
import com.inkomoko.smarttask.emailservice.email.repositories.EmailRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final EmailRepository emailRepository;

    private final JavaMailSender emailSender;
    private final TemplateEngine templateEngine;

    @KafkaListener(topics = "${spring.kafka.topic.name}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(Map<String, Object> req) {
        System.out.println("Consume email: ");
        EmailModel previous = emailRepository.findByReference(req.get("reference").toString());
        if(previous != null) {
            if(previous.getStatus().equals(EmailStatus.CREATED)) {
                previous.setUpdatedAt(LocalDateTime.now());
                previous.setStatus(EmailStatus.PENDING);
                emailRepository.save(previous);

                try {
                    JSONObject information = new JSONObject(previous.getInformation());
                    Map<String, Object> parameters = new JSONObject(Optional.ofNullable(information.get("parameters")).map(Object::toString).orElse("{}")).toMap();
                    sendHtmlEmail(req.get("receiver").toString(), previous.getSubject(), parameters, Optional.ofNullable(information.get("template")).map(Object::toString).orElse(""));
                    previous.setStatus(EmailStatus.SENT);
                    previous.setUpdatedAt(LocalDateTime.now());
                    emailRepository.save(previous);
                } catch (MessagingException e) {

                    e.printStackTrace();
                    logger.error(e.getMessage());
                }
            }
        }
    }

    public void sendHtmlEmail(String to, String subject, Map<String, Object> variables, String template) throws MessagingException {
        Context context = new Context();
        context.setVariables(variables);
        String htmlContent = templateEngine.process(template, context);

        System.out.println(htmlContent + variables.toString());

        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
        helper.setFrom("gymcliques@ethiomega.com");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        emailSender.send(message);
    }
}
