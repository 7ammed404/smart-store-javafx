package smart.store;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;

import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Dedicated Sales History & Manager Reports Screen
 * Displays all recorded invoices starting from 1 (chronological order: 1, 2, 3, 4...),
 * supports period filtering, and exports reports to Excel (.csv) and PDF (.pdf).
 */
public class SalesHistoryView {

    private final ObservableList<InvoiceRecord> masterRecords = FXCollections.observableArrayList();
    private final FilteredList<InvoiceRecord> filteredRecords = new FilteredList<>(masterRecords, r -> true);
    private final TableView<InvoiceRecord> table = new TableView<>();

    private final ComboBox<String> periodFilterCombo = new ComboBox<>(
            FXCollections.observableArrayList("All Sales History", "Today's Sales", "Last 7 Days (Weekly)")
    );
    private final TextField searchField = new TextField();

    private final Label totalOrdersLabel = new Label("0");
    private final Label totalItemsSoldLabel = new Label("0");
    private final Label totalDiscountsLabel = new Label("0.00 EGP");
    private final Label totalRevenueLabel = new Label("0.00 EGP");
    private final Label statusLabel = new Label();

    private BorderPane root;

    public BorderPane getView() {
        if (root == null) {
            root = build();
        }
        refreshHistory();
        return root;
    }

    private BorderPane build() {
        BorderPane pane = new BorderPane();
        pane.setStyle("-fx-background-color: #F4F6F9; -fx-font-family: 'Segoe UI', Arial, sans-serif;");

        pane.setTop(buildHeader());

        VBox centerBox = new VBox(18, buildKpiSummaryRow(), buildToolbarAndTableCard());
        centerBox.setPadding(new Insets(22, 28, 24, 28));
        pane.setCenter(centerBox);

        return pane;
    }

    private HBox buildHeader() {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(18, 28, 18, 28));
        header.setStyle("-fx-background-color: linear-gradient(to right, #0F172A, #1E293B);");

        VBox titleBox = new VBox(3);
        Label title = new Label("Sales History & Financial Reports");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(Color.WHITE);

        Label subtitle = new Label("View complete invoice records, filter by date range, and export reports to Excel or PDF");
        subtitle.setFont(Font.font("Segoe UI", 13));
        subtitle.setTextFill(Color.web("#94A3B8"));
        titleBox.getChildren().addAll(title, subtitle);

        header.getChildren().add(titleBox);
        return header;
    }

    private HBox buildKpiSummaryRow() {
        HBox row = new HBox(16,
                createKpiCard("Filtered Invoices", totalOrdersLabel, "#2563EB"),
                createKpiCard("Total Units Sold", totalItemsSoldLabel, "#0D9488"),
                createKpiCard("Total Discounts Given", totalDiscountsLabel, "#D97706"),
                createKpiCard("Total Net Revenue", totalRevenueLabel, "#16A34A")
        );
        row.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));
        return row;
    }

    private VBox createKpiCard(String caption, Label valueLabel, String colorHex) {
        Label cap = new Label(caption);
        cap.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12.5));
        cap.setTextFill(Color.web("#64748B"));

        valueLabel.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 22));
        valueLabel.setTextFill(Color.web(colorHex));

        VBox card = new VBox(6, cap, valueLabel);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(15,23,42,0.06), 10, 0, 0, 3);");
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    @SuppressWarnings("unchecked")
    private VBox buildToolbarAndTableCard() {
        VBox card = new VBox(14);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(15,23,42,0.08), 12, 0, 0, 4);");
        VBox.setVgrow(card, Priority.ALWAYS);

        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        Label filterLbl = new Label("Report Period:");
        filterLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        filterLbl.setTextFill(Color.web("#334155"));

        periodFilterCombo.setValue("All Sales History");
        periodFilterCombo.setPrefWidth(190);
        periodFilterCombo.setStyle("-fx-font-size: 13px; -fx-background-radius: 7;");
        periodFilterCombo.setOnAction(e -> applyFilters());

        searchField.setPromptText("Search by Invoice # or Date (YYYY-MM-DD)...");
        searchField.setPrefWidth(260);
        searchField.setStyle("-fx-background-radius: 7; -fx-border-color: #CBD5E1; -fx-border-radius: 7; -fx-padding: 7 12 7 12;");
        searchField.textProperty().addListener((obs, oldV, newV) -> applyFilters());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setStyle("-fx-background-color: #E2E8F0; -fx-text-fill: #1E293B; -fx-font-weight: bold; "
                + "-fx-background-radius: 7; -fx-padding: 8 14 8 14; -fx-cursor: hand;");
        refreshBtn.setOnAction(e -> refreshHistory());

        Button exportExcelBtn = new Button("Export Excel (.csv)");
        exportExcelBtn.setStyle("-fx-background-color: #15803D; -fx-text-fill: white; -fx-font-weight: bold; "
                + "-fx-font-size: 13px; -fx-background-radius: 7; -fx-padding: 8 16 8 16; -fx-cursor: hand;");
        exportExcelBtn.setOnAction(e -> handleExportExcel());

        Button exportPdfBtn = new Button("Export PDF (.pdf)");
        exportPdfBtn.setStyle("-fx-background-color: #DC2626; -fx-text-fill: white; -fx-font-weight: bold; "
                + "-fx-font-size: 13px; -fx-background-radius: 7; -fx-padding: 8 16 8 16; -fx-cursor: hand;");
        exportPdfBtn.setOnAction(e -> handleExportPdf());

        Button printReportBtn = new Button("Print Table");
        printReportBtn.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-weight: bold; "
                + "-fx-font-size: 13px; -fx-background-radius: 7; -fx-padding: 8 16 8 16; -fx-cursor: hand;");
        printReportBtn.setOnAction(e -> handlePrintReport());

        toolbar.getChildren().addAll(filterLbl, periodFilterCombo, searchField, spacer, refreshBtn, exportExcelBtn, exportPdfBtn, printReportBtn);

        table.setItems(filteredRecords);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No recorded invoices match the selected period."));
        table.setStyle("-fx-font-size: 13.5px;");
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<InvoiceRecord, String> invIdCol = new TableColumn<>("#");
        invIdCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().invoiceNumber));
        invIdCol.setMinWidth(70);
        invIdCol.setMaxWidth(90);
        invIdCol.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");

        TableColumn<InvoiceRecord, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().dateText));
        dateCol.setMinWidth(120);
        dateCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<InvoiceRecord, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().timeText));
        timeCol.setMinWidth(110);
        timeCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<InvoiceRecord, Integer> itemsCol = new TableColumn<>("Items Purchased");
        itemsCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().itemCount));
        itemsCol.setMinWidth(125);
        itemsCol.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");

        TableColumn<InvoiceRecord, String> subCol = new TableColumn<>("Subtotal (Before Discount)");
        subCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(String.format(Locale.US, "%.2f EGP", c.getValue().subtotal)));
        subCol.setMinWidth(170);
        subCol.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<InvoiceRecord, String> discCol = new TableColumn<>("Discount Saved");
        discCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(String.format(Locale.US, "- %.2f EGP", c.getValue().discount)));
        discCol.setMinWidth(140);
        discCol.setStyle("-fx-alignment: CENTER-RIGHT; -fx-text-fill: #16A34A; -fx-font-weight: bold;");

        TableColumn<InvoiceRecord, String> totalCol = new TableColumn<>("Final Total Paid");
        totalCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(String.format(Locale.US, "%.2f EGP", c.getValue().finalTotal)));
        totalCol.setMinWidth(160);
        totalCol.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold; -fx-text-fill: #0F172A;");

        table.getColumns().addAll(invIdCol, dateCol, timeCol, itemsCol, subCol, discCol, totalCol);

        statusLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        statusLabel.setTextFill(Color.web("#0F766E"));

        card.getChildren().addAll(toolbar, table, statusLabel);
        return card;
    }

    public void refreshHistory() {
        masterRecords.clear();
        FileRepository repo = new FileRepository();
        List<String> lines = repo.loadData("data/invoices.txt");

        int index = 1;
        for (String line : lines) {
            String[] parts = line.split("\\|");
            if (parts.length >= 5) {
                String rawTimestamp = parts[0].trim();
                LocalDate parsedDate = LocalDate.now();
                String dateStr = rawTimestamp;
                String timeStr = "";

                if (rawTimestamp.contains("T")) {
                    String[] dt = rawTimestamp.split("T");
                    dateStr = dt[0];
                    timeStr = dt[1].length() > 8 ? dt[1].substring(0, 8) : dt[1];
                    try {
                        parsedDate = LocalDate.parse(dateStr);
                    } catch (Exception ignored) {
                    }
                }

                double sub = parseDoubleSafe(parts[1]);
                double disc = parseDoubleSafe(parts[2]);
                double tot = parseDoubleSafe(parts[3]);
                int items = (int) parseDoubleSafe(parts[4]);

                // Numbered sequentially starting from 1 (1, 2, 3, 4...) with newest added at the bottom
                masterRecords.add(new InvoiceRecord(String.valueOf(index++), parsedDate, dateStr, timeStr, sub, disc, tot, items));
            }
        }

        applyFilters();
    }

    private void applyFilters() {
        String period = periodFilterCombo.getValue();
        String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        LocalDate today = LocalDate.now();

        filteredRecords.setPredicate(rec -> {
            boolean matchesPeriod = true;
            if ("Today's Sales".equals(period)) {
                matchesPeriod = rec.date.equals(today);
            } else if ("Last 7 Days (Weekly)".equals(period)) {
                matchesPeriod = !rec.date.isBefore(today.minusDays(7));
            }

            boolean matchesQuery = query.isEmpty()
                    || rec.invoiceNumber.toLowerCase().contains(query)
                    || rec.dateText.toLowerCase().contains(query);

            return matchesPeriod && matchesQuery;
        });

        updateSummaryKpis();
    }

    private void updateSummaryKpis() {
        int count = filteredRecords.size();
        int items = filteredRecords.stream().mapToInt(r -> r.itemCount).sum();
        double discounts = filteredRecords.stream().mapToDouble(r -> r.discount).sum();
        double revenue = filteredRecords.stream().mapToDouble(r -> r.finalTotal).sum();

        totalOrdersLabel.setText(String.valueOf(count));
        totalItemsSoldLabel.setText(String.valueOf(items));
        totalDiscountsLabel.setText(String.format(Locale.US, "%.2f EGP", discounts));
        totalRevenueLabel.setText(String.format(Locale.US, "%.2f EGP", revenue));
    }

    /**
     * Exports the currently filtered sales records to an Excel-compatible CSV file (.csv).
     * Uses OutputStreamWriter (Java 8+ compatible) and Locale.US so Excel columns always align cleanly.
     */
    private void handleExportExcel() {
        if (filteredRecords.isEmpty()) {
            setStatus("No sales records available to export.", true);
            Alert warn = new Alert(Alert.AlertType.WARNING, "There are no sales records to export.", ButtonType.OK);
            warn.setHeaderText(null);
            warn.showAndWait();
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Sales Report to Excel (.csv)");
        chooser.setInitialFileName("SmartStore_Sales_Report_" + LocalDate.now() + ".csv");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel CSV File (*.csv)", "*.csv"));

        File chosenFile = chooser.showSaveDialog(root.getScene().getWindow());
        if (chosenFile == null) return;

        if (!chosenFile.getName().toLowerCase().endsWith(".csv")) {
            chosenFile = new File(chosenFile.getAbsolutePath() + ".csv");
        }
        final File targetFile = chosenFile;

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(targetFile), StandardCharsets.UTF_8))) {
            writer.write('\uFEFF'); // UTF-8 BOM for Excel
            writer.write("Invoice #,Date,Time,Items Purchased,Subtotal (EGP),Discount Saved (EGP),Final Total Paid (EGP)");
            writer.newLine();

            double sumSub = 0, sumDisc = 0, sumTot = 0;
            int sumItems = 0;

            for (InvoiceRecord r : filteredRecords) {
                writer.write(String.format(Locale.US, "%s,%s,%s,%d,%.2f,%.2f,%.2f",
                        r.invoiceNumber, r.dateText, r.timeText, r.itemCount, r.subtotal, r.discount, r.finalTotal));
                writer.newLine();
                sumItems += r.itemCount;
                sumSub += r.subtotal;
                sumDisc += r.discount;
                sumTot += r.finalTotal;
            }

            writer.newLine();
            writer.write(String.format(Locale.US, "TOTAL (%s),,,%d,%.2f,%.2f,%.2f",
                    periodFilterCombo.getValue(), sumItems, sumSub, sumDisc, sumTot));
            writer.newLine();
            writer.flush();

            setStatus("Excel report exported successfully to: " + targetFile.getAbsolutePath(), false);
            showExportSuccessAlert("Excel Export Successful", targetFile, "Open Excel File");
        } catch (IOException ex) {
            setStatus("Failed to export Excel file: " + ex.getMessage(), true);
            Alert err = new Alert(Alert.AlertType.ERROR, "Could not save Excel file:\n" + ex.getMessage(), ButtonType.OK);
            err.setHeaderText("Export Failed");
            err.showAndWait();
        }
    }

    /**
     * Exports a valid standalone PDF document (.pdf) in pure Java.
     */
    private void handleExportPdf() {
        if (filteredRecords.isEmpty()) {
            setStatus("No sales records available to export.", true);
            Alert warn = new Alert(Alert.AlertType.WARNING, "There are no sales records to export.", ButtonType.OK);
            warn.setHeaderText(null);
            warn.showAndWait();
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Sales Report to PDF (.pdf)");
        chooser.setInitialFileName("SmartStore_Sales_Report_" + LocalDate.now() + ".pdf");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Document (*.pdf)", "*.pdf"));

        File chosenFile = chooser.showSaveDialog(root.getScene().getWindow());
        if (chosenFile == null) return;

        if (!chosenFile.getName().toLowerCase().endsWith(".pdf")) {
            chosenFile = new File(chosenFile.getAbsolutePath() + ".pdf");
        }
        final File targetFile = chosenFile;

        try {
            List<String> reportLines = new ArrayList<>();
            reportLines.add("==========================================================================");
            reportLines.add("                   SMART STORE SYSTEM - SALES REPORT                      ");
            reportLines.add("==========================================================================");
            reportLines.add("Report Period : " + periodFilterCombo.getValue());
            reportLines.add("Generated On  : " + LocalDateTime.now().toString().replace("T", " ").substring(0, 19));
            reportLines.add("--------------------------------------------------------------------------");
            reportLines.add(String.format(Locale.US, "%-6s %-12s %-10s %-7s %-12s %-11s %-12s",
                    "#", "Date", "Time", "Items", "Subtotal", "Discount", "Final Total"));
            reportLines.add("--------------------------------------------------------------------------");

            double sumSub = 0, sumDisc = 0, sumTot = 0;
            int sumItems = 0;

            for (InvoiceRecord r : filteredRecords) {
                reportLines.add(String.format(Locale.US, "%-6s %-12s %-10s %-7d %10.2f   %9.2f   %10.2f EGP",
                        r.invoiceNumber, r.dateText, r.timeText, r.itemCount, r.subtotal, r.discount, r.finalTotal));
                sumItems += r.itemCount;
                sumSub += r.subtotal;
                sumDisc += r.discount;
                sumTot += r.finalTotal;
            }

            reportLines.add("==========================================================================");
            reportLines.add(String.format(Locale.US, "Total Invoices       : %d", filteredRecords.size()));
            reportLines.add(String.format(Locale.US, "Total Units Sold     : %d items", sumItems));
            reportLines.add(String.format(Locale.US, "Gross Subtotal       : %.2f EGP", sumSub));
            reportLines.add(String.format(Locale.US, "Total Discounts      : -%.2f EGP", sumDisc));
            reportLines.add(String.format(Locale.US, "NET SALES REVENUE    : %.2f EGP", sumTot));
            reportLines.add("==========================================================================");

            writeSimplePdfDocument(targetFile, reportLines);
            setStatus("PDF report exported successfully to: " + targetFile.getAbsolutePath(), false);
            showExportSuccessAlert("PDF Export Successful", targetFile, "Open PDF File");
        } catch (IOException ex) {
            setStatus("Failed to export PDF: " + ex.getMessage(), true);
        }
    }

    private void showExportSuccessAlert(String title, File file, String openBtnLabel) {
        ButtonType openBtn = new ButtonType(openBtnLabel);
        ButtonType okBtn = new ButtonType("Done", ButtonBar.ButtonData.OK_DONE);
        Alert alert = new Alert(Alert.AlertType.INFORMATION,
                "Report saved successfully to:\n" + file.getAbsolutePath(),
                openBtn, okBtn);
        alert.setTitle(title);
        alert.setHeaderText(title);

        Optional<ButtonType> choice = alert.showAndWait();
        if (choice.isPresent() && choice.get() == openBtn) {
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                }
            } catch (Exception ex) {
                setStatus("File saved, but could not auto-open: " + ex.getMessage(), false);
            }
        }
    }

    private void handlePrintReport() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(root.getScene().getWindow())) {
            boolean success = job.printPage(table);
            if (success) {
                job.endJob();
                setStatus("Sales report sent to printer successfully.", false);
            }
        }
    }

    public static void writeSimplePdfDocument(File outputFile, List<String> lines) throws IOException {
        StringBuilder stream = new StringBuilder();
        stream.append("BT\n/F1 10 Tf\n14 TL\n40 790 Td\n");

        int maxLines = Math.min(lines.size(), 52);
        for (int i = 0; i < maxLines; i++) {
            String escaped = lines.get(i)
                    .replace("\\", "\\\\")
                    .replace("(", "\\(")
                    .replace(")", "\\)");
            stream.append("(").append(escaped).append(") Tj T*\n");
        }
        stream.append("ET\n");

        byte[] contentBytes = stream.toString().getBytes(StandardCharsets.US_ASCII);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<Integer> offsets = new ArrayList<>();

        writeAscii(out, "%PDF-1.4\n");

        offsets.add(out.size());
        writeAscii(out, "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");

        offsets.add(out.size());
        writeAscii(out, "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");

        offsets.add(out.size());
        writeAscii(out, "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 842] "
                + "/Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>\nendobj\n");

        offsets.add(out.size());
        writeAscii(out, "4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Courier >>\nendobj\n");

        offsets.add(out.size());
        writeAscii(out, "5 0 obj\n<< /Length " + contentBytes.length + " >>\nstream\n");
        out.write(contentBytes);
        writeAscii(out, "\nendstream\nendobj\n");

        int xrefOffset = out.size();
        writeAscii(out, "xref\n0 6\n0000000000 65535 f \n");
        for (int offset : offsets) {
            writeAscii(out, String.format("%010d 00000 n \n", offset));
        }
        writeAscii(out, "trailer\n<< /Size 6 /Root 1 0 R >>\nstartxref\n" + xrefOffset + "\n%%EOF\n");

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            out.writeTo(fos);
        }
    }

    private static void writeAscii(ByteArrayOutputStream out, String text) throws IOException {
        out.write(text.getBytes(StandardCharsets.US_ASCII));
    }

    private double parseDoubleSafe(String val) {
        try {
            return Double.parseDouble(val.trim());
        } catch (Exception e) {
            return 0.0;
        }
    }

    private void setStatus(String msg, boolean isError) {
        statusLabel.setText(msg);
        statusLabel.setTextFill(isError ? Color.web("#DC2626") : Color.web("#0F766E"));
    }

    public static class InvoiceRecord {
        public final String invoiceNumber;
        public final LocalDate date;
        public final String dateText;
        public final String timeText;
        public final double subtotal;
        public final double discount;
        public final double finalTotal;
        public final int itemCount;

        public InvoiceRecord(String invoiceNumber, LocalDate date, String dateText, String timeText,
                             double subtotal, double discount, double finalTotal, int itemCount) {
            this.invoiceNumber = invoiceNumber;
            this.date = date;
            this.dateText = dateText;
            this.timeText = timeText;
            this.subtotal = subtotal;
            this.discount = discount;
            this.finalTotal = finalTotal;
            this.itemCount = itemCount;
        }
    }
}