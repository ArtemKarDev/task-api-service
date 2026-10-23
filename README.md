
запуск приложения из терминала
.\mvnw.cmd spring-boot:run

сборка образа docker
docker build -t task-api-service .


создать сеть
docker network create task-api-network
docker network create jenkins


подключить контейнер к сети
docker network connect task-api-network task-api-postgres

проверка
docker network inspect task-api-network

исправим адрес подключения к БД только для контейнера
docker run -d --name task-api-service --network task-api-network -p 8080:8080 -e DB_PASSWORD=local_dev_password -e SPRING_DATASOURCE_URL=jdbc:postgresql://task-api-postgres:5432/taskdb task-api-service
что тут:
--network task-api-network — подключает приложение к общей сети с PostgreSQL.
-p 8080:8080 — открывает API на компьютере по порту 8080.
SPRING_DATASOURCE_URL — переопределяет адрес БД внутри контейнера, чтобы использовать имя task-api-postgres.
DB_PASSWORD — передаёт пароль базы данных через переменную окружения.


проверка логов приложения  если оно запустилось
docker logs task-api-service


отдельный Docker volume для настроек Jenkins. В нём будут храниться Pipeline, конфигурация, плагины и учётные записи — они не потеряются при пересоздании контейнера.
docker volume create jenkins_home
docker volume ls

Запусти Jenkins-контейнер
docker run -d --name jenkins --restart unless-stopped --network jenkins -p 8081:8080 -p 50000:50000 -v jenkins_home:/var/jenkins_home jenkins/jenkins:lts-jdk21

Что здесь:
- jenkins/jenkins:lts-jdk21 — стабильная LTS-версия Jenkins на Java 21.
- 8081:8080 — веб-интерфейс Jenkins будет доступен на порту 8081, чтобы не конфликтовать с твоим API на 8080.
- jenkins_home — постоянное хранилище настроек.
- --restart unless-stopped — Docker будет перезапускать Jenkins автоматически.
- 50000:50000 — порт для подключаемых Jenkins-агентов; на первом этапе он нам не понадобится, но пока оставим стандартную настройку.

docker ps --filter "name=jenkins"
http://localhost:8081
Чтобы получить первоначальный пароль администратора
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword

Команда покажет, какие каталоги и сокеты подключены к контейнеру Jenkins.
docker inspect jenkins --format "{{json .Mounts}}"
проверим, доступен ли Docker CLI внутри контейнера Jenkins.
docker exec jenkins sh -c "docker --version"

Dockerfile
-----------------
FROM jenkins/jenkins:lts-jdk21

USER root

RUN apt-get update \
&& apt-get install -y --no-install-recommends docker.io \
&& rm -rf /var/lib/apt/lists/*
-------------------
USER jenkins
сборка - запускается из папки с докер-файлом
docker build -t jenkins-docker:lts .

