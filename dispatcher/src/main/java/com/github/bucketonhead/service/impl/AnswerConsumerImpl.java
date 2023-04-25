package com.github.bucketonhead.service.impl;

import com.github.bucketonhead.constants.RabbitQueue;
import com.github.bucketonhead.controller.UpdateController;
import com.github.bucketonhead.service.AnswerConsumer;
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
    public void consume(SendMessage sendMessage) {
        log.debug("Получено сообщение[text='{}'] из очереди[name='{}'] для чата[id={}]",
                sendMessage.getText(), RabbitQueue.ANSWER_MESSAGE, sendMessage.getChatId());
        updateController.setView(sendMessage);
    }
}
