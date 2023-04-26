package com.github.bucketonhead.service.impl;

import com.github.bucketonhead.dao.AppUserDAO;
import com.github.bucketonhead.dao.RawDataDAO;
import com.github.bucketonhead.entity.AppUser;
import com.github.bucketonhead.entity.RawData;
import com.github.bucketonhead.entity.enums.AppUserState;
import com.github.bucketonhead.service.MainService;
import com.github.bucketonhead.service.ProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

@Service
@Slf4j
@RequiredArgsConstructor
public class MainServiceImpl implements MainService {
    private final ProducerService producerService;
    private final RawDataDAO rawDataDAO;
    private final AppUserDAO appUserDAO;

    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);
        var textMessage = update.getMessage();
        var tgUser = textMessage.getFrom();
        var appUser = findOrSaveAppUser(tgUser);

        var botMessage = SendMessage.builder()
                .chatId(textMessage.getChatId())
                .text("Сообщение получено! 🙂")
                .build();
        producerService.producerAnswer(botMessage);
    }

    private void saveRawData(Update update) {
        RawData rawData = RawData.builder()
                .event(update)
                .build();
        rawDataDAO.save(rawData);
    }

    private AppUser findOrSaveAppUser(User tgUser) {
        AppUser persistenceAppUser = appUserDAO.findByTelegramUserId(tgUser.getId());
        if (persistenceAppUser == null) {
            AppUser transientAppUser = AppUser.builder()
                    .telegramUserId(tgUser.getId())
                    .firstName(tgUser.getFirstName())
                    .lastName(tgUser.getLastName())
                    .username(tgUser.getUserName())
                    // TODO: поменять на false после реализации email-сервиса
                    .isActive(Boolean.TRUE)
                    .state(AppUserState.BASIC_STATE)
                    .build();
            persistenceAppUser = appUserDAO.save(transientAppUser);
        }
        return persistenceAppUser;
    }
}
