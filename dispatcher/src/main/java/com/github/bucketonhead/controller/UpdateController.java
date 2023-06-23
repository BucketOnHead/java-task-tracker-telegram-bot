package com.github.bucketonhead.controller;

import com.github.bucketonhead.consts.MessagePattern;
import com.github.bucketonhead.service.rabbitmq.producer.UpdateProducer;
import com.github.bucketonhead.utils.MessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@Slf4j
public class UpdateController {
    private final UpdateProducer updateProducer;
    private final TelegramBot telegramBot;

    public UpdateController(@Lazy TelegramBot telegramBot,
                            UpdateProducer updateProducer) {
        this.telegramBot = telegramBot;
        this.updateProducer = updateProducer;
    }

    public void setView(SendMessage msg) {
        log.info("View set: chat_id={}, text='{}'", msg.getChatId(), msg.getText());
        log.debug("View set: {}", msg);
        telegramBot.sendBotMessageIgnoreException(msg);
    }

    public void processUpdate(Update update) {
        if (update == null) {
            log.warn("Received null update");
            return;
        }

        log.info("Update processing: id={}", update.getUpdateId());
        log.debug("Update processing: {}", update);
        if (update.hasMessage()) {
            distributeMessageByType(update);
        } else {
            log.warn("Unsupported message type: {}", update);
        }
    }

    private void distributeMessageByType(Update upd) {
        log.info("Update distributing: id={}", upd.getUpdateId());
        log.debug("Update distributing: {}", upd);
        var msg = upd.getMessage();
        if (msg.hasText()) {
            processTextMessage(upd);
        } else {
            setUnsupportedMessageTypeView(upd);
        }
    }

    private void processTextMessage(Update upd) {
        updateProducer.produceTextMessage(upd);
        log.info("Update(TextMessage) processed: update_id={}, message_id={}",
                upd.getUpdateId(), upd.getMessage().getMessageId());
        log.debug("Update(TextMessage) processed: {}", upd);
    }

    private void setUnsupportedMessageTypeView(Update update) {
        String text = String.format(MessagePattern.ERROR, "Неподдерживаемый тип сообщения 🚫");
        setView(MessageUtils.buildTextMessage(update, text));
    }
}
