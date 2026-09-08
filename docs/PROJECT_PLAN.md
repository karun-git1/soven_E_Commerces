# Project Plan

## Goal
Build a simple, scalable e-commerce platform: businesses manage products and orders; customers browse, buy, and track orders.

## Development Phases (as implemented)

| Phase | Work | Status |
|---|---|---|
| 1 | Spring Boot + Maven + database setup (H2 dev / MySQL prod) | ✅ |
| 2 | User + Role + Registration | ✅ |
| 3 | Spring Security + Login | ✅ |
| 4 | Category + Product management | ✅ |
| 5 | Product catalog + Search | ✅ |
| 6 | Shopping Cart | ✅ |
| 7 | Checkout + Shipping Address | ✅ |
| 8 | Payment Gateway abstraction + Payment Status | ✅ (mock gateway; real provider stubbed) |
| 9 | Order creation + Stock deduction | ✅ |
| 10 | Customer Order History | ✅ |
| 11 | Admin Order Management | ✅ |
| 12 | Order Tracking | ✅ |
| 13 | Admin Dashboard | ✅ |
| 14 | Validation + Exception Handling | ✅ |
| 15 | UI/UX + Responsive Design | ✅ (Bootstrap 5) |
| 16 | Testing | ✅ (unit tests for core services) |
| 17 | Docker + Production Configuration | ✅ |
| 18 | Final Documentation | ✅ (this `docs/` folder) |

## Out of Scope for v1 (suggested follow-ups)
- Real payment provider integration (Razorpay/Stripe SDK call)
- Email/SMS notifications on order status change
- Product reviews & ratings
- Coupon codes / discounts
- Multi-image product galleries
- Admin analytics charts
