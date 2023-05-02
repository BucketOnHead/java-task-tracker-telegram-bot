package com.github.bucketonhead.utils;

import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@UtilityClass
public class MessageUtils {

    public SendMessage buildTextMessage(Update update, String text) {
        return SendMessage.builder()
                .chatId(update.getMessage().getChatId())
                .text(text)
                .build();
    }
}
