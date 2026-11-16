package smart.store;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Main Store Dashboard
 * Connects Dashboard Overview, Product Catalog, Shopping Cart, Invoice & Checkout, and Sales History & Reports.
 */
public class MainDashboardGUI {

    private final StoreManager storeManager;
    private final BorderPane mainLayout = new BorderPane();
    private final StackPane contentArea = new StackPane();

    // Child Views
    private final ProductCatalogView catalogView;
    private final CartGUI cartGUI;
    private final InvoiceView invoiceView;
    private final SalesHistoryView salesHistoryView;
    private VBox homeOverviewPane;

    // Navigation Sidebar Buttons
    private final Button navHomeBtn = new Button("Dashboard Overview");
    private final Button navCatalogBtn = new Button("Product Catalog");
    private final Button navCartBtn = new Button("Shopping Cart (0)");
    private final Button navInvoiceBtn = new Button("Invoice & Checkout");
    private final Button navSalesHistoryBtn = new Button("Sales & Reports");

    // Live Home KPI Labels
    private final Label kpiProductsCount = new Label("0");
    private final Label kpiStockCount = new Label("0");
    private final Label kpiCartItems = new Label("0");
    private final Label kpiCartTotal = new Label("0.00 EGP");
    private final Label kpiInvoicesCount = new Label("0");
    private final Label kpiTotalRevenue = new Label("0.00 EGP");

    public MainDashboardGUI(StoreManager storeManager) {
        this.storeManager = storeManager;

        this.catalogView = new ProductCatalogView(
                storeManager,
                this::refreshAllIndicators,
                this::showCartScreen
        );

        this.cartGUI = new CartGUI(
                storeManager,
                this::refreshAllIndicators,
                this::showInvoiceScreen
        );

        this.salesHistoryView = new SalesHistoryView();

        this.invoiceView = new InvoiceView(
                storeManager,
                this::refreshAllIndicators,
                this::showSalesHistoryScreen
        );

        buildDashboardLayout();
    }

    public BorderPane getView() {
        refreshAllIndicators();
        return mainLayout;
    }

    private void buildDashboardLayout() {
        mainLayout.setStyle("-fx-background-color: #F4F6F9; -fx-font-family: 'Segoe UI', Arial, sans-serif;");

        VBox sidebar = buildSidebar();
        mainLayout.setLeft(sidebar);

        homeOverviewPane = buildHomeOverviewPane();
        contentArea.getChildren().add(homeOverviewPane);
        mainLayout.setCenter(contentArea);

        showHomeScreen();
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPrefWidth(250);
        sidebar.setPadding(new Insets(24, 16, 24, 16));
        sidebar.setStyle("-fx-background-color: #0F172A;");

        Label brandTitle = new Label("SMART STORE");
        brandTitle.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 21));
        brandTitle.setTextFill(Color.WHITE);

        Label brandSub = new Label("Management & POS System");
        brandSub.setFont(Font.font("Segoe UI", 12));
        brandSub.setTextFill(Color.web("#94A3B8"));

        VBox brandBox = new VBox(3, brandTitle, brandSub);
        brandBox.setPadding(new Insets(0, 8, 18, 8));

        setupNavButton(navHomeBtn, this::showHomeScreen);
        setupNavButton(navCatalogBtn, this::showCatalogScreen);
        setupNavButton(navCartBtn, this::showCartScreen);
        setupNavButton(navInvoiceBtn, this::showInvoiceScreen);
        setupNavButton(navSalesHistoryBtn, this::showSalesHistoryScreen);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button saveDataBtn = new Button("Sync Inventory Data");
        saveDataBtn.setMaxWidth(Double.MAX_VALUE);
        saveDataBtn.setStyle("-fx-background-color: #1E293B; -fx-text-fill: #38BDF8; -fx-font-weight: bold; "
                + "-fx-font-size: 13px; -fx-background-radius: 8; -fx-padding: 10 14 10 14; -fx-cursor: hand; -fx-border-color: #334155; -fx-border-radius: 8;");
        saveDataBtn.setOnAction(e -> storeManager.saveProducts());

        sidebar.getChildren().addAll(
                brandBox,
                navHomeBtn,
                navCatalogBtn,
                navCartBtn,
                navInvoiceBtn,
                navSalesHistoryBtn,
                spacer,
                new Separator(),
                saveDataBtn
        );
        return sidebar;
    }

    private void setupNavButton(Button btn, Runnable action) {
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setOnAction(e -> action.run());
    }

    private void setActiveNavButton(Button activeBtn) {
        Button[] all = {navHomeBtn, navCatalogBtn, navCartBtn, navInvoiceBtn, navSalesHistoryBtn};
        for (Button btn : all) {
            if (btn == activeBtn) {
                btn.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-weight: bold; "
                        + "-fx-font-size: 14px; -fx-background-radius: 8; -fx-padding: 12 16 12 16; -fx-cursor: hand;");
            } else {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #CBD5E1; -fx-font-weight: bold; "
                        + "-fx-font-size: 14px; -fx-background-radius: 8; -fx-padding: 12 16 12 16; -fx-cursor: hand;");
            }
        }
    }

    private VBox buildHomeOverviewPane() {
        VBox page = new VBox(24);
        page.setPadding(new Insets(28, 32, 28, 32));

        Label welcomeTitle = new Label("Main Store Dashboard");
        welcomeTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        welcomeTitle.setTextFill(Color.web("#0F172A"));

        Label welcomeSub = new Label("Integrated retail management, inventory tracking, customer orders, and sales analytics");
        welcomeSub.setFont(Font.font("Segoe UI", 13.5));
        welcomeSub.setTextFill(Color.web("#64748B"));

        VBox headerBox = new VBox(4, welcomeTitle, welcomeSub);

        HBox kpiRow = new HBox(14,
                createKpiCard("Active Products", kpiProductsCount, "#2563EB"),
                createKpiCard("Units in Stock", kpiStockCount, "#0D9488"),
                createKpiCard("Items in Cart", kpiCartItems, "#D97706"),
                createKpiCard("Current Cart Total", kpiCartTotal, "#7C3AED"),
                createKpiCard("Completed Orders", kpiInvoicesCount, "#0284C7"),
                createKpiCard("Total Sales Revenue", kpiTotalRevenue, "#16A34A")
        );
        kpiRow.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));

        Label modulesHeading = new Label("Store Management Sections");
        modulesHeading.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        modulesHeading.setTextFill(Color.web("#0F172A"));

        HBox moduleCards = new HBox(16,
                createModuleCard(
                        "Product Catalog",
                        "Browse and manage store inventory across Electronics, Clothing, and custom categories, update stock, and add items to customer orders.",
                        "Open Product Catalog",
                        "#2563EB",
                        this::showCatalogScreen
                ),
                createModuleCard(
                        "Shopping Cart",
                        "Review selected items in the current customer order, adjust quantities or remove products, and monitor the live order subtotal.",
                        "Open Shopping Cart",
                        "#0F172A",
                        this::showCartScreen
                ),
                createModuleCard(
                        "Invoice & Checkout",
                        "Complete customer orders, apply promotional percentage discounts with live preview, and print or save official customer receipts.",
                        "Open Invoice & Checkout",
                        "#16A34A",
                        this::showInvoiceScreen
                ),
                createModuleCard(
                        "Sales & Reports",
                        "Inspect complete historical sales records, filter daily or weekly transactions, and export manager reports to Excel or PDF.",
                        "Open Sales Reports",
                        "#7C3AED",
                        this::showSalesHistoryScreen
                )
        );
        moduleCards.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));

        page.getChildren().addAll(headerBox, kpiRow, modulesHeading, moduleCards);
        return page;
    }

    private VBox createKpiCard(String title, Label valueLabel, String accentHex) {
        Label cap = new Label(title);
        cap.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        cap.setTextFill(Color.web("#64748B"));

        valueLabel.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 20));
        valueLabel.setTextFill(Color.web(accentHex));

        VBox card = new VBox(8, cap, valueLabel);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(15,23,42,0.06), 10, 0, 0, 3);");
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    private VBox createModuleCard(String title, String description, String buttonText, String btnHex, Runnable action) {
        Label titleLbl = new Label(title);
        titleLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 17));
        titleLbl.setTextFill(Color.web("#0F172A"));

        Label descLbl = new Label(description);
        descLbl.setWrapText(true);
        descLbl.setFont(Font.font("Segoe UI", 13));
        descLbl.setTextFill(Color.web("#475569"));
        descLbl.setMinHeight(80);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button launchBtn = new Button(buttonText);
        launchBtn.setMaxWidth(Double.MAX_VALUE);
        launchBtn.setStyle("-fx-background-color: " + btnHex + "; -fx-text-fill: white; -fx-font-weight: bold; "
                + "-fx-font-size: 13px; -fx-background-radius: 8; -fx-padding: 10 14 10 14; -fx-cursor: hand;");
        launchBtn.setOnAction(e -> action.run());

        VBox card = new VBox(14, titleLbl, descLbl, spacer, launchBtn);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(15,23,42,0.08), 12, 0, 0, 4);");
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    public void showHomeScreen() {
        setActiveNavButton(navHomeBtn);
        refreshAllIndicators();
        setCenterNode(homeOverviewPane);
    }

    public void showCatalogScreen() {
        setActiveNavButton(navCatalogBtn);
        catalogView.refreshProductData();
        refreshAllIndicators();
        setCenterNode(catalogView.getView());
    }

    public void showCartScreen() {
        setActiveNavButton(navCartBtn);
        cartGUI.refreshCartView();
        refreshAllIndicators();
        setCenterNode(cartGUI.createCartView());
    }

    public void showInvoiceScreen() {
        setActiveNavButton(navInvoiceBtn);
        invoiceView.refreshInvoiceView();
        refreshAllIndicators();
        setCenterNode(invoiceView.getView());
    }

    public void showSalesHistoryScreen() {
        setActiveNavButton(navSalesHistoryBtn);
        salesHistoryView.refreshHistory();
        refreshAllIndicators();
        setCenterNode(salesHistoryView.getView());
    }

    private void setCenterNode(Node node) {
        contentArea.getChildren().setAll(node);
    }

    public void refreshAllIndicators() {
        List<Product> activeProducts = storeManager.getProducts().stream()
                .filter(Product::isActive)
                .collect(Collectors.toList());
        int totalStock = activeProducts.stream().mapToInt(Product::getQuantity).sum();
        int cartCount = storeManager.getCart().getItemCount();
        double cartSubtotal = storeManager.getCart().calculateTotal();

        FileRepository repo = new FileRepository();
        List<String> invoiceLines = repo.loadData("data/invoices.txt");
        int invoicesSaved = invoiceLines.size();
        double cumulativeRevenue = 0.0;

        for (String line : invoiceLines) {
            String[] parts = line.split("\\|");
            if (parts.length >= 4) {
                try {
                    cumulativeRevenue += Double.parseDouble(parts[3]);
                } catch (NumberFormatException ignored) {
                }
            }
        }

        kpiProductsCount.setText(String.valueOf(activeProducts.size()));
        kpiStockCount.setText(String.valueOf(totalStock));
        kpiCartItems.setText(String.valueOf(cartCount));
        kpiCartTotal.setText(String.format("%.2f EGP", cartSubtotal));
        kpiInvoicesCount.setText(String.valueOf(invoicesSaved));
        kpiTotalRevenue.setText(String.format("%.2f EGP", cumulativeRevenue));

        navCartBtn.setText("Shopping Cart (" + cartCount + ")");
    }
}