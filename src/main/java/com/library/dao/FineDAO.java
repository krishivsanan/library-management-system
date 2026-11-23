
package com.library.dao;

import com.library.model.Fine;
import com.library.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import com.library.service.FineCalculator;

public class FineDAO {

    // 1. CREATE FINE WITHIN AN EXISTING TRANSACTION
    public boolean createFine(Connection conn, int issueId,
                              LocalDate dueDate, LocalDate returnDate)
            throws SQLException {

        BigDecimal fineAmount = FineCalculator.calculate(dueDate, returnDate);
        if (fineAmount.compareTo(BigDecimal.ZERO) == 0) return false;

        String query = "INSERT INTO fines " +
                "(issue_id, fine_amount, payment_status) " +
                "VALUES (?, ?, 'PENDING')";

        try (PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, issueId);
            ps.setBigDecimal(2, fineAmount);

            return ps.executeUpdate() > 0;
        }
    }

    // 2. FETCH ALL FINES
    public java.util.List<Fine> getAllFines() {

        java.util.List<Fine> fines = new java.util.ArrayList<>();
        String query = "SELECT * FROM fines ORDER BY fine_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Date paymentDate = rs.getDate("payment_date");
                Fine fine = new Fine(
                        rs.getInt("fine_id"),
                        rs.getInt("issue_id"),
                        rs.getBigDecimal("fine_amount"),
                        rs.getString("payment_status"),
                        paymentDate == null ? null : paymentDate.toLocalDate()
                );
                fines.add(fine);
            }

        } catch (SQLException e) {
            System.out.println("Error displaying fines: " + e.getMessage());
        }

        return fines;
    }


    // 3. PAY FINE
    public boolean payFine(int fineId) {

        String selectQuery = "SELECT payment_status FROM fines WHERE fine_id = ? FOR UPDATE";
        String updateQuery = "UPDATE fines " +
                "SET payment_status = 'PAID', payment_date = ? " +
                "WHERE fine_id = ? AND payment_status = 'PENDING'";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // Check if fine exists and whether it is already paid
                try (PreparedStatement ps = conn.prepareStatement(selectQuery)) {
                    ps.setInt(1, fineId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            System.out.println("Fine record not found.");
                            conn.rollback();
                            return false;
                        }

                        if ("PAID".equalsIgnoreCase(rs.getString("payment_status"))) {
                            System.out.println("This fine has already been paid.");
                            conn.rollback();
                            return false;
                        }
                    }
                }

                // Update payment status to PAID
                try (PreparedStatement ps = conn.prepareStatement(updateQuery)) {
                    ps.setDate(1, Date.valueOf(LocalDate.now()));
                    ps.setInt(2, fineId);

                    if (ps.executeUpdate() == 0) {
                        conn.rollback();
                        return false;
                    }
                }

                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            System.out.println("Error paying fine: " + e.getMessage());
            return false;
        }
    }
}
