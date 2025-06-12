package com.example.stores.service.impl;

import com.example.stores.config.DatabaseConnection;
import com.example.stores.model.Warranty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WarrantyService {

    private static final Logger logger = Logger.getLogger(WarrantyService.class.getName());

    public Warranty getWarrantyByOrderDetailId(String orderDetailsID) {
        Warranty warranty = null;
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DatabaseConnection.getConnection();
            String sql = "SELECT * FROM Warranty WHERE orderDetailsID = ?";
            statement = connection.prepareStatement(sql);
            statement.setString(1, orderDetailsID);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                warranty = new Warranty();
                warranty.setWarrantyID(resultSet.getInt("warrantyID"));
                warranty.setOrderDetailsID(resultSet.getString("orderDetailsID"));
                warranty.setWarrantyType(resultSet.getString("warrantyType"));
                warranty.setStartDate(resultSet.getDate("startDate"));
                warranty.setEndDate(resultSet.getDate("endDate"));
                warranty.setNotes(resultSet.getString("notes"));
                // Status là computed column trong DB, nên có thể lấy nếu cần
                warranty.setStatus(resultSet.getString("status"));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving warranty information", e);
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Error closing database resources", e);
            }
        }

        return warranty;
    }
}