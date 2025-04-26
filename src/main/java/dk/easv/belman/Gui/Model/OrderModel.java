package dk.easv.belman.Gui.Model;

import dk.easv.belman.BE.Order;
import dk.easv.belman.BLL.OrderBLL;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.SQLException;

public class OrderModel {
    private final ObservableList<Order> orders = FXCollections.observableArrayList();
    private final OrderBLL orderBLL = new OrderBLL();

    public ObservableList<Order> getOrders() {
        return orders;
    }

    public void loadOrders() {
        try {
            orders.setAll(orderBLL.getAllOrders());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addOrder(Order entry) throws SQLException {
        orderBLL.insertOrder(entry);
        loadOrders();
    }
}
