package smart.store;

public abstract class Product {
    private int id;
    private String name;
    private double price;
    private int quantity;
    private String description;
    private boolean isActive;

    public Product(int id, String name, double price, int quantity,
                   String description, boolean isActive) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID must be greater than zero.");
        }
        if (price < 0 || quantity < 0) {
            throw new IllegalArgumentException("Price and quantity cannot be negative.");
        }

        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.description = description;
        this.isActive = isActive;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        if (price >= 0) {
            this.price = price;
        }
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity >= 0) {
            this.quantity = quantity;
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isAvailable() {
        return isActive && quantity > 0;
    }

    public void increaseQuantity(int amount) {
        if (amount > 0) {
            quantity += amount;
        }
    }

    public boolean decreaseQuantity(int amount) {
        if (amount <= 0 || amount > quantity) {
            return false;
        }
        quantity -= amount;
        return true;
    }

    public double calculateStockValue() {
        return price * quantity;
    }

    public String getFormattedPrice() {
        return String.format("%.2f EGP", price);
    }

    public abstract String getDetailsList();

    public abstract String getProductType();
}