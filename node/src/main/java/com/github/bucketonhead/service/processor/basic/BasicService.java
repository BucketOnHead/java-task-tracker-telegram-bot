package com.github.bucketonhead.service.processor.basic;

import com.github.bucketonhead.entity.AppUser;
import com.github.bucketonhead.service.processor.CommandProcessor;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface BasicService extends CommandProcessor {
    void processTaskModeCommand(AppUser user, Message msg);

    void processMainCommand(AppUser user, Message msg);

    void processHelpCommand(Message msg);

    void processStartCommand(AppUser user, Message msg);

    void processDeleteCommand(AppUser user, Message msg);
}
