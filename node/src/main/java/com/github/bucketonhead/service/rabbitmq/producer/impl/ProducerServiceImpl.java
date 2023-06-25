package com.github.bucketonhead.service.rabbitmq.producer.impl;

import com.github.bucketonhead.consts.RabbitQueue;
import com.github.bucketonhead.service.rabbitmq.producer.ProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProducerServiceImpl implements ProducerService {
    private final RabbitTemplate rabbitTemplate;

    @Override
    public void producerAnswer(SendMessage msg) {
        log.info("Producing message: chat_id={}, text='{}'", msg.getChatId(), msg.getText());
        log.debug("Producing message: {}", msg);
        rabbitTemplate.convertAndSend(RabbitQueue.ANSWER_MESSAGE, msg);
    }
}
