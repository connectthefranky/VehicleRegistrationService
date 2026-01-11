# Vehicle Registration Service

Simple HTTP service for managing vehicle registrations and account statistics, built with plain Java (no external dependencies).

## Configuration & Run

1. Ensure Java 17+ is installed.
2. Compile:
   ```bash
   javac src/Main.java src/server/*.java src/controller/*.java src/service/*.java src/service/impl/*.java src/repository/Database.java src/repository/*.java src/repository/impl/*.java src/model/*.java src/util/*.java
   ```
3. Run:
   ```bash
   java -cp src Main
   ```
4. Service is available at: `http://localhost:8089`

## Endpoints

### Create account
`POST /account`

Body:
```json
{ "accountId": "testni.test@test.com" }
```

### Register vehicle
`POST /register`

Headers:
```
Authorization: Basic base64(accountId:password)
```

Body:
```json
{ "registrationCode": "555K487", "validUntil": "2024-11-18" }
```

### Statistics
`GET /statistics`

Headers:
```
Authorization: Basic base64(accountId:password)
```

### Check registration
`GET /registration/{registrationCode}`

### Help
`GET /help`

### Simple GUI
`GET /gui`
