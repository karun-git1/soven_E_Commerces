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

## Razorpay Checkout

Razorpay mode is enabled with environment variables and the existing application properties:

```powershell
$env:RAZORPAY_KEY_ID="rzp_test_your_key"
$env:RAZORPAY_KEY_SECRET="your_test_secret"
```

Set `app.payment.provider=razorpay` in `application.properties` (or an active profile). Never commit the secret.

The Razorpay flow is asynchronous because the browser opens Razorpay Checkout:

1. `POST /payment/razorpay/order` validates the cart and stock, saves a pending local order, and creates a Razorpay order in paise.
2. `static/js/payment.js` opens Razorpay Checkout. Razorpay handles UPI, Google Pay, PhonePe, Paytm, cards, net banking, and wallets according to the merchant account configuration.
3. `POST /payment/razorpay/verify` checks that the gateway order belongs to the local payment and verifies the HMAC-SHA256 signature on the server.
4. Only after verification does the application mark the payment and order successful, reduce stock, and clear the cart.

The application never receives raw card number, expiry, or CVV values. Mock mode remains the default for local tests. Production should additionally add Razorpay webhooks for recovery when a customer loses connectivity after payment.
