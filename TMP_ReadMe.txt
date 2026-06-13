# Task Management Platform (Microservices Architecture)

A robust, microservice-based Task Management Platform built with Spring Boot 3 and Java 17+. This system demonstrates modern backend engineering principles, including stateless JWT authentication, inter-service HTTP communication, and strict role-based access control.

## 🏗️ Architecture Overview

The platform currently consists of two isolated microservices operating within a monorepo structure. Each service maintains strict boundary isolation with its own dedicated PostgreSQL database.

### 1. User & Auth Service (UAS) - Port `8081`
**Database:** `authdb`
The identity provider and gatekeeper of the system. 
* Handles user registration and authentication.
* Generates and validates stateless JSON Web Tokens (JWTs).
* Enforces role-based access control (`USER` vs `ADMIN`).
* Exposes a lightweight internal bridge endpoint (`/exists`) for other services to verify user identities.

### 2. Task Management Service (TMS) - Port `8082`
**Database:** `taskdb`
The core business logic engine.
* Manages Projects and Tasks.
* Enforces structural validation (Tasks cannot be orphaned without a Project).
* Implements a synchronous HTTP client (via Spring `RestClient`) to validate `assigneeUserId` against the Auth Service before assigning tasks.
* Uses targeted `PATCH` endpoints for partial updates (e.g., status transitions vs. reassignments).

---

## 🚀 Prerequisites

Before running the platform, ensure you have the following installed:
* **Java 17** or higher
* **Maven 3.8+**
* **PostgreSQL 14+**
* **Git**

---

## 🛠️ Setup & Installation

### 1. Database Configuration
Open pgAdmin or your PostgreSQL CLI and create the two required databases:
```sql
CREATE DATABASE authdb;
CREATE DATABASE taskdb;

## JWT Configuration for Both the servies
jwt.secret=your_very_long_base64_encoded_secret_key_here
jwt.expiration=86400000

To generate JWT secret

For window user
$bytes = New-Object Byte[] 64; [Security.Cryptography.RNGCryptoServiceProvider]::Create().GetBytes($bytes); [Convert]::ToBase64String($bytes)

For bash
openssl rand -base64 64


## To Run Application
Start Auth Service

cd uas\uas
mvn spring-boot:run

Start Task Service
cd tms\tms
mvn spring-boot:run


📖 API Documentation (Swagger/OpenAPI)
Both services generate real-time, interactive API documentation. Once the applications are running, you can explore and test the endpoints via your browser:

Auth Service UI: http://localhost:8081/swagger-ui/index.html

Task Service UI: http://localhost:8082/swagger-ui/index.html