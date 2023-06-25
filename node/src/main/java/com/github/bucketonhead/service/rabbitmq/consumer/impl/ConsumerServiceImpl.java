package com.github.bucketonhead.service.rabbitmq.consumer.impl;

import com.github.bucketonhead.consts.RabbitQueue;
import com.github.bucketonhead.service.processor.main.MainService;
import com.github.bucketonhead.service.rabbitmq.consumer.ConsumerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConsumerServiceImpl implements ConsumerService {
    private final MainService mainService;

    @Override
    @RabbitListener(queues = RabbitQueue.TEXT_MESSAGE_UPDATE)
    public void consumeTextMessageUpdate(Update update) {
        var msg = update.getMessage();
        log.info("Received sendMessage: chat_id={}, text='{}'", msg.getChatId(), msg.getText());
        log.debug("Received sendMessage: {}", msg);
        try {
            mainService.processTextMessage(update);
        } catch (RuntimeException ex) {
            log.error("main service error: {}", ex.getMessage());
        }
    }
}
