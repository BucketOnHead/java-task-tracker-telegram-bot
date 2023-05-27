package com.github.bucketonhead.service.sender.impl;

import com.github.bucketonhead.service.rabbitmq.ProducerService;
import com.github.bucketonhead.service.sender.MessageSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Service
@RequiredArgsConstructor
public class MessageSenderImpl implements MessageSender {
    private final ProducerService producerService;

    @Override
    public void send(SendMessage msg) {
        producerService.producerAnswer(msg);
    }
}
