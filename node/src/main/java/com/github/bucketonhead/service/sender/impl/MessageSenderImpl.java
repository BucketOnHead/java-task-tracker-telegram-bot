package com.github.bucketonhead.service.sender.impl;

import com.github.bucketonhead.service.rabbitmq.producer.ProducerService;
import com.github.bucketonhead.service.sender.MessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageSenderImpl implements MessageSender {
    private final ProducerService producerService;

    @Override
    public void send(SendMessage msg) {
        log.info("Sending message: chat_id={}, text='{}'", msg.getChatId(), msg.getText());
        log.debug("Sending message: {}", msg);
        producerService.producerAnswer(msg);
    }
}
