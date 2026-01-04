# ⚡ PowerSight - eps_nwt  
**Real-time Electricity Consumption & Smart Grid Monitoring Platform**

PowerSight is a cloud-native platform designed to monitor, analyze and manage electricity consumption for **households, buildings and entire cities** in real time.  
It simulates a national-scale smart-meter infrastructure similar to what a utility like **EPS Serbia** would operate.

---

## 🌍 What is PowerSight?

PowerSight connects **thousands of smart meters (simulators)** to a **central cloud platform** that:
- collects electricity usage every minute
- tracks device availability (online/offline)
- generates bills
- provides real-time analytics for users and administrators

The system supports:
- citizens (households),
- utility officers,
- and administrators operating the national grid.

---

## 🧠 Core Features

### 🔌 Smart Meter Simulation
- AMQP-based communication with the platform  
- 1 minute = 1 simulated hour of consumption  
- Realistic day/night usage patterns  
- Heartbeat & offline detection  
- Offline buffering & 90-day local storage  

### 📊 Real-Time Monitoring
- Live consumption via WebSockets  
- City-level, building-level & household analytics  
- Interactive charts for:
  - last hour
  - day
  - month
  - year  
- Availability tracking (uptime & downtime)

### 🏠 Property & Household Management
- Register buildings and apartments
- Upload ownership documents (PDF/images)
- GIS-based location selection (map)
- Admin approval workflows

### 👥 User & Access Control
- Citizens, officers & administrators
- Ownership verification
- Access delegation between users
- Secure role separation

### 💳 Billing & Payments
- Monthly PDF invoices with QR codes
- Tariff-based electricity calculation
- Online card payment simulation
- Email delivery of bills & receipts

### 📅 Utility Office Scheduling
- Officers manage their calendars
- Citizens book 30-minute appointments
- Conflict-free scheduling

---

## 🏗 System Architecture

[ Smart Meter Simulators ]  
│  
(AMQP)  
│  
[ Message Broker ]  
│  
┌────────▼─────────┐  
│ Backend API │  
│ (Spring Boot) │  
└────────┬─────────┘  
│  
┌─────────▼─────────┐  
│ Time-Series DB │ ← energy readings & device status  
│ (InfluxDB) │  
└─────────┬─────────┘  
│  
┌─────────▼─────────┐  
│ Relational DB │ ← users, homes, billing  
│ (PostgreSQL) │  
└─────────┬─────────┘  
│  
┌──────▼───────┐  
│ Frontend UI │ ←  Angular  
└──────────────┘  


---

## 🧰 Technology Stack

| Layer | Technology |
|------|-----------|
| Backend | Java + Spring Boot |
| Frontend | Angular |
| Messaging | AMQP (RabbitMQ) |
| Realtime | WebSockets |
| Databases | PostgreSQL + InfluxDB |
| Reverse Proxy | Nginx |
| Storage | Object storage for files |
| API Docs | OpenAPI |
| Charts | Chart.js / ECharts |
| Maps | leaflet |
| Load Testing | JMeter / Locust |

---

## 🚀 Running the Project

```bash
# start infrastructure
docker-compose up

# backend
cd backend
./mvnw spring-boot:run

# frontend
cd frontend
npm install
npm run build
```

The platform will be available via Nginx at:
```bash
http://localhost
```

## 🔐 Default Admin
On first startup, a superadmin account is created:

**Username**: admin  
**Password**: generated on first boot

The password is written to a text file whose path is shown in the logs.
The admin must change it on first login.

## 🧪 Smart Meter Simulator

Run multiple simulators:

```bash
cd simulator
go run simulator.go -id="1" -municipality="novi sad"
go run simulator.go -id="2" -municipality="kragujevac"
go run simulator.go -id="3" -municipality="vrbas"
```


Each simulator:
sends heartbeats
streams consumption
buffers data when offline

## 📈 Designed for Scale

PowerSight is built to support:

millions of meters
thousands of concurrent users
real-time dashboards
fault-tolerant data ingestion

This mirrors how a national electricity provider would operate its digital grid.

## 👤 Authors

Developed as part of a course Advanced Web Technologies - Napredne veb tehnologije (2024/25)
Faculty of Technical Sciences — Novi Sad

