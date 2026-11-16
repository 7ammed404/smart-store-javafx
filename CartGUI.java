package smart.store;

import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Shopping Cart GUI
 * Displays selected products in the Cart, supports live quantity +/- adjustments,
 * removing items, quick-adding with exact typed quantity, and calculating cumulative total.
 */
public class CartGUI extends Application {

    private StoreManager storeManager;
    private Cart cart;
    private Runnable onDataChanged;
    private Runnable onProceedToInvoice;

    private final ObservableList<CartRow> cartRows = FXCollections.observableArrayList();
    private TableView<CartRow> cartTable;

    private Label itemCountBadge;
    private Label uniqueTypesValueLabel;
    private Label totalPiecesValueLabel;
    private Label subtotalValueLabel;
    private Label statusFeedbackLabel;

    private ComboBox<Product> quickAddProductCombo;
    private Spinner<Integer> quickAddQtySpinner;

    private BorderPane root;

    public CartGUI() {
    }

    public CartGUI(StoreManager storeManager) {
        this(storeManager, null, null);
    }

    public CartGUI(StoreManager storeManager, Runnable onDataChanged, Runnable onProceedToInvoice) {
        if (storeManager != null) {
            this.storeManager = storeManager;
            this.cart = storeManager.getCart();
        } else {
            this.cart = new Cart();
            this.storeManager = new StoreManager(this.cart, new FileRepository());
        }
        this.onDataChanged = onDataChanged;
        this.onProceedToInvoice = onProceedToInvoice;
    }

    @Override
    public void start(Stage primaryStage) {
        if (this.cart == null || this.storeManager == null) {
            this.cart = new Cart();
            this.storeManager = new StoreManager(this.cart, new FileRepository());
            this.storeManager.loadProducts();
        }

        BorderPane view = createCartView();
        Scene scene = new Scene(view, 1080, 680);
        primaryStage.setTitle("Smart Store System - Shopping Cart");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public BorderPane createCartView() {
        if (root == null) {
            root = new BorderPane();
            root.setStyle("-fx-background-color: #F4F6F9; -fx-font-family: 'Segoe UI', Arial, sans-serif;");

            HBox headerBar = buildHeaderBar();
            root.setTop(headerBar);

            VBox centerSection = buildCenterTableSection();
            BorderPane.setMargin(centerSection, new Insets(20, 12, 20, 24));
            root.setCenter(centerSection);

            VBox summarySection = buildTotalSummaryPanel();
            BorderPane.setMargin(summarySection, new Insets(20, 24, 20, 12));
            root.setRight(summarySection);
        }
        refreshCartView();
        return root;
    }

    private HBox buildHeaderBar() {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(18, 28, 18, 28));
        header.setStyle("-fx-background-color: linear-gradient(to right, #1E293B, #0F172A);");

        VBox titleBox = new VBox(3);
        Label titleLabel = new Label("Shopping Cart");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.WHITE);

        Label subtitleLabel = new Label("Review selected items, adjust quantities, or remove products from the current order");
        subtitleLabel.setFont(Font.font("Segoe UI", 13));
        subtitleLabel.setTextFill(Color.web("#94A3B8"));
        titleBox.getChildren().addAll(titleLabel, subtitleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        itemCountBadge = new Label("0 Items in Cart");
        itemCountBadge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        itemCountBadge.setTextFill(Color.WHITE);
        itemCountBadge.setStyle("-fx-background-color: #2563EB; -fx-background-radius: 20; -fx-padding: 7 18 7 18;");

        header.getChildren().addAll(titleBox, spacer, itemCountBadge);
        return header;
    }

    @SuppressWarnings("unchecked")
    private VBox buildCenterTableSection() {
        VBox container = new VBox(14);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(15,23,42,0.08), 12, 0, 0, 4);");
        VBox.setVgrow(container, Priority.ALWAYS);

        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label sectionTitle = new Label("Selected Products in Cart");
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        sectionTitle.setTextFill(Color.web("#1E293B"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button removeSelectedBtn = new Button("Remove Selected Product");
        removeSelectedBtn.setStyle("-fx-background-color: #DC2626; -fx-text-fill: white; -fx-font-weight: bold; "
                + "-fx-font-size: 13px; -fx-background-radius: 8; -fx-padding: 8 16 8 16; -fx-cursor: hand;");
        removeSelectedBtn.setOnAction(e -> handleRemoveSelectedProduct());

        Button clearCartBtn = new Button("Clear Cart");
        clearCartBtn.setStyle("-fx-background-color: #E2E8F0; -fx-text-fill: #1E293B; -fx-font-weight: bold; "
                + "-fx-font-size: 13px; -fx-background-radius: 8; -fx-padding: 8 16 8 16; -fx-cursor: hand;");
        clearCartBtn.setOnAction(e -> handleClearCart());

        topRow.getChildren().addAll(sectionTitle, spacer, removeSelectedBtn, clearCartBtn);

        cartTable = new TableView<>();
        cartTable.setItems(cartRows);
        cartTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        cartTable.setStyle("-fx-background-radius: 8; -fx-border-color: #E2E8F0; -fx-border-radius: 8; -fx-font-size: 13px;");
        VBox.setVgrow(cartTable, Priority.ALWAYS);

        Label emptyPlaceholder = new Label("Your shopping cart is empty.\nSelect products from the Product Catalog or use the quick-add bar below.");
        emptyPlaceholder.setStyle("-fx-text-fill: #64748B; -fx-font-size: 14px; -fx-alignment: center; -fx-text-alignment: center;");
        cartTable.setPlaceholder(emptyPlaceholder);

        TableColumn<CartRow, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> data.getValue().idProperty());
        idCol.setMaxWidth(60);
        idCol.setMinWidth(50);
        idCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<CartRow, String> nameCol = new TableColumn<>("Product Name");
        nameCol.setCellValueFactory(data -> data.getValue().nameProperty());
        nameCol.setMinWidth(135);
        nameCol.setStyle("-fx-alignment: CENTER-LEFT; -fx-font-weight: bold;");

        TableColumn<CartRow, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(data -> data.getValue().categoryProperty());
        categoryCol.setMinWidth(90);
        categoryCol.setMaxWidth(110);
        categoryCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<CartRow, String> specsCol = new TableColumn<>("Specifications");
        specsCol.setCellValueFactory(data -> data.getValue().specificationsProperty());
        specsCol.setMinWidth(165);

        TableColumn<CartRow, Number> unitPriceCol = new TableColumn<>("Unit Price");
        unitPriceCol.setCellValueFactory(data -> data.getValue().unitPriceProperty());
        unitPriceCol.setMinWidth(105);
        unitPriceCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        unitPriceCol.setCellFactory(col -> createCurrencyCell());

        // Interactive Qty in Cart Column with [-] Qty [+] buttons
        TableColumn<CartRow, Number> qtyCol = new TableColumn<>("Qty in Cart");
        qtyCol.setCellValueFactory(data -> data.getValue().quantityProperty());
        qtyCol.setMinWidth(125);
        qtyCol.setMaxWidth(135);
        qtyCol.setStyle("-fx-alignment: CENTER;");
        qtyCol.setCellFactory(col -> new TableCell<CartRow, Number>() {
            private final Button minusBtn = new Button("-");
            private final Label qtyLbl = new Label();
            private final Button plusBtn = new Button("+");
            private final HBox box = new HBox(8, minusBtn, qtyLbl, plusBtn);

            {
                box.setAlignment(Pos.CENTER);
                String btnStyle = "-fx-background-color: #E2E8F0; -fx-text-fill: #0F172A; -fx-font-weight: bold; "
                        + "-fx-background-radius: 5; -fx-padding: 2 8 2 8; -fx-cursor: hand;";
                minusBtn.setStyle(btnStyle);
                plusBtn.setStyle(btnStyle);
                qtyLbl.setStyle("-fx-font-weight: bold; -fx-min-width: 24; -fx-alignment: center;");

                minusBtn.setOnAction(e -> {
                    CartRow row = getTableView().getItems().get(getIndex());
                    changeRowQuantity(row, -1);
                });
                plusBtn.setOnAction(e -> {
                    CartRow row = getTableView().getItems().get(getIndex());
                    changeRowQuantity(row, +1);
                });
            }

            @Override
            protected void updateItem(Number value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setGraphic(null);
                } else {
                    qtyLbl.setText(String.valueOf(value.intValue()));
                    setGraphic(box);
                }
            }
        });

        TableColumn<CartRow, Number> lineTotalCol = new TableColumn<>("Line Total");
        lineTotalCol.setCellValueFactory(data -> data.getValue().lineTotalProperty());
        lineTotalCol.setMinWidth(120);
        lineTotalCol.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;");
        lineTotalCol.setCellFactory(col -> createCurrencyCell());

        TableColumn<CartRow, Void> actionCol = new TableColumn<>("Action");
        actionCol.setMinWidth(90);
        actionCol.setMaxWidth(100);
        actionCol.setStyle("-fx-alignment: CENTER;");
        actionCol.setCellFactory(col -> new TableCell<CartRow, Void>() {
            private final Button deleteBtn = new Button("Remove");

            {
                deleteBtn.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; "
                        + "-fx-font-weight: bold; -fx-font-size: 12px; -fx-background-radius: 6; "
                        + "-fx-padding: 4 10 4 10; -fx-cursor: hand;");
                deleteBtn.setOnAction(e -> {
                    CartRow row = getTableView().getItems().get(getIndex());
                    removeProductById(row.getProduct().getId(), row.getName(), row.getQuantity());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });

        cartTable.getColumns().addAll(idCol, nameCol, categoryCol, specsCol, unitPriceCol, qtyCol, lineTotalCol, actionCol);

        HBox quickAddBar = buildQuickAddBar();

        statusFeedbackLabel = new Label("Ready. You can adjust quantities with +/- or remove any product from the cart.");
        statusFeedbackLabel.setFont(Font.font("Segoe UI", 12.5));
        statusFeedbackLabel.setTextFill(Color.web("#475569"));

        container.getChildren().addAll(topRow, cartTable, quickAddBar, statusFeedbackLabel);
        return container;
    }

    private HBox buildQuickAddBar() {
        HBox bar = new HBox(10);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(10, 14, 10, 14));
        bar.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #E2E8F0; "
                + "-fx-border-radius: 8; -fx-background-radius: 8;");

        Label addTitle = new Label("Quick Add Product:");
        addTitle.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        addTitle.setTextFill(Color.web("#334155"));

        quickAddProductCombo = new ComboBox<>();
        quickAddProductCombo.setPrefWidth(280);
        quickAddProductCombo.setConverter(new StringConverter<Product>() {
            @Override
            public String toString(Product product) {
                if (product == null) return "";
                return "#" + product.getId() + " - " + product.getName()
                        + " (" + product.getFormattedPrice() + " | Stock: " + product.getQuantity() + ")";
            }

            @Override
            public Product fromString(String string) {
                return null;
            }
        });

        Label qtyLabel = new Label("Qty:");
        qtyLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));

        quickAddQtySpinner = new Spinner<>(1, 999, 1);
        quickAddQtySpinner.setPrefWidth(78);
        quickAddQtySpinner.setEditable(true);

        // Commit typed quantity automatically whenever focus leaves the spinner
        quickAddQtySpinner.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                parseAndCommitSpinnerQty(quickAddQtySpinner);
            }
        });

        Button addBtn = new Button("+ Add to Cart");
        addBtn.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-weight: bold; "
                + "-fx-font-size: 13px; -fx-background-radius: 8; -fx-padding: 7 15 7 15; -fx-cursor: hand;");
        addBtn.setOnAction(e -> handleQuickAddProduct());

        bar.getChildren().addAll(addTitle, quickAddProductCombo, qtyLabel, quickAddQtySpinner, addBtn);
        return bar;
    }

    private VBox buildTotalSummaryPanel() {
        VBox panel = new VBox(16);
        panel.setPrefWidth(320);
        panel.setMinWidth(300);
        panel.setPadding(new Insets(22));
        panel.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(15,23,42,0.08), 12, 0, 0, 4);");

        Label summaryTitle = new Label("Order Summary");
        summaryTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        summaryTitle.setTextFill(Color.web("#1E293B"));

        Label summarySubtitle = new Label("Live cumulative cart total");
        summarySubtitle.setFont(Font.font("Segoe UI", 12));
        summarySubtitle.setTextFill(Color.web("#64748B"));

        VBox titleGroup = new VBox(2, summaryTitle, summarySubtitle);

        VBox totalHighlightCard = new VBox(8);
        totalHighlightCard.setPadding(new Insets(18));
        totalHighlightCard.setAlignment(Pos.CENTER_LEFT);
        totalHighlightCard.setStyle("-fx-background-color: linear-gradient(to bottom right, #EFF6FF, #DBEAFE); "
                + "-fx-background-radius: 10; -fx-border-color: #BFDBFE; -fx-border-radius: 10;");

        Label cumulativeCaption = new Label("CUMULATIVE CART TOTAL");
        cumulativeCaption.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        cumulativeCaption.setTextFill(Color.web("#1D4ED8"));

        subtotalValueLabel = new Label("0.00 EGP");
        subtotalValueLabel.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 26));
        subtotalValueLabel.setTextFill(Color.web("#1E3A8A"));

        totalHighlightCard.getChildren().addAll(cumulativeCaption, subtotalValueLabel);

        VBox breakdownBox = new VBox(12);
        breakdownBox.setPadding(new Insets(6, 0, 6, 0));

        uniqueTypesValueLabel = new Label("0");
        totalPiecesValueLabel = new Label("0");

        breakdownBox.getChildren().addAll(
                createSummaryRow("Distinct Products:", uniqueTypesValueLabel),
                createSummaryRow("Total Units in Cart:", totalPiecesValueLabel)
        );

        Separator sep = new Separator();

        Button calculateTotalBtn = new Button("Update Order Total");
        calculateTotalBtn.setMaxWidth(Double.MAX_VALUE);
        calculateTotalBtn.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-weight: bold; "
                + "-fx-font-size: 13px; -fx-background-radius: 8; -fx-padding: 10 16 10 16; -fx-cursor: hand;");
        calculateTotalBtn.setOnAction(e -> {
            refreshCartView();
            double total = cart.calculateTotal();
            setStatus("Order Subtotal Updated: " + String.format("%.2f EGP", total), false);
        });

        Button proceedInvoiceBtn = new Button("Proceed to Invoice & Checkout ->");
        proceedInvoiceBtn.setMaxWidth(Double.MAX_VALUE);
        proceedInvoiceBtn.setStyle("-fx-background-color: #16A34A; -fx-text-fill: white; -fx-font-weight: bold; "
                + "-fx-font-size: 13.5px; -fx-background-radius: 8; -fx-padding: 11 18 11 18; -fx-cursor: hand;");
        proceedInvoiceBtn.setOnAction(e -> {
            if (onProceedToInvoice != null) {
                onProceedToInvoice.run();
            }
        });

        Region bottomSpacer = new Region();
        VBox.setVgrow(bottomSpacer, Priority.ALWAYS);

        panel.getChildren().addAll(
                titleGroup,
                totalHighlightCard,
                breakdownBox,
                sep,
                calculateTotalBtn,
                proceedInvoiceBtn
        );

        return panel;
    }

    public final void refreshCartView() {
        cartRows.clear();

        Map<Integer, CartRow> grouped = new LinkedHashMap<>();
        for (Product product : cart.getItems()) {
            int id = product.getId();
            if (grouped.containsKey(id)) {
                grouped.get(id).incrementQuantity();
            } else {
                grouped.put(id, new CartRow(product, 1));
            }
        }
        cartRows.addAll(grouped.values());

        double subtotal = cart.calculateTotal();
        int totalPieces = cart.getItemCount();
        int distinctTypes = grouped.size();

        if (itemCountBadge != null) {
            itemCountBadge.setText(totalPieces + (totalPieces == 1 ? " Item in Cart" : " Items in Cart"));
        }
        if (uniqueTypesValueLabel != null) {
            uniqueTypesValueLabel.setText(String.valueOf(distinctTypes));
        }
        if (totalPiecesValueLabel != null) {
            totalPiecesValueLabel.setText(String.valueOf(totalPieces));
        }
        if (subtotalValueLabel != null) {
            subtotalValueLabel.setText(String.format("%.2f EGP", subtotal));
        }

        if (storeManager != null && quickAddProductCombo != null) {
            Product currentSelection = quickAddProductCombo.getValue();
            List<Product> storeProducts = storeManager.getProducts().stream()
                    .filter(Product::isActive)
                    .collect(Collectors.toList());
            quickAddProductCombo.setItems(FXCollections.observableArrayList(storeProducts));
            if (currentSelection != null) {
                for (Product p : storeProducts) {
                    if (p.getId() == currentSelection.getId()) {
                        quickAddProductCombo.setValue(p);
                        break;
                    }
                }
            } else if (!storeProducts.isEmpty()) {
                quickAddProductCombo.getSelectionModel().selectFirst();
            }
        }
    }

    /**
     * Adjusts the quantity of a product inside the cart by +1 or -1 while keeping StoreManager stock in sync.
     */
    private void changeRowQuantity(CartRow row, int delta) {
        Product product = row.getProduct();
        int productId = product.getId();
        int currentQty = row.getQuantity();

        if (delta > 0) {
            if (storeManager != null) {
                if (storeManager.buyProduct(productId, delta)) {
                    storeManager.saveProducts();
                    refreshCartView();
                    notifyDataChanged();
                    setStatus("Increased '" + row.getName() + "' quantity to " + (currentQty + delta)
                            + ". Total: " + String.format("%.2f EGP", cart.calculateTotal()), false);
                } else {
                    setStatus("No more stock available for '" + row.getName() + "'.", true);
                }
            } else {
                cart.addProduct(product, delta);
                refreshCartView();
                notifyDataChanged();
            }
        } else if (delta < 0) {
            int newQty = currentQty + delta;
            if (newQty <= 0) {
                removeProductById(productId, row.getName(), currentQty);
            } else {
                cart.removeProduct(productId);
                cart.addProduct(product, newQty);
                if (storeManager != null) {
                    Product storeProduct = storeManager.findProductById(productId);
                    if (storeProduct != null) {
                        storeProduct.increaseQuantity(-delta);
                        storeManager.saveProducts();
                    }
                }
                refreshCartView();
                notifyDataChanged();
                setStatus("Decreased '" + row.getName() + "' quantity to " + newQty
                        + ". Total: " + String.format("%.2f EGP", cart.calculateTotal()), false);
            }
        }
    }

    private void handleRemoveSelectedProduct() {
        CartRow selected = cartTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatus("Please select a product row from the table first.", true);
            return;
        }
        removeProductById(selected.getProduct().getId(), selected.getName(), selected.getQuantity());
    }

    private void removeProductById(int productId, String productName, int removedQuantity) {
        cart.removeProduct(productId);

        if (storeManager != null) {
            Product storeProduct = storeManager.findProductById(productId);
            if (storeProduct != null && removedQuantity > 0) {
                storeProduct.increaseQuantity(removedQuantity);
                storeManager.saveProducts();
            }
        }

        refreshCartView();
        notifyDataChanged();
        setStatus("Removed '" + productName + "' from cart. New Total: "
                + String.format("%.2f EGP", cart.calculateTotal()), false);
    }

    private void handleClearCart() {
        if (cart.isEmpty()) {
            setStatus("Cart is already empty.", true);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Clear Shopping Cart");
        confirm.setHeaderText("Remove all products from the cart?");
        confirm.setContentText("This will return all items to stock and reset the total to 0.00 EGP.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (storeManager != null) {
                for (CartRow row : cartRows) {
                    Product storeProduct = storeManager.findProductById(row.getId());
                    if (storeProduct != null) {
                        storeProduct.increaseQuantity(row.getQuantity());
                    }
                }
                storeManager.saveProducts();
            }

            cart.clear();
            refreshCartView();
            notifyDataChanged();
            setStatus("Shopping cart cleared. Cumulative total is 0.00 EGP.", false);
        }
    }

    /**
     * Reads the exact typed quantity from the Quick-Add Spinner editor and adds that many items to the Cart.
     */
    private void handleQuickAddProduct() {
        Product selectedProduct = quickAddProductCombo.getValue();
        if (selectedProduct == null) {
            setStatus("Please select a product to add.", true);
            return;
        }

        int qty = parseAndCommitSpinnerQty(quickAddQtySpinner);
        if (qty <= 0) {
            setStatus("Quantity must be at least 1.", true);
            return;
        }

        if (storeManager != null) {
            boolean bought = storeManager.buyProduct(selectedProduct.getId(), qty);
            if (!bought) {
                setStatus("Insufficient stock for '" + selectedProduct.getName() + "' (Available: " + selectedProduct.getQuantity() + ").", true);
                return;
            }
            storeManager.saveProducts();
        } else {
            cart.addProduct(selectedProduct, qty);
        }

        refreshCartView();
        notifyDataChanged();

        double lineAdded = selectedProduct.getPrice() * qty;
        setStatus("Added " + qty + "x '" + selectedProduct.getName() + "' (" + String.format("%.2f EGP", lineAdded)
                + "). Cumulative total: " + String.format("%.2f EGP", cart.calculateTotal()), false);

        quickAddQtySpinner.getValueFactory().setValue(1);
        quickAddQtySpinner.getEditor().setText("1");
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

    private void notifyDataChanged() {
        if (onDataChanged != null) {
            onDataChanged.run();
        }
    }

    private HBox createSummaryRow(String labelText, Label valueLabel) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);

        Label lbl = new Label(labelText);
        lbl.setFont(Font.font("Segoe UI", 13.5));
        lbl.setTextFill(Color.web("#475569"));

        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        valueLabel.setTextFill(Color.web("#1E293B"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        row.getChildren().addAll(lbl, spacer, valueLabel);
        return row;
    }

    private TableCell<CartRow, Number> createCurrencyCell() {
        return new TableCell<CartRow, Number>() {
            @Override
            protected void updateItem(Number value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : String.format("%.2f EGP", value.doubleValue()));
            }
        };
    }

    private void setStatus(String message, boolean isError) {
        if (statusFeedbackLabel != null) {
            statusFeedbackLabel.setText(message);
            statusFeedbackLabel.setTextFill(isError ? Color.web("#DC2626") : Color.web("#0F766E"));
        }
    }

    public static class CartRow {
        private final Product product;
        private final SimpleIntegerProperty id;
        private final SimpleStringProperty name;
        private final SimpleStringProperty category;
        private final SimpleStringProperty specifications;
        private final SimpleDoubleProperty unitPrice;
        private final SimpleIntegerProperty quantity;
        private final SimpleDoubleProperty lineTotal;

        public CartRow(Product product, int quantity) {
            this.product = product;
            this.id = new SimpleIntegerProperty(product.getId());
            this.name = new SimpleStringProperty(product.getName());
            this.category = new SimpleStringProperty(ProductCatalogView.extractDisplayCategory(product));
            this.specifications = new SimpleStringProperty(ProductCatalogView.extractDisplaySpecs(product));
            this.unitPrice = new SimpleDoubleProperty(product.getPrice());
            this.quantity = new SimpleIntegerProperty(quantity);
            this.lineTotal = new SimpleDoubleProperty(product.getPrice() * quantity);
        }

        public void incrementQuantity() {
            int newQty = this.quantity.get() + 1;
            this.quantity.set(newQty);
            this.lineTotal.set(this.unitPrice.get() * newQty);
        }

        public Product getProduct() { return product; }
        public int getId() { return id.get(); }
        public SimpleIntegerProperty idProperty() { return id; }
        public String getName() { return name.get(); }
        public SimpleStringProperty nameProperty() { return name; }
        public SimpleStringProperty categoryProperty() { return category; }
        public SimpleStringProperty specificationsProperty() { return specifications; }
        public SimpleDoubleProperty unitPriceProperty() { return unitPrice; }
        public int getQuantity() { return quantity.get(); }
        public SimpleIntegerProperty quantityProperty() { return quantity; }
        public SimpleDoubleProperty lineTotalProperty() { return lineTotal; }
    }

    public static void main(String[] args) {
        launch(args);
    }
}