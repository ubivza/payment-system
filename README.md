# Инструкция по запуску

1. Склонировать репозиторий `git clone https://github.com/ubivza/payment-system.git`
2. Перейти в директорию куда был склонирован репозиторий
3. В консоли прописать `docker compose up`
4. Проверить работоспособность микросервиса с помощью приложенной постман коллекции postman/Test individuals api 2.postman_collection.json
   
Микросервис individuals-api представляет из себя оркестратор аутентификации, при посредничестве Keycloak.
Предоставляет реактивное API для
регистрации,
логина,
обновления токена,
получения информации о текущем пользователе.

## Используемые технологии

- Java 24
- Spring Boot 3.5.5
- Spring WebFlux
- Spring Security
- Spring Boot Actuator
- Gradle (Kotlin DSL)
- Keycloak 26.2
- Prometheus
- Micrometer
- Logback (JSON формат)
- Loki
- OpenAPI 3.0 (в формате YAML)
- OpenAPI Generator Plugin (для Gradle)
- JUnit 5
- Mockito
- Testcontainers
- Docker
- Docker Compose
- Grafana
- Tempo
- Nexus
- Feign
- Grafana Alloy

## Endpoints

- `POST /v1/auth/registration` — регистрация нового пользователя (без авторизации)
- `POST /v1/auth/login` — аутентификация пользователя по email и паролю (без авторизации)
- `POST /v1/auth/refresh-token` — обновление access/refresh токена (без авторизации)
- `GET /v1/auth/me` — получение информации о текущем пользователе (требуется Bearer Token)
- `DELETE /v1/individual/delete` - деактивация пользователя (требуется Bearer Token)
- `PUT /v1/individual/update` - обновление данных пользователя (требуется Bearer Token)

Сервис не имеет своей БД, всю логику по выдаче токенов, их валидации и менеджмент пользователей переложен на Keycloak по REST API через Admin API:

- `/realms/{realm}/protocol/openid-connect/token` - для получения/обновления токена
- `/admin/realms/{realm}/users` - для создания нового пользователя 

Взаимодейтсвие с Keycloak API производится `org.springframework.web.reactive.function.client.WebClient`.

## Флоу регистрации построен так:

1. Запрос POST `/v1/auth/registration` на API individuals-api;
2. Получение админ токена посредством client-id + client-secret у Keycloak для создания пользователя;
3. Запрос на создание пользователя в person-service с админ токеном `POST /v1/individuals`;
4. Создание пользователя с админ токеном доступа POST `/admin/realms/{realm}/users` и inner_id полученном в ответ в пункте 3 (айди сущности Individual в бд person-service);
5. Если ответ 201, записывается метрика успешной регистрации (MetricsCollector), которая используется для создания дашборда, и отправляется запрос в Keycloak POST `/realms/{realm}/protocol/openid-connect/token` на выдачу токена по email + password только что созданного пользователя;
6. В случае ошибки про сохранение в Keycloak вызывается компенсирующая сохранение в person-service логика - `DELETE /v1/individuals/{id}`;
7. Получаем ответ 201 и токен доступа + рефреш токен в случае успеха, в случае ошибки в контракте 400, в случае если такой имеил уже зарегистрирован - 409.

## Логин флоу:

1. Запрос POST `/v1/auth/login` на API individuals-api;
1. Отправляется запрос в Keycloak POST `/realms/{realm}/protocol/openid-connect/token`;
2. В случае ошибки кредов ответ будет 401, в случае успеха будет выдана пара токенов в ответе.

## Флоу обновления токена:

1. Запрос POST `/v1/auth/refresh-token` на API individuals-api;
2. Запрос на POST `/realms/{realm}/protocol/openid-connect/token` Keycloak;
3. В случае успеха 200 и новая пара токенов, в случае неудачи 401.

## Флоу получения информации о текущем пользователе:

1. Запрос GET `/v1/auth/me` на API individuals-api;
2. Spring Security проверяет issuer токена с помощью библиотеки `org.springframework.boot:spring-boot-starter-oauth2-resource-server`, и `spring.security.oauth2.resourceserver.jwt.issuer-uri` указания ссылки на issuer в application.yml;
3. Из контекста Spring Security достается токен, а из него inner_id и email пользователя;
4. С inner_id и email с помощью админ токена сервис запрашивает у person-service данные о пользователе по `GET /v1/individuals`;
5. Данные отдаются клиенту.

## Флоу обновления данных текущего пользователя:

1. Запрос `PUT /v1/individual/update` на API individuals-api;
2. Spring Security проверяет issuer токена с помощью библиотеки `org.springframework.boot:spring-boot-starter-oauth2-resource-server`, и `spring.security.oauth2.resourceserver.jwt.issuer-uri` указания ссылки на issuer в application.yml;
3. Из контекста Spring Security достается токен, а из него inner_id пользователя;
4. С inner_id с помощью админ токена сервис запрашивает у person-service обновление данных о пользователе по `PUT /v1/individuals/{inner_id}`;
5. 200 в случае успеха;

## Флоу деактивации аккаунта текущего пользователя:

1. Запрос `DELETE /v1/individual/delete` на API individuals-api;
2. Spring Security проверяет issuer токена с помощью библиотеки `org.springframework.boot:spring-boot-starter-oauth2-resource-server`, и `spring.security.oauth2.resourceserver.jwt.issuer-uri` указания ссылки на issuer в application.yml;
3. Из контекста Spring Security достается токен, а из него inner_id пользователя;
4. С inner_id с помощью админ токена сервис запрашивает у person-service деактивацию аккаунта пользователя `DELETE /v1/individuals/{inner_id}`;
5. 200 в случае успеха;

## Nexus

person-service публикует jar с Feign клиентом, который подтягивается в individuals-api и дергается для общения с person-service.

## Логирование

Логирование реализовано с помощью @Slf4j
Логи пишутся в stdout докер контейнера, откуда их достает Alloy и пушит в Loki, откуда их подтягивает Grafana.

## ExceptionHandling

За обработку ошибок и отправку результата на клиент отвечает `@ControllerAdvice` GlobalExceptionHandler.

## Трейсинг

Собирается с помощью Alloy, хранятся в Tempo, отображаются в Grafana.

## Метрики

Собираются с помощью Prometheus, `org.springframework.boot:spring-boot-starter-actuator` и `io.micrometer:micrometer-registry-prometheus:1.15.4`, и отправляются в Grafana для построения дашбордов.

## Тестирование

Тестирование разделено на юнит и интеграционное. Юнит проверяет отдельные сервисы и контроллер, интеграционное проверяет с помощью контейнера с Keycloak и WireMock работу сервиса в связке с person-service.
