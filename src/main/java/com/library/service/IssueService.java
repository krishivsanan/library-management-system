
package com.library.service;

import com.library.dao.FineDAO;
import com.library.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.time.LocalDate;

public class IssueService {

    private final FineDAO fineDAO = new FineDAO();

    // RETURN BOOK AND GENERATE FINE IN ONE TRANSACTION
    public boolean returnBook(int issueId, LocalDate returnDate) {

        String selectQuery =
                "SELECT book_id, issue_date, due_date, status " +
                        "FROM issued_books WHERE issue_id = ? FOR UPDATE";

        String returnQuery =
                "UPDATE issued_books " +
                        "SET return_date = ?, status = 'RETURNED' " +
                        "WHERE issue_id = ? AND status = 'ISSUED'";

        String bookQuery =
                "UPDATE books " +
                        "SET available_copies = available_copies + 1 " +
                        "WHERE book_id = ?";

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {

                int bookId;
                LocalDate issueDate;
                LocalDate dueDate;

                // STEP 1: FETCH ISSUE RECORD
                try (PreparedStatement ps =
                             conn.prepareStatement(selectQuery)) {

                    ps.setInt(1, issueId);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next()) {
                            System.out.println("Issue record not found.");
                            conn.rollback();
                            return false;
                        }

                        if (!"ISSUED".equals(rs.getString("status"))) {
                            System.out.println("Book is already returned.");
                            conn.rollback();
                            return false;
                        }

                        bookId = rs.getInt("book_id");
                        issueDate = rs.getDate("issue_date").toLocalDate();
                        dueDate = rs.getDate("due_date").toLocalDate();
                    }
                }

                if (returnDate == null || returnDate.isBefore(issueDate)) {
                    System.out.println("Return date cannot be before the issue date.");
                    conn.rollback();
                    return false;
                }

                // STEP 2: UPDATE ISSUE STATUS
                try (PreparedStatement ps =
                             conn.prepareStatement(returnQuery)) {

                    ps.setDate(1, Date.valueOf(returnDate));
                    ps.setInt(2, issueId);

                    if (ps.executeUpdate() == 0) {
                        throw new SQLException("Could not update issue record.");
                    }
                }

                // STEP 3: INCREASE AVAILABLE BOOK COPIES
                try (PreparedStatement ps =
                             conn.prepareStatement(bookQuery)) {

                    ps.setInt(1, bookId);

                    if (ps.executeUpdate() == 0) {
                        throw new SQLException("Could not update book copies.");
                    }
                }

                // STEP 4: GENERATE FINE IF BOOK IS OVERDUE
                boolean fineGenerated = fineDAO.createFine(
                        conn, issueId, dueDate, returnDate
                );

                // STEP 5: COMMIT TRANSACTION
                conn.commit();

                System.out.println("Book returned successfully.");

                if (fineGenerated) {
                    System.out.println("Late fine generated successfully.");
                } else {
                    System.out.println("No fine applicable.");
                }

                return true;

            } catch (SQLException e) {

                conn.rollback();
                System.out.println("Transaction failed: " + e.getMessage());
                return false;

            }

        } catch (SQLException e) {

            System.out.println("Database error: " + e.getMessage());
            return false;
        }
    }
}
