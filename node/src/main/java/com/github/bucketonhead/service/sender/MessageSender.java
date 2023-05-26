package com.github.bucketonhead.service.sender;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public interface MessageSender {
    void send(SendMessage msg);

    default void sendText(String text, Long chatId) {
        SendMessage msg = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();
        send(msg);
    }
}
