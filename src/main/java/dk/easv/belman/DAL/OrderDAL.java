package dk.easv.belman.DAL;

import dk.easv.belman.BE.Order;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAL {
    public List<Order> getAll() throws SQLException {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT * FROM Orders";

        try (Connection conn = DBConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Order(
                        rs.getInt("id"),
                        rs.getString("order_number"),
                        rs.getString("customer_name"),
                        rs.getString("created_date"),
                        rs.getString("status")
                ));
            }
        }
        return list;
    }

    public void insert(Order entry) throws SQLException {
        String sql = "INSERT INTO Orders (order_number, customer_name, status) VALUES (?, ?, ?)";
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entry.getOrderNumber());
            ps.setString(2, entry.getCustomerName());
            ps.setString(3, entry.getStatus());
            ps.executeUpdate();
        }
    }
}
