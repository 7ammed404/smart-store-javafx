package smart.store;

import java.util.ArrayList;
import java.util.List;

public class Cart {
    private final ArrayList<Product> items = new ArrayList<>();

    public void addProduct(Product product) {
        if (product != null) {
            items.add(product);
        }
    }

    public void addProduct(Product product, int quantity) {
        if (product == null || quantity <= 0) {
            return;
        }
        for (int i = 0; i < quantity; i++) {
            items.add(product);
        }
    }

    public void removeProduct(int productId) {
        items.removeIf(product -> product.getId() == productId);
    }

    public double calculateTotal() {
        double total = 0;
        for (Product product : items) {
            total += product.getPrice();
        }
        return total;
    }

    public List<Product> getItems() {
        return new ArrayList<>(items);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public int getItemCount() {
        return items.size();
    }

    public void clear() {
        items.clear();
    }
}