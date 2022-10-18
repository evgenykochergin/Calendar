# Calendar app

## Requirements
Написать бэкенд для сервиса Календарь.

(обязательное требование) Сервис должен иметь HTTP API, позволяющее:
* создать пользователя;
* создать встречу в календаре пользователя со списком приглашенных пользователей;
* получить детали встречи;
* принять или отклонить приглашение другого пользователя;
* найти все встречи пользователя для заданного промежутка времени;
* для заданного списка пользователей и минимальной продолжительности встречи, найти ближайшей интервал времени, в котором все эти пользователи свободны.
(Важно) У встреч в календаре должна быть возможна настройка повторов. В повторах нужно поддержать все возможности, доступные в Google-календаре, кроме Сustom.

Из дополнительного были реализованы:
* аутентификация пользователя;
* поддержка видимости встреч (если встреча приватная, другие пользователи могут получить только информацию о занятости пользователя, но не детали встречи);

## Technologies used
- **Javalin** - a very lightweight web framework
- **Jooq** - for working with RDBMs, it generates Java code from your database and lets you build type safe SQL queries through its fluent API
- **H2** - an in-memory RDBMs, can be replaced with any other RDBMS, e.g. postges or mysql
- **Flyway** - a database migration tool
- **Jackson** - JSON serialization and deserialization
- **HikariCP** - JDBC connection pool
- **JUnit 5, AssertJ, REST-assured** - tests

## Build (could be skipped since jar file is committed)
You should have a Maven installed in place.

After you should navigate to project folder and execute following command:
```bash
mvn clean install
```
As a result you will get a calendar-app-1.0-SNAPSHOT.jar file inside target folder.

## Run application
```bash
java -jar target/calendar-app-1.0-SNAPSHOT.jar 
```

