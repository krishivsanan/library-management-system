
package com.library.dao;

import com.library.model.IssuedBook;
import com.library.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IssueDAO {

    // 1. ISSUE BOOK
    public boolean issueBook(IssuedBook issue) {
        if (issue == null || issue.getBookId() <= 0 || issue.getMemberId() <= 0
                || issue.getIssueDate() == null || issue.getDueDate() == null
                || issue.getDueDate().isBefore(issue.getIssueDate())) {
            System.out.println("Invalid issue details or dates.");
            return false;
        }

        String bookQuery =
                "SELECT available_copies FROM books WHERE book_id=? FOR UPDATE";

        String memberQuery =
                "SELECT status FROM members WHERE member_id=?";

        String issueQuery =
                "INSERT INTO issued_books " +
                        "(book_id, member_id, issue_date, due_date, status) " +
                        "VALUES (?, ?, ?, ?, 'ISSUED')";

        String updateQuery =
                "UPDATE books SET available_copies = available_copies - 1 " +
                        "WHERE book_id=?";

        try (Connection con = DBConnection.getConnection()) {

            con.setAutoCommit(false);

            try {
                // Check book availability
                try (PreparedStatement ps = con.prepareStatement(bookQuery)) {

                    ps.setInt(1, issue.getBookId());

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next() || rs.getInt("available_copies") <= 0) {
                            con.rollback();
                            return false;
                        }
                    }
                }

                // Check member status
                try (PreparedStatement ps = con.prepareStatement(memberQuery)) {

                    ps.setInt(1, issue.getMemberId());

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next() ||
                                !"ACTIVE".equalsIgnoreCase(rs.getString("status"))) {
                            con.rollback();
                            return false;
                        }
                    }
                }

                // Insert issue record
                try (PreparedStatement ps = con.prepareStatement(issueQuery)) {

                    ps.setInt(1, issue.getBookId());
                    ps.setInt(2, issue.getMemberId());
                    ps.setDate(3, Date.valueOf(issue.getIssueDate()));
                    ps.setDate(4, Date.valueOf(issue.getDueDate()));

                    if (ps.executeUpdate() == 0) {
                        con.rollback();
                        return false;
                    }
                }

                // Decrease available copies
                try (PreparedStatement ps = con.prepareStatement(updateQuery)) {

                    ps.setInt(1, issue.getBookId());
                    if (ps.executeUpdate() == 0) {
                        con.rollback();
                        return false;
                    }
                }

                con.commit();
                return true;

            } catch (SQLException e) {
                con.rollback();
                throw e;
            }

        } catch (SQLException e) {
            System.out.println("Error issuing book: " + e.getMessage());
            return false;
        }
    }


    // Book returns are handled by IssueService so fine generation and inventory updates stay atomic.

    // 2. DISPLAY ALL ISSUED BOOKS
    public List<IssuedBook> displayIssuedBooks() {

        List<IssuedBook> issues = new ArrayList<>();

        String query = "SELECT * FROM issued_books";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                IssuedBook issue = new IssuedBook();

                issue.setIssueId(rs.getInt("issue_id"));
                issue.setBookId(rs.getInt("book_id"));
                issue.setMemberId(rs.getInt("member_id"));
                issue.setIssueDate(
                        rs.getDate("issue_date").toLocalDate()
                );
                issue.setDueDate(
                        rs.getDate("due_date").toLocalDate()
                );

                Date returned = rs.getDate("return_date");

                if (returned != null) {
                    issue.setReturnDate(returned.toLocalDate());
                }

                issue.setStatus(rs.getString("status"));

                issues.add(issue);
            }

        } catch (SQLException e) {
            System.out.println("Error displaying issued books: " + e.getMessage());
        }

        return issues;
    }
}
