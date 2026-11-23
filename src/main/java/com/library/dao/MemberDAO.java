
package com.library.dao;

import com.library.model.Member;
import com.library.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MemberDAO {

    // Convert ResultSet row into Member object
    private Member mapMember(ResultSet rs) throws SQLException {

        Member member = new Member();

        member.setMemberId(rs.getInt("member_id"));
        member.setName(rs.getString("name"));
        member.setEmail(rs.getString("email"));
        member.setPhone(rs.getString("phone"));
        member.setAddress(rs.getString("address"));
        member.setRegistrationDate(
                rs.getDate("registration_date").toLocalDate()
        );
        member.setStatus(rs.getString("status"));

        return member;
    }


    // 1. ADD MEMBER
    public boolean addMember(Member member) {
        if (!validMember(member, false)) {
            System.out.println("Invalid member details. Name, valid email and registration date are required.");
            return false;
        }

        String query = "INSERT INTO members " +
                "(name, email, phone, address, registration_date, status) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, member.getName().trim());
            ps.setString(2, member.getEmail().trim());
            String phone = (member.getPhone() == null || member.getPhone().isBlank()) ? null : member.getPhone().trim();
            ps.setString(3, phone);
            String address = (member.getAddress() == null || member.getAddress().isBlank()) ? null : member.getAddress().trim();
            ps.setString(4, address);
            ps.setDate(5, Date.valueOf(member.getRegistrationDate()));
            ps.setString(6, member.getStatus());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error adding member: " + e.getMessage());
            return false;
        }
    }


    // 2. DISPLAY ALL MEMBERS
    public List<Member> displayAllMembers() {

        List<Member> members = new ArrayList<>();

        String query = "SELECT * FROM members";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                members.add(mapMember(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error displaying members: " + e.getMessage());
        }

        return members;
    }


    // 3. SEARCH MEMBER BY NAME, EMAIL OR PHONE
    public List<Member> searchMember(String keyword) {

        List<Member> members = new ArrayList<>();

        String query = "SELECT * FROM members " +
                "WHERE name LIKE ? OR email LIKE ? OR phone LIKE ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            String search = "%" + keyword + "%";

            ps.setString(1, search);
            ps.setString(2, search);
            ps.setString(3, search);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    members.add(mapMember(rs));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error searching members: " + e.getMessage());
        }

        return members;
    }


    // 4. UPDATE MEMBER
    public boolean updateMember(Member member) {
        if (!validMember(member, true)) {
            System.out.println("Invalid member details.");
            return false;
        }

        String query = "UPDATE members SET name=?, email=?, " +
                "phone=?, address=?, status=? WHERE member_id=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, member.getName().trim());
            ps.setString(2, member.getEmail().trim());
            String phone = (member.getPhone() == null || member.getPhone().isBlank()) ? null : member.getPhone().trim();
            ps.setString(3, phone);
            String address = (member.getAddress() == null || member.getAddress().isBlank()) ? null : member.getAddress().trim();
            ps.setString(4, address);
            ps.setString(5, member.getStatus());
            ps.setInt(6, member.getMemberId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error updating member: " + e.getMessage());
            return false;
        }
    }


    // 5. DELETE MEMBER
    public boolean deleteMember(int memberId) {

        String query = "DELETE FROM members WHERE member_id=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setInt(1, memberId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error deleting member: " + e.getMessage());
            return false;
        }
    }
    private boolean validMember(Member member, boolean requireId) {
        return member != null && (!requireId || member.getMemberId() > 0)
                && member.getName() != null && !member.getName().isBlank()
                && member.getEmail() != null
                && member.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
                && (member.getPhone() == null || member.getPhone().isBlank() || member.getPhone().matches("^[0-9]{7,15}$"))
                && (!requireId ? member.getRegistrationDate() != null : true)
                && ("ACTIVE".equals(member.getStatus()) || "INACTIVE".equals(member.getStatus()));
    }
}
