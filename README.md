# VULN-1 / FIX-1 - SQL injection in doctor search

v1 and v2 are separate apps (the course requires separate folders or branches).
Copy each `src/` into its own Spring Boot project (v1 and v2).

## Run v1 (vulnerable, localhost only)
    cd v1-vulnerable && ./mvnw spring-boot:run        # http://localhost:8081

## Run v2 (secure)
    mysql -u root -p < secure-v2/db/least-privilege-user.sql   # once; edit the password first
    export DB_USER=ju_portal_app
    export DB_PASS='your-strong-password'
    cd secure-v2 && ./mvnw spring-boot:run        # http://localhost:8082

Markers: VULN-1 / FIX-1 = SQL injection; VULN-10 / FIX-10 = information disclosure through errors
(numbers match the table in the project guideline).
