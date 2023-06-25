package com.github.bucketonhead.controller;

import com.github.bucketonhead.config.BotConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@Slf4j
@RequiredArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {
    private final BotConfig botConfig;
    private final UpdateController updateController;

    @Override
    public String getBotUsername() {
        return botConfig.getName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        log.info("Received update: id={}", update.getUpdateId());
        log.debug("Received update: {}", update);
        updateController.processUpdate(update);
    }

    public void sendBotMessageIgnoreException(SendMessage msg) {
        if (msg != null) {
            try {
                execute(msg);
                log.info("Message sent successfully: chat_id={}, text='{}'", msg.getChatId(), msg.getText());
                log.debug("Message sent successfully: {}", msg);
            } catch (TelegramApiException ex) {
                log.error("Failed to send message: {}", ex.getMessage());
            }
        }
    }
}
