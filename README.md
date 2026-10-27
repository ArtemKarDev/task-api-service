
## Запуск стендов в Docker

Один `compose.yaml` поднимает любой стенд. Стенд выбирается файлом окружения (`--env-file`), у каждого свой проект, порты и том с данными.

| Стенд | Приложение | БД | Файл | Порт приложения | Порт БД |
|---|---|---|---|---|---|
| local | из IDEA | Docker | `env/local.env` | 8080 | 5433 |
| dev | Docker | Docker | `env/dev.env` | 8180 | 5432 |
| stage | Docker | Docker | `env/stage.env` | 8280 | 5434 |

### Первый запуск

Скопируйте шаблоны (настоящие `*.env` в Git не попадают):

```powershell
Copy-Item env\local.env.example env\local.env
Copy-Item env\dev.env.example env\dev.env
Copy-Item env\stage.env.example env\stage.env
```

### Команды

```powershell
# local: в Docker только БД, приложение запускается из IDEA
docker compose --env-file env/local.env up -d postgres

# dev и stage: БД и приложение
docker compose --env-file env/dev.env up -d --build
docker compose --env-file env/stage.env up -d --build

# состояние и логи
docker compose --env-file env/stage.env ps
docker compose --env-file env/stage.env logs -f app

# остановка (данные сохраняются)
docker compose --env-file env/stage.env down
```

Проверка работы: `http://localhost:8280/actuator/health` (порт зависит от стенда).

> **Осторожно:** `docker compose down -v` удаляет том, то есть **все данные стенда**.
> Имя проекта dev (`task-api-service`) нельзя менять: по нему подключается том с данными.

Схему БД создаёт и меняет Flyway (`src/main/resources/db/migration`). Применённые миграции править нельзя, изменения делаются новым файлом `V2__...sql`.