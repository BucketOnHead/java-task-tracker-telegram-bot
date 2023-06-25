package com.github.bucketonhead.service.rabbitmq.consumer.impl;

import com.github.bucketonhead.consts.RabbitQueue;
import com.github.bucketonhead.controller.UpdateController;
import com.github.bucketonhead.service.rabbitmq.consumer.AnswerConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnswerConsumerImpl implements AnswerConsumer {
    private final UpdateController updateController;

    @Override
    @RabbitListener(queues = RabbitQueue.ANSWER_MESSAGE)
    public void consume(SendMessage msg) {
        log.info("Received sendMessage: chat_id={}, text='{}'", msg.getChatId(), msg.getText());
        log.debug("Received sendMessage: {}", msg);
        updateController.setView(msg);
    }
}
