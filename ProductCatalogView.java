package smart.store;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Product Catalog GUI
 * Supports Electronics, Clothing, and "Other" (any custom category like Foods, Perfumes, Books, etc.)
 * with full file persistence inside data/products.txt without modifying Backend classes.
 */
public class ProductCatalogView {

    private static final int LOW_STOCK_THRESHOLD = 10;
    private static final int FIRST_PRODUCT_ID = 101;
    public static final String CUSTOM_CAT_PREFIX = "CUSTOM_CAT:";

    private final StoreManager storeManager;
    private final Runnable onDataChanged;
    private final Runnable onNavigateToCart;

    private final ObservableList<Product> masterData = FXCollections.observableArrayList();
    private final FilteredList<Product> filteredData = new FilteredList<>(masterData, p -> true);
    private final SortedList<Product> sortedData = new SortedList<>(filteredData);

    private final TableView<Product> table = new TableView<>();
    private final TextField searchInput = new TextField();
    private final ComboBox<String> typeCombo = new ComboBox<>(
            FXCollections.observableArrayList("Electronics", "Clothing", "Other")
    );
    private final TextField customCategoryInput = new TextField();
    private VBox customCategoryBox;

    private final TextField nameInput = new TextField();
    private final TextField specsInput = new TextField();
    private final TextField priceInput = new TextField();
    private final TextField quantityInput = new TextField();
    private final Spinner<Integer> buyQtySpinner = new Spinner<>(1, 999, 1);

    private final Button addToCartBtn = new Button("Add to Cart");
    private final Button goToCartBtn = new Button("Go to Cart (0)");
    private final Button deleteBtn = new Button("Delete");
    private final Button refreshBtn = new Button("Refresh");

    private final Label totalProductsValue = new Label();
    private final Label unitsValue = new Label();
    private final Label inventoryValue = new Label();
    private final Label lowStockValue = new Label();
    private final Label feedbackLabel = new Label();
    private final PauseTransition feedbackTimer = new PauseTransition(Duration.seconds(3.5));

    private VBox root;

    public ProductCatalogView(StoreManager storeManager) {
        this(storeManager, null, null);
    }

    public ProductCatalogView(StoreManager storeManager, Runnable onDataChanged, Runnable onNavigateToCart) {
        this.storeManager = storeManager;
        this.onDataChanged = onDataChanged;
        this.onNavigateToCart = onNavigateToCart;
    }

    public VBox getView() {
        if (root == null) {
            root = build();
        }
        refreshProductData();
        return root;
    }

    public void refreshProductData() {
        if (storeManager != null && storeManager.getProducts() != null) {
            List<Product> activeProducts = storeManager.getProducts().stream()
                    .filter(Product::isActive)
                    .collect(Collectors.toList());
            masterData.setAll(activeProducts);
        } else {
            masterData.clear();
        }
        table.refresh();
        updateStats();
        updateCartBadge();
    }

    private VBox build() {
        masterData.addListener((ListChangeListener<Product>) c -> updateStats());
        sortedData.comparatorProperty().bind(table.comparatorProperty());

        configureTable();
        configureSearch();
        configureFeedback();

        VBox page = new VBox(20, buildHeaderRow(), buildStatsRow(), buildContent());
        page.setPadding(new Insets(24, 28, 24, 28));
        page.setStyle("-fx-background-color: #F4F6F9; -fx-font-family: 'Segoe UI', Arial, sans-serif;");
        VBox.setVgrow(page, Priority.ALWAYS);
        return page;
    }

    private HBox buildHeaderRow() {
        Label title = new Label("Product Catalog");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #0F172A;");

        Label subtitle = new Label("Browse available store products, categories, prices, and stock quantities");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748B;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        goToCartBtn.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-weight: bold; "
                + "-fx-font-size: 13.5px; -fx-background-radius: 8; -fx-padding: 9 18 9 18; -fx-cursor: hand;");
        goToCartBtn.setOnAction(e -> {
            if (onNavigateToCart != null) {
                onNavigateToCart.run();
            }
        });

        HBox row = new HBox(16, new VBox(3, title, subtitle), spacer, feedbackLabel, goToCartBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private HBox buildStatsRow() {
        lowStockValue.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #EA580C;");

        HBox row = new HBox(16,
                statCard("Total Products", totalProductsValue),
                statCard("Units in Stock", unitsValue),
                statCard("Inventory Value", inventoryValue),
                statCard("Low Stock Items", lowStockValue));
        row.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));
        return row;
    }

    private VBox statCard(String caption, Label value) {
        Label cap = new Label(caption);
        cap.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #64748B; -fx-font-weight: bold;");

        if (value != lowStockValue) {
            value.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #0F172A;");
        }

        VBox card = new VBox(6, cap, value);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 16; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(15,23,42,0.06), 10, 0, 0, 3);");
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    private HBox buildContent() {
        VBox tableCard = buildTableCard();
        HBox.setHgrow(tableCard, Priority.ALWAYS);
        HBox content = new HBox(20, tableCard, buildFormCard());
        VBox.setVgrow(content, Priority.ALWAYS);
        return content;
    }

    private VBox buildTableCard() {
        searchInput.setPromptText("Search by product name, category, or ID...");
        searchInput.setPrefWidth(270);
        searchInput.setStyle("-fx-background-radius: 7; -fx-border-color: #CBD5E1; -fx-border-radius: 7; -fx-padding: 7 12 7 12;");

        Label qtyLabel = new Label("Qty:");
        qtyLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");
        buyQtySpinner.setPrefWidth(78);
        buyQtySpinner.setEditable(true);

        buyQtySpinner.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                parseAndCommitSpinnerQty(buyQtySpinner);
            }
        });

        addToCartBtn.setStyle("-fx-background-color: #0F172A; -fx-text-fill: white; -fx-font-weight: bold; "
                + "-fx-background-radius: 7; -fx-padding: 8 15 8 15; -fx-cursor: hand;");
        deleteBtn.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; -fx-font-weight: bold; "
                + "-fx-background-radius: 7; -fx-padding: 8 14 8 14; -fx-cursor: hand;");
        refreshBtn.setStyle("-fx-background-color: #E2E8F0; -fx-text-fill: #1E293B; -fx-font-weight: bold; "
                + "-fx-background-radius: 7; -fx-padding: 8 14 8 14; -fx-cursor: hand;");

        addToCartBtn.setOnAction(e -> handleAddToCart());
        deleteBtn.setOnAction(e -> handleDeleteProduct());
        refreshBtn.setOnAction(e -> refreshProductData());

        addToCartBtn.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        deleteBtn.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox toolbar = new HBox(10, searchInput, spacer, refreshBtn, deleteBtn, qtyLabel, buyQtySpinner, addToCartBtn);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        VBox.setVgrow(table, Priority.ALWAYS);
        VBox card = new VBox(16, toolbar, table);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 18; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(15,23,42,0.06), 10, 0, 0, 3);");
        return card;
    }

    private VBox buildFormCard() {
        Label heading = new Label("Add / Restock Product");
        heading.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #0F172A;");

        typeCombo.setValue("Electronics");
        typeCombo.setMaxWidth(Double.MAX_VALUE);
        typeCombo.setStyle("-fx-background-radius: 6; -fx-border-color: #CBD5E1; -fx-border-radius: 6; -fx-padding: 3;");

        // Dynamic Custom Category Box (shown only when "Other" is selected)
        customCategoryBox = field("Custom Category Name", customCategoryInput, "e.g. Foods, Perfumes, Books...");
        customCategoryBox.setVisible(false);
        customCategoryBox.setManaged(false);

        typeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isOther = "Other".equalsIgnoreCase(newVal);
            customCategoryBox.setVisible(isOther);
            customCategoryBox.setManaged(isOther);
            if (isOther) {
                customCategoryInput.requestFocus();
            }
        });

        Button addBtn = new Button("Save Product");
        addBtn.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-weight: bold; "
                + "-fx-background-radius: 7; -fx-padding: 10 16 10 16; -fx-cursor: hand;");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setOnAction(e -> handleAddProduct());

        nameInput.setOnAction(e -> handleAddProduct());
        specsInput.setOnAction(e -> handleAddProduct());
        priceInput.setOnAction(e -> handleAddProduct());
        quantityInput.setOnAction(e -> handleAddProduct());
        customCategoryInput.setOnAction(e -> handleAddProduct());

        Label typeLabel = new Label("Category");
        typeLabel.setStyle("-fx-font-size: 12.5px; -fx-font-weight: bold; -fx-text-fill: #334155;");
        VBox typeBox = new VBox(6, typeLabel, typeCombo);

        VBox card = new VBox(12,
                heading,
                typeBox,
                customCategoryBox,
                field("Product Name", nameInput, "e.g. Organic Honey"),
                field("Specifications (Optional)", specsInput, "e.g. 500g Pure / Brand / Size"),
                field("Price (EGP)", priceInput, "0.00"),
                field("Quantity", quantityInput, "0"),
                addBtn);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 20; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(15,23,42,0.06), 10, 0, 0, 3);");
        card.setPrefWidth(300);
        card.setMinWidth(300);
        card.setMaxWidth(300);
        card.setAlignment(Pos.TOP_LEFT);
        return card;
    }

    private VBox field(String labelText, TextField input, String prompt) {
        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 12.5px; -fx-font-weight: bold; -fx-text-fill: #334155;");
        input.setPromptText(prompt);
        input.setStyle("-fx-background-radius: 6; -fx-border-color: #CBD5E1; -fx-border-radius: 6; -fx-padding: 7 10 7 10;");
        return new VBox(5, label, input);
    }

    @SuppressWarnings("unchecked")
    private void configureTable() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No products available in catalog."));
        table.setItems(sortedData);
        table.setStyle("-fx-font-size: 13px;");

        TableColumn<Product, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getId()));
        idCol.setMaxWidth(70);
        idCol.setMinWidth(55);

        TableColumn<Product, String> nameCol = new TableColumn<>("Product Name");
        nameCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getName()));
        nameCol.setMinWidth(170);
        nameCol.setStyle("-fx-font-weight: bold;");

        TableColumn<Product, String> typeCol = new TableColumn<>("Category");
        typeCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(extractDisplayCategory(c.getValue())));
        typeCol.setMinWidth(105);
        typeCol.setMaxWidth(130);

        TableColumn<Product, String> detailsCol = new TableColumn<>("Specifications");
        detailsCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(extractDisplaySpecs(c.getValue())));
        detailsCol.setMinWidth(180);

        TableColumn<Product, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getPrice()));
        priceCol.setMinWidth(115);
        priceCol.setCellFactory(col -> new TableCell<Product, Double>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : String.format("%.2f EGP", value));
            }
        });

        TableColumn<Product, Integer> stockCol = new TableColumn<>("Available Qty");
        stockCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getQuantity()));
        stockCol.setMinWidth(120);
        stockCol.setCellFactory(col -> new TableCell<Product, Integer>() {
            private final Label badge = new Label();

            @Override
            protected void updateItem(Integer qty, boolean empty) {
                super.updateItem(qty, empty);
                if (empty || qty == null) {
                    setGraphic(null);
                    return;
                }
                if (qty <= 0) {
                    badge.setText("Out of stock");
                    badge.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; -fx-font-weight: bold; -fx-padding: 3 8 3 8; -fx-background-radius: 10;");
                } else if (qty <= LOW_STOCK_THRESHOLD) {
                    badge.setText(qty + " · Low");
                    badge.setStyle("-fx-background-color: #FEF3C7; -fx-text-fill: #D97706; -fx-font-weight: bold; -fx-padding: 3 8 3 8; -fx-background-radius: 10;");
                } else {
                    badge.setText(qty + " In Stock");
                    badge.setStyle("-fx-background-color: #DCFCE7; -fx-text-fill: #15803D; -fx-font-weight: bold; -fx-padding: 3 8 3 8; -fx-background-radius: 10;");
                }
                setGraphic(badge);
            }
        });

        table.getColumns().addAll(idCol, nameCol, typeCol, detailsCol, priceCol, stockCol);

        idCol.setSortType(TableColumn.SortType.ASCENDING);
        table.getSortOrder().add(idCol);

        table.getSortOrder().addListener((ListChangeListener<TableColumn<Product, ?>>) c ->
                Platform.runLater(() -> {
                    if (table.getSortOrder().isEmpty()) {
                        table.getSortOrder().add(idCol);
                    }
                }));
    }

    /**
     * Returns the custom category name if the product was created under "Other",
     * or returns the standard product type (Electronics / Clothing).
     */
    public static String extractDisplayCategory(Product p) {
        if (p instanceof Clothing) {
            Clothing c = (Clothing) p;
            if (c.getMaterial() != null && c.getMaterial().startsWith(CUSTOM_CAT_PREFIX)) {
                return c.getMaterial().substring(CUSTOM_CAT_PREFIX.length());
            }
        }
        return p.getProductType();
    }

    /**
     * Returns clean specifications for Electronics, Clothing, or Custom Category products.
     */
    public static String extractDisplaySpecs(Product p) {
        if (p instanceof Clothing) {
            Clothing c = (Clothing) p;
            if (c.getMaterial() != null && c.getMaterial().startsWith(CUSTOM_CAT_PREFIX)) {
                return (c.getDescription() != null && !c.getDescription().isEmpty())
                        ? c.getDescription()
                        : "General Item";
            }
            return "Size: " + c.getSize() + " | " + c.getColor() + " | " + c.getMaterial();
        } else if (p instanceof Electronics) {
            Electronics e = (Electronics) p;
            return e.getBrand() + " " + e.getModel() + " (" + e.getWarrantyPeriod() + "m warranty)";
        }
        return p.getDescription();
    }

    private void configureSearch() {
        searchInput.textProperty().addListener((obs, oldText, newText) -> {
            String q = newText == null ? "" : newText.trim().toLowerCase();
            filteredData.setPredicate(p -> q.isEmpty()
                    || p.getName().toLowerCase().contains(q)
                    || extractDisplayCategory(p).toLowerCase().contains(q)
                    || String.valueOf(p.getId()).contains(q));
        });
    }

    private void handleAddProduct() {
        String selectedType = typeCombo.getValue();
        String customCat = customCategoryInput.getText().trim();

        if ("Other".equalsIgnoreCase(selectedType) && customCat.isEmpty()) {
            showFeedback("Please enter a Custom Category Name (e.g. Foods).", true);
            customCategoryInput.requestFocus();
            return;
        }

        String name = nameInput.getText().trim();
        if (name.isEmpty()) {
            showFeedback("Product name is required.", true);
            nameInput.requestFocus();
            return;
        }

        double price;
        int quantity;
        try {
            price = Double.parseDouble(priceInput.getText().trim());
            quantity = Integer.parseInt(quantityInput.getText().trim());
        } catch (NumberFormatException ex) {
            showFeedback("Price and quantity must be valid numbers.", true);
            return;
        }
        if (price < 0 || quantity < 0) {
            showFeedback("Price and quantity cannot be negative.", true);
            return;
        }

        String userSpecs = specsInput.getText().trim();
        Product existing = storeManager.findProductByName(name);
        Product result;

        if (existing != null && existing.isActive()) {
            int oldQuantity = existing.getQuantity();
            existing.setActive(true);
            existing.setPrice(price);
            if (!userSpecs.isEmpty()) {
                existing.setDescription(userSpecs);
            }
            if (quantity > 0) {
                storeManager.restockProduct(existing.getId(), quantity);
            }
            storeManager.saveProducts();
            result = existing;
            refreshProductData();
            notifyDataChanged();
            showFeedback(existing.getName() + " updated: stock " + oldQuantity + " -> " + existing.getQuantity(), false);
        } else {
            int id = nextId();
            if ("Other".equalsIgnoreCase(selectedType)) {
                String details = userSpecs.isEmpty() ? (customCat + " Item") : userSpecs;
                result = new Clothing(id, name, price, quantity, details, true, "-", "-", CUSTOM_CAT_PREFIX + customCat);
            } else if ("Clothing".equalsIgnoreCase(selectedType)) {
                String details = userSpecs.isEmpty() ? name : userSpecs;
                result = new Clothing(id, name, price, quantity, details, true, "L", "Black", "Cotton");
            } else {
                String brand = userSpecs.isEmpty() ? "Generic" : userSpecs;
                result = new Electronics(id, name, price, quantity, name, true, 12, brand, "Standard");
            }

            storeManager.addProduct(result);
            storeManager.saveProducts();
            refreshProductData();
            notifyDataChanged();
            showFeedback("\"" + name + "\" added under category \"" + extractDisplayCategory(result) + "\".", false);
        }

        nameInput.clear();
        specsInput.clear();
        priceInput.clear();
        quantityInput.clear();
        customCategoryInput.clear();
        nameInput.requestFocus();

        table.getSelectionModel().select(result);
        table.scrollTo(result);
    }

    private void handleDeleteProduct() {
        Product selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Remove \"" + selected.getName() + "\" from the catalog?",
                ButtonType.CANCEL, ButtonType.OK);
        confirm.setTitle("Delete Product");
        confirm.setHeaderText(null);

       confirm.showAndWait().filter(b -> b == ButtonType.OK).ifPresent(b -> {
            storeManager.removeProduct(selected.getId());
            storeManager.saveProducts();
            refreshProductData();
            notifyDataChanged();
            showFeedback("\"" + selected.getName() + "\" deleted permanently.", false);
        });
    }

    private void handleAddToCart() {
        Product selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        int qty = parseAndCommitSpinnerQty(buyQtySpinner);
        if (qty <= 0) {
            showFeedback("Quantity must be at least 1.", true);
            return;
        }

        if (selected.getQuantity() < qty) {
            showFeedback("Not enough stock for " + selected.getName() + " (Available: " + selected.getQuantity() + ").", true);
            return;
        }

        if (storeManager.buyProduct(selected.getId(), qty)) {
            storeManager.saveProducts();
            refreshProductData();
            notifyDataChanged();
            table.getSelectionModel().select(selected);

            double addedTotal = selected.getPrice() * qty;
            showFeedback("Added " + qty + "x " + selected.getName() + " (" + String.format("%.2f EGP", addedTotal) + ") to Cart!", false);

            buyQtySpinner.getValueFactory().setValue(1);
            buyQtySpinner.getEditor().setText("1");
        } else {
            showFeedback("Could not add product to cart.", true);
        }
    }

    private int parseAndCommitSpinnerQty(Spinner<Integer> spinner) {
        String rawText = spinner.getEditor().getText();
        if (rawText != null && !rawText.trim().isEmpty()) {
            try {
                int parsed = Integer.parseInt(rawText.trim());
                if (parsed > 0) {
                    spinner.getValueFactory().setValue(parsed);
                    return parsed;
                } else {
                    return -1;
                }
            } catch (NumberFormatException ex) {
                return -1;
            }
        }
        return spinner.getValue() != null ? spinner.getValue() : 1;
    }

   private int nextId() {
        List<Product> all = storeManager.getProducts();
        Set<Integer> used = all.stream().map(Product::getId).collect(Collectors.toSet());
        int id = 1;
        while (used.contains(id)) {
            id++;
        }
        return id;
    }

    private void updateStats() {
        int units = masterData.stream().mapToInt(Product::getQuantity).sum();
        double value = masterData.stream().mapToDouble(p -> p.getPrice() * p.getQuantity()).sum();
        long low = masterData.stream().filter(p -> p.getQuantity() <= LOW_STOCK_THRESHOLD).count();

        totalProductsValue.setText(String.valueOf(masterData.size()));
        unitsValue.setText(String.valueOf(units));
        inventoryValue.setText(String.format("%.2f EGP", value));
        lowStockValue.setText(String.valueOf(low));
    }

    private void updateCartBadge() {
        int count = (storeManager != null && storeManager.getCart() != null)
                ? storeManager.getCart().getItemCount()
                : 0;
        goToCartBtn.setText("Go to Cart (" + count + ")");
    }

    private void notifyDataChanged() {
        if (onDataChanged != null) {
            onDataChanged.run();
        }
    }

    private void configureFeedback() {
        feedbackLabel.setVisible(false);
        feedbackLabel.managedProperty().bind(feedbackLabel.visibleProperty());
        feedbackLabel.setMaxWidth(450);
        feedbackTimer.setOnFinished(e -> feedbackLabel.setVisible(false));
    }

    private void showFeedback(String message, boolean error) {
        feedbackLabel.setText(message);
        feedbackLabel.setStyle(error
                ? "-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; -fx-font-weight: bold; -fx-padding: 6 12 6 12; -fx-background-radius: 6;"
                : "-fx-background-color: #DCFCE7; -fx-text-fill: #15803D; -fx-font-weight: bold; -fx-padding: 6 12 6 12; -fx-background-radius: 6;");
        feedbackLabel.setVisible(true);
        feedbackTimer.playFromStart();
    }
}