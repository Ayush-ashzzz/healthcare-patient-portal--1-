# Healthcare Patient Portal (Spring Boot + Maven + MySQL)

A backend REST API starter for patients to register, log in/out, view their basic profile, upload prescriptions/medical reports, list report metadata, and download their own uploaded files.

> **Important:** This is a learning/demo starter, not a production-ready clinical system. It handles sensitive health data. Before real use, add CSRF protection for browser sessions, HTTPS, robust file-content inspection/antivirus, audit trails, retention/consent rules, backups, rate limits, monitoring, secret management, and a privacy/security review appropriate to your jurisdiction. The demo disables CSRF to simplify Postman/curl testing.

## 1. Technology stack

- Java 21
- Spring Boot 3.5.x
- Maven
- Spring Web / REST
- Spring Security with server-side HTTP session
- Spring Data JPA / Hibernate
- MySQL 8+
- Bean Validation
- Local filesystem storage for uploaded files

## 2. Features

- Patient registration with unique email and BCrypt password hashing
- Session-based login and logout
- Authenticated patient profile endpoint
- Prescription/report upload (PDF, JPEG, PNG; max 10 MB)
- Patient-scoped report list and download; one patient cannot access another patient's report by ID
- Validation and basic JSON error responses

## 3. Prerequisites

Install Java 21, Maven 3.9+, and MySQL 8+. Verify:

```bash
java -version
mvn -version
mysql --version
```

## 4. MySQL configuration

Create a database and least-privilege application user (run as a MySQL administrator):

```sql
CREATE DATABASE healthcare_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'healthcare_app'@'localhost' IDENTIFIED BY 'replace-with-a-strong-password';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, REFERENCES ON healthcare_db.* TO 'healthcare_app'@'localhost';
FLUSH PRIVILEGES;
```

Set environment variables before starting the app. Do not commit real credentials:

**Linux/macOS**
```bash
export DB_URL='jdbc:mysql://localhost:3306/healthcare_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
export DB_USERNAME='healthcare_app'
export DB_PASSWORD='replace-with-a-strong-password'
export UPLOAD_DIR='./uploads'
```

**PowerShell**
```powershell
$env:DB_URL='jdbc:mysql://localhost:3306/healthcare_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
$env:DB_USERNAME='healthcare_app'
$env:DB_PASSWORD='replace-with-a-strong-password'
$env:UPLOAD_DIR='./uploads'
```

Defaults are in `src/main/resources/application.properties` for local development only. Replace the placeholder password. Hibernate uses `ddl-auto=update` for a quick demo; use Flyway/Liquibase migrations and `validate` in production.

## 5. Run the application

From the project root:

```bash
mvn clean test
mvn spring-boot:run
```

The API listens on `http://localhost:8080`. The upload folder is created automatically. Keep `uploads/` outside any public static-resource directory and back it up securely.

Build an executable jar:

```bash
mvn clean package
java -jar target/healthcare-patient-portal-0.0.1-SNAPSHOT.jar
```

## 6. API reference

All JSON endpoints use `Content-Type: application/json` except multipart upload. Login creates a session cookie (`JSESSIONID`); send that cookie with subsequent requests.

| Method | Endpoint | Auth | Purpose |
|---|---|---:|---|
| POST | `/api/auth/register` | No | Create patient account |
| POST | `/api/auth/login` | No | Authenticate and create session |
| POST | `/api/auth/logout` | Yes | Invalidate session and clear cookie |
| GET | `/api/auth/me` | Yes | Current authenticated profile |
| GET | `/api/patients/me` | Yes | Current patient's basic profile |
| POST | `/api/reports` | Yes | Upload a prescription/report |
| GET | `/api/reports` | Yes | List report metadata for current patient |
| GET | `/api/reports/{id}/download` | Yes | Download own report |

### Register

```http
POST /api/auth/register
Content-Type: application/json
```

```json
{
  "fullName": "Asha Kumar",
  "email": "asha@example.com",
  "password": "ExamplePass123!",
  "dateOfBirth": "1992-04-15",
  "phone": "+91-9000000000",
  "gender": "Female",
  "address": "Chennai, India"
}
```

Returns `201 Created` with profile data; password/hash is never returned. Password must be at least 8 characters.

### Login and retain the session

For curl, use a cookie jar:

```bash
curl -i -c cookies.txt -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"asha@example.com","password":"ExamplePass123!"}'
```

The response sets `JSESSIONID`. The example intentionally uses session authentication, not JWT. In a browser, the client should send credentials using same-origin requests or `credentials: "include"` with carefully configured CORS if cross-origin.

### View profile

```bash
curl -b cookies.txt http://localhost:8080/api/patients/me
```

### Upload prescription/report

```bash
curl -b cookies.txt -X POST http://localhost:8080/api/reports \
  -F 'file=@./prescription.pdf;type=application/pdf' \
  -F 'description=Prescription from clinic'
```

Allowed declared MIME types: `application/pdf`, `image/jpeg`, `image/png`. Max 10 MB. MIME headers can be spoofed; production must inspect actual file signatures and scan uploads.

### List all reports for the logged-in patient

```bash
curl -b cookies.txt http://localhost:8080/api/reports
```

Example response:

```json
[
  {
    "id": 1,
    "originalFileName": "prescription.pdf",
    "contentType": "application/pdf",
    "fileSize": 42810,
    "description": "Prescription from clinic",
    "uploadedAt": "2026-10-22T12:00:00",
    "downloadUrl": "/api/reports/1/download"
  }
]
```

### Download a report

```bash
curl -b cookies.txt -OJ http://localhost:8080/api/reports/1/download
```

The API scopes report lookup by both report ID and authenticated patient ID. An ID belonging to another patient returns 404.

### Logout

```bash
curl -i -b cookies.txt -c cookies.txt -X POST http://localhost:8080/api/auth/logout
```

A successful logout returns `204 No Content` and invalidates the server session. After logout, protected endpoints return `401` (or `403` depending on the security entry-point behavior).

## 7. Code flow / architecture

```text
HTTP request
  -> SecurityFilterChain (public register/login; other routes require session)
  -> Controller (HTTP / JSON validation)
  -> Service (business rules)
  -> Spring Data JPA Repository
  -> Hibernate
  -> MySQL

Registration:
AuthController -> PatientService -> BCryptPasswordEncoder -> PatientRepository -> patients table

Login:
AuthController -> AuthenticationManager -> UserDetailsService -> PatientRepository
               -> BCrypt verifies password -> SecurityContext stored in HTTP session

Profile:
SecurityContext Authentication -> PatientService -> PatientRepository -> PatientProfile DTO
(never return passwordHash)

Upload:
ReportController -> ReportService validates size/type/name -> writes UUID-named file to UPLOAD_DIR
                 -> MedicalReportRepository saves metadata -> medical_reports table

List/download:
Authenticated email -> Patient lookup -> report query constrained to patient ID
                    -> DTO list or protected file stream
```

### Package structure

```text
src/main/java/com/example/healthcare/
  HealthcareApplication.java
  config/
    AuthenticationConfig.java
    SecurityConfig.java
  controller/
    ApiExceptionHandler.java
    AuthController.java
    PatientController.java
    ReportController.java
  dto/
    LoginRequest.java
    PatientProfile.java
    RegisterRequest.java
    ReportResponse.java
  entity/
    MedicalReport.java
    Patient.java
  repository/
    MedicalReportRepository.java
    PatientRepository.java
  service/
    PatientService.java
    ReportService.java
src/main/resources/application.properties
```

### Database tables

- `patients`: ID, name, unique email, BCrypt password hash, DOB, phone, gender, address, creation time.
- `medical_reports`: ID, patient foreign key, original filename, generated storage filename, MIME type, file size, description, upload time.

Hibernate creates/updates these tables at startup in the demo configuration.

## 8. Postman quick start

1. `POST http://localhost:8080/api/auth/register` with the JSON above.
2. `POST http://localhost:8080/api/auth/login` with email/password. Ensure Postman retains cookies.
3. `GET http://localhost:8080/api/patients/me`.
4. `POST http://localhost:8080/api/reports`, Body → form-data: `file` (File), `description` (Text).
5. `GET http://localhost:8080/api/reports`.
6. `GET http://localhost:8080/api/reports/1/download`.
7. `POST http://localhost:8080/api/auth/logout`.

## 9. Configuration notes and production hardening

- **CSRF:** Disabled for demo convenience. For browser cookie/session auth, enable CSRF and send the CSRF token on state-changing requests. Do not deploy this exact setting to production.
- **CORS:** No permissive CORS policy is configured. If a separate frontend is added, allow only its exact origin and configure credentials intentionally.
- **Password handling:** BCrypt hash only; never log passwords. Consider breached-password checks and rate limiting.
- **File security:** MIME type and filename checks are baseline only. Add signature/magic-byte validation, malware scanning, quotas, encrypted storage, and authorized audit logging. Keep uploaded content private.
- **Session security:** Use HTTPS, secure/HttpOnly/SameSite cookie attributes, session timeout, and appropriate session-fixation protections.
- **Secrets:** Use environment variables or a secret manager; don't commit credentials.
- **Schema:** Replace Hibernate `update` with migration tooling and `validate`.
- **Privacy:** Apply least-privilege access, audit access to health records, retention/deletion policies, consent handling, and applicable healthcare/privacy law requirements.
- **Tests:** Add integration tests for registration/login/logout, file validation, ownership isolation, and database migrations before production.

## 10. Troubleshooting

- `Communications link failure`: confirm MySQL is running, host/port are correct, and DB user has privileges.
- `Access denied for user`: check `DB_USERNAME` / `DB_PASSWORD`.
- `Unsupported class file version`: verify Java 21 is used by Maven (`mvn -version`).
- Upload returns `415`: ensure file content type is PDF/JPEG/PNG.
- Upload returns `413`: file exceeds the configured 10 MB limit.
- Protected endpoint redirects or returns an auth error: login first and preserve `JSESSIONID`.

## 11. Scope limitations

This backend does not include a frontend, clinician/admin roles, appointment scheduling, report authoring by providers, password reset, email verification, JWT, or cloud object storage. “Reports” here means uploaded files and their metadata, not automatically interpreted lab results.
