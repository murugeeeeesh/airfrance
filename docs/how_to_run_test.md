# How to Run the Project and Tests

## Prerequisites

- Java 21 (the project uses Java 21).
- Maven 3.9 or newer, unless the Maven wrapper is configured correctly.
- PowerShell on Windows, or a shell that can run the Maven commands below.

Verify Java and Maven:

```powershell
java -version
mvn -version
```

The Java version output should report version 21. Maven should use the same Java 21 installation.

## Configure admin access

Public product listing and detail endpoints work without credentials. To enable the admin API, set these variables before starting the app:

```powershell
$env:ADMIN_USERNAME = "admin"
$env:ADMIN_PASSWORD = "replace-with-a-strong-password"
$env:JWT_SECRET = "replace-with-at-least-32-random-characters"
```

`JWT_SECRET` must contain at least 32 UTF-8 bytes. Keep production credentials and signing keys private; do not commit them to source control. If `JWT_SECRET` is not set, the application creates a temporary random key, so issued tokens stop working after restart. Admin login is disabled unless both `ADMIN_USERNAME` and `ADMIN_PASSWORD` are configured.

## Run the application

From the project root (`D:\java_ecommerce\airfrance\airfrance`), start Spring Boot with Maven:

```powershell
mvn spring-boot:run
```

The service listens on `http://localhost:8080`. Keep this terminal running while you call endpoints. Stop it with **Ctrl+C**.

To compile and package the application:

```powershell
mvn package
```

The executable JAR is written under `target`. To run it after packaging:

```powershell
java -jar .\target\airfrance-0.0.1-SNAPSHOT.jar
```

Set the admin environment variables in the same PowerShell session before either start command if you need admin endpoints.

### Admin username and password

The application does **not** have a built-in admin username or password. Use the values you set for `ADMIN_USERNAME` and `ADMIN_PASSWORD` before starting the application. For example, if you start it with:

```powershell
$env:ADMIN_USERNAME = "admin"
$env:ADMIN_PASSWORD = "admin123"
$env:JWT_SECRET = "local-development-secret-at-least-32-characters"
mvn spring-boot:run
```

then the local Postman login values are:

- Username: `admin`
- Password: `admin123`

These are example **local development** credentials, not application defaults. Choose your own password and never use example credentials in production. If you start the app without setting `ADMIN_USERNAME` and `ADMIN_PASSWORD`, admin login is disabled.

## Run all tests

From the project root:

```powershell
mvn test
```

The tests use an in-memory H2 database and do not need Docker or a separately running application. The HTTP integration tests start Spring Boot on a random local port and clean up their product data before each test.

Run only the real-HTTP endpoint test suites:

```powershell
mvn "-Dtest=*Api*Test,*EndpointsTest" test
```

Run a single suite:

```powershell
mvn "-Dtest=PublicProductEndpointsTest" test
mvn "-Dtest=AdminProductEndpointsTest" test
mvn "-Dtest=ProductApiSecurityEndpointsTest" test
```

Run a single test method:

```powershell
mvn "-Dtest=PublicProductEndpointsTest#versionOneListingAndDetailsReturnRequestedLanguage" test
```

### Maven Wrapper

The project includes `mvnw.cmd`, but the wrapper can fail on machines without its expected Maven local-repository directory. If this happens, use the installed `mvn` command shown above. Alternatively, configure Maven and its local repository, then run:

```powershell
.\mvnw.cmd test
```

## Product API endpoints

| Method | Endpoint | Access | Description |
| --- | --- | --- | --- |
| `GET` | `/api/v1/products` | Public | Paginated active product list; supports `search`, `page`, and `size` |
| `GET` | `/api/v1/products/{id}` | Public | Product details in English or French |
| `GET` | `/api/v2/products` | Public | Paginated active products with both translations |
| `GET` | `/api/v2/products/{id}` | Public | Product details with both translations |
| `POST` | `/api/v1/auth/token` | Public | Exchange admin credentials for a JWT |
| `GET` | `/api/v1/admin/products` | Admin | Search and filter products, including inactive products |
| `POST` | `/api/v1/admin/products` | Admin | Create a product |
| `PUT` | `/api/v1/admin/products/{id}` | Admin | Edit a product |
| `GET` | `/api/v1/admin/products/{id}/revisions` | Admin | View product revision history |
| `POST` | `/api/v1/admin/products/{id}/revisions/{revision}/restore` | Admin | Restore a prior revision as a new revision |

### Bilingual responses

For v1, request the response language using `Accept-Language`. The default language is English:

```powershell
curl.exe -H "Accept-Language: fr" "http://localhost:8080/api/v1/products"
curl.exe -H "Accept-Language: en" "http://localhost:8080/api/v1/products"
```

Version 2 includes both English (`en`) and French (`fr`) translations in each product response.

### Admin login and API calls

Get an admin token:

```powershell
$body = @{
    username = $env:ADMIN_USERNAME
    password = $env:ADMIN_PASSWORD
} | ConvertTo-Json

$login = Invoke-RestMethod `
    -Method Post `
    -Uri "http://localhost:8080/api/v1/auth/token" `
    -ContentType "application/json" `
    -Body $body

$token = $login.accessToken
```

### Test the API in Postman

Keep the application running at `http://localhost:8080`, then create and send these Postman requests:

1. **Log in to obtain an admin token**
   - Method: `POST`
   - URL: `http://localhost:8080/api/v1/auth/token`
   - Body → **raw** → **JSON**:

     ```json
     {
       "username": "admin",
       "password": "admin123"
     }
     ```

   - Replace these example credentials with the exact values configured in `ADMIN_USERNAME` and `ADMIN_PASSWORD` when you started the app.
   - A successful response contains `accessToken`, `tokenType`, and `expiresIn`. Copy the `accessToken` value.

2. **Call a public product endpoint**
   - Method: `GET`
   - URL: `http://localhost:8080/api/v1/products`
   - No authentication is required.
   - To request French, add the header `Accept-Language: fr`. The default is English.

3. **Call a protected admin endpoint**
   - Method: `GET`
   - URL: `http://localhost:8080/api/v1/admin/products`
   - Authorization tab → Type: **Bearer Token** → paste the `accessToken` from the login response into the Token field.
   - You can also use the header `Authorization: Bearer <accessToken>`.

4. **Create a product**
   - Method: `POST`
   - URL: `http://localhost:8080/api/v1/admin/products`
   - Set Authorization to **Bearer Token** as above.
   - Set `Content-Type: application/json` (Postman sets this when Body is raw JSON).
   - Body → **raw** → **JSON**:

     ```json
     {
       "sku": "TRAVEL-001",
       "nameEn": "Travel kit",
       "nameFr": "Kit de voyage",
       "descriptionEn": "Travel essentials",
       "descriptionFr": "Essentiels de voyage",
       "categoryEn": "Travel",
       "categoryFr": "Voyage",
       "price": 49.95,
       "currency": "EUR",
       "status": "ACTIVE"
     }
     ```

   - A successful create returns **201 Created** and includes the generated product `id`. Use that ID in the detail, edit, and revision requests.

5. **Try the other endpoints**
   - `GET http://localhost:8080/api/v1/products/{id}` — public detail; send `Accept-Language: fr` for French.
   - `GET http://localhost:8080/api/v2/products` — public list with both languages.
   - `GET http://localhost:8080/api/v2/products/{id}` — public bilingual detail.
   - `PUT http://localhost:8080/api/v1/admin/products/{id}` — edit; send the product JSON body and admin Bearer token.
   - `GET http://localhost:8080/api/v1/admin/products/{id}/revisions` — view edit history; admin Bearer token required.
   - `POST http://localhost:8080/api/v1/admin/products/{id}/revisions/{revision}/restore` — restore a revision; admin Bearer token required.

Admin endpoints return **401 Unauthorized** if the token is missing, invalid, or expired. Re-run the login request to obtain a fresh token. Public product endpoints return active products only. Data stored in the default H2 database is cleared when the application stops.

Use the token to list all products, including inactive ones:

```powershell
Invoke-RestMethod `
    -Uri "http://localhost:8080/api/v1/admin/products?status=INACTIVE" `
    -Headers @{ Authorization = "Bearer $token" }
```

Create and edit requests require English and French product fields. Example request body:

```powershell
$product = @{
    sku = "TRAVEL-001"
    nameEn = "Travel kit"
    nameFr = "Kit de voyage"
    descriptionEn = "Travel essentials"
    descriptionFr = "Essentiels de voyage"
    categoryEn = "Travel"
    categoryFr = "Voyage"
    price = 49.95
    currency = "EUR"
    status = "ACTIVE"
} | ConvertTo-Json
```

Product status must be `ACTIVE` or `INACTIVE`; currency must be a three-letter uppercase code; price must be non-negative. Public APIs only return active products.

## Test coverage

The automated suites cover:

- Public v1 and v2 product listings and details.
- English/French language selection and bilingual translations.
- Search, pagination, and invalid pagination values.
- Active/inactive product visibility.
- Admin token issuance, invalid credentials, and malformed login requests.
- Admin authentication failures for protected routes.
- Product create and edit, validation, duplicate SKU, and persistence.
- Revision history, attribution, and restoration.
- Missing product and revision error responses.
- Unit tests for response mapping and language negotiation.

## Test results and logs

Maven writes test reports to:

```text
target/surefire-reports/
```

The project-level runtime validation summary is in [runtime-validation-report.md](../runtime-validation-report.md).

## Database note

The default database is in-memory H2. Product data is cleared when the application stops. For durable or production use, configure a persistent database using Spring's `spring.datasource.*` settings and use a production-ready schema migration strategy.
