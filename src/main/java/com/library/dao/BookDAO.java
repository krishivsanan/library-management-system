
package com.library.dao;

import com.library.model.Book;
import com.library.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {

    // 1. ADD BOOK
    public boolean addBook(Book book) {
        if (book == null || blank(book.getTitle()) || blank(book.getAuthor())
                || blank(book.getIsbn()) || book.getTotalCopies() <= 0
                || book.getAvailableCopies() != book.getTotalCopies()) {
            System.out.println("Invalid book details or copy count.");
            return false;
        }

        String query = "INSERT INTO books " +
                "(title, author, category, isbn, total_copies, available_copies) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, book.getTitle().trim());
            ps.setString(2, book.getAuthor().trim());
            ps.setString(3, blank(book.getCategory()) ? null : book.getCategory().trim());
            ps.setString(4, book.getIsbn().trim());
            ps.setInt(5, book.getTotalCopies());
            ps.setInt(6, book.getAvailableCopies());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error adding book: " + e.getMessage());
            return false;
        }
    }


    // 2. DISPLAY ALL BOOKS
    public List<Book> displayAllBooks() {

        List<Book> books = new ArrayList<>();

        String query = "SELECT * FROM books";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Book book = new Book();

                book.setBookId(rs.getInt("book_id"));
                book.setTitle(rs.getString("title"));
                book.setAuthor(rs.getString("author"));
                book.setCategory(rs.getString("category"));
                book.setIsbn(rs.getString("isbn"));
                book.setTotalCopies(rs.getInt("total_copies"));
                book.setAvailableCopies(rs.getInt("available_copies"));

                books.add(book);
            }

        } catch (SQLException e) {
            System.out.println("Error displaying books: " + e.getMessage());
        }

        return books;
    }


    // 3. SEARCH BOOK BY TITLE, AUTHOR OR CATEGORY
    public List<Book> searchBook(String keyword) {

        List<Book> books = new ArrayList<>();

        String query = "SELECT * FROM books " +
                "WHERE title LIKE ? OR author LIKE ? OR category LIKE ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            String search = "%" + keyword + "%";

            ps.setString(1, search);
            ps.setString(2, search);
            ps.setString(3, search);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    Book book = new Book();

                    book.setBookId(rs.getInt("book_id"));
                    book.setTitle(rs.getString("title"));
                    book.setAuthor(rs.getString("author"));
                    book.setCategory(rs.getString("category"));
                    book.setIsbn(rs.getString("isbn"));
                    book.setTotalCopies(rs.getInt("total_copies"));
                    book.setAvailableCopies(rs.getInt("available_copies"));

                    books.add(book);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error searching books: " + e.getMessage());
        }

        return books;
    }


    // 4. UPDATE BOOK DETAILS AND TOTAL INVENTORY SAFELY
    public boolean updateBook(Book book) {
        if (book == null || book.getBookId() <= 0 || blank(book.getTitle())
                || blank(book.getAuthor()) || blank(book.getIsbn()) || book.getTotalCopies() < 0) {
            System.out.println("Invalid book details.");
            return false;
        }

        String selectQuery =
                "SELECT total_copies, available_copies FROM books WHERE book_id = ? FOR UPDATE";

        String updateQuery =
                "UPDATE books SET title=?, author=?, category=?, isbn=?, " +
                        "total_copies=?, available_copies=? WHERE book_id=?";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            try {
                int issuedCopies;
                try (PreparedStatement ps = con.prepareStatement(selectQuery)) {
                    ps.setInt(1, book.getBookId());

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            con.rollback();
                            System.out.println("Book ID not found.");
                            return false;
                        }

                        issuedCopies = rs.getInt("total_copies")
                                - rs.getInt("available_copies");
                    }
                }

                if (book.getTotalCopies() < issuedCopies) {
                    con.rollback();
                    System.out.println("Total copies cannot be less than the number of copies currently issued ("
                            + issuedCopies + ").");
                    return false;
                }

                if (book.getTotalCopies() < 0) {
                    con.rollback();
                    System.out.println("Total copies cannot be negative.");
                    return false;
                }

                int newAvailableCopies = book.getTotalCopies() - issuedCopies;

                try (PreparedStatement ps = con.prepareStatement(updateQuery)) {
                    ps.setString(1, book.getTitle().trim());
                    ps.setString(2, book.getAuthor().trim());
                    ps.setString(3, blank(book.getCategory()) ? null : book.getCategory().trim());
                    ps.setString(4, book.getIsbn().trim());
                    ps.setInt(5, book.getTotalCopies());
                    ps.setInt(6, newAvailableCopies);
                    ps.setInt(7, book.getBookId());

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
            System.out.println("Error updating book: " + e.getMessage());
            return false;
        }
    }


    // 5. DELETE BOOK
    public boolean deleteBook(int bookId) {

        String query = "DELETE FROM books WHERE book_id=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setInt(1, bookId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error deleting book: " + e.getMessage());
            return false;
        }
    }


    // 6. DISPLAY AVAILABLE BOOKS
    public List<Book> displayAvailableBooks() {

        List<Book> books = new ArrayList<>();

        String query = "SELECT * FROM books WHERE available_copies > 0";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Book book = new Book();

                book.setBookId(rs.getInt("book_id"));
                book.setTitle(rs.getString("title"));
                book.setAuthor(rs.getString("author"));
                book.setCategory(rs.getString("category"));
                book.setIsbn(rs.getString("isbn"));
                book.setTotalCopies(rs.getInt("total_copies"));
                book.setAvailableCopies(rs.getInt("available_copies"));

                books.add(book);
            }

        } catch (SQLException e) {
            System.out.println("Error displaying available books: " + e.getMessage());
        }

        return books;
    }
    private boolean blank(String value) { return value == null || value.isBlank(); }
}
