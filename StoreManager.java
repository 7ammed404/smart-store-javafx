package smart.store;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StoreManager {
    private static final String PRODUCTS_FILE = "data/products.txt";
    private static final String INVOICES_FILE = "data/invoices.txt";
    private static final String SEPARATOR = "|";

    private final ArrayList<Product> products;
    private final Cart cart;
    private final IRepository repository;

    public StoreManager(Cart cart, IRepository repository) {
        this.products = new ArrayList<>();
        this.cart = cart == null ? new Cart() : cart;
        this.repository = repository == null ? new FileRepository() : repository;
    }

    public void addProduct(Product product) {
        if (product == null) {
            return;
        }
        if (findProductById(product.getId()) != null) {
            System.out.println("Product ID already exists.");
            return;
        }
        products.add(product);
        System.out.println("Product added successfully.");
    }

    public Product findProductById(int id) {
        for (Product product : products) {
            if (product.getId() == id) {
                return product;
            }
        }
        return null;
    }

    public Product findProductByName(String name) {
        if (name == null) {
            return null;
        }
        for (Product product : products) {
            if (product.getName() != null && product.getName().equalsIgnoreCase(name)) {
                return product;
            }
        }
        return null;
    }

    public void displayProducts() {
        if (products.isEmpty()) {
            System.out.println("No products available.");
            return;
        }
        System.out.println("\n===== Store Products =====");
        for (Product product : products) {
            System.out.println(product.getDetailsList());
        }
    }

    public boolean buyProduct(int id, int quantity) {
        Product product = findProductById(id);

        if (product == null) {
            System.out.println("Product not found.");
            return false;
        }
        if (quantity <= 0) {
            System.out.println("Quantity must be greater than zero.");
            return false;
        }
        if (!product.isAvailable() || product.getQuantity() < quantity) {
            System.out.println("Product is not available in this quantity.");
            return false;
        }

        product.decreaseQuantity(quantity);
        cart.addProduct(product, quantity);
        System.out.println("Product added to cart.");
        return true;
    }

    public boolean restockProduct(int id, int quantity) {
        Product product = findProductById(id);
        if (product == null || quantity <= 0) {
            System.out.println("Product not found or quantity is invalid.");
            return false;
        }
        product.increaseQuantity(quantity);
        System.out.println("Stock updated successfully.");
        return true;
    }
    
    public boolean removeProduct(int id) {
        return products.removeIf(product -> product.getId() == id);
    }

    public Cart getCart() {
        return cart;
    }

    public List<Product> getProducts() {
        return new ArrayList<>(products);
    }

    public void loadProducts() {
        products.clear();
        List<String> rows = repository.loadData(PRODUCTS_FILE);

        for (String row : rows) {
            try {
                Product product = createProduct(row);
                if (product != null && product.isActive() && findProductById(product.getId()) == null) {
                    products.add(product);
                }
            } catch (RuntimeException e) {
                System.out.println("A saved product could not be loaded: " + row);
            }
        }
    }

    public boolean saveProducts() {
        List<String> rows = new ArrayList<>();
        for (Product product : products) {
            rows.add(productToLine(product));
        }
        return repository.saveData(PRODUCTS_FILE, rows);
    }

    public boolean saveInvoice(Invoice invoice) {
        if (invoice == null) {
            return false;
        }

        List<String> invoices = repository.loadData(INVOICES_FILE);
        String line = LocalDateTime.now() + SEPARATOR
                + invoice.getSubtotal() + SEPARATOR
                + invoice.getDiscountAmount() + SEPARATOR
                + invoice.getTotal() + SEPARATOR
                + invoice.getCart().getItemCount();
        invoices.add(line);
        return repository.saveData(INVOICES_FILE, invoices);
    }

    public void clearCart() {
        cart.clear();
    }

    private String clean(String value) {
        if (value == null) {
            return "";
        }
        return value.replace(SEPARATOR, "/").replace("\n", " ").replace("\r", " ");
    }

    private String productToLine(Product product) {
        String line = product.getProductType() + SEPARATOR
                + product.getId() + SEPARATOR
                + clean(product.getName()) + SEPARATOR
                + product.getPrice() + SEPARATOR
                + product.getQuantity() + SEPARATOR
                + clean(product.getDescription()) + SEPARATOR
                + product.isActive();

        if (product instanceof Electronics) {
            Electronics item = (Electronics) product;
            line += SEPARATOR + item.getWarrantyPeriod()
                    + SEPARATOR + clean(item.getBrand())
                    + SEPARATOR + clean(item.getModel());
        } else if (product instanceof Clothing) {
            Clothing item = (Clothing) product;
            line += SEPARATOR + clean(item.getSize())
                    + SEPARATOR + clean(item.getColor())
                    + SEPARATOR + clean(item.getMaterial());
        }
        return line;
    }

    private Product createProduct(String line) {
        String[] data = line.split("\\|", -1);
        if (data.length < 10) {
            return null;
        }

        String type = data[0];
        int id = Integer.parseInt(data[1]);
        String name = data[2];
        double price = Double.parseDouble(data[3]);
        int quantity = Integer.parseInt(data[4]);
        String description = data[5];
        boolean active = Boolean.parseBoolean(data[6]);

        if (type.equals("Electronics")) {
            return new Electronics(id, name, price, quantity, description, active,
                    Integer.parseInt(data[7]), data[8], data[9]);
        }
        if (type.equals("Clothing")) {
            return new Clothing(id, name, price, quantity, description, active,
                    data[7], data[8], data[9]);
        }
        return null;
    }
}