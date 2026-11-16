package smart.store;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Invoice & Order Checkout Screen
 * Spacious left panel for live discount selection & order calculation,
 * compact thermal-receipt preview on the right, plus direct printing & PDF receipt download.
 */
public class InvoiceView {

    private final StoreManager storeManager;
    private final Runnable onCheckoutCompleted;
    private final Runnable onOpenSalesHistory;

    private final TextArea receiptPreviewArea = new TextArea();
    private final RadioButton noDiscountRadio = new RadioButton("Standard Price (No Discount - 0%)");
    private final RadioButton percentageDiscountRadio = new RadioButton("Promotional Percentage Discount (%)");
    private final TextField discountPercentField = new TextField("10");

    private final Label itemsCountValueLabel = new Label("0 Items");
    private final Label subtotalValueLabel = new Label("0.00 EGP");
    private final Label discountRateValueLabel = new Label("0%");
    private final Label discountAmountValueLabel = new Label("- 0.00 EGP");
    private final Label finalPayableValueLabel = new Label("0.00 EGP");
    private final Label statusLabel = new Label();

    private double currentDiscountPercentage = 0.0;
    private BorderPane root;

    public InvoiceView(StoreManager storeManager, Runnable onCheckoutCompleted) {
        this(storeManager, onCheckoutCompleted, null);
    }

    public InvoiceView(StoreManager storeManager, Runnable onCheckoutCompleted, Runnable onOpenSalesHistory) {
        this.storeManager = storeManager;
        this.onCheckoutCompleted = onCheckoutCompleted;
        this.onOpenSalesHistory = onOpenSalesHistory;
    }

    public BorderPane getView() {
        if (root == null) {
            root = build();
        }
        refreshInvoiceView();
        return root;
    }

    private BorderPane build() {
        BorderPane pane = new BorderPane();
        pane.setStyle("-fx-background-color: #F4F6F9; -fx-font-family: 'Segoe UI', Arial, sans-serif;");

        pane.setTop(buildHeader());

        // Left: Spacious Discount & Payment Configuration | Right: Compact Receipt Preview
        HBox mainContent = new HBox(22, buildSpaciousCheckoutPanel(), buildCompactReceiptPanel());
        mainContent.setPadding(new Insets(22, 28, 22, 28));
        pane.setCenter(mainContent);

        return pane;
    }

    private HBox buildHeader() {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(18, 28, 18, 28));
        header.setStyle("-fx-background-color: linear-gradient(to right, #0F172A, #1E293B);");

        VBox titleBox = new VBox(3);
        Label title = new Label("Invoice & Order Checkout");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(Color.WHITE);

        Label subtitle = new Label("Configure promotional discounts with live calculation, confirm payment, and print or download the customer receipt");
        subtitle.setFont(Font.font("Segoe UI", 13));
        subtitle.setTextFill(Color.web("#94A3B8"));
        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button viewHistoryBtn = new Button("Open Sales History & Reports ->");
        viewHistoryBtn.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-weight: bold; "
                + "-fx-font-size: 13px; -fx-background-radius: 8; -fx-padding: 9 16 9 16; -fx-cursor: hand;");
        viewHistoryBtn.setOnAction(e -> {
            if (onOpenSalesHistory != null) {
                onOpenSalesHistory.run();
            }
        });

        header.getChildren().addAll(titleBox, spacer, viewHistoryBtn);
        return header;
    }

    /**
     * Wide, clear panel for selecting discounts (with instant live updates as you type or click preset buttons)
     * and viewing the complete payment breakdown.
     */
    private VBox buildSpaciousCheckoutPanel() {
        VBox panel = new VBox(18);
        panel.setPadding(new Insets(24));
        panel.setStyle("-fx-background-color: white; -fx-background-radius: 12; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(15,23,42,0.08), 12, 0, 0, 4);");
        HBox.setHgrow(panel, Priority.ALWAYS);

        Label sectionHeader = new Label("1. Discount Configuration (Live Update)");
        sectionHeader.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        sectionHeader.setTextFill(Color.web("#0F172A"));

        ToggleGroup group = new ToggleGroup();
        noDiscountRadio.setToggleGroup(group);
        percentageDiscountRadio.setToggleGroup(group);
        noDiscountRadio.setSelected(true);
        noDiscountRadio.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        percentageDiscountRadio.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));

        // Live Discount Input Row + Quick Preset Buttons
        Label pctLabel = new Label("Discount Percentage (%):");
        pctLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13.5));
        pctLabel.setTextFill(Color.web("#334155"));

        discountPercentField.setPrefWidth(110);
        discountPercentField.setDisable(true);
        discountPercentField.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 7; "
                + "-fx-border-color: #CBD5E1; -fx-border-radius: 7; -fx-padding: 7 12 7 12;");

        // Instant live calculation as the user types any number!
        discountPercentField.textProperty().addListener((obs, oldText, newText) -> {
            if (percentageDiscountRadio.isSelected()) {
                parseAndApplyLiveDiscount(newText);
            }
        });

        HBox presetButtonsBox = new HBox(8);
        presetButtonsBox.setAlignment(Pos.CENTER_LEFT);
        Label quickLbl = new Label("Quick Presets:");
        quickLbl.setFont(Font.font("Segoe UI", 13));
        quickLbl.setTextFill(Color.web("#64748B"));
        presetButtonsBox.getChildren().add(quickLbl);

        int[] presets = {5, 10, 15, 20, 25, 50};
        for (int p : presets) {
            Button presetBtn = new Button(p + "%");
            presetBtn.setStyle("-fx-background-color: #EFF6FF; -fx-text-fill: #1D4ED8; -fx-font-weight: bold; "
                    + "-fx-background-radius: 6; -fx-border-color: #BFDBFE; -fx-border-radius: 6; -fx-padding: 5 12 5 12; -fx-cursor: hand;");
            presetBtn.setOnAction(e -> {
                percentageDiscountRadio.setSelected(true);
                discountPercentField.setDisable(false);
                discountPercentField.setText(String.valueOf(p));
                parseAndApplyLiveDiscount(String.valueOf(p));
            });
            presetButtonsBox.getChildren().add(presetBtn);
        }

        HBox customInputRow = new HBox(12, pctLabel, discountPercentField, presetButtonsBox);
        customInputRow.setAlignment(Pos.CENTER_LEFT);
        customInputRow.setPadding(new Insets(8, 0, 4, 24));

        noDiscountRadio.setOnAction(e -> {
            discountPercentField.setDisable(true);
            currentDiscountPercentage = 0.0;
            refreshInvoiceView();
            setStatus("Standard price selected (0% discount).", false);
        });

        percentageDiscountRadio.setOnAction(e -> {
            discountPercentField.setDisable(false);
            discountPercentField.requestFocus();
            parseAndApplyLiveDiscount(discountPercentField.getText());
        });

        Separator sep = new Separator();

        // 2. Clear Payment Breakdown Section
        Label breakdownHeader = new Label("2. Payment & Order Breakdown");
        breakdownHeader.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        breakdownHeader.setTextFill(Color.web("#0F172A"));

        itemsCountValueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        subtotalValueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        discountRateValueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        discountRateValueLabel.setTextFill(Color.web("#2563EB"));
        discountAmountValueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        discountAmountValueLabel.setTextFill(Color.web("#16A34A"));
        finalPayableValueLabel.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 28));
        finalPayableValueLabel.setTextFill(Color.web("#15803D"));

        VBox totalsCard = new VBox(12,
                summaryLine("Total Units in Order:", itemsCountValueLabel),
                summaryLine("Order Subtotal (Before Discount):", subtotalValueLabel),
                summaryLine("Applied Discount Rate:", discountRateValueLabel),
                summaryLine("Total Discount Savings:", discountAmountValueLabel),
                new Separator(),
                summaryLine("FINAL AMOUNT DUE:", finalPayableValueLabel)
        );
        totalsCard.setPadding(new Insets(18, 22, 18, 22));
        totalsCard.setStyle("-fx-background-color: #F0FDF4; -fx-border-color: #BBF7D0; -fx-border-radius: 10; -fx-background-radius: 10;");

        // Action Buttons Row
        Button confirmCheckoutBtn = new Button("Confirm & Complete Order");
        confirmCheckoutBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(confirmCheckoutBtn, Priority.ALWAYS);
        confirmCheckoutBtn.setStyle("-fx-background-color: #16A34A; -fx-text-fill: white; -fx-font-weight: bold; "
                + "-fx-font-size: 15px; -fx-background-radius: 8; -fx-padding: 13 20 13 20; -fx-cursor: hand;");
        confirmCheckoutBtn.setOnAction(e -> handleConfirmAndSaveInvoice());

        Button printReceiptBtn = new Button("Print Receipt");
        printReceiptBtn.setStyle("-fx-background-color: #0F172A; -fx-text-fill: white; -fx-font-weight: bold; "
                + "-fx-font-size: 14px; -fx-background-radius: 8; -fx-padding: 13 18 13 18; -fx-cursor: hand;");
        printReceiptBtn.setOnAction(e -> handlePrintReceipt(receiptPreviewArea.getText()));

        Button downloadPdfBtn = new Button("Save Receipt PDF");
        downloadPdfBtn.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-weight: bold; "
                + "-fx-font-size: 14px; -fx-background-radius: 8; -fx-padding: 13 18 13 18; -fx-cursor: hand;");
        downloadPdfBtn.setOnAction(e -> handleDownloadReceiptPdf(receiptPreviewArea.getText()));

        HBox actionButtonsRow = new HBox(12, confirmCheckoutBtn, printReceiptBtn, downloadPdfBtn);
        actionButtonsRow.setAlignment(Pos.CENTER_LEFT);

        statusLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13.5));
        statusLabel.setTextFill(Color.web("#0F766E"));

        panel.getChildren().addAll(
                sectionHeader,
                noDiscountRadio,
                percentageDiscountRadio,
                customInputRow,
                sep,
                breakdownHeader,
                totalsCard,
                actionButtonsRow,
                statusLabel
        );
        return panel;
    }

    /**
     * Compact, receipt-sized panel on the right showing the live thermal receipt preview.
     */
    private VBox buildCompactReceiptPanel() {
        VBox box = new VBox(12);
        box.setPrefWidth(390);
        box.setMinWidth(370);
        box.setMaxWidth(410);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: white; -fx-background-radius: 12; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(15,23,42,0.08), 12, 0, 0, 4);");

        Label sectionTitle = new Label("Customer Receipt Preview");
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        sectionTitle.setTextFill(Color.web("#0F172A"));

        Label sub = new Label("Compact POS receipt ready for printing or PDF save");
        sub.setFont(Font.font("Segoe UI", 12));
        sub.setTextFill(Color.web("#64748B"));

        receiptPreviewArea.setEditable(false);
        receiptPreviewArea.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace; -fx-font-size: 12.5px; "
                + "-fx-control-inner-background: #F8FAFC; -fx-text-fill: #0F172A; -fx-border-color: #CBD5E1; -fx-border-radius: 8;");
        VBox.setVgrow(receiptPreviewArea, Priority.ALWAYS);

        box.getChildren().addAll(new VBox(2, sectionTitle, sub), receiptPreviewArea);
        return box;
    }

    private HBox summaryLine(String label, Label valueLabel) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        Label l = new Label(label);
        l.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        l.setTextFill(Color.web("#334155"));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        row.getChildren().addAll(l, spacer, valueLabel);
        return row;
    }

    private void parseAndApplyLiveDiscount(String rawText) {
        if (rawText == null || rawText.trim().isEmpty()) {
            currentDiscountPercentage = 0.0;
            refreshInvoiceView();
            return;
        }
        try {
            double pct = Double.parseDouble(rawText.trim());
            new PercentageDiscount(pct); // validates 0..100
            currentDiscountPercentage = pct;
            refreshInvoiceView();
            setStatus("Live Discount Active: " + String.format("%.1f%%", pct) + " off order subtotal.", false);
        } catch (NumberFormatException ex) {
            setStatus("Please enter a valid number for discount percentage.", true);
        } catch (IllegalArgumentException ex) {
            setStatus(ex.getMessage(), true);
        }
    }

    public void refreshInvoiceView() {
        Cart cart = storeManager.getCart();
        double activePct = (percentageDiscountRadio.isSelected() && currentDiscountPercentage > 0)
                ? currentDiscountPercentage
                : 0.0;

        DiscountStrategy strategy = (activePct > 0)
                ? new PercentageDiscount(activePct)
                : new NoDiscount();

        Invoice invoice = new Invoice(cart, strategy);

        int itemCount = cart.getItemCount();
        double subtotal = invoice.getSubtotal();
        double discountAmount = invoice.getDiscountAmount();
        double finalTotal = invoice.getTotal();

        itemsCountValueLabel.setText(itemCount + (itemCount == 1 ? " Unit" : " Units"));
        subtotalValueLabel.setText(String.format("%.2f EGP", subtotal));
        discountRateValueLabel.setText(String.format("%.1f%%", activePct));
        discountAmountValueLabel.setText(String.format("- %.2f EGP", discountAmount));
        finalPayableValueLabel.setText(String.format("%.2f EGP", finalTotal));

        receiptPreviewArea.setText(buildFormattedReceipt(cart, invoice, activePct));
    }

    private String buildFormattedReceipt(Cart cart, Invoice invoice, double activePct) {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("           SMART STORE SYSTEM           \n");
        sb.append("          OFFICIAL POS RECEIPT          \n");
        sb.append("========================================\n");
        sb.append("Date: ").append(LocalDate.now()).append("\n");
        sb.append("----------------------------------------\n");

        if (cart.isEmpty()) {
            sb.append("\n  [No items in cart - Add products]\n\n");
        } else {
            sb.append(String.format("%-16s %-5s %14s%n", "Product", "Qty", "Total"));
            sb.append("----------------------------------------\n");

            Map<Integer, Integer> counts = new LinkedHashMap<>();
            Map<Integer, Product> productMap = new LinkedHashMap<>();
            for (Product p : cart.getItems()) {
                counts.put(p.getId(), counts.getOrDefault(p.getId(), 0) + 1);
                productMap.put(p.getId(), p);
            }

            for (Map.Entry<Integer, Product> entry : productMap.entrySet()) {
                Product p = entry.getValue();
                int qty = counts.get(entry.getKey());
                double lineTotal = p.getPrice() * qty;
                String name = p.getName().length() > 15 ? p.getName().substring(0, 15) : p.getName();
                sb.append(String.format("%-16s x%-4d %10.2f EGP%n", name, qty, lineTotal));
            }
            sb.append("----------------------------------------\n");
        }

        sb.append(String.format("Total Units      : %d%n", cart.getItemCount()));
        sb.append(String.format("Subtotal         : %.2f EGP%n", invoice.getSubtotal()));
        sb.append(String.format("Discount Rate    : %.1f%%%n", activePct));
        sb.append(String.format("Discount Saved   : -%.2f EGP%n", invoice.getDiscountAmount()));
        sb.append("========================================\n");
        sb.append(String.format("FINAL TOTAL DUE  : %.2f EGP%n", invoice.getTotal()));
        sb.append("========================================\n");
        sb.append("     Thank you for shopping with us!    \n");
        return sb.toString();
    }

    private void handleConfirmAndSaveInvoice() {
        Cart cart = storeManager.getCart();
        if (cart.isEmpty()) {
            setStatus("Cannot complete checkout: Shopping Cart is empty!", true);
            Alert warn = new Alert(Alert.AlertType.WARNING, "Your shopping cart is empty. Please add products first.", ButtonType.OK);
            warn.setHeaderText(null);
            warn.showAndWait();
            return;
        }

        double activePct = (percentageDiscountRadio.isSelected() && currentDiscountPercentage > 0)
                ? currentDiscountPercentage
                : 0.0;

        DiscountStrategy strategy = (activePct > 0)
                ? new PercentageDiscount(activePct)
                : new NoDiscount();

        Invoice invoice = new Invoice(cart, strategy);
        String finalReceiptText = buildFormattedReceipt(cart, invoice, activePct);

        if (storeManager.saveInvoice(invoice)) {
            storeManager.clearCart();
            storeManager.saveProducts();

            noDiscountRadio.setSelected(true);
            discountPercentField.setDisable(true);
            currentDiscountPercentage = 0.0;

            refreshInvoiceView();
            if (onCheckoutCompleted != null) {
                onCheckoutCompleted.run();
            }

            setStatus("Order completed and recorded in sales history!", false);

            // Offer immediate Print or Save PDF of the completed receipt
            ButtonType printBtn = new ButtonType("Print Receipt");
            ButtonType savePdfBtn = new ButtonType("Save as PDF");
            ButtonType closeBtn = new ButtonType("Done", ButtonBar.ButtonData.CANCEL_CLOSE);

            Alert confirmDialog = new Alert(Alert.AlertType.INFORMATION,
                    "Order has been saved successfully.\nWould you like to print the customer receipt or save it as a PDF?",
                    printBtn, savePdfBtn, closeBtn);
            confirmDialog.setTitle("Order Completed");
            confirmDialog.setHeaderText("Payment Confirmed & Recorded");

            Optional<ButtonType> choice = confirmDialog.showAndWait();
            if (choice.isPresent()) {
                if (choice.get() == printBtn) {
                    handlePrintReceipt(finalReceiptText);
                } else if (choice.get() == savePdfBtn) {
                    handleDownloadReceiptPdf(finalReceiptText);
                }
            }
        } else {
            setStatus("Error saving invoice.", true);
        }
    }

    private void handlePrintReceipt(String receiptText) {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(root.getScene().getWindow())) {
            Label printableNode = new Label(receiptText);
            printableNode.setFont(Font.font("Courier New", 11));
            printableNode.setPadding(new Insets(20));
            boolean printed = job.printPage(printableNode);
            if (printed) {
                job.endJob();
                setStatus("Receipt sent to printer successfully.", false);
            }
        }
    }

    private void handleDownloadReceiptPdf(String receiptText) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Customer Receipt as PDF");
        chooser.setInitialFileName("SmartStore_Receipt_" + LocalDate.now() + ".pdf");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Document (*.pdf)", "*.pdf"));

        File file = chooser.showSaveDialog(root.getScene().getWindow());
        if (file == null) return;

        try {
            List<String> lines = Arrays.asList(receiptText.split("\r?\n"));
            SalesHistoryView.writeSimplePdfDocument(file, lines);
            setStatus("Receipt PDF saved to: " + file.getAbsolutePath(), false);
        } catch (IOException ex) {
            setStatus("Could not save PDF: " + ex.getMessage(), true);
        }
    }

    private void setStatus(String msg, boolean isError) {
        statusLabel.setText(msg);
        statusLabel.setTextFill(isError ? Color.web("#DC2626") : Color.web("#0F766E"));
    }
}