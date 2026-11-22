package com.ju.healthportal.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.regex.Pattern;

// v2 - SECURE version
@RestController
@RequestMapping("/api/doctors")
public class DoctorSearchController {

    private static final Logger log = LoggerFactory.getLogger(DoctorSearchController.class);

    // FIX-1: Server-side whitelist validation.
    // Letters (any language, incl. Arabic), spaces, dot, apostrophe, hyphen; 1 to 50 chars.
    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{L} .'-]{1,50}$");

    // FIX-1: No hard-coded credentials. The DataSource is built by Spring Boot from
    // application.properties, which reads DB_USER / DB_PASS from environment variables.
    private final DataSource dataSource;

    public DoctorSearchController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchDoctors(@RequestParam("name") String doctorName) {

        if (doctorName == null || !NAME_PATTERN.matcher(doctorName).matches()) {
            log.warn("SECURITY: rejected doctor search input (failed whitelist validation)");
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid search text. Use letters and spaces only."));
        }

        // FIX-1: Parameterised query - the SQL structure is fixed before user data is bound
        String sql = "SELECT id, name, specialty FROM doctors WHERE name LIKE ? ESCAPE '\\\\'";

        // Escape LIKE wildcards so % and _ are searched literally
        String safeTerm = doctorName.replace("\\", "\\\\")
                                    .replace("%", "\\%")
                                    .replace("_", "\\_");

        List<Map<String, String>> results = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + safeTerm + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, String> doctor = new LinkedHashMap<>();
                    doctor.put("id", rs.getString("id"));
                    doctor.put("name", rs.getString("name"));
                    doctor.put("specialty", rs.getString("specialty"));
                    results.add(doctor);
                }
            }
            return ResponseEntity.ok(results);

        } catch (SQLException e) {
            // FIX-10: Details are logged on the server only; the user gets a generic message
            log.error("Doctor search failed", e);
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Something went wrong. Please try again later."));
        }
    }
}
