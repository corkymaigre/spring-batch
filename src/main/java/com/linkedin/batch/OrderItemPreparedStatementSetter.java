package com.linkedin.batch;

import org.springframework.batch.item.database.ItemPreparedStatementSetter;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class OrderItemPreparedStatementSetter implements ItemPreparedStatementSetter<Order> {

    @Override
    public void setValues(Order order, PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setLong(1, order.getOrderId());
        preparedStatement.setString(2, order.getFirstName());
        preparedStatement.setString(3, order.getLastName());
        preparedStatement.setString(4, order.getEmail());
        preparedStatement.setString(5, order.getItemId());
        preparedStatement.setString(6, order.getItemName());
        preparedStatement.setBigDecimal(7, order.getCost());
        preparedStatement.setDate(8, new Date(order.getShipDate().getTime()));
    }
}
