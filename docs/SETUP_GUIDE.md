# Setup Guide

## Prerequisites
- JDK 21
- Maven 3.9+ (or use your IDE's bundled Maven)
- (Optional, for production profile) MySQL 8
- (Optional) Docker & Docker Compose

## 1. Quick Start (H2, zero setup)

```bash
cd ecommerce-platform
mvn spring-boot:run
```

Visit **http://localhost:8080**. The `dev` profile (H2 in-memory DB) is active by default.

Seeded accounts/data:
- Admin login: `admin@shop.com` / `admin123`
- Categories: Electronics, Fashion, Home & Kitchen, Books

H2 console: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:ecommercedb`
- User: `sa`, Password: (blank)

## 2. Running Against MySQL

1. Create nothing manually — `createDatabaseIfNotExist=true` handles it.
2. Export environment variables and run with the `prod` profile:

```bash
export SPRING_PROFILES_ACTIVE=prod
export DB_URL="jdbc:mysql://localhost:3306/ecommerce_db?createDatabaseIfNotExist=true"
export DB_USERNAME=root
export DB_PASSWORD=nurak@1234
mvn spring-boot:run
```

## 3. Docker Compose (MySQL + App together)

```bash
export MYSQL_ROOT_PASSWORD=nurak@1234
export DB_PASSWORD=nurak@1234
docker compose up --build
```

This builds the app image (multi-stage Dockerfile), starts MySQL, waits for its healthcheck, then starts the app with the `prod` profile pointed at the `mysql` service. Uploaded product images persist in the `uploads_data` volume.

To stop and remove containers (keeping volumes/data):
```bash
docker compose down
```

To also wipe data volumes:
```bash
docker compose down -v
```

## 4. Building a Standalone JAR

```bash
mvn clean package
java -jar target/platform-1.0.0.jar
```

Pass `--spring.profiles.active=prod` and the `DB_*` env vars as needed.

## 5. Running Tests

```bash
mvn test
```

## 6. Common Configuration

| Property | Purpose | Default |
|---|---|---|
| `server.port` | HTTP port | `8080` |
| `app.upload.dir` | Where product images are stored on disk | `uploads/products` |
| `spring.servlet.multipart.max-file-size` | Max upload size | `5MB` |
| `app.payment.provider` | Reserved for selecting a payment gateway bean | `mock` |

## 7. First Steps After Startup

1. Log in as `admin@shop.com` / `admin123`.
2. Go to **Admin → Categories** and add/confirm categories.
3. Go to **Admin → Products → Add Product** to publish your first product (with image).
4. Log out, register a customer account, browse `/products`, add items to cart, and complete checkout with any payment method (Mock gateway will succeed).
5. Check **My Orders** as the customer and **Admin → Orders** as the admin to update status and watch the tracking timeline update.
