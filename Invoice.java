package smart.store;

public class Invoice {
    private final Cart cart;
    private final DiscountStrategy discountStrategy;

    public Invoice(Cart cart, DiscountStrategy discountStrategy) {
        if (cart == null || discountStrategy == null) {
            throw new IllegalArgumentException("Cart and discount strategy are required.");
        }
        this.cart = cart;
        this.discountStrategy = discountStrategy;
    }

    public double getSubtotal() {
        return cart.calculateTotal();
    }

    public double getTotal() {
        return discountStrategy.applyDiscount(getSubtotal());
    }

    public double getDiscountAmount() {
        return getSubtotal() - getTotal();
    }

    public Cart getCart() {
        return cart;
    }
}