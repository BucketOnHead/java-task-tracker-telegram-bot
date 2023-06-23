package com.github.bucketonhead.service.rabbitmq.producer.impl;

import com.github.bucketonhead.service.rabbitmq.producer.UpdateProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
@Slf4j
@RequiredArgsConstructor
public class UpdateProducerImpl implements UpdateProducer {
    private final RabbitTemplate rabbitTemplate;

    @Override
    public void produce(String rabbitQueue, Update update) {
        var msg = update.getMessage();
        log.info("Producing message: from_id={}, text='{}'", msg.getFrom().getId(), msg.getText());
        log.debug("Producing message: {}", msg);
        rabbitTemplate.convertAndSend(rabbitQueue, update);
    }
}
