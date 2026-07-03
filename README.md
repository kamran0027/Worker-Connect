# WorkerConnect – Online Local Worker Hiring Platform

## Tech Stack
- **Backend**: Spring Boot 3.2 (Java 17)
- **Frontend**: Thymeleaf + Bootstrap 5
- **Database**: MySQL
- **Security**: Spring Security 6 (Role-Based: USER, WORKER, ADMIN)
- **ORM**: Spring Data JPA / Hibernate
- **Payments**: Razorpay + Stripe
- **PDF**: iText 5
- **Email**: Spring Mail (Gmail SMTP)

---

## Quick Start

### 1. Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8+

### 2. Database
```sql
CREATE DATABASE workerconnect_db;
```

### 3. Configuration — `src/main/resources/application.properties`
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/workerconnect_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD

spring.mail.username=YOUR_GMAIL
spring.mail.password=YOUR_APP_PASSWORD

razorpay.key.id=YOUR_RAZORPAY_KEY_ID
razorpay.key.secret=YOUR_RAZORPAY_KEY_SECRET

```

### 4. Run
```bash
mvn spring-boot:run
```
Open: http://localhost:8080

---

## Default Admin Credentials
| Email | Password |
|-------|----------|
| admin@workerconnect.com | Admin@123 |

---

## Features by Role

### User (Customer)
- Register / Login / Forgot Password / Change Password
- Search workers by city, profession, price, rating
- Book workers with date, time, description
- Digital agreement – view & sign
- Online payment via Razorpay or COD
- Download invoice (PDF)
- Download agreement (PDF)
- Rate & review workers
- View booking history & payment history

### Worker
- Register (pending admin approval)
- Full profile: personal, professional, pricing, availability
- Upload Aadhaar, PAN, certificates
- View booking requests → Accept / Reject
- Sign service agreement
- Mark work started / completed
- View earnings dashboard & reviews

### Admin
- Dashboard with live stats (users, workers, revenue, bookings)
- User management (view, search, activate/deactivate, delete)
- Worker management (view documents, approve/reject/suspend/delete)
- Booking management (view all, cancel, resolve disputes)
- Agreement management (view, download PDF)
- Payment management (view all, download invoices)
- Category management (add, edit, toggle, delete)
- Review moderation (hide, delete fake reviews)

---

## Project Structure
```
src/main/java/com/workerconnect/
├── config/          # Security, Web MVC, Data Initializer
├── controller/      # Auth, Home, User, Worker, Admin
├── dto/             # Registration & booking DTOs
├── enums/           # Role, BookingStatus, PaymentStatus, etc.
├── model/           # JPA Entities
├── repository/      # Spring Data JPA Repositories
├── security/        # CustomUserDetailsService
├── service/         # Business logic
└── util/            # FileStorageService

src/main/resources/
├── templates/       # Thymeleaf HTML templates
│   ├── auth/        # login, register, forgot/reset password
│   ├── user/        # dashboard, profile, bookings, payments
│   ├── worker/      # dashboard, profile, bookings, earnings
│   ├── admin/       # dashboard + all management pages
│   └── workers/     # public search & detail pages
└── static/
    ├── css/style.css
    └── js/app.js
```

---

## Payment Setup
### Razorpay
1. Create account at razorpay.com
2. Get Key ID & Secret from Dashboard → Settings → API Keys
3. Add to application.properties



## Email Setup (Gmail)
1. Enable 2FA on Gmail
2. Generate App Password: Google Account → Security → App Passwords
3. Add email + app password to application.properties
