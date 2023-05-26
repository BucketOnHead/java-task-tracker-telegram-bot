package com.github.bucketonhead.service.processor.task;

import com.github.bucketonhead.entity.AppUser;
import com.github.bucketonhead.service.processor.CommandProcessor;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface TaskService extends CommandProcessor {
    void processHelpCommand(Message msg);

    void processNewTaskCommand(AppUser user, Message msg);

    void processMyTasksCommand(AppUser user, Message msg);

    void processDoneTaskCommand(AppUser user, Message msg);
}
