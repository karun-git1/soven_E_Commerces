# Payment Flow

## Overview

```
Cart → Checkout (address + payment method) → PaymentService.processPayment()
     → PaymentGateway.charge() → SUCCESS → Order CONFIRMED, stock reduced, cart cleared
                                → FAILED  → PaymentException, whole transaction rolled back
```

## Sequence

1. Customer submits the checkout form (`POST /checkout/place-order`).
2. `CheckoutService.checkout()` starts a single `@Transactional` unit of work:
   - Loads the cart and fails fast if it's empty (`OrderException`).
   - Re-checks stock for every cart line (`InsufficientStockException` if anything changed since the item was added).
   - Builds the `Order`, `OrderItem`s and `ShippingAddress`, and saves the order with status `PENDING`.
3. `PaymentService.processPayment(order, method)`:
   - Looks up any existing `Payment` for this order.
   - If one is already `SUCCESS`, refuses immediately (`PaymentException`) — this is the duplicate-payment guard.
   - Otherwise creates/reuses a `Payment` row, marks it `PROCESSING`, and calls the configured `PaymentGateway`.
4. `MockPaymentGateway.charge(...)`:
   - `COD` always "succeeds" immediately (cash is collected later).
   - Any other method succeeds with a generated transaction id, **except** a reserved test amount (`₹13.00`) which is used to simulate a decline in demos/tests.
5. Back in `CheckoutService`:
   - Payment `SUCCESS` → order status becomes `CONFIRMED`, stock is decremented per line item, and the cart is cleared.
   - Payment `FAILED` → a `PaymentException` is thrown, which rolls back the entire transaction (order and payment are not persisted), and the customer is shown the payment-failed page.

## Duplicate Payment Protection

Two layers:
- **Application layer** — `PaymentService` checks for an existing `SUCCESS` payment before charging again.
- **Database layer** — `payments.order_id` is a UNIQUE column, so even under concurrent requests the database will reject a second payment row for the same order.

## Payment States

```
INITIATED → PROCESSING → SUCCESS
                       → FAILED (customer may retry checkout)
```

## Swapping in a Real Gateway

1. Implement the real call inside `RazorpayPaymentGateway.charge(...)` using the provider's SDK and the `app.payment.razorpay.key` / `app.payment.razorpay.secret` properties.
2. Update `PaymentService` to select between `MockPaymentGateway` and `RazorpayPaymentGateway` based on the `app.payment.provider` property (or use Spring's `@Primary`/`@Qualifier` to pick the active bean).
3. Add webhook handling if the provider confirms payment asynchronously rather than synchronously.
