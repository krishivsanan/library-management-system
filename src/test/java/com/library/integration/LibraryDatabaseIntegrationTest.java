package com.library.integration;

import com.library.dao.BookDAO;
import com.library.dao.FineDAO;
import com.library.dao.IssueDAO;
import com.library.dao.MemberDAO;
import com.library.model.Book;
import com.library.model.Fine;
import com.library.model.IssuedBook;
import com.library.model.Member;
import com.library.service.IssueService;
import com.library.util.DBConnection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** Opt-in integration test. Uses the configured DB and cleans up its own rows. */
class LibraryDatabaseIntegrationTest {
    @Test
    void completeBorrowReturnAndFinePaymentFlow() throws Exception {
        Assumptions.assumeTrue("true".equalsIgnoreCase(System.getenv("LIBRARY_DB_TESTS")),
                "Set LIBRARY_DB_TESTS=true to run tests against the configured MySQL database.");

        BookDAO books = new BookDAO();
        MemberDAO members = new MemberDAO();
        IssueDAO issues = new IssueDAO();
        FineDAO fines = new FineDAO();
        IssueService service = new IssueService();

        String suffix = UUID.randomUUID().toString().substring(0, 8);
        Book book = new Book("Integration " + suffix, "Test Author", "Test", "IT" + suffix, 1, 1);
        Member member = new Member(0, "Integration " + suffix, "it" + suffix + "@example.test",
                "9" + suffix.replaceAll("[^0-9]", "0"), "Test Address", LocalDate.now(), "ACTIVE");
        int bookId = 0, memberId = 0, issueId = 0, fineId = 0;
        try {
            assertTrue(books.addBook(book));
            try (Connection c = DBConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement("SELECT book_id FROM books WHERE isbn=?")) {
                ps.setString(1, book.getIsbn());
                try (ResultSet rs = ps.executeQuery()) { assertTrue(rs.next()); bookId = rs.getInt(1); }
            }
            assertTrue(members.addMember(member));
            try (Connection c = DBConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement("SELECT member_id FROM members WHERE email=?")) {
                ps.setString(1, member.getEmail());
                try (ResultSet rs = ps.executeQuery()) { assertTrue(rs.next()); memberId = rs.getInt(1); }
            }

            assertEquals(1, books.searchBook(book.getTitle()).size());
            assertEquals(1, members.searchMember(member.getName()).size());
            IssuedBook issue = new IssuedBook(0, bookId, memberId, LocalDate.now(), LocalDate.now().plusDays(14), null, "ISSUED");
            assertTrue(issues.issueBook(issue));
            try (Connection c = DBConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement("SELECT issue_id FROM issued_books WHERE book_id=? AND member_id=? AND status='ISSUED' ORDER BY issue_id DESC LIMIT 1")) {
                ps.setInt(1, bookId); ps.setInt(2, memberId);
                try (ResultSet rs = ps.executeQuery()) { assertTrue(rs.next()); issueId = rs.getInt(1); }
            }
            assertTrue(books.displayAvailableBooks().isEmpty());
            LocalDate due = LocalDate.now().plusDays(14);
            final int createdIssueId = issueId;
            assertFalse(service.returnBook(createdIssueId, LocalDate.now().minusDays(1)),
                    "A return date before the issue date must be rejected.");
            assertTrue(service.returnBook(createdIssueId, due.plusDays(2)));
            List<Fine> generated = fines.getAllFines().stream().filter(f -> f.getIssueId() == createdIssueId).toList();
            assertEquals(1, generated.size());
            assertEquals(new java.math.BigDecimal("10.00"), generated.get(0).getFineAmount());
            fineId = generated.get(0).getFineId();
            assertTrue(fines.payFine(fineId));
            assertFalse(fines.payFine(fineId));
            final int createdBookId = bookId;
            assertEquals(1, books.displayAvailableBooks().stream().filter(b -> b.getBookId() == createdBookId).count());
        } finally {
            try (Connection c = DBConnection.getConnection()) {
                if (fineId > 0) try (PreparedStatement ps = c.prepareStatement("DELETE FROM fines WHERE fine_id=?")) { ps.setInt(1, fineId); ps.executeUpdate(); }
                if (issueId > 0) try (PreparedStatement ps = c.prepareStatement("DELETE FROM issued_books WHERE issue_id=?")) { ps.setInt(1, issueId); ps.executeUpdate(); }
                if (memberId > 0) try (PreparedStatement ps = c.prepareStatement("DELETE FROM members WHERE member_id=?")) { ps.setInt(1, memberId); ps.executeUpdate(); }
                if (bookId > 0) try (PreparedStatement ps = c.prepareStatement("DELETE FROM books WHERE book_id=?")) { ps.setInt(1, bookId); ps.executeUpdate(); }
            }
        }
    }
}
