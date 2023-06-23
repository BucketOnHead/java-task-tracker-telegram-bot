# TaskTrackifyBot v1.0.1

> TaskTrackifyBot - это телеграм-бот, предназначенный
> для планирования задач и повышения продуктивности.
> Он обеспечивает удобный способ записи, просмотра
> и удаления задач, помогая вам организовать свою работу.

## Оглавление

- [Архитектура проекта](#архитектура-проекта)
- [Инструкция по установке](#инструкция-по-установке)
- [Описание переменных окружения](#описание-переменных-окружения)
- [Функционал](#функционал)

## Архитектура проекта

![project_architecture.png](.readme/project_architecture.png)

<p align="right">
    <a href="#Оглавление">назад</a>
</p>

## Инструкция по установке

- [Требования](#требования)
- [Установка](#установка)
- [Запуск](#запуск)

<p align="right">
    <a href="#оглавление">назад</a>
</p>

### Требования

- Git
- Docker
- JDK 11
- Apache Maven

<p align="right">
    <a href="#инструкция-по-установке">назад</a>
</p>

### Установка

1. Склонируйте проект с помощью команды:

```bash
git clone https://github.com/BucketOnHead/java-task-tracker-telegram-bot
```

2. Заполните файл `.env.example` и переименуйте его в `.env`:

```dotenv
TELEGRAM_BOT_USERNAME=YourBotUsername
TELEGRAM_BOT_TOKEN=YourBotToken
# and others
```

3. Перейдите в директорию проекта:

```bash
cd java-task-tracker-telegram-bot
```

4. Соберите проект с помощью Apache Maven:

```bash
mvn clean install
```

<p align="right">
    <a href="#инструкция-по-установке">назад</a>
</p>

### Запуск

После установки проекта, вы можете запустить его с помощью команды:

```bash
docker-compose up -d
```

<p align="right">
    <a href="#инструкция-по-установке">назад</a>
</p>

## Описание переменных окружения

> ⚠ Предупреждение! Этот раздел может устареть со временем!

| Переменная окружения    | Описание                                  |
|:------------------------|:------------------------------------------|
| `TELEGRAM_BOT_USERNAME` | Имя вашего бота в Telegram                |
| `TELEGRAM_BOT_TOKEN`    | Токен вашего бота в Telegram              |
| `RABBITMQ_DEFAULT_USER` | Имя пользователя для RabbitMQ             |
| `RABBITMQ_DEFAULT_PASS` | Пароль пользователя для RabbitMQ          |
| `COMMON_DB_NAME`        | Имя общей базы данных                     |
| `COMMON_DB_USER`        | Имя пользователя для общей базы данных    |
| `COMMON_DB_PASSWORD`    | Пароль пользователя для общей базы данных |

<p align="right">
    <a href="#оглавление">назад</a>
</p>

## Функционал

> ⚠ Предупреждение! Этот раздел может устареть со временем!

| Команда     | Функция                             |
|:------------|:------------------------------------|
| `/start`    | Запуск бота                         |
| `/delete`   | Удаление аккаунта                   |
| `/help`     | Получение списка доступных команд   |
| `/profile`  | Получить информацию о профиле       |
| `/taskmode` | Переход в режим управления задачами |
| `/main`     | Переход в главный режим             |
| `/task`     | Создание задачи                     |
| `/tasks`    | Получение списка задач              |
| `/donetask` | Удаление задачи из списка           |

<p align="right">
    <a href="#оглавление">назад</a>
</p>