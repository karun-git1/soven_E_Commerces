# Endpoint Reference

This app is server-rendered (Thymeleaf), so "endpoints" below are MVC routes rather than a JSON API. All POST routes are form submissions.

## Public

| Method | Path | Description |
|---|---|---|
| GET | `/`, `/home` | Landing page with featured products & categories |
| GET | `/products` | Browse/search/filter products (`categoryId`, `search`, `page`) |
| GET | `/products/{id}` | Product detail page |
| GET | `/categories` | List categories |
| GET | `/auth/login` | Login page |
| POST | `/auth/login` | Submits credentials (handled by Spring Security) |
| GET | `/auth/register` | Registration form |
| POST | `/auth/register` | Create a new customer account |
| POST | `/auth/logout` | Log out |

## Authenticated (Customer or Admin)

| Method | Path | Description |
|---|---|---|
| GET | `/cart` | View cart |
| POST | `/cart/add/{productId}` | Add product to cart (`quantity`) |
| POST | `/cart/update/{itemId}` | Update quantity of a cart line |
| POST | `/cart/remove/{itemId}` | Remove a cart line |
| GET | `/checkout` | Checkout page (address + payment method) |
| POST | `/checkout/place-order` | Places the order and charges payment |
| GET | `/checkout/success/{orderId}` | Order confirmation page |
| GET | `/payment/success/{orderId}` | Payment success page |
| GET | `/payment/failed/{orderId}` | Payment failure page |
| GET | `/orders` | Current user's order history |
| GET | `/orders/{id}` | Order detail (owner only) |
| GET | `/orders/{id}/track` | Order tracking timeline |
| GET | `/profile` | View profile |
| POST | `/profile` | Update name/phone/address |

## Admin only (`ROLE_ADMIN`)

| Method | Path | Description |
|---|---|---|
| GET | `/admin`, `/admin/dashboard` | Dashboard KPIs + recent orders |
| GET | `/admin/products` | List all products |
| GET | `/admin/products/add` | Add-product form |
| POST | `/admin/products/add` | Create product (multipart form, optional image) |
| GET | `/admin/products/edit/{id}` | Edit-product form |
| POST | `/admin/products/edit/{id}` | Update product |
| POST | `/admin/products/delete/{id}` | Delete product |
| POST | `/admin/products/toggle-availability/{id}` | Toggle availability |
| GET | `/admin/categories` | List categories |
| GET | `/admin/categories/add` | Add-category form |
| POST | `/admin/categories/add` | Create category |
| POST | `/admin/categories/delete/{id}` | Delete category |
| GET | `/admin/orders` | List all orders |
| GET | `/admin/orders/{id}` | Order detail + status updater |
| POST | `/admin/orders/{id}/status` | Update order status |

## Error Handling

`GlobalExceptionHandler` maps domain exceptions to a shared `error.html` page:

| Exception | Meaning |
|---|---|
| `ResourceNotFoundException` | 404 — entity not found or not owned by the current user |
| `InsufficientStockException` | 400 — requested quantity exceeds available stock |
| `PaymentException` | 400 — payment failed or order already paid |
| `OrderException` | 400 — invalid checkout state (e.g., empty cart, bad payment method) |
