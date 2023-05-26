package com.github.bucketonhead.service.processor.task.impl;

import com.github.bucketonhead.dao.AppTaskJpaRepository;
import com.github.bucketonhead.dao.AppUserJpaRepository;
import com.github.bucketonhead.entity.AppTask;
import com.github.bucketonhead.entity.AppUser;
import com.github.bucketonhead.entity.enums.BotState;
import com.github.bucketonhead.service.processor.basic.enums.BasicCommand;
import com.github.bucketonhead.service.processor.task.TaskService;
import com.github.bucketonhead.service.processor.task.enums.TaskCommand;
import com.github.bucketonhead.service.sender.MessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
    private final MessageSender msgSender;
    private final AppUserJpaRepository appUserJpaRepository;
    private final AppTaskJpaRepository appTaskJpaRepository;

    @Override
    public void processCommand(AppUser user, Message msg) {
        if (!TaskCommand.isCommandPattern(msg.getText())) {
            var responseMessage = "Я бы с удовольствие поговорил, " +
                    "но я просто бот ☺";
            msgSender.send(responseMessage, msg.getChatId());
            return;
        }

        var cmd = TaskCommand.fromValue(msg.getText());
        if (cmd == null) {
            var responseMessage = "Команда не распознана!";
            msgSender.sendError(responseMessage, msg.getChatId());
            return;
        }

        if (TaskCommand.HELP == cmd) {
            processHelpCommand(msg);
        } else if (TaskCommand.NEW_TASK == cmd) {
            processNewTaskCommand(user, msg);
        } else if (TaskCommand.MY_TASKS == cmd) {
            processMyTasksCommand(user, msg);
        } else if (TaskCommand.DONE_TASK == cmd) {
            processDoneTaskCommand(user, msg);
        } else if (TaskCommand.BACK == cmd) {
            processBackCommand(user, msg);
            processHelpCommand(msg);
        } else {
            var responseMessage = "Если вы видите это сообщение, " +
                    "значит разработчик забыл подключить " +
                    "эту функциональность, попробуйте позже!";
            msgSender.sendError(responseMessage, msg.getChatId());
        }
    }

    @Override
    public void processHelpCommand(Message msg) {
        Map<Object, String> cmdDesc = new LinkedHashMap<>();
        cmdDesc.put(BasicCommand.MAIN, "вернуться в главное меню");
        cmdDesc.put(TaskCommand.NEW_TASK, "создать простую задачу без привязки ко времени");
        cmdDesc.put(TaskCommand.MY_TASKS, "получить список своих задач");
        cmdDesc.put(TaskCommand.DONE_TASK, "отметить задачу выполненной");
        cmdDesc.put(TaskCommand.HELP, "получить список доступных команд");

        var responseMessage = "Список доступных команд:" + cmdDesc.entrySet()
                .stream()
                .map(entry -> String.format("%n%n%s - %s.", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining());
        msgSender.send(responseMessage, msg.getChatId());
    }

    @Override
    public void processNewTaskCommand(AppUser user, Message msg) {
        String responseMessage;
        if (BotState.WAIT_TASK == user.getState()) {
            if (TaskCommand.isCommandPattern(msg.getText())) {
                var cmd = TaskCommand.fromValue(msg.getText());
                if (cmd != null) {
                    processBackCommand(user, msg);

                    if (TaskCommand.BACK == cmd) {
                        processHelpCommand(msg);
                    } else {
                        processCommand(user, msg);
                    }
                    return;
                }
            }

            AppTask transientAppTask = AppTask.builder()
                    .description(msg.getText())
                    .creator(user)
                    .build();
            appTaskJpaRepository.save(transientAppTask);
            responseMessage = String.format("" +
                    "Записали 😉 Что-то ещё?%n%n" +
                    "%s - назад", TaskCommand.BACK);
        } else {
            user.setState(BotState.WAIT_TASK);
            appUserJpaRepository.save(user);
            responseMessage = "Что записать?";
        }

        msgSender.send(responseMessage, msg.getChatId());
    }

    @Override
    public void processMyTasksCommand(AppUser user, Message msg) {
        String responseMessage;
        var tasks = user.getTasks();
        if (tasks == null || tasks.isEmpty()) {
            responseMessage = "Вы ещё не создали ни одной задачи 🥺\n\n" +
                    TaskCommand.NEW_TASK + " - создать задачу";
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("Ваши задачи:");
            for (int i = 0; i < tasks.size(); i++) {
                sb.append(String.format("%n%n📌 Задача#%d%n", i + 1));
                sb.append(tasks.get(i).getDescription());
            }
            responseMessage = sb.toString();
        }

        msgSender.send(responseMessage, msg.getChatId());
    }

    @Override
    public void processDoneTaskCommand(AppUser user, Message msg) {
        String responseMessage;
        if (BotState.DONE_TASK == user.getState()) {
            if (TaskCommand.isCommandPattern(msg.getText())) {
                var cmd = TaskCommand.fromValue(msg.getText());
                if (cmd != null) {
                    processBackCommand(user, msg);

                    if (TaskCommand.BACK == cmd) {
                        processHelpCommand(msg);
                    } else {
                        processCommand(user, msg);
                    }
                    return;
                }
            }

            var tasks = user.getTasks();

            var choose = msg.getText();
            int taskNumber;
            try {
                taskNumber = Integer.parseInt(choose);
            } catch (NumberFormatException e) {
                responseMessage = "Нужно указать номер задачи (1 - " + tasks.size() + ")";
                msgSender.sendError(responseMessage, msg.getChatId());
                return;
            }

            if (taskNumber < 1 || taskNumber > tasks.size()) {
                responseMessage = "Неверный номер задачи! Укажите число от 1 до " + tasks.size();
                msgSender.sendError(responseMessage, msg.getChatId());
                return;
            }

            tasks.remove(taskNumber - 1);
            appUserJpaRepository.save(user);
            if (tasks.isEmpty()) {
                responseMessage = String.format("" +
                                "Готово! Вычеркнули задачу %d " +
                                "из списка задач, больше задач нет 🙃",
                        taskNumber);
                msgSender.send(responseMessage, msg.getChatId());
                processBackCommand(user, msg);
                processHelpCommand(msg);
                return;
            } else {
                processMyTasksCommand(user, msg);
                responseMessage = String.format("" +
                                "Готово! Вычеркнули задачу %d " +
                                "из списка задач, отметить ещё одну?%n" +
                                "%n%s - выйти",
                        taskNumber,
                        TaskCommand.BACK);
            }
        } else {
            var tasks = user.getTasks();
            if (tasks.isEmpty()) {
                responseMessage = "У вас нет ни одной задачи 🥺\n\n" +
                        TaskCommand.NEW_TASK + " - создать задачу";
                msgSender.send(responseMessage, msg.getChatId());
                return;
            }
            processMyTasksCommand(user, msg);

            user.setState(BotState.DONE_TASK);
            appUserJpaRepository.save(user);
            responseMessage = "Какую задачу вычеркнуть? (1 - " + tasks.size() + ")";
        }

        msgSender.send(responseMessage, msg.getChatId());
    }

    @Override
    public void processBackCommand(AppUser user, Message msg) {
        String responseMessage;
        if (BotState.TASK_MODE == user.getState()) {
            responseMessage = "Вы уже в режиме задач 😉";
        } else {
            user.setState(BotState.TASK_MODE);
            appUserJpaRepository.save(user);
            responseMessage = "Вернули вас назад!";
        }

        msgSender.send(responseMessage, msg.getChatId());
    }
}
