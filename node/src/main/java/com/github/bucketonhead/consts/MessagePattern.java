package com.github.bucketonhead.consts;

import lombok.experimental.UtilityClass;

@UtilityClass
@SuppressWarnings("ConcatenationWithEmptyString")
public class MessagePattern {

    public final String ERROR = "" +
            "⚠  Ошибка%n" +
            "%n" +
            "%s";
    
    public final String PROFILE_INFO = "" +
            "Ваш профиль ⚡%n" +
            "%n" +
            "ID:  `%d`%n" +
            "Ник:  %s%n" +
            "Имя:  %s%n" +
            "%n" +
            "Вы с нами уже %d дн! ♥";
}
