package dk.easv.belman.BE;

public class Order {
    private int id;
    private String orderNumber;
    private String customerName;
    private String createdDate;
    private String status;

    public Order(int id, String orderNumber, String customerName, String createdDate, String status) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.customerName = customerName;
        this.createdDate = createdDate;
        this.status = status;
    }
    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
