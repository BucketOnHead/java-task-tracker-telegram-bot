package com.github.bucketonhead.service.processor;

import com.github.bucketonhead.entity.AppUser;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface CommandProcessor {
    void processCommand(AppUser user, Message message);
}
