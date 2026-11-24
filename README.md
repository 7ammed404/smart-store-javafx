# 🏪 Smart Store System
### JavaFX Point of Sale (POS) & Inventory Management System

A feature-rich desktop application built with **Java and JavaFX**, designed to simplify retail store operations through an integrated Point of Sale (POS), inventory management, and sales reporting system.

Smart Store provides a centralized dashboard where store operators can manage products, monitor stock levels, process customer orders, apply promotional discounts, generate invoices, and analyze sales performance.

The project focuses on applying **Object-Oriented Programming (OOP)** principles and software design patterns to build a modular, maintainable, and extensible application with persistent file-based storage.

---

## 📌 Overview

Managing a retail store involves more than simply tracking products and recording purchases.

Store operators need an organized system to manage inventory, process transactions, calculate discounts, generate invoices, and keep track of business performance.

**Smart Store brings these operations together in one desktop application**, providing a structured workflow from product management to final invoice generation and financial reporting.

---

## ✨ Key Features

### 1. 📊 Main Dashboard

A centralized dashboard providing an overview of store activity and quick access to the system's main modules.

- **Live Store KPIs:**
  - Active Products.
  - Total Units in Stock.
  - Items Currently in Cart.
  - Current Cart Total.
  - Completed Orders.
  - Cumulative Sales Revenue.
- **Unified Navigation:** Sidebar navigation and quick-access cards for seamless movement between modules.
- **Business Overview:** Monitor essential store metrics from a single screen.

### 2. 📦 Product Catalog & Inventory Management

A complete product management module designed to organize and control store inventory.

- **Multi-Category Support:**
  - Electronics.
  - Clothing.
  - Custom categories through the `Other` option, including Foods, Perfumes, Books, and more.
- **Product Management:** Add new products, restock existing items, and permanently delete products.
- **Automatic Product IDs:** Sequential auto-incrementing identifiers for newly created products.
- **Instant Search:** Search products by ID, name, or category.
- **Stock Monitoring:** Visual inventory indicators:
  - 🟢 In Stock.
  - 🟡 Low Stock.
  - 🔴 Out of Stock.
- **Direct Cart Integration:** Add products to the shopping cart with specified quantities while updating inventory accordingly.

### 3. 🛒 Shopping Cart

An interactive shopping cart that handles product selection and order preparation.

- **Structured Order Display:** Products are displayed using JavaFX `TableView`, including:
  - Product Name.
  - Unit Price.
  - Quantity.
  - Specifications.
  - Line Total.
- **Quantity Management:** Increase or decrease product quantities using interactive controls.
- **Product Removal:** Remove individual products from the cart.
- **Clear Cart:** Empty the entire cart while automatically restoring reserved stock.
- **Quick Add:** Add products directly to the current order.
- **Automatic Calculations:** Continuously calculate the order subtotal using `calculateTotal()`.

### 4. 🧾 Invoice & Checkout

A dedicated checkout module that handles invoice generation and promotional discounts.

- **Flexible Discount Strategies:**
  - `NoDiscount`: Standard pricing without discounts.
  - `PercentageDiscount`: Percentage-based promotional discounts.
- **Quick Discount Presets:** 5%, 10%, 15%, 20%, 25%, and 50%.
- **Live Calculations:** Instantly update the final invoice total when the discount changes.
- **Receipt Preview:** Display a formatted receipt before completing the transaction.
- **Print Receipt:** Print invoices directly.
- **PDF Export:** Save generated receipts as PDF files.

### 5. 📈 Sales History & Financial Reports

A dedicated sales management module for tracking completed transactions and reviewing business performance.

- **Chronological Sales Ledger:** Automatically numbered invoices with sequential identifiers.
- **Transaction Details:**
  - Invoice Number.
  - Date and Time.
  - Number of Items.
  - Subtotal.
  - Discount Saved.
  - Final Total.
- **Date-Based Filtering:**
  - All Sales History.
  - Today's Sales.
  - Last 7 Days (Weekly).
- **Report Export:**
  - Export reports as CSV files compatible with Microsoft Excel.
  - Generate standalone PDF reports.
  - Print sales reports directly.

---

## 🏗️ Architecture & Design Patterns

The application follows a modular Object-Oriented architecture, separating business logic, data models, storage operations, and graphical user interface components.

### Object-Oriented Programming (OOP)

The project demonstrates several core OOP concepts:

- **Abstraction:** An abstract `Product` class defines the common structure of store products.
- **Inheritance:** Specialized product classes such as `Electronics` and `Clothing` extend the base `Product` class.
- **Polymorphism:** Different product types share a common abstraction while supporting specialized behavior.
- **Encapsulation:** Business data and operations are organized into dedicated classes.

### Design Patterns

#### 1. Strategy Pattern

Used to implement flexible discount calculations.

- `DiscountStrategy` — Defines the discount calculation contract.
- `NoDiscount` — Implements standard pricing.
- `PercentageDiscount` — Implements percentage-based discounts.

This approach allows discount strategies to be changed without tightly coupling discount logic to invoice calculations.

#### 2. Repository Pattern

Used to separate data storage operations from the application's business logic.

- `IRepository` — Defines the repository interface.
- `FileRepository` — Implements file-based data persistence.

Products and invoices are stored in:

- `data/products.txt`
- `data/invoices.txt`

#### 3. Modular Architecture

The JavaFX interface is divided into independent view classes, while `StoreManager` coordinates the core store operations.

This separation improves maintainability, organization, and code reusability.

---

## 🗂️ Project Structure

```text
smart-store-javafx/
│
├── src/
│   └── smart/store/
│       ├── Main.java
│       ├── MainDashboardGUI.java
│       ├── ProductCatalogView.java
│       ├── CartGUI.java
│       ├── InvoiceView.java
│       ├── SalesHistoryView.java
│       │
│       ├── StoreManager.java
│       │
│       ├── Product.java
│       ├── Electronics.java
│       ├── Clothing.java
│       ├── Cart.java
│       ├── Invoice.java
│       │
│       ├── DiscountStrategy.java
│       ├── NoDiscount.java
│       ├── PercentageDiscount.java
│       │
│       ├── IRepository.java
│       └── FileRepository.java
│
├── README.md
└── .gitignore
```

> **Note:** The package directory shown above is illustrative. Use the actual package name and folder structure from your repository if they differ.

---

## 🛠️ Technologies & Tools

| Technology | Purpose |
|---|---|
| Java | Core programming language |
| JavaFX | Desktop graphical user interface |
| OOP | Application architecture and code organization |
| Strategy Pattern | Flexible discount calculations |
| Repository Pattern | Data access abstraction |
| File I/O | Persistent product and invoice storage |
| CSV | Financial report export |
| PDF | Invoice and report generation |
| IntelliJ IDEA | Development environment |
| Git & GitHub | Version control and project hosting |

---

## ⚙️ Installation & Setup

### Prerequisites

Before running the application, make sure you have:

- Java JDK (JDK 11 or later recommended).
- A compatible JavaFX SDK.
- IntelliJ IDEA or another Java IDE.

### 1. Clone the Repository

```bash
git clone https://github.com/<your-username>/smart-store-javafx.git
```

### 2. Open the Project

Open the cloned project in IntelliJ IDEA.

Configure the project SDK and add the JavaFX SDK library through:

`File → Project Structure → Libraries`

### 3. Configure JavaFX

For JDK 11 or later, configure the following VM options under:

`Run → Edit Configurations → VM Options`

```text
--module-path "PATH_TO_JAVAFX_LIB" --add-modules javafx.controls,javafx.fxml
```

Replace `PATH_TO_JAVAFX_LIB` with the actual path to your JavaFX SDK `lib` directory.

### 4. Run the Application

Locate and execute:

```text
Main.java
```

The application should launch with the main store dashboard.

**Note:** The application automatically creates the required `data/` directory and storage files (`products.txt` and `invoices.txt`) when they are needed.

---

## 🎯 Project Goals

This project was developed to put software engineering concepts into practice through a realistic desktop application.

Key learning objectives include:

- Applying OOP principles in a practical system.
- Implementing software design patterns.
- Building modular JavaFX desktop interfaces.
- Managing inventory and sales workflows.
- Working with file-based data persistence.
- Generating invoices and financial reports.
- Connecting multiple application modules through centralized business logic.

---

## 👨‍💻 Developer

Developed as a practical Java application focused on Object-Oriented Programming, software architecture, and desktop application development.

**Smart Store System — Bringing Store Operations Together.**
