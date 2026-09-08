# Order Tracking

## Status Lifecycle

```
PENDING → CONFIRMED → PROCESSING → SHIPPED → OUT_FOR_DELIVERY → DELIVERED
                (any stage, admin action) → CANCELLED
```

- `PENDING`: order row created but payment not yet confirmed (transient — normally never visible since the transaction commits `CONFIRMED` directly on payment success).
- `CONFIRMED`: payment succeeded; order is queued for fulfillment.
- `PROCESSING`: admin has started preparing the order.
- `SHIPPED`: order has left the warehouse.
- `OUT_FOR_DELIVERY`: order is with the courier for final delivery.
- `DELIVERED`: order completed.
- `CANCELLED`: terminal state, shown distinctly (not part of the progress timeline).

## Customer View (`/orders/{id}/track`)

Renders a vertical timeline (`orders/order-tracking.html`) that marks each stage "done" by comparing `order.status.ordinal()` against each milestone's ordinal in the `OrderStatus` enum — so any status at or beyond a milestone lights it up. `CANCELLED` is special-cased and shown as a standalone banner instead of the timeline, since its ordinal doesn't represent linear progress.

## Admin View (`/admin/orders/{id}`)

Admins update status via a dropdown bound to `OrderStatusRequest` → `OrderService.updateStatus()`, which parses the submitted value against the `OrderStatus` enum and persists it. There's no workflow engine enforcing valid transitions in v1 — an admin can set any status at any time; add a transition-validation rule in `OrderService.updateStatus()` if you need to restrict that (e.g., disallow moving backwards, or out of `CANCELLED`/`DELIVERED`).
