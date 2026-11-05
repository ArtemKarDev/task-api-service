# task-api-service

Учебный сервис управления задачами (интерфейс «Task Lab»): REST API и веб-страницы, хранение в PostgreSQL, обмен сообщениями через Kafka, авторизация по ролям. Проект учебный: на нём отрабатываются приёмы разработки сервиса и его проверки. Автотесты лежат в отдельном репозитории [task-api-tests](https://github.com/ArtemKarDev/task-api-tests).

## Оглавление

- [Что это](#что-это)
- [Возможности](#возможности)
- [Стек](#стек)
- [Архитектура](#архитектура)
- [Быстрый старт](#быстрый-старт)
- [Веб-страницы](#веб-страницы)
- [REST API](#rest-api)
    - [Задачи](#задачи)
    - [Формат ошибок](#формат-ошибок)
    - [Swagger](#swagger)
- [Запуск стендов в Docker](#запуск-стендов-в-docker)

---

## Что это

Сервис работает с одной сущностью: **задача** (`Task`).

| Поле | Правила |
|---|---|
| `title` | название, обязательное, до 255 символов |
| `description` | описание, необязательное, до 5000 символов |
| `status` | статус, обязательный: `NEW`, `IN_PROGRESS`, `DONE` |
| `priority` | приоритет, обязательный: `LOW`, `MEDIUM`, `HIGH` |
| `assignee` | исполнитель, необязательный, до 100 символов |

Пустое описание и пустой исполнитель сохраняются как «не задано». Идентификатор и времена создания и изменения назначает сервис.

<p align="right"><a href="#оглавление">↑ к оглавлению</a></p>

---

## Возможности

- **Веб-интерфейс**: список и поиск задач (по названию, описанию и исполнителю, с фильтром по статусу), создание, изменение, удаление (только администратор), вход и выход, смена своего пароля, страница «О проекте».
- **REST API**: получение списка и одной задачи, создание, полная и частичная правка, удаление; единый формат ошибок; описание в Swagger UI.
- **Kafka**: сервис пишет события об изменениях задач в топик `task_out` и создаёт задачи по командам из топика `task_in`; некорректные команды уходят в `task_in_dlq`.
- **Авторизация**: вход по токену (JWT) для API, вход через форму с сессией для страниц; роли `USER` и `ADMIN`; администратор управляет пользователями.
- **Данные**: PostgreSQL, схему создаёт и меняет Flyway.
- **Запуск**: стенды `local`, `dev`, `stage` в Docker, автоматическое развёртывание и проверки в Jenkins.

<p align="right"><a href="#оглавление">↑ к оглавлению</a></p>

---

## Стек

| Что | Чем |
|---|---|
| Язык и платформа | Java 21, Spring Boot 4.1 |
| Веб | Spring MVC, Thymeleaf |
| Данные | Spring Data JPA (Hibernate), PostgreSQL 17, Flyway |
| Безопасность | Spring Security 7: сервер ресурсов JWT (API), вход через форму и сессии (страницы) |
| Сообщения | Spring for Apache Kafka (брокер apache/kafka 4.2 в режиме KRaft) |
| Документация API | springdoc-openapi (Swagger UI) |
| Прочее | Lombok, Actuator (наружу открыт только `health`) |
| Запуск | Docker и Docker Compose, Jenkins |

<p align="right"><a href="#оглавление">↑ к оглавлению</a></p>

---

## Архитектура

У сервиса два «входа» с разной защитой: страницы для людей и REST для программ. Оба приводят к одним и тем же сервисам и данным.

```mermaid
flowchart LR
    B["Браузер"] --> SP["Цепочка для страниц<br/>форма входа, сессия"]
    C["Программы (REST)"] --> SA["Цепочка для API<br/>токен JWT"]
    SP --> PC["Страницы (Thymeleaf)"]
    SA --> RC["REST-контроллеры"]
    PC --> SV["Сервисы<br/>TaskService, UserService"]
    RC --> SV
    SV --> RP["Репозитории (JPA)"] --> DB[("PostgreSQL")]
    TIN[["task_in"]] --> KL["Слушатель команд"] --> SV
    SV --> KS["Отправитель событий"] --> TOUT[["task_out"]]
    KL -- "ошибка" --> TDLQ[["task_in_dlq"]]
```

### Структура кода

Пакет `ru.learning.taskapiservice`:

| Пакет | Что внутри |
|---|---|
| `config` | описание API для Swagger (`OpenApiConfig`), журнал HTTP-запросов (`HttpLoggingFilter`) |
| `controller` | страницы: `StaticPageController`, `TaskPageController`, `ProfileController`; REST: `TaskController`, `AuthController`, `CurrentUserController`, `AdminUserController` |
| `dto` | запросы, ответы и формы, перечисления `Priority` и `TaskStatus`, единый формат ошибки `ApiErrorResponse` |
| `entity` | `Task`, `UserAccount`, `Role`, `ProcessedMessage` (обработанные сообщения Kafka) |
| `exceptions` | исключения и обработчики ошибок (`GlobalExceptionHandler` для REST, `ApiRoutingExceptionHandler` для неизвестных адресов API) |
| `kafka` | топики, события, команды, слушатель команд, отправители событий и отклонённых сообщений |
| `repository` | интерфейсы Spring Data JPA |
| `security` | цепочки безопасности, выдача и проверка токенов, загрузка пользователей |
| `service` | `TaskService`, `UserService`, `AuthService` |

<p align="right"><a href="#оглавление">↑ к оглавлению</a></p>

---
## Веб-страницы

Все страницы, кроме страницы входа, требуют входа через форму. Без входа браузер перенаправляется на `/login`.

| Адрес | Что это | Кто может |
|---|---|---|
| `/` | перенаправляет на `/tasks` | вошедший |
| `/login` | форма входа (`POST /login` отправляет её) | все |
| `/logout` | выход (только методом `POST`, форма в шапке) | вошедший |
| `/tasks` | список и поиск задач: параметр `q` (по названию, описанию и исполнителю) и `status` (фильтр по статусу) | вошедший |
| `/tasks/new` | форма создания задачи (`POST /tasks`) | вошедший |
| `/tasks/{id}/edit` | форма изменения (`POST /tasks/{id}`) | вошедший |
| `/tasks/{id}/delete` | удаление, только методом `POST` (кнопка в списке) | **только `ADMIN`**; у `USER` кнопки нет, а запрос получит 403 |
| `/profile/password` | смена своего пароля | вошедший |
| `/about` | страница «О проекте» | вошедший |

Страницы используют защиту форм (CSRF): токен подставляется в каждую форму автоматически.

<p align="right"><a href="#оглавление">↑ к оглавлению</a></p>

---

## REST API

Адреса под `/api/`. Кроме `POST /api/auth/login`, все требуют токен в заголовке `Authorization: Bearer <токен>`; как его получить, описано в разделе про авторизацию. Тела запросов и ответов в формате JSON. Лишние поля в запросе не игнорируются, а отклоняются (400).

### Задачи

| Метод и адрес | Что делает | Успех | Ошибки |
|---|---|---|---|
| `GET /api/tasks` | все задачи (фильтров и постраничного вывода пока нет) | 200 | 401 |
| `GET /api/tasks/{id}` | одна задача | 200 | 400 (идентификатор не число), 401, 404 |
| `POST /api/tasks` | создать задачу | 201 и созданная задача | 400, 401 |
| `PUT /api/tasks/{id}` | заменить задачу целиком; необязательные поля, которых нет в запросе или которые пустые, очищаются | 200 | 400, 401, 404 |
| `PATCH /api/tasks/{id}` | изменить только указанные поля; очистить поле через `PATCH` нельзя (для этого `PUT`); хотя бы одно поле обязательно | 200 | 400, 401, 404 |
| `DELETE /api/tasks/{id}` | удалить задачу | 204 | 400, 401, **403** (только `ADMIN`), 404 |

Пример (PowerShell):

```powershell
$base = "http://localhost:8080"
$login = Invoke-RestMethod -Method Post -Uri "$base/api/auth/login" -ContentType "application/json" -Body (@{ username = "user"; password = "<пароль>" } | ConvertTo-Json)
$h = @{ Authorization = "Bearer $($login.accessToken)" }

# создать
Invoke-RestMethod -Method Post -Uri "$base/api/tasks" -Headers $h -ContentType "application/json" -Body '{"title":"Prepare report","status":"NEW","priority":"HIGH"}'

# прочитать
Invoke-RestMethod -Uri "$base/api/tasks/1" -Headers $h
```

Поля задачи и их правила описаны в разделе «Что это».

### Формат ошибок

Все ошибки API имеют одинаковую форму:

```json
{
  "timestamp": "2026-11-05T12:40:00.123Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "details": [ { "field": "title", "message": "must not be blank" } ],
  "path": "/api/tasks"
}
```

| Код | Когда | `message` |
|---|---|---|
| 400 | не прошла проверка полей | `Validation failed` (подробности в `details`: `must not be blank`, `must not exceed N characters`, `must not be null`) |
| 400 | битый JSON, пустое тело, неверное значение перечисления, неизвестное поле | `Request body is missing, malformed or contains an invalid value` |
| 400 | параметр адреса не того типа (`/api/tasks/abc`) | `Invalid request parameter` |
| 400 | `PATCH` без полей | `At least one field must be provided` |
| 401 | нет токена или он неверен или просрочен | `Authentication required` или `Invalid or expired token` |
| 403 | вошёл, но роли не хватает | `Access denied: insufficient permissions` |
| 404 | нет такой задачи или неизвестный адрес | сообщение о ненайденной задаче или `Resource not found` |
| 405 | метод не поддерживается адресом | заголовок `Allow` перечисляет допустимые методы |
| 409 | нарушение целостности данных, занятый логин | `Data conflict…`, `Username already exists` |
| 500 | необработанная ошибка | `Internal server error` (подробности только в журнале, не в ответе) |

### Swagger

Описание API в Swagger UI: `http://localhost:8080/swagger-ui/index.html`, спецификация OpenAPI: `http://localhost:8080/v3/api-docs`. Оба адреса открыты без входа. Описание схемы безопасности (кнопка «Authorize») пока не добавлено: защищённые методы из Swagger UI без токена вернут 401.

<p align="right"><a href="#оглавление">↑ к оглавлению</a></p>

---

## Быстрый старт

Нужны: JDK 21, Docker Desktop, IDE (например, IntelliJ IDEA). Стенд `local`: база и Kafka работают в Docker, сам сервис запускается из IDE.

1. Подготовьте файл настроек стенда (один раз) и запустите базу и Kafka:

```powershell
Copy-Item env\local.env.example env\local.env
docker compose --env-file env/local.env up -d postgres kafka
```

2. Запустите класс `TaskApiServiceApplication` из IDE, добавив в настройки запуска переменные окружения (значения учебные):

```
APP_SECURITY_USER_PASSWORD=local_user_password
APP_SECURITY_ADMIN_PASSWORD=local_admin_password
APP_SECURITY_JWT_SECRET=local-dev-jwt-secret-0123456789abcdef
```

Без этих трёх переменных сервис не запустится (сделано намеренно: нельзя случайно запустить его с пустыми паролями). Адреса базы (`localhost:5433`) и Kafka (`localhost:9092`) по умолчанию уже подходят для стенда `local`.

3. Откройте `http://localhost:8080/`: откроется форма входа. Войдите как `user` или `admin` (пароли из пункта 2).

Полезные адреса: Swagger UI `http://localhost:8080/swagger-ui/index.html`, проверка работоспособности `http://localhost:8080/actuator/health`.

<p align="right"><a href="#оглавление">↑ к оглавлению</a></p>

---