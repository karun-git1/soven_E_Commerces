# Database Design

## Tables

### users
| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | |
| name | VARCHAR | |
| email | VARCHAR | UNIQUE |
| password | VARCHAR | BCrypt hash |
| role | VARCHAR | ROLE_CUSTOMER / ROLE_ADMIN |
| phone | VARCHAR | nullable |
| address | VARCHAR | nullable |
| created_at | TIMESTAMP | |

### categories
| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | |
| name | VARCHAR | UNIQUE |
| description | VARCHAR | |

### products
| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | |
| name | VARCHAR | |
| description | VARCHAR(2000) | |
| price | DECIMAL(10,2) | |
| image_url | VARCHAR | nullable, served from `/uploads/products/**` |
| stock | INT | |
| available | BOOLEAN | |
| category_id | BIGINT FK → categories.id | |
| created_at / updated_at | TIMESTAMP | |

### carts
| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | |
| user_id | BIGINT FK → users.id | UNIQUE (one cart per user) |

### cart_items
| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | |
| cart_id | BIGINT FK → carts.id | |
| product_id | BIGINT FK → products.id | |
| quantity | INT | |

### orders
| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | |
| user_id | BIGINT FK → users.id | |
| total_amount | DECIMAL(10,2) | |
| status | VARCHAR | OrderStatus enum |
| payment_method | VARCHAR | PaymentMethod enum |
| address_id | BIGINT FK → shipping_addresses.id | |
| created_at / updated_at | TIMESTAMP | |

### order_items
| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | |
| order_id | BIGINT FK → orders.id | |
| product_id | BIGINT FK → products.id | |
| quantity | INT | |
| price | DECIMAL(10,2) | price captured at time of purchase |

### shipping_addresses
| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | |
| full_name, phone, address_line, city, state, postal_code, country | VARCHAR | |

### payments
| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | |
| order_id | BIGINT FK → orders.id | **UNIQUE** — one payment record per order |
| method | VARCHAR | PaymentMethod enum |
| status | VARCHAR | PaymentStatus enum |
| amount | DECIMAL(10,2) | |
| transaction_id | VARCHAR | nullable until success |
| paid_at | TIMESTAMP | nullable |

## Relationships

```
users 1───1 carts 1───N cart_items N───1 products N───1 categories
users 1───N orders 1───N order_items N───1 products
orders 1───1 shipping_addresses
orders 1───1 payments
```

## Key Constraints for Correctness

- `payments.order_id` is UNIQUE at the schema level, so the database itself prevents two payment rows for the same order.
- `PaymentService` additionally checks in application code whether an existing payment for the order is already `SUCCESS` before attempting to charge again, giving defense-in-depth against double-charging.
- Stock is decremented only after payment succeeds and only inside the same transaction that creates the order, so a failed payment never touches inventory.
