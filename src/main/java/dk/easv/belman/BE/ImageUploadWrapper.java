package dk.easv.belman.BE;

import java.io.File;

public class ImageUploadWrapper {
    private final File imageFile;
    private String angle;

    public ImageUploadWrapper(File imageFile, String angle) {
        this.imageFile = imageFile;
        this.angle = angle;
    }

    public File getImageFile() { return imageFile; }
    public String getAngle() { return angle; }
    public void setAngle(String angle) { this.angle = angle; }
}
