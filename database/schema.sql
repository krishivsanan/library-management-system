-- Library Management System schema (MySQL 8.0+)
-- Safe for a fresh setup. Existing tables are not altered or dropped.
CREATE DATABASE IF NOT EXISTS library_management;
USE library_management;

CREATE TABLE IF NOT EXISTS books (
  book_id INT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(150) NOT NULL,
  author VARCHAR(100) NOT NULL,
  category VARCHAR(60),
  isbn VARCHAR(20) NOT NULL UNIQUE,
  total_copies INT NOT NULL,
  available_copies INT NOT NULL,
  CONSTRAINT chk_books_total CHECK (total_copies >= 0),
  CONSTRAINT chk_books_available CHECK (available_copies >= 0 AND available_copies <= total_copies)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS members (
  member_id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(100) NOT NULL UNIQUE,
  phone VARCHAR(15) UNIQUE,
  address VARCHAR(255),
  registration_date DATE NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  CONSTRAINT chk_member_status CHECK (status IN ('ACTIVE','INACTIVE'))
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS issued_books (
  issue_id INT PRIMARY KEY AUTO_INCREMENT,
  book_id INT NOT NULL,
  member_id INT NOT NULL,
  issue_date DATE NOT NULL,
  due_date DATE NOT NULL,
  return_date DATE NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'ISSUED',
  CONSTRAINT fk_issue_book FOREIGN KEY (book_id) REFERENCES books(book_id) ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_issue_member FOREIGN KEY (member_id) REFERENCES members(member_id) ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT chk_issue_status CHECK (status IN ('ISSUED','RETURNED')),
  CONSTRAINT chk_issue_dates CHECK (due_date >= issue_date AND (return_date IS NULL OR return_date >= issue_date)),
  INDEX idx_issue_status (status), INDEX idx_issue_due_date (due_date)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS fines (
  fine_id INT PRIMARY KEY AUTO_INCREMENT,
  issue_id INT NOT NULL UNIQUE,
  fine_amount DECIMAL(10,2) NOT NULL,
  payment_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  payment_date DATE NULL,
  CONSTRAINT fk_fine_issue FOREIGN KEY (issue_id) REFERENCES issued_books(issue_id) ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT chk_fine_amount CHECK (fine_amount >= 0),
  CONSTRAINT chk_fine_payment_status CHECK (payment_status IN ('PENDING','PAID'))
) ENGINE=InnoDB;
