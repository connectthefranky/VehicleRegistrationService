# Vehicle Registration Service

Simple HTTP service for managing vehicle registrations and account statistics, built with plain Java (no external dependencies).

## Configuration & Run

1. Ensure Java 17+ is installed.
2. Navigate to folder containing VRS.jar
3. Run:
   ```bash
   java -jar VRS.jar
   ```
4. Service is available at: `http://localhost:8080`

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
