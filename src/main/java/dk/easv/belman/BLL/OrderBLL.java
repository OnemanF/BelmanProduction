package dk.easv.belman.BLL;

import dk.easv.belman.BE.Order;
import dk.easv.belman.DAL.OrderDAL;

import java.sql.SQLException;
import java.util.List;

public class OrderBLL {
    private final OrderDAL dal = new OrderDAL();

    public List<Order> getAllOrders() throws SQLException {
        return dal.getAll();
    }

    public void insertOrder(Order entry) throws SQLException {
        dal.insert(entry);
    }
}
