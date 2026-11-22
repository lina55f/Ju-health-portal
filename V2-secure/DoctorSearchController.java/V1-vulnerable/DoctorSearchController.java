package com.ju.healthportal.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.*;

// v1 - INTENTIONALLY VULNERABLE. Lab / localhost use only. Never deploy.
@RestController
@RequestMapping("/api/doctors")
public class DoctorSearchController {

    private static final String DB_URL  = "jdbc:mysql://localhost:3306/ju_health";
    private static final String DB_USER = "root";      // weak on purpose in v1: root account, hard-coded password
    private static final String DB_PASS = "password";

    @GetMapping("/search")
    public ResponseEntity<?> searchDoctors(@RequestParam("name") String doctorName) {
        List<Map<String, String>> results = new ArrayList<>();

        // VULN-1: SQL Injection - user input is concatenated directly into the SQL string
        String sql = "SELECT id, name, specialty, phone FROM doctors WHERE name LIKE '%"
                   + doctorName + "%'";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Map<String, String> doctor = new LinkedHashMap<>();
                doctor.put("id", rs.getString(1));
                doctor.put("name", rs.getString(2));
                doctor.put("specialty", rs.getString(3));
                results.add(doctor);
            }
            return ResponseEntity.ok(results);

        } catch (SQLException e) {
            // VULN-10: Information disclosure through errors
            // The raw SQL, the database error and the SQLState are sent back to the user
            Map<String, String> error = new LinkedHashMap<>();
            error.put("error", e.getMessage());
            error.put("sqlState", e.getSQLState());
            error.put("query", sql);
            return ResponseEntity.status(500).body(error);
        }
    }
}
