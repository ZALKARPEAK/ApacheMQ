package com.example.apachemq.consumer;

import com.example.apachemq.Entity.MessageEntity;
import com.example.apachemq.Entity.MessageRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MessageController {

    private final MessageRepo messageRepo;
    private final JmsTemplate jmsTemplate;

    @Value("${activemq.queue.name}")
    private String destinationQueue;

    @GetMapping("/send")
    public String sendMessage() {
        final String message = "Hello, random String!" + UUID.randomUUID();

        jmsTemplate.send(destinationQueue, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                return session.createTextMessage(message);
            }
        });

        log.info("Сообщение успешно отправлено: " + message);

        return message;
    }

    @GetMapping("/receive")
    public String receiveMessage() {
        Message message = jmsTemplate.receive(destinationQueue);
        if (message instanceof TextMessage) {
            try {
                String text = ((TextMessage) message).getText();
                System.out.println("Получено сообщение: " + text);
                MessageEntity messageEntity = new MessageEntity();
                messageEntity.setText(text);
                messageRepo.save(messageEntity);
                return text;
            } catch (JMSException e) {
                throw new RuntimeException(e);
            }
        }
        return "Нет новых сообщений";
    }
}