package dk.easv.belman.Gui.Controller;

import dk.easv.belman.Utility.ModelException;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import dk.easv.belman.BE.UploadEntry;
import dk.easv.belman.Gui.Model.UploadModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Window;
import javax.imageio.ImageIO;
import javafx.geometry.Insets;
import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import static dk.easv.belman.Gui.Model.UploadModel.getInstance;

public class ReportPreviewController {

    @FXML
    private Label orderNumberLabel;
    @FXML
    private Label submissionDateLabel;
    @FXML
    private VBox imageContainer;
    @FXML
    private TextArea notesTextArea;

    private final UploadModel uploadModel = getInstance();
    private List<UploadEntry> currentUploads;

    public void loadReportData(String orderNumber) {
        imageContainer.getChildren().clear();

        currentUploads = uploadModel.getUploadsByOrderNumber(orderNumber);

        if (currentUploads.isEmpty()) {
            orderNumberLabel.setText("N/A");
            return;
        }

        UploadEntry first = currentUploads.get(0);
        orderNumberLabel.setText(first.getOrderNumber());
        submissionDateLabel.setText(first.getUploadDate());

        for (UploadEntry upload : currentUploads) {
            try {
                File imgFile = new File(upload.getImagePath());
                if (!imgFile.exists()) continue;

                Image image = new Image(imgFile.toURI().toString(), 200, 0, true, true);
                ImageView imageView = new ImageView(image);
                imageView.setPreserveRatio(true);
                imageView.setFitWidth(200);

                Label angleLabel = new Label("Angle: " + upload.getImageType());
                VBox itemBox = new VBox(5, imageView, angleLabel);
                itemBox.setAlignment(Pos.CENTER);
                itemBox.setPadding(new Insets(10));
                imageContainer.getChildren().add(itemBox);
            } catch (Exception e) {
                throw new ModelException("Failed to load image for upload: " + upload.getImagePath(), e);
            }
        }
    }

    public void handlePdf(ActionEvent actionEvent) {
        generatePdfReport();
    }

    public void generatePdfReport() {
        final float MARGIN = 50, IMAGE_WIDTH = 250f, IMAGE_SPACING = 20f, TEXT_LINE_SPACING = 16f;
        final float PAGE_WIDTH = PDRectangle.A4.getWidth();
        final float PAGE_HEIGHT = PDRectangle.A4.getHeight();

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            PDPageContentStream stream = new PDPageContentStream(document, page);
            float y = PAGE_HEIGHT - MARGIN;

            stream.beginText();
            stream.setFont(PDType1Font.HELVETICA_BOLD, 18);
            stream.setLeading(22f);
            stream.newLineAtOffset(MARGIN, y);
            stream.showText("Belman A/S Quality Control Report");
            stream.newLine();
            stream.setFont(PDType1Font.HELVETICA, 12);
            stream.showText("Order Number: " + orderNumberLabel.getText());
            stream.newLine();
            stream.showText("Submission Date: " + submissionDateLabel.getText());
            stream.endText();

            y -= 110;

            for (UploadEntry upload : currentUploads) {
                try {
                    File imgFile = new File(upload.getImagePath());
                    if (!imgFile.exists()) continue;

                    BufferedImage bufferedImage = ImageIO.read(imgFile);
                    float scale = IMAGE_WIDTH / bufferedImage.getWidth();
                    float imageHeight = bufferedImage.getHeight() * scale;

                    if (y - imageHeight < MARGIN) {
                        stream.close();
                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        stream = new PDPageContentStream(document, page);
                        y = PAGE_HEIGHT - MARGIN;
                    }

                    PDImageXObject pdImage = PDImageXObject.createFromFileByExtension(imgFile, document);
                    stream.drawImage(pdImage, MARGIN, y - imageHeight, IMAGE_WIDTH, imageHeight);
                    y -= imageHeight + 14;

                    stream.beginText();
                    stream.setFont(PDType1Font.HELVETICA_OBLIQUE, 10);
                    stream.newLineAtOffset(MARGIN, y);
                    stream.showText("Angle: " + upload.getImageType());
                    stream.endText();

                    y -= IMAGE_SPACING;
                } catch (Exception e) {
                    throw new ModelException("Failed to process image: " + upload.getImagePath(), e);
                }
            }

            float estimatedNoteHeight = (notesTextArea.getText().split("\n").length + 2) * TEXT_LINE_SPACING;
            if (y - estimatedNoteHeight < MARGIN) {
                stream.close();
                page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                stream = new PDPageContentStream(document, page);
                y = PAGE_HEIGHT - MARGIN;
            }

            stream.beginText();
            stream.setFont(PDType1Font.HELVETICA_BOLD, 16);
            stream.setLeading(TEXT_LINE_SPACING);
            stream.newLineAtOffset(MARGIN, y);
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
            if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(output);

        } catch (IOException e) {
            showAlert("Error creating PDF: " + e.getMessage());
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setContentText(message);
        try {
            Stage stage = (Stage) orderNumberLabel.getScene().getWindow();
            alert.initOwner(stage);
        } catch (Exception ignored) {
        }
        alert.show();
    }

    private String sanitizeFilename(String input) {
        return input.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    public void handleBack(ActionEvent actionEvent) {
        try {
            for (Window window : Window.getWindows()) {
                if (window instanceof Stage stage && stage.isShowing() && stage != ((Node) actionEvent.getSource()).getScene().getWindow()) {
                    stage.setFullScreen(true);
                    stage.setFullScreenExitHint("");
                    break;
                }
            }
            ((Stage) ((Node) actionEvent.getSource()).getScene().getWindow()).close();
        } catch (Exception e) {
            showAlert("Failed to go back: " + e.getMessage());
        }
    }
}