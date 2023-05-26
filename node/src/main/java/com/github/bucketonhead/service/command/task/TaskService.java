package com.github.bucketonhead.service.command.task;

import com.github.bucketonhead.entity.AppUser;
import com.github.bucketonhead.service.command.CommandProcessor;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface TaskService extends CommandProcessor {
    String processHelpCommand();

    String processNewTaskCommand(AppUser user, Message msg);

    String processMyTasksCommand(AppUser user);

    String processDoneTaskCommand(AppUser user, Message msg);
}
