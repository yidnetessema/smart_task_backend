package com.inkomoko.smarttask.email.services;

import com.inkomoko.smarttask.user.repository.UserRepository;
import com.inkomoko.smarttask.email.enums.EmailStatus;
import com.inkomoko.smarttask.email.models.EmailModel;
import com.inkomoko.smarttask.email.repositories.EmailRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final EmailRepository emailRepository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;

    @Value("${spring.kafka.topic.name}")
    private String topicName;

    private final KafkaTemplate<String, Map<String, Object>> kafkaTemplate;

    public String sendEmail(Map<String, Object> emailDto) {

        String reference = UUID.randomUUID().toString();
//        emailDto.setReference(reference);

        emailDto.put("reference", reference);


        //send kafka message
        Message<Map<String, Object>> message = MessageBuilder.withPayload(emailDto).setHeader(KafkaHeaders.TOPIC, topicName).build();
        try {
            kafkaTemplate.send(message);
//            EmailModel email = modelMapper.map(emailDto, EmailModel.class);

            EmailModel email = EmailModel.builder()
                    .reference(reference)
                    .subject(emailDto.get("subject").toString())
                    .body(emailDto.get("body").toString())
                    .information(emailDto.get("information").toString())
                    .receiver(emailDto.get("receiver").toString())
                    .status(EmailStatus.CREATED)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            emailRepository.save(email);
        } catch (Exception e) {
            e.printStackTrace();

//            System.out.println("Sending email: " + emailDto.getReceiver());

        }

        return emailDto.get("reference").toString();
    }
}
