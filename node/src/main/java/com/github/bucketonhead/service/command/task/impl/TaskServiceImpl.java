package com.github.bucketonhead.service.command.task.impl;

import com.github.bucketonhead.dao.AppTaskJpaRepository;
import com.github.bucketonhead.dao.AppUserJpaRepository;
import com.github.bucketonhead.entity.AppTask;
import com.github.bucketonhead.entity.AppUser;
import com.github.bucketonhead.entity.enums.BotState;
import com.github.bucketonhead.service.command.task.TaskService;
import com.github.bucketonhead.service.command.task.enums.TaskCommand;
import com.github.bucketonhead.service.command.main.enums.ServiceCommand;
import com.github.bucketonhead.service.rabbitmq.ProducerService;
import com.github.bucketonhead.utils.ResponseMessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
    private final ProducerService producerService;
    private final AppUserJpaRepository appUserJpaRepository;
    private final AppTaskJpaRepository appTaskJpaRepository;

    @Override
    public void processCommand(AppUser user, Message msg) {
        if (!TaskCommand.isCommandPattern(msg.getText())) {
            var responseMessage = "Я бы с удовольствие поговорил, " +
                    "но я просто бот ☺";
            sendMessage(responseMessage, msg.getChatId());
            return;
        }

        var command = TaskCommand.fromValue(msg.getText());
        if (command == null) {
            var text = "Команда не распознана!";
            var responseMessage = ResponseMessageUtils.buildErrorMessage(text);
            sendMessage(responseMessage, msg.getChatId());
            return;
        }

        String responseMessage;
        if (TaskCommand.HELP == command) {
            responseMessage = processHelpCommand();
        } else if (TaskCommand.NEW_TASK == command) {
            responseMessage = processNewTaskCommand(user, msg);
        } else if (TaskCommand.MY_TASKS == command) {
            responseMessage = processMyTasksCommand(user);
        } else if (TaskCommand.DONE_TASK == command) {
            responseMessage = processDoneTaskCommand(user, msg);
        } else {
            var text = "Если вы видите это сообщение, " +
                    "значит разработчик забыл подключить " +
                    "эту функциональность, попробуйте позже!";
            responseMessage = ResponseMessageUtils.buildErrorMessage(text);
        }
        sendMessage(responseMessage, msg.getChatId());
    }

    @Override
    public String processHelpCommand() {
        Map<TaskCommand, String> cmdDesc = new LinkedHashMap<>();
        cmdDesc.put(TaskCommand.NEW_TASK, "создать простую задачу без привязки ко времени");
        cmdDesc.put(TaskCommand.DONE_TASK, "отметить задачу выполненной");
        cmdDesc.put(TaskCommand.MY_TASKS, "получить список своих задач");
        cmdDesc.put(TaskCommand.HELP, "получить список доступных команд");

        return "Список доступных команд:" + cmdDesc.entrySet()
                .stream()
                .map(entry -> String.format("%n%n%s - %s.", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining());
    }

    @Override
    public String processNewTaskCommand(AppUser user, Message msg) {
        String responseMessage;
        if (BotState.WAIT_TASK == user.getState()) {
            AppTask transientAppTask = AppTask.builder()
                    .description(msg.getText())
                    .creator(user)
                    .build();
            appTaskJpaRepository.save(transientAppTask);
            responseMessage = String.format("" +
                    "Записали 😉 Что-то ещё?%n%n" +
                    "%s - выйти", ServiceCommand.CANCEL);
        } else {
            user.setState(BotState.WAIT_TASK);
            appUserJpaRepository.save(user);
            responseMessage = "Что записать?";
        }

        return responseMessage;
    }

    @Override
    public String processMyTasksCommand(AppUser user) {
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

        return responseMessage;
    }

    @Override
    public String processDoneTaskCommand(AppUser user, Message msg) {
        String responseMessage;
        if (BotState.DONE_TASK == user.getState()) {
            var tasks = user.getTasks();

            var choose = msg.getText();
            int taskNumber;
            try {
                taskNumber = Integer.parseInt(choose);
            } catch (NumberFormatException e) {
                var text = "Нужно указать номер задачи (1 - " + tasks.size() + ")";
                return ResponseMessageUtils.buildErrorMessage(text);
            }


            if (taskNumber < 1 || taskNumber > tasks.size()) {
                var text = "Неверный номер задачи! Укажите число от 1 до " + tasks.size();
                return ResponseMessageUtils.buildErrorMessage(text);
            }

            tasks.remove(taskNumber - 1);
            appUserJpaRepository.save(user);

            if (tasks.isEmpty()) {
                responseMessage = String.format("" +
                                "Готово! Вычеркнули задачу %d " +
                                "из списка задач, больше задач нет 🙃",
                        taskNumber);
                user.setState(BotState.TASK_MODE);
                appUserJpaRepository.save(user);
            } else {
                responseMessage = String.format("" +
                                "Готово! Вычеркнули задачу %d " +
                                "из списка задач, отметить ещё одну?%n" +
                                "%n%s - выйти",
                        taskNumber,
                        ServiceCommand.CANCEL);
            }
        } else {
            var tasks = user.getTasks();
            if (tasks.isEmpty()) {
                return "У вас нет ни одной задачи 🥺\n\n" +
                        TaskCommand.NEW_TASK + " - создать задачу";
            }

            user.setState(BotState.DONE_TASK);
            appUserJpaRepository.save(user);
            responseMessage = "Какую задачу вычеркнуть? (1 - " + tasks.size() + ")";
        }

        return responseMessage;
    }

    private void sendMessage(String text, Long chatId) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();
        producerService.producerAnswer(sendMessage);
    }
}
