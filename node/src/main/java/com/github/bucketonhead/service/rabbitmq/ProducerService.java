package com.github.bucketonhead.service.rabbitmq;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public interface ProducerService {
    void producerAnswer(SendMessage sendMessage);
}
