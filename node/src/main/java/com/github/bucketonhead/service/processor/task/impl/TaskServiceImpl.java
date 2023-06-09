package com.github.bucketonhead.service.processor.task.impl;

import com.github.bucketonhead.cache.AppCache;
import com.github.bucketonhead.dao.AppTaskJpaRepository;
import com.github.bucketonhead.dao.AppUserJpaRepository;
import com.github.bucketonhead.entity.task.AppTask;
import com.github.bucketonhead.entity.user.AppUser;
import com.github.bucketonhead.entity.user.enums.BotState;
import com.github.bucketonhead.service.processor.main.enums.AppCommand;
import com.github.bucketonhead.service.processor.task.TaskService;
import com.github.bucketonhead.service.sender.MessageSender;
import com.github.bucketonhead.utils.ResponseMessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.ArrayList;
import java.util.LinkedHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
    private final MessageSender msgSender;
    private final AppUserJpaRepository appUserJpaRepository;
    private final AppTaskJpaRepository appTaskJpaRepository;
    private final AppCache<Long, AppUser> appUserCache;

    @Override
    public void processCommand(AppUser user, Message msg) {
        if (!AppCommand.isCommandPattern(msg.getText())) {
            processNotCommand(msg);
            return;
        }

        var cmd = AppCommand.parseAppCommand(msg.getText());
        if (cmd == null) {
            processBadCommand(msg);
        } else if (AppCommand.BACK == cmd) {
            processBackCommand(user, msg);
            processHelpCommand(msg);
        } else if (AppCommand.DONE_TASK == cmd) {
            processDoneTaskCommand(user, msg);
        } else if (AppCommand.HELP == cmd) {
            processHelpCommand(msg);
        } else if (AppCommand.MY_TASKS == cmd) {
            processMyTasksCommand(user, msg);
        } else if (AppCommand.NEW_TASK == cmd) {
            processNewTaskCommand(user, msg);
        } else {
            processNotImplemented(msg);
        }
    }

    private void processNotCommand(Message msg) {
        log.info("Processing not command");
        var text = "Я бы с удовольствие поговорил, " +
                "но я просто бот, выполняющий команды ☺";
        msgSender.send(text, msg.getChatId());
    }

    private void processBadCommand(Message msg) {
        log.info("Processing bad command");
        var text = "Команда не распознана!";
        msgSender.sendError(text, msg.getChatId());
    }

    private void processNotImplemented(Message msg) {
        log.error("Processing not implemented");
        var text = "Если вы видите это сообщение, " +
                "значит разработчик забыл подключить " +
                "эту функциональность, попробуйте позже!";
        msgSender.sendError(text, msg.getChatId());
    }

    @Override
    public void processBackCommand(AppUser user, Message msg) {
        log.info("Processing back command");
        String responseMessage;
        if (BotState.TASK_MODE == user.getState()) {
            responseMessage = "Вы уже в режиме задач 😉";
        } else {
            user.setState(BotState.TASK_MODE);
            var savedUser = appUserJpaRepository.save(user);
            appUserCache.put(savedUser);

            responseMessage = "Вернули вас назад!";
        }

        msgSender.send(responseMessage, msg.getChatId());
    }

    @Override
    public void processDoneTaskCommand(AppUser user, Message msg) {
        log.info("Processing done task command");
        if (BotState.DONE_TASK == user.getState()) {
            var cmd = AppCommand.parseAppCommand(msg.getText());
            if (cmd != null) {
                if (AppCommand.BACK == cmd) {
                    processBackCommand(user, msg);
                    processHelpCommand(msg);
                } else {
                    processBackCommand(user, msg);
                    processCommand(user, msg);
                }
                return;
            }

            processDeleteTask(user, msg);
        } else {
            var tasks = user.getTasks();
            if (tasks.isEmpty()) {
                processNoTasks(msg);
                return;
            }

            user.setState(BotState.DONE_TASK);
            var savedUser = appUserJpaRepository.save(user);
            appUserCache.put(savedUser);

            processMyTasksCommand(user, msg);
            var text = "Какую задачу вычеркнуть? " +
                    "(от 1 до " + tasks.size() + ")";
            msgSender.send(text, msg.getChatId());
        }
    }

    private void processDeleteTask(AppUser user, Message msg) {
        log.info("Processing delete task");
        int taskNumber = processChooseTaskNumber(user, msg);
        if (taskNumber == -1) {
            return;
        }

        var tasks = user.getTasks();

        var task = tasks.get(taskNumber - 1);
        appTaskJpaRepository.deleteById(task.getId());

        user.getTasks().remove(task);
        appUserCache.put(user);

        if (tasks.isEmpty()) {
            user.setTasks(null);
            var text = "Готово! Вычеркнули задачу, больше задач нет 🙃";
            msgSender.send(text, msg.getChatId());
            processBackCommand(user, msg);
            processHelpCommand(msg);
        } else {
            processMyTasksCommand(user, msg);
            var text = "Готово! Вычеркнули задачу " +
                    "из списка задач, отметить ещё одну?\n\n" +
                    AppCommand.BACK + " - вернуться назад";
            msgSender.send(text, msg.getChatId());
        }
    }

    private int processChooseTaskNumber(AppUser user, Message msg) {
        log.info("Processing choose task number");
        int taskNumber;
        try {
            taskNumber = Integer.parseInt(msg.getText());
            if (taskNumber < 1 || taskNumber > user.getTasks().size()) {
                processUnknownTaskNumber(user, msg);
                return -1;
            }
        } catch (NumberFormatException ex) {
            processBadTaskNumber(user, msg);
            return -1;
        }

        return taskNumber;
    }

    private void processUnknownTaskNumber(AppUser user, Message msg) {
        log.info("Processing unknown task number");
        var text = "Неверный номер задачи! Укажите число " +
                "от 1 до " + user.getTasks().size();
        msgSender.sendError(text, msg.getChatId());
    }

    private void processBadTaskNumber(AppUser user, Message msg) {
        log.info("Processing bad task number");
        var text = "Нужно указать номер задачи " +
                "от 1 до " + user.getTasks().size();
        msgSender.sendError(text, msg.getChatId());
    }

    private void processNoTasks(Message msg) {
        log.info("Processing no tasks");
        var text = "У вас нет ни одной задачи 🥺\n\n" +
                AppCommand.NEW_TASK + " - создать задачу";
        msgSender.send(text, msg.getChatId());
    }

    @Override
    public void processHelpCommand(Message msg) {
        log.info("Processing help command");
        var cmdDesc = new LinkedHashMap<AppCommand, String>();
        cmdDesc.put(AppCommand.MAIN, "вернуться в главное меню");
        cmdDesc.put(AppCommand.NEW_TASK, "создать простую задачу без привязки ко времени");
        cmdDesc.put(AppCommand.MY_TASKS, "получить список своих задач");
        cmdDesc.put(AppCommand.DONE_TASK, "отметить задачу выполненной");
        cmdDesc.put(AppCommand.HELP, "получить список доступных команд");

        var text = ResponseMessageUtils.buildHelp(cmdDesc);
        msgSender.send(text, msg.getChatId());
    }

    @Override
    public void processMyTasksCommand(AppUser user, Message msg) {
        log.info("Processing my tasks command");
        String text;
        var tasks = user.getTasks();
        if (tasks == null || tasks.isEmpty()) {
            text = "Вы ещё не создали ни одной задачи 🥺\n\n" +
                    AppCommand.NEW_TASK + " - создать задачу";
        } else {
            StringBuilder sb = new StringBuilder("Ваши задачи:");
            for (int i = 0; i < tasks.size(); i++) {
                sb.append(String.format("%n%n📌 Задача#%d%n", i + 1));
                sb.append(tasks.get(i).getDescription());
            }
            text = sb.toString();
        }

        msgSender.send(text, msg.getChatId());
    }

    @Override
    public void processNewTaskCommand(AppUser user, Message msg) {
        log.info("Processing new task command");
        if (BotState.WAIT_TASK == user.getState()) {
            var cmd = AppCommand.parseAppCommand(msg.getText());
            if (cmd != null) {
                if (AppCommand.BACK == cmd) {
                    processBackCommand(user, msg);
                    processHelpCommand(msg);
                } else {
                    processBackCommand(user, msg);
                    processCommand(user, msg);
                }
                return;
            }

            processNewTask(user, msg);
        } else {
            user.setState(BotState.WAIT_TASK);
            var savedUser = appUserJpaRepository.save(user);
            appUserCache.put(savedUser);

            var text = "Что записать?";
            msgSender.send(text, msg.getChatId());
        }
    }

    private void processNewTask(AppUser user, Message msg) {
        log.info("Processing new task");
        AppTask transientAppTask = AppTask.builder()
                .description(msg.getText())
                .creator(user)
                .build();
        var savedTask = appTaskJpaRepository.save(transientAppTask);

        if (user.getTasks() == null) {
            var tasks = new ArrayList<AppTask>();
            user.setTasks(tasks);
        }
        user.getTasks().add(savedTask);
        appUserCache.put(user);

        var text = "Записали 😉 Что-то ещё?\n\n" +
                AppCommand.BACK + " - назад";
        msgSender.send(text, msg.getChatId());
    }
}
