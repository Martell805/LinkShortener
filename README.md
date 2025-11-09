# Link Shortener - Сервис сокращения ссылок

## Описание

Link Shortener - это консольное приложение для сокращения URL-ссылок с возможностью настройки ограничений по времени и количеству использований. Приложение предоставляет удобный интерфейс для управления ссылками и их использования.

## Установка и запуск

### Требования
- Java 17 или выше
- Maven 3.6 или выше
- База данных (H2 in-memory по умолчанию)

### Сборка и запуск

1. **Клонирование репозитория:**
```bash
git clone https://github.com/Martell805/LinkShortener
cd link-shortener
```

2. **Сборка приложения:**
```bash
./gradlew build
```

3. **Запуск приложения:**
```bash
java -jar target/link-shortener-1.0.0.jar
```

### Конфигурация

Основные настройки в `application.yml`:
```yml
# Настройки базы данных
spring:
  datasource:
    url: jdbc:h2:mem:links
    driverClassName: org.h2.Driver

app:
  # Базовый URL для сокращенных ссылок
  defaultLink: http://vovandiya.ru
  # Cron удаления истёкших ссылок
  deleteLinksCron: "0/30 * * * * *"
```

## Использование

### Анонимные действия (до входа в систему)

1. **Регистрация**
   ```
   Available actions:
       1. Register
       2. Login
       3. Use link
       4. Exit

   Choose action: 1
   Enter your name: John Doe
   Hello John Doe! Your uuid is 550e8400-e29b-41d4-a716-446655440000
   ```

2. **Вход**
   ```
   Choose action: 2
   Enter your UUID: 550e8400-e29b-41d4-a716-446655440000
   Hello John Doe!
   ```

3. **Использование ссылки**
   ```
   Choose action: 3
   Enter link: http://localhost:8080/abc123def45
   ```

### Действия авторизованного пользователя

1. **Генерация ссылки**
   ```
   Current user: John Doe
   Available actions:
       1. Generate link
       2. Get my links
       3. Change my link
       4. Delete my link
       5. Logout
       6. Exit

   Choose action: 1
   Enter link: https://example.com
   Enter max uses (blank for infinite): 10
   Enter expiration date in hours (blank for infinite): 24
   Generated link: http://localhost:8080/abc123def45
   ```

2. **Просмотр моих ссылок**
   ```
   Choose action: 2
   Your links are:
        abc123def45
            Times used: 5
            Max uses: 10
            Expires in: 2024-01-15T14:30:00
            Destination: https://example.com
   ```

3. **Изменение параметров ссылки**
   ```
   Choose action: 3
   Enter link: abc123def45
   What parameter do you want to change?
       1. Max uses
       2. Expiration date
   Choose: 1
   Enter max uses (blank for infinite): 20
   Link was successfully changed!
   ```

4. **Удаление ссылки**
   ```
   Choose action: 4
   Enter link: abc123def45
   Link was successfully deleted!
   ```

## Архитектура и решения

### Структура проекта

```
src/main/java/ru/vovandiya/linkshortener/
├── action/              # Действия пользователя
│   ├── Action.java                 # Интерфейс для действий
│   ├── AnonymousAction.java        # Интерфейс для анонимных действий
│   ├── ChangeMyLinkAction.java     # Изменение ссылки
│   ├── DeleteMyLinkAction.java     # Удаление ссылки
│   ├── GenerateLinkAction.java     # Генерация ссылки
│   ├── GetMyLinksAction.java       # Просмотр ссылок
│   ├── LoginAction.java            # Вход в систему
│   ├── LogoutAction.java           # Выход из системы
│   ├── RegisterAction.java         # Регистрация
│   └── UseLinkAction.java          # Использование ссылки
├── cli/
│   └── UserInputCLI.java           # Основной CLI-интерфейс
├── entity/             # Сущности базы данных
│   ├── Link.java                   # Ссылка
│   └── User.java                   # Пользователь
├── exception/          # Исключения
│   ├── ExitException.java          # Исключение для выхода
│   ├── LinkNotFoundException.java  # Ссылка не найдена
│   └── UserNotFoundException.java  # Пользователь не найден
├── repository/         # Репозитории Spring Data JPA
│   ├── LinkRepository.java
│   └── UserRepository.java
├── service/            # Бизнес-логика
│   ├── LinkService.java
│   └── UserService.java
└────── sheduler/
        └── SchedulerTasks.java
```

### Ключевые архитектурные решения

1. **Слоистая архитектура**
    - **Controller Layer**: `UserInputCLI` - обработка пользовательского ввода
    - **Service Layer**: `UserService`, `LinkService` - бизнес-логика
    - **Repository Layer**: `UserRepository`, `LinkRepository` - работа с данными
    - **Entity Layer**: `User`, `Link` - модели данных

2. **Принцип разделения интерфейсов**
    - `Action` - базовый интерфейс для всех действий
    - `AnonymousAction` - действия доступные без авторизации

3. **Генерация коротких ссылок**
    - Используется SHA-256 хеширование с усечением до 10 символов
    - Добавление временной метки для уникальности
    - Обработка коллизий через повторные попытки

4. **Управление состоянием пользователя**
    - Статическое поле `currentUser` в `UserService`
    - Простая модель аутентификации через UUID

5. **Ограничения ссылок**
    - Максимальное количество использований
    - Время expiration
    - Автоматическая проверка при использовании

### Бизнес-логика

1. **Создание ссылки:**
    - Валидация URL
    - Генерация уникального короткого кода
    - Сохранение с указанием владельца

2. **Использование ссылки:**
    - Проверка существования
    - Проверка лимитов использования
    - Проверка срока действия
    - Инкремент счетчика использований
    - Автоматическое открытие в браузере

3. **Управление ссылками:**
    - Только владелец может изменять/удалять ссылки
    - Просмотр статистики использования

## Тестирование

### Структура тестов

```
src/test/java/ru/vovandiya/linkshortener/
├── action/              # Тесты действий
│   ├── ChangeMyLinkActionTest.java
│   ├── DeleteMyLinkActionTest.java
│   ├── GenerateLinkActionTest.java
│   ├── GetMyLinksActionTest.java
│   ├── LoginActionTest.java
│   ├── LogoutActionTest.java
│   ├── RegisterActionTest.java
│   └── UseLinkActionTest.java
└── service/            # Тесты сервисов
    ├── LinkServiceTest.java
    └── UserServiceTest.java
```

### Подход к тестированию

1. **Unit-тесты с Mockito**
    - Изоляция тестируемых компонентов
    - Мокирование зависимостей
    - Проверка взаимодействий между компонентами

2. **Тестирование пользовательского ввода**
    - Использование Reflection для доступа к private Scanner
    - Эмуляция пользовательского ввода через ByteArrayInputStream
    - Тестирование всех возможных сценариев ввода

3. **Покрытие основных сценариев**
    - Позитивные тесты (успешные операции)
    - Негативные тесты (ошибки ввода, исключения)
    - Граничные случаи

### Запуск тестов

```bash
# Запуск всех тестов
./gradlew test
```

### Покрытие тестами

- **Service Layer**: 85%+ покрытия бизнес-логики
- **Action Layer**: 90%+ покрытия пользовательских сценариев
