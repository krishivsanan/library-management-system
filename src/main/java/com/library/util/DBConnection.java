package com.library.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL = valueOrDefault(
            "LIBRARY_DB_URL",
            "jdbc:mysql://localhost:3306/library_management"
    );

    private static final String USER = valueOrDefault("LIBRARY_DB_USER", "root");
    private static final String PASSWORD = System.getenv("LIBRARY_DB_PASSWORD");

    private static String valueOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    public static Connection getConnection() throws SQLException {
        if (PASSWORD == null || PASSWORD.isBlank()) {
            throw new SQLException(
                    "Database password is not configured. Set the LIBRARY_DB_PASSWORD environment variable."
            );
        }

        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
