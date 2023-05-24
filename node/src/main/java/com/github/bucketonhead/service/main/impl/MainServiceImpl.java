package com.github.bucketonhead.service.main.impl;

import com.github.bucketonhead.dao.AppTaskJpaRepository;
import com.github.bucketonhead.dao.AppUserJpaRepository;
import com.github.bucketonhead.dao.RawDataJpaRepository;
import com.github.bucketonhead.entity.AppTask;
import com.github.bucketonhead.entity.AppUser;
import com.github.bucketonhead.entity.RawData;
import com.github.bucketonhead.entity.enums.BotState;
import com.github.bucketonhead.service.main.MainService;
import com.github.bucketonhead.service.main.enums.ServiceCommand;
import com.github.bucketonhead.service.rabbitmq.ProducerService;
import com.github.bucketonhead.utils.ResponseMessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

@Service
@Slf4j
@RequiredArgsConstructor
public class MainServiceImpl implements MainService {
    private final ProducerService producerService;
    private final RawDataJpaRepository rawDataJpaRepository;
    private final AppUserJpaRepository appUserJpaRepository;
    private final AppTaskJpaRepository appTaskJpaRepository;

    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);
        var msg = update.getMessage();
        var appUser = findOrSaveAppUser(msg.getFrom());

        var serviceCommand = ServiceCommand.fromValue(msg.getText());
        if (ServiceCommand.CANCEL.equals(serviceCommand)) {
            processCancelCommand(appUser, msg);
            return;
        }

        if (BotState.BASIC == appUser.getState()) {
            processBasicStateCommand(appUser, msg);
        } else if (BotState.WAIT_FOR_EMAIL == appUser.getState()) {
            // TODO: реализовать после добавления email-сервиса
        } else if (BotState.CREATE_TASK == appUser.getState()) {
            processCreateTaskStateCommand(appUser, msg);
        } else if (BotState.DONE_TASK == appUser.getState()) {
            processDoneTaskStateCommand(appUser, msg);
        } else {
            log.error("state: {}, не реализован", appUser.getState());
            var text = "Разработчик допустил ошибку при реализации " +
                    "этой функциональности, попробуйте позже! " +
                    "А пока вернём вас в главное меню ☺";
            var responseMessage = ResponseMessageUtils.buildErrorMessage(text);
            sendResponseMessage(responseMessage, msg.getChatId());
            processCancelCommand(appUser, msg);
        }
    }

    private void processDoneTaskStateCommand(AppUser appUser, Message msg) {
        var choose = msg.getText();
        int taskNumber;
        try {
            taskNumber = Integer.parseInt(choose);
        } catch (NumberFormatException e) {
            var text = "Нужно указать номер задачи";
            var responseMessage = ResponseMessageUtils.buildErrorMessage(text);
            sendResponseMessage(responseMessage, msg.getChatId());
            return;
        }

        var tasks = appUser.getTasks();
        if (taskNumber < 1 || taskNumber > tasks.size()) {
            var text = "Неверный номер задачи! Укажите число от 1 до " + tasks.size();
            var responseMessage = ResponseMessageUtils.buildErrorMessage(text);
            sendResponseMessage(responseMessage, msg.getChatId());
            return;
        }

        appUser.getTasks().remove(taskNumber - 1);
        appUserJpaRepository.save(appUser);
        var responseMessage = String.format("Готово! Вычеркнули задачу %d " +
                "из списка задач, отметить ещё одну?%n" +
                "%n%s - выйти", taskNumber, ServiceCommand.CANCEL);
        sendResponseMessage(responseMessage, msg.getChatId());
    }

    private void processCreateTaskStateCommand(AppUser appUser, Message msg) {
        AppTask transientAppTask = AppTask.builder()
                .description(msg.getText())
                .creator(appUser)
                .build();
        appTaskJpaRepository.save(transientAppTask);
        var responseMessage = String.format("Записали 😉 Что-то ещё?%n%n" +
                "%s - выйти", ServiceCommand.CANCEL);
        sendResponseMessage(responseMessage, msg.getChatId());
    }

    private void processBasicStateCommand(AppUser appUser, Message msg) {
        String responseMessage;
        if (!msg.getText().startsWith(ServiceCommand.PREFIX)) {
            responseMessage = "Я бы с удовольствие поговорил, " +
                    "но я просто бот, выполняющий команды ☺";
            sendResponseMessage(responseMessage, msg.getChatId());
            return;
        }

        var serviceCommand = ServiceCommand.fromValue(msg.getText());
        if (serviceCommand == null) {
            var text = "Команда не распознана!";
            responseMessage = ResponseMessageUtils.buildErrorMessage(text);
        } else if (ServiceCommand.HELP == serviceCommand) {
            var pattern = "Список доступных команд:%n%n" +
                    "%s - %s.%n%n" +
                    "%s - %s.%n%n" +
                    "%s - %s.";
            responseMessage = String.format(pattern,
                    ServiceCommand.CANCEL, "отмена выполнения текущей команды",
                    ServiceCommand.NEW_TASK, "создать простую задачу, без привязки ко времени",
                    ServiceCommand.MY_TASKS, "получить задачи");
        } else if (ServiceCommand.START == serviceCommand) {
            var text = "Приветствую! Чтобы посмотреть список " +
                    "доступных команд используйте %s";
            responseMessage = String.format(text, ServiceCommand.HELP);
        } else if (ServiceCommand.NEW_TASK == serviceCommand) {
            appUser.setState(BotState.CREATE_TASK);
            appUserJpaRepository.save(appUser);
            responseMessage = "Что записать?";
        } else if (ServiceCommand.MY_TASKS == serviceCommand) {
            var tasks = appUser.getTasks();
            if (tasks == null || tasks.isEmpty()) {
                responseMessage = "Вы ещё не создали ни одной задачи 🥺\n\n" +
                        ServiceCommand.NEW_TASK + " - создать задачу";
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("Ваши задачи:");
                for (int i = 0; i < tasks.size(); i++) {
                    sb.append(String.format("%n%n📌 Задача#%d%n", i + 1));
                    sb.append(tasks.get(i).getDescription());
                }
                sb.append(String.format("%n%n%s - выполнить задачу.", ServiceCommand.DONE_TASK));

                responseMessage = sb.toString();
            }
        } else if (ServiceCommand.DONE_TASK == serviceCommand) {
            appUser.setState(BotState.DONE_TASK);
            appUserJpaRepository.save(appUser);
            responseMessage = "Какую задачу вычеркнуть? (Укажите номер)";
        } else {
            var text = "Если вы видите это сообщение, " +
                    "значит разработчик забыл подключить " +
                    "эту функциональность, попробуйте позже!";
            responseMessage = ResponseMessageUtils.buildErrorMessage(text);
        }

        sendResponseMessage(responseMessage, msg.getChatId());
    }

    private void processCancelCommand(AppUser appUser, Message msg) {
        String responseMessage;
        if (BotState.BASIC == appUser.getState()) {
            responseMessage = "Вы уже в главном меню 😉";
        } else {
            appUser.setState(BotState.BASIC);
            appUserJpaRepository.save(appUser);
            responseMessage = "Вернули вас в главное меню!";
        }
        sendResponseMessage(responseMessage, msg.getChatId());
    }

    private void sendResponseMessage(String text, Long chatId) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();
        producerService.producerAnswer(sendMessage);
    }

    private void saveRawData(Update update) {
        RawData rawData = RawData.builder()
                .event(update)
                .build();
        rawDataJpaRepository.save(rawData);
    }

    private AppUser findOrSaveAppUser(User tgUser) {
        AppUser persistenceAppUser = appUserJpaRepository.findByTelegramUserId(tgUser.getId());
        if (persistenceAppUser == null) {
            AppUser transientAppUser = AppUser.builder()
                    .telegramUserId(tgUser.getId())
                    .firstName(tgUser.getFirstName())
                    .lastName(tgUser.getLastName())
                    .username(tgUser.getUserName())
                    // TODO: поменять на false после реализации email-сервиса
                    .isActive(Boolean.TRUE)
                    .state(BotState.BASIC)
                    .build();
            persistenceAppUser = appUserJpaRepository.save(transientAppUser);
        }
        return persistenceAppUser;
    }
}
