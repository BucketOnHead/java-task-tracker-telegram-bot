package com.github.bucketonhead.utils;

import com.github.bucketonhead.entity.AppUser;
import lombok.experimental.UtilityClass;

import java.time.Duration;
import java.time.LocalDateTime;

@UtilityClass
public class ResponseMessageUtils {
    private final String ERROR_PATTERN = "" +
            "⚠  Ошибка%n" +
            "%n" +
            "%s";
    private final String PROFILE_INFO_PATTERN = "" +
            "Ваш профиль ⚡%n" +
            "%n" +
            "ID:  `%d`%n" +
            "Ник:  %s%n" +
            "Имя:  %s%n" +
            "%n" +
            "Вы с нами уже %d дней! ♥";

    public String buildError(String text) {
        return String.format(ERROR_PATTERN, text);
    }

    public String buildProfileInfo(AppUser user) {
        long id = user.getTelegramUserId();
        String nick = user.getUsername() != null ? user.getUsername() : "Не указан";

        String firstName = user.getFirstName() != null ? user.getFirstName() : "";
        String lastName = user.getLastName() != null ? user.getLastName() : "";
        String name;
        if (!firstName.isEmpty() && !lastName.isEmpty()) {
            name = firstName + " " + lastName;
        } else if (!firstName.isEmpty()) {
            name = firstName;
        } else if (!lastName.isEmpty()) {
            name = lastName;
        } else {
            name = "Не указано";
        }

        long days = Duration.between(user.getFirstLoginDate(), LocalDateTime.now()).toDays();

        return String.format(PROFILE_INFO_PATTERN, id, nick, name, days);
    }
}
