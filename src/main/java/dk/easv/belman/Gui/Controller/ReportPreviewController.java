package dk.easv.belman.Gui.Controller;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
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
import javafx.stage.Window;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReportPreviewController {
    @FXML
    private Label orderNumberLabel;
    @FXML
    private Label submittedByLabel;
    @FXML
    private Label submissionDateLabel;
    @FXML
    private ListView<ImageView> reportImagesList;
    @FXML
    private TextArea notesTextArea;

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

    public void handlePdf(ActionEvent actionEvent) {
        generatePdfReport();
    }

    public void generatePdfReport() {
        final float MARGIN = 50;
        final float IMAGE_WIDTH = 250f;
        final float IMAGE_SPACING = 20f;
        final float TEXT_LINE_SPACING = 16f;
        final float PAGE_WIDTH = PDRectangle.A4.getWidth();
        final float PAGE_HEIGHT = PDRectangle.A4.getHeight();

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            PDPageContentStream stream = new PDPageContentStream(document, page);
            float yPosition = PAGE_HEIGHT - MARGIN;

            stream.beginText();
            stream.setFont(PDType1Font.HELVETICA_BOLD, 18);
            stream.setLeading(22f);
            stream.newLineAtOffset(MARGIN, yPosition);
            stream.showText("Belman A/S Quality Control Report");
            stream.newLine();

            stream.setFont(PDType1Font.HELVETICA, 12);
            stream.showText("Order Number: " + orderNumberLabel.getText());
            stream.newLine();
            stream.showText("Submitted By: " + submittedByLabel.getText());
            stream.newLine();
            stream.showText("Submission Date: " + submissionDateLabel.getText());
            stream.endText();

            yPosition -= 110;

            for (ImageView imageView : reportImagesList.getItems()) {
                try {
                    URI imageUri = new URI(imageView.getImage().getUrl());
                    File imgFile = new File(imageUri.getPath());
                    if (!imgFile.exists()) continue;

                    BufferedImage bufferedImage = ImageIO.read(imgFile);
                    float scale = IMAGE_WIDTH / bufferedImage.getWidth();
                    float imageHeight = bufferedImage.getHeight() * scale;

                    if (yPosition - imageHeight < MARGIN) {
                        stream.close();
                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        stream = new PDPageContentStream(document, page);
                        yPosition = PAGE_HEIGHT - MARGIN;
                    }

                    PDImageXObject pdImage = PDImageXObject.createFromFileByExtension(imgFile, document);
                    stream.drawImage(pdImage, MARGIN, yPosition - imageHeight, IMAGE_WIDTH, imageHeight);
                    yPosition -= imageHeight + IMAGE_SPACING;

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            float estimatedNoteHeight = (notesTextArea.getText().split("\n").length + 2) * TEXT_LINE_SPACING;

            if (yPosition - estimatedNoteHeight < MARGIN) {
                stream.close();
                page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                stream = new PDPageContentStream(document, page);
                yPosition = PAGE_HEIGHT - MARGIN;
            }

            stream.beginText();
            stream.setFont(PDType1Font.HELVETICA_BOLD, 16);
            stream.setLeading(TEXT_LINE_SPACING);
            stream.newLineAtOffset(MARGIN, yPosition);
            stream.showText("QA Notes:");
            stream.newLine();

            stream.setFont(PDType1Font.HELVETICA, 12);
            for (String line : notesTextArea.getText().split("\n")) {
                stream.showText(line);
                stream.newLine();
            }

            stream.endText();
            stream.close();

            File output = new File("QC_Report_" + sanitizeFilename(orderNumberLabel.getText()) + ".pdf");
            document.save(output);

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(output);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error creating PDF: " + e.getMessage());
        }
    }
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);

        Stage stage = (Stage) orderNumberLabel.getScene().getWindow();
        alert.initOwner(stage);

        alert.show();
    }

    private String sanitizeFilename(String input) {
        return input.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    public void handleBack(ActionEvent actionEvent) {
        for (Window window : Window.getWindows()) {
            if (window instanceof Stage stage && stage.isShowing() && stage != ((Node) actionEvent.getSource()).getScene().getWindow()) {
                stage.setFullScreen(true);
                stage.setFullScreenExitHint("");
                break;
            }
        }
        ((Stage) ((Node) actionEvent.getSource()).getScene().getWindow()).close();
    }
}