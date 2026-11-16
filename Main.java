//
//package smart.store;
//
//
//import javafx.application.Application;
//import javafx.scene.Scene;
//import javafx.stage.Stage;
//
//public class Main extends Application {
//
//    @Override
//    public void start(Stage primaryStage) {
//        Cart cart = new Cart();
//        IRepository repository = new FileRepository();
//        StoreManager manager = new StoreManager(cart, repository);
//
//        // Load saved products from data/products.txt or initialize dummy products
//        manager.loadProducts();
//        if (manager.getProducts().isEmpty()) {
//            addDummyProducts(manager);
//            manager.saveProducts();
//        }
//
//        // Launch the Main Dashboard GUI connecting all 4 tasks
//        MainDashboardGUI dashboard = new MainDashboardGUI(manager);
//        Scene scene = new Scene(dashboard.getView(), 1260, 760);
//
//        primaryStage.setTitle("Smart Store System - Main Dashboard");
//        primaryStage.setMinWidth(1050);
//        primaryStage.setMinHeight(650);
//        primaryStage.setScene(scene);
//        primaryStage.show();
//    }
//
//    private static void addDummyProducts(StoreManager manager) {
//        manager.addProduct(new Electronics(101, "Laptop Dell Inspiron", 15000, 5,
//                "Professional laptop", true, 24, "Dell", "Inspiron"));
//        manager.addProduct(new Electronics(102, "Logitech Wireless Mouse", 500, 15,
//                "Wireless mouse", true, 12, "Logitech", "M185"));
//        manager.addProduct(new Clothing(103, "Cotton T-Shirt", 350, 20,
//                "Cotton T-Shirt", true, "L", "Black", "Cotton"));
//        manager.addProduct(new Electronics(104, "Mechanical Keyboard", 950, 8,
//                "RGB Mechanical keyboard", true, 12, "Redragon", "K552"));
//        manager.addProduct(new Clothing(105, "Winter Hoodie", 750, 12,
//                "Heavy cotton hoodie", true, "XL", "Navy", "Cotton"));
//    }
//
//    public static void main(String[] args) {
//        launch(args);
//    }
//}

package smart.store;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        Cart cart = new Cart();
        IRepository repository = new FileRepository();
        StoreManager manager = new StoreManager(cart, repository);

        // Load saved products from data/products.txt (starts empty if none exist)
        manager.loadProducts();

        MainDashboardGUI dashboard = new MainDashboardGUI(manager);
        Scene scene = new Scene(dashboard.getView(), 1260, 760);

        primaryStage.setTitle("Smart Store System - Main Dashboard");
        primaryStage.setMinWidth(1050);
        primaryStage.setMinHeight(650);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}