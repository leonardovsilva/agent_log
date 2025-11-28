package com.assistant.service;

import java.sql.*;
import java.io.File;
import java.io.IOException;

public class DatabaseService {
    private static final String DB_URL = "jdbc:sqlite:history.db";

    public DatabaseService() {
        initDb();
    }

    private void initDb() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS analysis_history (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                         "prompt TEXT," +
                         "response TEXT," +
                         "created_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
                         ")";
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }

    public String getHistory(String prompt) {
        String sql = "SELECT response FROM analysis_history WHERE prompt = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, prompt);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("response");
            }
        } catch (SQLException e) {
            System.err.println("Error getting history: " + e.getMessage());
        }
        return null;
    }

    public void saveHistory(String prompt, String response) {
        String sql = "INSERT INTO analysis_history(prompt, response) VALUES(?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, prompt);
            pstmt.setString(2, response);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving history: " + e.getMessage());
        }
    }
}
