# Инструкция по запуску

1. Склонировать репозиторий `git clone https://github.com/ubivza/payment-system.git`
2. Перейти в директорию куда был склонирован репозиторий
3. В консоли прописать `docker compose up`
4. Зайти в grafana по http://localhost:3000/
5. Добавить два datasource: 
	- http://loki:3100
	- http://prometheus:9090
6. Импортировать дашборд в виде JSON из файла `individuals-api/grafana/grafana-dashbord.json`
7. Зайти в keycloak по http://localhost:8080/
8. Перейти в realm -> payment-system -> clients -> credentials и поменять client-secret на секрет из docker compose поле `services.individuals-api.environment.KEYCLOAK_CLIENT_SECRET`
	8.1 Optional Если Keycloak требует HTTPS нужно зайти в настройки докера -> Resources -> Network и поставить галочку Enable host networking
9. Перезапустить individuals-keycloak и individuals-api
10. Проверить работоспособность микросервиса с помощью приложенной постман коллекции individuals-api/postman/Test individuals api.postman_collection.json

Микросервис представляет из себя оркестратор аутентификации, при посредничестве Keycloak.
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

## Endpoints

- `POST /v1/auth/registration` — регистрация нового пользователя (без авторизации)
- `POST /v1/auth/login` — аутентификация пользователя по email и паролю (без авторизации)
- `POST /v1/auth/refresh-token` — обновление access/refresh токена (без авторизации)
- `GET /v1/auth/me` — получение информации о текущем пользователе (требуется Bearer Token)

## Архитектура сервиса
individuals-api/  
├── gradle  
│   ├── build    
│   ├── gradle  
│   ├── grafana  
│	  │	  └──grafana-dashbord.json  
│   ├── openapi  
│	  │	  └──individuals-api.yml  
│   ├── src/main/java/com.example.individualsapi  
│   │   ├─ client  
│   │   │	└── KeycloakClient  
│   │   ├─ config  
│   │   │   └── SecurityConfig  
│   │   ├─ controller  
│   │   │   └── AuthController  
│   │   ├── exception  
│   │   │   ├── BadCredentialsException  
│   │   │   ├── KeycloakErrorResponse  
│   │   │   ├── NotFoundException  
│   │   │   ├── NotValidException  
│   │   │   └── UserAlreadyExistsException  
│   │   ├── filter  
│   │   │   ├── RequestLoggingFilter  
│   │   │   ├── ResponseLoggingFilter  
│	  │	  │  	└── WebClientLoggingFilter  
│   │   ├── handler  
│   │   │   ├── GlobalExceptionHandler  
│   │   ├── service  
│   │   │   ├── api  
│   │   │   │   ├── TokenService  
│   │   │   │   └── UserService  
│   │   │   └── impl  
│   │   │       ├── ContextUserSubExtractor  
│   │   │       ├── MetricsCollector  
│   │   │       ├── TokenServiceImpl  
│   │   │       └── UserServiceImpl  
│	  │	  ├── util  
│   │	  │	  └── LoggingUtils  
│   │	  ├── IndividualsApiApplication  
│	  │	  ├── resources  
│   │	  │	  ├── application.yml  
│   │	  │	  ├── logback-spring.xml  
│   │	  │	  └── realm-config.json  
│	  │	  └── test  


Сервис не имеет своей БД, всю логику по выдаче токенов, их валидации и менеджмент пользователей переложен на Keycloak по REST API через Admin API:

- `/realms/{realm}/protocol/openid-connect/token` - для получения/обновления токена
- `/admin/realms/{realm}/users/{id}` - для получения информации о текущем пользователе
- `/admin/realms/{realm}/users` - для создания нового пользователя 

Взаимодейтсвие с Keycloak API производится `org.springframework.web.reactive.function.client.WebClient`.

## Флоу регистрации построен так:

1. Запрос POST `/v1/auth/registration` на API individuals-api;
2. Получение админ токена посредством client-id + client-secret у Keycloak для создания пользователя;
3. Создание пользователя с админ токеном доступа POST `/admin/realms/{realm}/users`;
4. Если ответ 201, записывается метрика успешной регистрации (MetricsCollector), которая используется для создания дашборда, и отправляется запрос в Keycloak POST `/realms/{realm}/protocol/openid-connect/token` на выдачу токена по email + password только что созданного пользователя;
5. Получаем ответ 201 и токен доступа + рефреш токен в случае успеха, в случае ошибки в контракте 400, в случае если такой имеил уже зарегистрирован - 409.

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
3. Из контекста Spring Security достается токен, а из него айди пользователя;
4. С айди пользователя с помощью админ токена сервис запрашивает у Keycloak данные о пользователе по `/admin/realms/{realm}/users/{id}`;
5. Данные отдаются клиенту.

## Логирование

Логирование WebClient производится с помощью ReactorClientHttpConnector настроенного на уровень DEBUG.
Логирование API самого сервиса сделано с помощью имплементаций `org.springframework.web.server.WebFilter` в классах RequestLoggingFilter и ResponseLoggingFilter.
Логи отправляются с помощью `com.github.loki4j:loki-logback-appender:2.0.0` в Loki -> Grafana.

## ExceptionHandling

За обработку ошибок и отправку результата на клиент отвечает `@ControllerAdvice` GlobalExceptionHandler.

## Метрики

Собираются с помощью Prometheus, `org.springframework.boot:spring-boot-starter-actuator` и `io.micrometer:micrometer-registry-prometheus:1.15.4`, и отправляются в Grafana для построения дашбордов.

## Тестирование

Тестирование разделено на юнит и интеграционное. Юнит проверяет отдельные сервисы и контроллер, интеграционное проверяет с помощью контейнера с Keycloak работу сервиса в целом.
