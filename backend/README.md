# CampusOS Backend

Spring Boot REST API for CampusOS authentication, student records, faculty, courses, and enrollments. PostgreSQL stores application data; protected requests use JWT bearer authentication.

## Requirements

- Java 21 or later
- PostgreSQL 14 or later

## PostgreSQL setup

Create a local database and application role, or use an existing PostgreSQL account:

```sql
CREATE DATABASE campusos;
CREATE USER campusos_app WITH PASSWORD 'choose-a-local-password';
GRANT ALL PRIVILEGES ON DATABASE campusos TO campusos_app;
```

Connect to the `campusos` database and grant schema access if using a newly created role:

```sql
GRANT ALL ON SCHEMA public TO campusos_app;
```

The application uses Hibernate `ddl-auto=update` to create/update tables and reads connection settings from environment variables. The defaults retain the original local setup (`localhost:5432/campusos`, user `postgres`, password `campusos123`); override them for your environment.

## Environment variables

`JWT_SECRET` is required and must contain at least 32 UTF-8 bytes. Generate a strong local value, for example:

```bash
export JWT_SECRET="$(openssl rand -base64 48)"
```

Database overrides are optional:

```bash
export DB_URL="jdbc:postgresql://localhost:5432/campusos"
export DB_USERNAME="campusos_app"
export DB_PASSWORD="choose-a-local-password"
```

The AI document-verification service logs in through `/api/auth/login`; set its `AI_SERVICE_EMAIL` and `AI_SERVICE_PASSWORD` in the AI service environment. No AI-service account or password is seeded by this backend.

## Start the application

From this directory:

```bash
./mvnw spring-boot:run
```

The API listens on `http://localhost:8080` by default.

## Tests

Run all JUnit tests:

```bash
./mvnw test
```

Build the application:

```bash
./mvnw package
```

## Swagger / OpenAPI

- Swagger UI: <http://localhost:8080/swagger-ui/index.html>
- OpenAPI JSON: <http://localhost:8080/v3/api-docs>

The Swagger UI and schema endpoints are public. Application endpoints remain protected by Spring Security. Use **Authorize** with the JWT returned by `/api/auth/login`; enter the token value and Swagger sends it as `Authorization: Bearer <token>`.

## Authentication and roles

Register and login endpoints are public. Registration creates a `STUDENT` account only. Passwords are stored using BCrypt. There are no demo credentials in the repository. Student, faculty, and admin CRUD write permissions are role-checked; provisioning an `ADMIN` account must be done through the trusted deployment/admin process, not public registration.

To create a local student account:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"name":"Demo Student","email":"demo.student@example.test","password":"use-a-local-password"}'
```

Log in and retain the returned token:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"demo.student@example.test","password":"use-a-local-password"}'
```

## Dummy student records

On application startup, Spring runs `src/main/resources/data.sql` after Hibernate has updated the schema. It inserts ten fictional records across Computer Science, Electronics Engineering, Mechanical Engineering, Civil Engineering, Business Administration, and Biotechnology, with semesters 2 through 8. Emails use the reserved `.test` domain. The insert omits generated IDs and skips roll numbers already present, so repeated starts do not duplicate the demo records.

Seeded roll numbers for AI OCR/cross-verification tests include `24CSE1001`, `22ECE2017`, and `24BBA5019`. The AI service must use a registered account and include its JWT when requesting `GET /api/students/{rollNumber}`.

## Example API requests

List students (requires any authenticated role):

```bash
curl http://localhost:8080/api/students \
  -H "Authorization: Bearer $TOKEN"
```

Look up the fictional OCR test record:

```bash
curl http://localhost:8080/api/students/24CSE1001 \
  -H "Authorization: Bearer $TOKEN"
```

Create a student (requires an ADMIN token):

```bash
curl -X POST http://localhost:8080/api/students \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"rollNumber":"24CSE9999","name":"Demo Learner","email":"demo.learner@example.test","department":"Computer Science","semester":3}'
```

Update and delete use `PUT /api/students/{id}` and `DELETE /api/students/{id}` respectively and require an ADMIN token. Student lookup supports both `GET /api/students/{rollNumber}` and `GET /api/students/id/{id}`.

Validation, not-found, authentication, and authorization failures return JSON error objects. Unexpected server errors do not include stack traces or internal exception details.