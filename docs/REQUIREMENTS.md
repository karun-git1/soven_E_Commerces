# Requirements

## 1. Product Management (Admin)
- Upload new products with name, description, price, category, stock, image
- Update product info, price, stock, availability
- Delete products
- View/manage all products

## 2. Product Catalog (Customer)
- Browse available products
- View product details, images, price, description
- Browse by category
- Search products by name

## 3. Shopping Cart (Customer)
- Add / remove products
- Increase / decrease quantity
- View cart total
- Review before checkout

## 4. User Accounts
- Registration & login (email + password, BCrypt-hashed)
- Profile management (name, phone, address)
- Cart tied to account

## 5. Checkout
- Review order
- Enter shipping information
- Calculate total
- Select payment method (UPI, Card, Net Banking, COD)
- Place order

## 6. Order Management
- Admin: view orders, customer & order details, update status
- Admin: track Pending → Confirmed → Processing → Shipped → Out for Delivery → Delivered (or Cancelled)
- Customer: view previous orders, current status, order details

## 7. Admin Dashboard
- Overview: products, categories, customers, orders, sales, availability

## Non-functional
- Role-based access control (Customer / Admin)
- Passwords never stored in plain text
- Stock re-validated at checkout time to avoid overselling
- One successful payment per order (no duplicate charges)
