# Delivery Scheduling MVP

A full-stack application for scheduling delivery time slots with concurrency protection.

## 📁 Project Structure

```
/
├── backend/          # Spring Boot REST API (Java 21)
├── frontend/         # Angular 18 SPA
└── README.md
```

## 🚀 Quick Start

### Prerequisites
- **Java 21** (JDK)
- **Maven 3.8+**
- **Node.js 20+**
- **npm 10+**

### Backend Setup

```bash
cd backend

# Run tests
mvn test

# Start the application
mvn spring-boot:run
```

The API will be available at: `http://localhost:8080`

### Frontend Setup

```bash
cd frontend

# Install dependencies
npm install

# Start development server
ng serve
# or
npm start
```

The UI will be available at: `http://localhost:4200`

### API Base URL Configuration

The frontend uses environment files for API configuration:

- **Development**: `src/environments/environment.ts` → `http://localhost:8080/api`
- **Production**: `src/environments/environment.prod.ts` → `/api` (relative)

To change the API URL, modify the `apiUrl` property in the appropriate environment file.

---

## 📋 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/delivery-methods` | List all delivery methods |
| `GET` | `/api/time-slots?method={M}&date={D}` | Get available slots |
| `POST` | `/api/reservations` | Create a reservation |
| `GET` | `/api/reservations/{id}` | Get reservation details |

### Example Requests

```bash
# Get delivery methods
curl http://localhost:8080/api/delivery-methods

# Get time slots for DRIVE on 2026-01-08
curl "http://localhost:8080/api/time-slots?method=DRIVE&date=2026-01-08"

# Make a reservation
curl -X POST http://localhost:8080/api/reservations \
  -H "Content-Type: application/json" \
  -d '{"method":"DRIVE","date":"2026-01-08","slotId":1,"customerId":"CUST-001"}'
```

---

## ⏰ Time Slot Rules

| Method | Date Restriction | Available Slots |
|--------|------------------|-----------------|
| **DRIVE** | Any future date | 09:00-11:00, 11:00-13:00, 14:00-16:00, 16:00-18:00 |
| **DELIVERY** | Any future date | 09:00-11:00, 11:00-13:00, 14:00-16:00, 16:00-18:00 |
| **DELIVERY_TODAY** | Today only | 14:00-16:00, 16:00-18:00 |
| **DELIVERY_ASAP** | Today only | Rolling 2-hour window (e.g., 14:00-16:00 if requested at 14:30) |

---

## 🔒 Concurrency Handling

Double-booking is prevented using **two complementary mechanisms**:

### 1. Database Constraints
- Unique constraint on `(method, date, start_time)` prevents duplicate slots
- Ensures data integrity at the database level

### 2. Optimistic Locking
- `@Version` field on `TimeSlot` entity
- When two users attempt to reserve the same slot simultaneously:
  1. Both read the slot with version `0`
  2. First transaction commits, incrementing version to `1`
  3. Second transaction fails with `OptimisticLockException`
  4. API returns `409 Conflict`

### Request Flow

```
User A: POST /reservations {slotId: 5}  ──┐
                                          ├──> Only ONE succeeds
User B: POST /reservations {slotId: 5}  ──┘    Other gets 409
```

---

## 🧪 Testing

### Run All Tests
```bash
cd backend
mvn test
```

### Unit Tests (with Mockito)
- `TimeSlotServiceTest` - Slot generation logic
- `ReservationServiceTest` - Reservation logic

### Integration Tests
- `ReservationConcurrencyTest` - Verifies only one concurrent reservation succeeds

---

## ☁️ Azure Deployment

### Architecture Overview

```
┌─────────────────────────────────────────────────────┐
│                   Azure App Service                  │
│  ┌─────────────────┐    ┌────────────────────────┐ │
│  │  Frontend (SPA) │───>│  Backend (Spring Boot) │ │
│  │   Static Files  │    │      REST API          │ │
│  └─────────────────┘    └──────────┬─────────────┘ │
└────────────────────────────────────┼───────────────┘
                                     │
                          ┌──────────▼──────────┐
                          │    Azure SQL DB     │
                          │   (replaces H2)     │
                          └─────────────────────┘
```

### Deployment Steps

#### 1. Create Azure Resources

```bash
# Create resource group
az group create --name delivery-rg --location westeurope

# Create App Service Plan (Linux, Java 21)
az appservice plan create \
  --name delivery-plan \
  --resource-group delivery-rg \
  --is-linux \
  --sku B1

# Create Web App
az webapp create \
  --name delivery-api-<unique> \
  --resource-group delivery-rg \
  --plan delivery-plan \
  --runtime "JAVA:21-java21"

# Create Azure SQL Database
az sql server create \
  --name delivery-sql-<unique> \
  --resource-group delivery-rg \
  --admin-user sqladmin \
  --admin-password <secure-password>

az sql db create \
  --name deliverydb \
  --resource-group delivery-rg \
  --server delivery-sql-<unique> \
  --service-objective S0
```

#### 2. Configure Application

Update `application.properties` for Azure SQL:

```properties
# Azure SQL Configuration
spring.datasource.url=jdbc:sqlserver://<server>.database.windows.net:1433;database=deliverydb
spring.datasource.username=sqladmin
spring.datasource.password=${AZURE_SQL_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.database-platform=org.hibernate.dialect.SQLServerDialect
```

Add SQL Server driver to `pom.xml`:

```xml
<dependency>
    <groupId>com.microsoft.sqlserver</groupId>
    <artifactId>mssql-jdbc</artifactId>
    <scope>runtime</scope>
</dependency>
```

#### 3. Set Environment Variables

```bash
az webapp config appsettings set \
  --name delivery-api-<unique> \
  --resource-group delivery-rg \
  --settings AZURE_SQL_PASSWORD=<secure-password>
```

#### 4. Deploy

```bash
# Build JAR
cd backend
mvn clean package -DskipTests

# Deploy to Azure
az webapp deploy \
  --name delivery-api-<unique> \
  --resource-group delivery-rg \
  --src-path target/delivery-scheduling-1.0.0-SNAPSHOT.jar
```

#### 5. Deploy Frontend

Build and deploy Angular as static files:

```bash
cd frontend
npm run build

# Upload to Azure Static Web Apps or blob storage
# Configure to proxy /api/* to backend App Service
```

### Azure Foundation Concepts

| Concept | Description |
|---------|-------------|
| **Resource Group** | Logical container for related Azure resources |
| **App Service Plan** | Defines compute resources (CPU, memory, scaling) |
| **App Service** | Managed platform for hosting web applications |
| **Azure SQL** | Fully managed relational database service |
| **Deployment Slots** | Enable zero-downtime deployments |

---

## 📐 Design Decisions

1. **On-the-fly slot generation**: Slots are created when first requested, not pre-seeded. This avoids storing millions of future slots.

2. **Single table for slots**: All delivery methods share the `time_slots` table, differentiated by `method` column.

3. **Stateless API**: No session management; each request is independent.

4. **Standalone Angular components**: Modern Angular 18 pattern without NgModules.

5. **customerId field**: Simple string identifier without full customer entity - keeps MVP focused.

---

## 🛠️ Technology Stack

### Backend
- Java 21
- Spring Boot 3.2.1
- Spring Data JPA
- H2 Database (dev) / Azure SQL (prod)
- Maven

### Frontend
- Angular 18
- Bootstrap 5.3
- TypeScript 5.4
- RxJS

---

## 📄 License

MIT License - Free for personal and commercial use.
