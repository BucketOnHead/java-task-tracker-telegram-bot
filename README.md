# TaskTrackifyBot

> TaskTrackifyBot - это телеграм-бот, предназначенный
> для планирования задач и повышения продуктивности.
> Он обеспечивает удобный способ записи, просмотра
> и удаления задач, помогая вам организовать свою работу.

## Оглавление

- [Инструкция по установке](#инструкция-по-установке)

## Инструкция по установке

- [Требования](#требования)
- [Установка](#установка)
- [Запуск](#запуск)

<div style="text-align: right">
    <a href="#оглавление">назад</a>
</div>

### Требования

- Git
- Docker
- JDK 11
- Apache Maven

<div style="text-align: right">
    <a href="#инструкция-по-установке">назад</a>
</div>

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

<div style="text-align: right">
    <a href="#инструкция-по-установке">назад</a>
</div>

### Запуск
После установки проекта, вы можете запустить его с помощью команды:
```bash
docker-compose up -d
```

<div style="text-align: right">
    <a href="#инструкция-по-установке">назад</a>
</div>
