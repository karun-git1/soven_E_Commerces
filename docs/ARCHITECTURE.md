# Architecture

## Stack
Java 21 + Spring Boot 3 (MVC, Security, Data JPA) + Thymeleaf + MySQL (H2 for dev) + a pluggable payment gateway abstraction.

## Layered Architecture

```
Presentation (Thymeleaf + Bootstrap)
        │
Controller (Spring MVC)
        │
Service (business logic, @Transactional boundaries)
        │
Repository (Spring Data JPA)
        │
Database (MySQL / H2)
```

Payment is a side-branch off the Service layer: `CheckoutService` calls `PaymentService`, which calls the `PaymentGateway` interface (`MockPaymentGateway` by default).

## Package Layout

```
com.ecommerce.platform
├── EcommerceApplication.java
├── config/            SecurityConfig, WebConfig, DataInitializer
├── controller/         Home/Auth/Product/Category/Cart/Checkout/Payment/Order/Profile
│   └── admin/          Admin/AdminProduct/AdminCategory/AdminOrder
├── entity/              User, Role, Product, Category, Cart, CartItem, Order, OrderItem,
│                        ShippingAddress, Payment, OrderStatus, PaymentMethod, PaymentStatus
├── repository/          Spring Data JPA repositories
├── service/             UserService, ProductService, CategoryService, CartService,
│                        CheckoutService, PaymentService, OrderService, FileStorageService
├── payment/             PaymentGateway, PaymentResult, MockPaymentGateway, RazorpayPaymentGateway
├── dto/                 Request objects for forms/binding
├── exception/           Custom exceptions + GlobalExceptionHandler
└── security/            CustomUserDetails, CustomUserDetailsService
```

## Request Flow — Add to Cart

```
Browser → CartController.addToCart()
        → CartService.addItem()
             - loads Cart by user id
             - loads Product, checks stock
             - creates/updates CartItem
        → CartItemRepository.save()
        → redirect back to product/cart page
```

## Request Flow — Checkout & Order Placement

```
CheckoutController.placeOrder()
  → CheckoutService.checkout()          [@Transactional]
       1. Load cart, fail if empty
       2. Re-validate stock for every line item
       3. Build ShippingAddress + Order + OrderItems (status = PENDING)
       4. Save Order
       5. PaymentService.processPayment(order, method)
            → PaymentGateway.charge(...)
            → guards against double-charging an already-SUCCESS payment
       6. If payment failed → throw PaymentException (transaction rolls back,
          order + payment record are not committed)
       7. If payment succeeded:
            - Order.status = CONFIRMED
            - reduce stock for each item
            - clear the cart
       8. Save & return Order
  → redirect to /checkout/success/{orderId}
```

Because steps 3–7 run inside one `@Transactional` method, a failed payment (or any exception) rolls back the whole order — stock is never deducted and the cart is never cleared unless payment actually succeeded.

## Order Status Lifecycle

```
PENDING → CONFIRMED → PROCESSING → SHIPPED → OUT_FOR_DELIVERY → DELIVERED
                 (any stage) → CANCELLED
```

`OrderStatus` is a plain enum ordered so that the customer-facing tracking timeline can compare `ordinal()` values to decide which steps are "done" (see `orders/order-tracking.html`), while `CANCELLED` is handled as a special terminal case shown separately.

## Security

- Spring Security with a `DaoAuthenticationProvider` backed by `CustomUserDetailsService` + BCrypt.
- `/admin/**` requires `ROLE_ADMIN`; `/cart/**`, `/checkout/**`, `/payment/**`, `/orders/**`, `/profile/**` require any authenticated user; everything else under `/products`, `/categories`, `/auth`, static assets is public.
- Passwords are BCrypt-hashed; the seeded demo admin password (`admin123`) should be changed before any real deployment.

## Payment Abstraction

```
PaymentService → PaymentGateway (interface)
                     ├── MockPaymentGateway (default; always succeeds, COD is deferred, one reserved amount simulates a decline)
                     └── RazorpayPaymentGateway (stub for a real provider integration)
```

`Payment` has a unique `order_id` foreign key at the JPA level, which combined with `PaymentService`'s check for an existing `SUCCESS` payment prevents an order from ever ending up with two successful charges.
