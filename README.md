# 🛒 ShopEase — E-Commerce Platform

A full-stack e-commerce web application built with **Java 21, Spring Boot 3, Spring Security, Spring Data JPA, Thymeleaf, and MySQL** (H2 for local dev). It follows the architecture and phased plan frozen in `docs/ARCHITECTURE.md`.

## Features

- **Product management** — admins can add, edit, delete products, upload images, set price/stock/availability, organize by category.
- **Product catalog** — customers browse, search, filter by category, view product details.
- **Shopping cart** — add/remove items, update quantities, live totals.
- **Checkout & payments** — shipping address, payment method selection (UPI / Card / Net Banking / COD), pluggable `PaymentGateway` abstraction backed by a `MockPaymentGateway` for local development, with double-payment protection at the DB level.
- **Order management & tracking** — customers see order history and a visual status timeline; admins update order status (Pending → Confirmed → Processing → Shipped → Out for Delivery → Delivered / Cancelled).
- **Accounts & security** — registration, login (BCrypt + Spring Security), role-based access (`ROLE_CUSTOMER`, `ROLE_ADMIN`), profile management.
- **Admin dashboard** — totals for products, customers, orders, sales, and a recent-orders table.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.3 (Web, Security, Data JPA, Validation, Thymeleaf) |
| Database | MySQL 8 (prod) / H2 in-memory (dev, zero setup) |
| Templating | Thymeleaf + Bootstrap 5 |
| Build | Maven |
| Containerization | Docker + docker-compose |

## Project Structure

See `docs/ARCHITECTURE.md` for the full package layout, ER diagrams, and request-flow diagrams (product management, cart, checkout, payment, order tracking, admin flows).

## Getting Started (Local / Dev — no MySQL needed)

The `dev` profile uses an in-memory H2 database, so you can run the whole app with just a JDK and Maven.

```bash
mvn spring-boot:run
```

The app starts on **http://localhost:8080** with the `dev` profile active by default (see `application.properties`).

On first run, `DataInitializer` seeds:
- An admin account: `admin@shop.com` / `admin123`
- Four starter categories: Electronics, Fashion, Home & Kitchen, Books

H2 console (dev only): http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:ecommercedb`)

## Running with MySQL (production profile)

```bash
export SPRING_PROFILES_ACTIVE=prod
export DB_URL=jdbc:mysql://localhost:3306/ecommerce_db?createDatabaseIfNotExist=true
export DB_USERNAME=root
export DB_PASSWORD=nurak@1234
mvn spring-boot:run
```

## Running with Docker Compose (MySQL + app)

```bash
export MYSQL_ROOT_PASSWORD=nurak@1234
export DB_PASSWORD=nurak@1234
docker compose up --build
```

This starts a MySQL container and the packaged Spring Boot app together, wired via the `prod` profile. The app will be available on **http://localhost:8080**.

## Deploying to a Java-capable host (recommended)

This project is a Spring Boot application, so it is not a good fit for Vercel as-is. For cloud deployment, use a Java-capable host such as **Render**, **Railway**, or **Azure App Service**.

### Render example

1. Push this repo to GitHub.
2. In Render, create a new `Web Service` using the existing `Dockerfile`.
3. Set environment variables:
   - `SPRING_PROFILES_ACTIVE=prod`
   - `DB_URL=jdbc:mysql://<host>:3306/ecommerce_db?createDatabaseIfNotExist=true`
   - `DB_USERNAME=<your-db-user>`
   - `DB_PASSWORD=<your-db-password>`
4. Deploy the service.

### Railway example

1. Create a new project and deploy from GitHub.
2. Add a MySQL service.
3. Link the app service to the database and provide the same `DB_*` environment variables.
4. Start the app.

> Vercel is suitable for frontend-only apps. This project requires a Java runtime and a database, so it should be hosted on a platform that supports those services.

## Building a JAR

```bash
mvn clean package
java -jar target/platform-1.0.0.jar
```

## Payment Gateway

Checkout uses a `PaymentGateway` interface (`payment` package). `MockPaymentGateway` is wired in by default and always succeeds (except a reserved test amount of ₹13.00, which simulates a decline, and `COD` which is always accepted since cash is collected on delivery). To integrate a real provider (Razorpay/Stripe), implement `PaymentGateway`, fill in `RazorpayPaymentGateway`, and swap the bean used by `PaymentService`.

## Default Roles & URL Access

| Path | Access |
|---|---|
| `/`, `/products/**`, `/categories/**`, `/auth/**` | Public |
| `/cart/**`, `/checkout/**`, `/payment/**`, `/orders/**`, `/profile/**` | Authenticated (customer or admin) |
| `/admin/**` | `ROLE_ADMIN` only |

## Running Tests

```bash
mvn test
```

Includes unit tests for `ProductService`, `CartService`, `PaymentService` (including double-payment protection), and `OrderService`.

## Notes / Next Steps

- Product images are stored on local disk under `uploads/products` and served via `/uploads/products/**`. For production at scale, swap `FileStorageService` for S3/Cloud Storage.
- `spring.jpa.hibernate.ddl-auto=update` is convenient for development; use a proper migration tool (Flyway/Liquibase) before going to production.
- The real payment gateway integration in `RazorpayPaymentGateway` is a stub — wire in the actual SDK call before enabling it.
