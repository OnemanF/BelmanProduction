package dk.easv.belman.BE;

public class UploadEntry {
    private int id;
    private String orderNumber;
    private String imagePath;
    private String status; // "pending", "approved", "rejected"
    private String uploadedBy;
    private String uploadDate;
    private String approvedBy;
    private String approvalDate;
    private String imageType;

    public UploadEntry(int id, String orderNumber, String imagePath, String status, String uploadedBy, String uploadDate, String approvedBy, String approvalDate, String imageType) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.imagePath = imagePath;
        this.status = status;
        this.uploadedBy = uploadedBy;
        this.uploadDate = uploadDate;
        this.approvedBy = approvedBy;
        this.approvalDate = approvalDate;
        this.imageType = imageType;
    }

    public int getId() { return id; }
    public String getOrderNumber() { return orderNumber; }
    public String getImagePath() { return imagePath; }
    public String getStatus() { return status; }
    public String getUploadedBy() { return uploadedBy; }
    public String getUploadDate() { return uploadDate; }
    public String getApprovedBy() {return approvedBy;}
    public void setApprovedBy(String approvedBy) {this.approvedBy = approvedBy;}
    public String getApprovalDate() {return approvalDate;}
    public void setApprovalDate(String approvalDate) {this.approvalDate = approvalDate;}
    public String getImageType() {return imageType;}
    public void setImageType(String imageType) {this.imageType = imageType;}

    public void setId(int id) { this.id = id; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public void setStatus(String status) { this.status = status; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }
    public void setUploadDate(String uploadDate) { this.uploadDate = uploadDate; }

    @Override
    public String toString() {
        return "Order #" + orderNumber + " - " + uploadedBy + " (" + uploadDate + ")";
    }
}
