package com.library.app;

import com.library.dao.BookDAO;
import com.library.dao.FineDAO;
import com.library.dao.IssueDAO;
import com.library.dao.MemberDAO;
import com.library.dao.ReportsDAO;
import com.library.model.Book;
import com.library.model.IssuedBook;
import com.library.model.Fine;
import com.library.model.Member;
import com.library.service.IssueService;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final Scanner sc = new Scanner(System.in);
    private static final BookDAO bookDAO = new BookDAO();
    private static final MemberDAO memberDAO = new MemberDAO();
    private static final IssueDAO issueDAO = new IssueDAO();
    private static final FineDAO fineDAO = new FineDAO();
    private static final ReportsDAO reportsDAO = new ReportsDAO();
    private static final IssueService issueService = new IssueService();

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n========== LIBRARY MANAGEMENT SYSTEM ==========");
            System.out.println("1. Book Management");
            System.out.println("2. Member Management");
            System.out.println("3. Issue Book");
            System.out.println("4. Return Book");
            System.out.println("5. Display Issued Books");
            System.out.println("6. Fine Management");
            System.out.println("7. Reports");
            System.out.println("0. Exit");
            System.out.print("Enter choice: ");

            switch (readInt()) {
                case 1 -> bookMenu();
                case 2 -> memberMenu();
                case 3 -> issueBook();
                case 4 -> returnBook();
                case 5 -> displayIssuedBooks();
                case 6 -> fineMenu();
                case 7 -> reportsMenu();
                case 0 -> {
                    System.out.println("Thank you for using Library Management System.");
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void bookMenu() {
        System.out.println("\n--- BOOK MANAGEMENT ---");
        System.out.println("1. Add Book");
        System.out.println("2. Display All Books");
        System.out.println("3. Search Book");
        System.out.println("4. Update Book Details");
        System.out.println("5. Delete Book");
        System.out.println("6. Display Available Books");
        System.out.print("Enter choice: ");

        switch (readInt()) {
            case 1 -> {
                Book book = new Book();
                System.out.print("Title: ");
                book.setTitle(sc.nextLine().trim());
                System.out.print("Author: ");
                book.setAuthor(sc.nextLine().trim());
                System.out.print("Category: ");
                book.setCategory(sc.nextLine().trim());
                System.out.print("ISBN: ");
                book.setIsbn(sc.nextLine().trim());
                System.out.print("Total copies: ");
                int copies = readInt();

                if (copies <= 0) {
                    System.out.println("Total copies must be greater than zero.");
                    return;
                }

                book.setTotalCopies(copies);
                if (book.getTitle().isBlank() || book.getAuthor().isBlank() || book.getIsbn().isBlank()) {
                    System.out.println("Title, author and ISBN are required.");
                    return;
                }
                book.setAvailableCopies(copies);
                System.out.println(bookDAO.addBook(book)
                        ? "Book added successfully."
                        : "Book could not be added. Check the ISBN and database error.");
            }
            case 2 -> displayBooks(bookDAO.displayAllBooks(), "LIBRARY BOOKS");
            case 3 -> {
                System.out.print("Enter title, author or category keyword: ");
                displayBooks(bookDAO.searchBook(sc.nextLine().trim()), "SEARCH RESULTS");
            }
            case 4 -> {
                Book book = new Book();
                System.out.print("Book ID: ");
                book.setBookId(readInt());
                System.out.print("New title: ");
                book.setTitle(sc.nextLine().trim());
                System.out.print("New author: ");
                book.setAuthor(sc.nextLine().trim());
                System.out.print("New category: ");
                book.setCategory(sc.nextLine().trim());
                System.out.print("New ISBN: ");
                book.setIsbn(sc.nextLine().trim());
                System.out.print("New total copies: ");
                book.setTotalCopies(readInt());

                System.out.println(bookDAO.updateBook(book)
                        ? "Book details and inventory updated successfully."
                        : "Book update failed. Check the ID, issued-copy limit and database error.");
            }
            case 5 -> {
                System.out.print("Enter Book ID: ");
                System.out.println(bookDAO.deleteBook(readInt())
                        ? "Book deleted successfully."
                        : "Book deletion failed. The book may not exist or may have issue history.");
            }
            case 6 -> displayBooks(bookDAO.displayAvailableBooks(), "AVAILABLE BOOKS");
            default -> System.out.println("Invalid choice.");
        }
    }

    private static void displayBooks(List<Book> books, String heading) {
        if (books.isEmpty()) {
            System.out.println("No books found.");
            return;
        }

        System.out.println("\n========== " + heading + " ==========");
        for (Book book : books) {
            System.out.println("----------------------------");
            System.out.println("Book ID: " + book.getBookId());
            System.out.println("Title: " + book.getTitle());
            System.out.println("Author: " + book.getAuthor());
            System.out.println("Category: " + (book.getCategory() == null ? "N/A" : book.getCategory()));
            System.out.println("ISBN: " + book.getIsbn());
            System.out.println("Total Copies: " + book.getTotalCopies());
            System.out.println("Available Copies: " + book.getAvailableCopies());
        }
    }

    private static void memberMenu() {
        System.out.println("\n--- MEMBER MANAGEMENT ---");
        System.out.println("1. Register Member");
        System.out.println("2. Display Members");
        System.out.println("3. Search Member");
        System.out.println("4. Update Member");
        System.out.println("5. Delete Member");
        System.out.print("Enter choice: ");

        switch (readInt()) {
            case 1 -> {
                Member member = new Member();
                System.out.print("Name: ");
                member.setName(sc.nextLine().trim());
                System.out.print("Email: ");
                member.setEmail(sc.nextLine().trim());
                System.out.print("Phone: ");
                member.setPhone(sc.nextLine().trim());
                System.out.print("Address: ");
                member.setAddress(sc.nextLine().trim());
                member.setRegistrationDate(LocalDate.now());
                member.setStatus("ACTIVE");
                if (member.getName().isBlank() || !member.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                    System.out.println("A name and valid email address are required.");
                    return;
                }
                if (!member.getPhone().isBlank() && !member.getPhone().matches("^[0-9]{7,15}$")) {
                    System.out.println("Phone must contain 7 to 15 digits.");
                    return;
                }

                System.out.println(memberDAO.addMember(member)
                        ? "Member registered successfully."
                        : "Member registration failed. Check email/phone uniqueness and database error.");
            }
            case 2 -> displayMembers(memberDAO.displayAllMembers());
            case 3 -> {
                System.out.print("Enter name, email or phone: ");
                displayMembers(memberDAO.searchMember(sc.nextLine().trim()));
            }
            case 4 -> {
                Member member = new Member();
                System.out.print("Member ID: ");
                member.setMemberId(readInt());
                System.out.print("New name: ");
                member.setName(sc.nextLine().trim());
                System.out.print("New email: ");
                member.setEmail(sc.nextLine().trim());
                System.out.print("New phone: ");
                member.setPhone(sc.nextLine().trim());
                System.out.print("New address: ");
                member.setAddress(sc.nextLine().trim());
                System.out.print("Status (ACTIVE/INACTIVE): ");
                member.setStatus(sc.nextLine().trim().toUpperCase());

                if (member.getName().isBlank() || !member.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                    System.out.println("A name and valid email address are required.");
                    return;
                }
                if (!member.getPhone().isBlank() && !member.getPhone().matches("^[0-9]{7,15}$")) {
                    System.out.println("Phone must contain 7 to 15 digits.");
                    return;
                }
                if (!member.getStatus().equals("ACTIVE")
                        && !member.getStatus().equals("INACTIVE")) {
                    System.out.println("Invalid status. Use ACTIVE or INACTIVE.");
                    return;
                }

                System.out.println(memberDAO.updateMember(member)
                        ? "Member updated successfully."
                        : "Member update failed. Check the ID and database error.");
            }
            case 5 -> {
                System.out.print("Enter Member ID: ");
                System.out.println(memberDAO.deleteMember(readInt())
                        ? "Member deleted successfully."
                        : "Member deletion failed. The member may not exist or may have issue history.");
            }
            default -> System.out.println("Invalid choice.");
        }
    }

    private static void displayMembers(List<Member> members) {
        if (members.isEmpty()) {
            System.out.println("No members found.");
            return;
        }

        System.out.println("\n========== MEMBERS ==========");
        for (Member member : members) {
            System.out.println("----------------------------");
            System.out.println("Member ID: " + member.getMemberId());
            System.out.println("Name: " + member.getName());
            System.out.println("Email: " + member.getEmail());
            System.out.println("Phone: " + (member.getPhone() == null ? "N/A" : member.getPhone()));
            System.out.println("Address: " + (member.getAddress() == null ? "N/A" : member.getAddress()));
            System.out.println("Registration Date: " + member.getRegistrationDate());
            System.out.println("Status: " + member.getStatus());
        }
    }

    private static void issueBook() {
        IssuedBook issue = new IssuedBook();

        System.out.print("Enter Book ID: ");
        issue.setBookId(readInt());
        System.out.print("Enter Member ID: ");
        issue.setMemberId(readInt());

        LocalDate issueDate = LocalDate.now();
        issue.setIssueDate(issueDate);
        issue.setDueDate(issueDate.plusDays(14));
        issue.setStatus("ISSUED");

        System.out.println(issueDAO.issueBook(issue)
                ? "Book issued successfully. Return within 14 days."
                : "Book could not be issued. Check book availability, member status and IDs.");
    }

    private static void returnBook() {
        System.out.print("Enter Issue ID: ");
        int issueId = readInt();
        System.out.print("Return date (YYYY-MM-DD), or press Enter for today: ");
        String enteredDate = sc.nextLine().trim();
        LocalDate returnDate;
        try {
            returnDate = enteredDate.isEmpty() ? LocalDate.now() : LocalDate.parse(enteredDate);
        } catch (java.time.format.DateTimeParseException e) {
            System.out.println("Invalid date. Use YYYY-MM-DD.");
            return;
        }
        issueService.returnBook(issueId, returnDate);
    }

    private static void displayIssuedBooks() {
        List<IssuedBook> issues = issueDAO.displayIssuedBooks();
        if (issues.isEmpty()) {
            System.out.println("No issue records found.");
            return;
        }

        System.out.println("\n========== ISSUE HISTORY ==========");
        for (IssuedBook issue : issues) {
            System.out.println("----------------------------");
            System.out.println("Issue ID: " + issue.getIssueId());
            System.out.println("Book ID: " + issue.getBookId());
            System.out.println("Member ID: " + issue.getMemberId());
            System.out.println("Issue Date: " + issue.getIssueDate());
            System.out.println("Due Date: " + issue.getDueDate());
            System.out.println("Return Date: "
                    + (issue.getReturnDate() == null ? "Not returned" : issue.getReturnDate()));
            System.out.println("Status: " + issue.getStatus());
        }
    }

    private static void fineMenu() {
        System.out.println("\n--- FINE MANAGEMENT ---");
        System.out.println("1. Display All Fines");
        System.out.println("2. Pay Fine");
        System.out.print("Enter choice: ");

        switch (readInt()) {
            case 1 -> displayFines();
            case 2 -> {
                System.out.print("Enter Fine ID: ");
                int fineId = readInt();
                if (fineDAO.payFine(fineId)) {
                    System.out.println("Fine paid successfully.");
                }
            }
            default -> System.out.println("Invalid choice.");
        }
    }

    private static void displayFines() {
        List<Fine> fines = fineDAO.getAllFines();
        if (fines.isEmpty()) {
            System.out.println("No fines found.");
            return;
        }

        System.out.println("\n========== FINES ==========");
        for (Fine fine : fines) {
            System.out.println("----------------------------");
            System.out.println("Fine ID: " + fine.getFineId());
            System.out.println("Issue ID: " + fine.getIssueId());
            System.out.println("Fine Amount: Rs. " + fine.getFineAmount());
            System.out.println("Payment Status: " + fine.getPaymentStatus());
            System.out.println("Payment Date: "
                    + (fine.getPaymentDate() == null ? "Not paid" : fine.getPaymentDate()));
        }
    }

    private static void reportsMenu() {
        System.out.println("\n--- REPORTS ---");
        System.out.println("1. Currently Issued Books");
        System.out.println("2. Overdue Books");
        System.out.println("3. Returned Books");
        System.out.print("Enter choice: ");
        switch (readInt()) {
            case 1 -> reportsDAO.printIssuedReport();
            case 2 -> reportsDAO.printOverdueReport();
            case 3 -> reportsDAO.printReturnedReport();
            default -> System.out.println("Invalid choice.");
        }
    }

    private static int readInt() {
        while (true) {
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Enter a valid number: ");
            }
        }
    }
}
