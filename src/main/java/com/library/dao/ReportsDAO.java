package com.library.dao;

import com.library.util.DBConnection;
import java.sql.*;

/** Read-only operational reports. */
public class ReportsDAO {
    public void printIssuedReport() {
        String sql = "SELECT i.issue_id,b.title,m.name,i.issue_date,i.due_date,i.status " +
                "FROM issued_books i JOIN books b ON b.book_id=i.book_id " +
                "JOIN members m ON m.member_id=i.member_id WHERE i.status='ISSUED' ORDER BY i.due_date";
        printRows("CURRENTLY ISSUED BOOKS", sql, 6);
    }
    public void printOverdueReport() {
        String sql = "SELECT i.issue_id,b.title,m.name,i.due_date,DATEDIFF(CURRENT_DATE,i.due_date) AS overdue_days " +
                "FROM issued_books i JOIN books b ON b.book_id=i.book_id " +
                "JOIN members m ON m.member_id=i.member_id " +
                "WHERE i.status='ISSUED' AND i.due_date<CURRENT_DATE ORDER BY i.due_date";
        printRows("OVERDUE BOOKS", sql, 5);
    }
    public void printReturnedReport() {
        String sql = "SELECT i.issue_id,b.title,m.name,i.issue_date,i.return_date " +
                "FROM issued_books i JOIN books b ON b.book_id=i.book_id " +
                "JOIN members m ON m.member_id=i.member_id WHERE i.status='RETURNED' ORDER BY i.return_date DESC";
        printRows("RETURNED BOOKS", sql, 5);
    }
    private void printRows(String heading, String sql, int columns) {
        System.out.println("\n========== " + heading + " ==========");
        try (Connection c=DBConnection.getConnection(); Statement st=c.createStatement(); ResultSet rs=st.executeQuery(sql)) {
            boolean found=false;
            while(rs.next()) { found=true; StringBuilder line=new StringBuilder();
                for(int i=1;i<=columns;i++){ if(i>1) line.append(" | "); line.append(rs.getString(i)); }
                System.out.println(line);
            }
            if(!found) System.out.println("No records found.");
        } catch(SQLException e) { System.out.println("Could not generate report: "+e.getMessage()); }
    }
}
