# Smart Store System — JavaFX POS & Inventory Management

A desktop **Point of Sale (POS), Inventory, and Sales Management System** built with **Java** and **JavaFX**. The application transitions a modular Object-Oriented backend into a multi-screen desktop dashboard supporting product catalog management, shopping cart operations, promotional discount invoicing, file-based persistence, and financial report exports (**Excel `.csv`** and **PDF**).

---

## Key Features

### 1. Main Store Dashboard (`MainDashboardGUI`)
- **Live Store KPIs**: Real-time tracking of active products, total units in stock, items in cart, current cart total, completed orders, and **cumulative sales revenue** across sessions.
- **Unified Navigation**: Sidebar and quick-access module cards connecting all screens seamlessly.

### 2. Product Catalog (`ProductCatalogView`)
- **Multi-Category Inventory**: Supports `Electronics`, `Clothing`, and dynamic custom categories via **`Other`** (e.g., *Foods*, *Perfumes*, *Books*).
- **Add, Restock & Delete**: Add new products with auto-incrementing sequential IDs, restock existing items, or permanently remove products from storage.
- **Search & Stock Alerts**: Instant search by ID, name, or category, plus visual stock badges (*In Stock*, *Low*, *Out of stock*).
- **Direct Add to Cart**: Specify exact quantities and add items directly to the customer cart while updating stock in real time.

### 3. Shopping Cart (`CartGUI`)
- **Order Review**: Displays selected items in a structured `TableView` with unit price, quantity, specifications, and line totals.
- **Interactive Controls**: Adjust item quantities inline (`-` / `+`), remove specific items (`removeProduct`), clear the cart (automatically restoring stock), or quick-add products.
- **Cumulative Calculation**: Computes live order subtotals via `calculateTotal()`.

### 4. Invoice & Checkout (`InvoiceView`)
- **Discount Strategies**: Supports **No Discount** (`NoDiscount`) and **Percentage Discount** (`PercentageDiscount`) with instant live calculation and quick preset buttons (`5%`, `10%`, `15%`, `20%`, `25%`, `50%`).
- **Receipt Preview & Output**: Generates a formatted POS receipt with options to **Print Receipt** directly or **Save Receipt as PDF**.

### 5. Sales History & Reports (`SalesHistoryView`)
- **Chronological Ledger**: Displays all completed invoices numbered sequentially (`1, 2, 3...`) with date, time, item count, subtotal, discount saved, and final total.
- **Period Filtering**: Filter by *All Sales History*, *Today's Sales*, or *Last 7 Days (Weekly)*.
- **One-Click Exports**: Export filtered reports to **Excel (`.csv`)** or standalone **PDF (`.pdf`)**, or print directly.

---

## Architecture & Design Patterns

- **Object-Oriented Inheritance**: Abstract `Product` base class extended by `Electronics` and `Clothing` (with custom category support).
- **Strategy Pattern**: `DiscountStrategy` interface implemented by `NoDiscount` and `PercentageDiscount` for flexible pricing in `Invoice`.
- **Repository Pattern**: `IRepository` interface implemented by `FileRepository` to persist products (`data/products.txt`) and invoices (`data/invoices.txt`).
- **Decoupled JavaFX UI**: Modular view classes (`MainDashboardGUI`, `ProductCatalogView`, `CartGUI`, `InvoiceView`, `SalesHistoryView`) synchronized with `StoreManager`.

---

## Project Structure

```text
smart-store-javafx/
├── src/
│   └── smart/store/               # (or smart_store/ depending on package name)
│       ├── Main.java              # Application entry point
│       ├── MainDashboardGUI.java  # Main dashboard & navigation controller
│       ├── ProductCatalogView.java# Product catalog, search & stock management
│       ├── CartGUI.java           # Shopping cart screen
│       ├── InvoiceView.java       # Checkout, discounts & receipt printing/PDF
│       ├── SalesHistoryView.java  # Sales ledger & Excel/PDF report exports
│       ├── StoreManager.java      # Core business logic & inventory operations
│       ├── Product.java           # Abstract product model
│       ├── Electronics.java       # Electronics product model
│       ├── Clothing.java          # Clothing product model
│       ├── Cart.java              # Shopping cart model
│       ├── Invoice.java           # Invoice calculation model
│       ├── DiscountStrategy.java  # Discount strategy interface
│       ├── NoDiscount.java        # 0% discount implementation
│       ├── PercentageDiscount.java# Percentage discount implementation
│       ├── IRepository.java       # Storage repository interface
│       └── FileRepository.java    # Text-file persistence implementation
├── README.md
└── .gitignore
```

---

## Prerequisites & How to Run

### Requirements
- **Java JDK 8 or higher** (JDK 11 / 17 recommended)
- **JavaFX SDK** (matching your JDK version if using JDK 11+)

### Running in IntelliJ IDEA
1. Clone the repository:
   ```bash
   git clone https://github.com/<your-username>/smart-store-javafx.git
   ```
2. Open the project in **IntelliJ IDEA** and ensure the **JavaFX SDK** library is added under **Project Structure -> Libraries**.
3. If using JDK 11+, add the JavaFX VM options in **Run -> Edit Configurations**:
   ```text
   --module-path "PATH_TO_JAVAFX_LIB" --add-modules javafx.controls,javafx.fxml
   ```
4. Run `Main.java`.
   *(Note: The `data/` folder and `products.txt` / `invoices.txt` files are created automatically on first use.)*
