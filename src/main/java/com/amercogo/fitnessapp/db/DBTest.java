package com.amercogo.fitnessapp.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DBTest {
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT 1");
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                System.out.println("✅ Connected! SELECT 1 = " + rs.getInt(1));
            }
        } catch (Exception e) {
            System.err.println("❌ Connection failed!");
            e.printStackTrace();
        }
    }
}
