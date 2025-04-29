package dk.easv.belman.Gui.Controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import dk.easv.belman.BE.UploadEntry;
import dk.easv.belman.Gui.Model.UploadModel;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReportPreviewController {
    @FXML private Label orderNumberLabel;
    @FXML private Label submittedByLabel;
    @FXML private Label submissionDateLabel;
    @FXML private ListView<ImageView> reportImagesList;

    private final UploadModel uploadModel = UploadModel.getInstance();
    private String orderNumber;

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public void loadReportData(String orderNumber) {
        List<UploadEntry> uploadsForOrder = uploadModel.getPendingUploads().stream()
                .filter(upload -> orderNumber.equals(upload.getOrderNumber()))
                .collect(Collectors.toList());

        if (uploadsForOrder.isEmpty()) {
            orderNumberLabel.setText("N/A");
            return;
        }

        UploadEntry first = uploadsForOrder.get(0);

        orderNumberLabel.setText(first.getOrderNumber());
        submittedByLabel.setText(first.getUploadedBy());
        submissionDateLabel.setText(first.getUploadDate());

        List<ImageView> thumbnails = new ArrayList<>();
        for (UploadEntry upload : uploadsForOrder) {
            try {
                Image img = new Image(new File(upload.getImagePath()).toURI().toString(), 200, 0, true, true);
                ImageView imageView = new ImageView(img);
                imageView.setPreserveRatio(true);
                imageView.setFitWidth(200);
                thumbnails.add(imageView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        reportImagesList.setItems(FXCollections.observableArrayList(thumbnails));
    }

    public void handleSendEmail(ActionEvent actionEvent) {
        generatePdfReport();
    }

    public void generatePdfReport() {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.setLeading(20f);

            // Add basic text
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
            contentStream.newLineAtOffset(50, 750);
            contentStream.showText("QC Report");
            contentStream.newLine();

            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.showText("Order Number: " + orderNumberLabel.getText());
            contentStream.newLine();
            contentStream.showText("Submitted By: " + submittedByLabel.getText());
            contentStream.newLine();
            contentStream.showText("Submission Date: " + submissionDateLabel.getText());
            contentStream.newLine();
            contentStream.newLine();
            contentStream.showText("Attached Images:");
            contentStream.newLine();
            contentStream.endText();

            float yPosition = 650;

            for (ImageView imageView : reportImagesList.getItems()) {
                try {
                    URI imageUri = new URI(imageView.getImage().getUrl());
                    File imgFile = new File(imageUri.getPath());

                    if (!imgFile.exists()) {
                        System.out.println("Image file not found: " + imgFile.getAbsolutePath());
                        continue;
                    }

                    PDImageXObject pdImage = PDImageXObject.createFromFileByExtension(imgFile, document);

                    if (yPosition < 150) { // if not enough space left
                        contentStream.close();
                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        yPosition = 750;
                    }

                    contentStream.drawImage(pdImage, 50, yPosition - 100, 200, 100);
                    yPosition -= 120;
                } catch (IOException | URISyntaxException e) {
                    e.printStackTrace();
                }
            }

            contentStream.close();

            // Save to file
            File output = new File("QC_Report_" + sanitizeFilename(orderNumberLabel.getText()) + ".pdf");
            document.save(output);

            showAlert("PDF Report generated successfully:\n" + output.getAbsolutePath());

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(output);
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error creating PDF: " + e.getMessage());
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String sanitizeFilename(String input) {
        return input.replaceAll("[\\\\/:*?\"<>|]", "_");
    }


    public void handleBack(ActionEvent actionEvent) {
        SceneLoader("QaDashboard.fxml", actionEvent);
    }

    private void SceneLoader(String fxmlFile, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dk/easv/belman/" + fxmlFile));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.setFullScreen(true);
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}