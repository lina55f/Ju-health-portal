-- FIX-1: least-privilege database account for v2 (run once as root on the lab DB)
-- The app no longer connects as root. This user can only read the tables it needs,
-- so even a successful injection could not DROP tables or read other databases.
-- Choose your own strong password; do not commit it to Git.

CREATE USER 'ju_portal_app'@'localhost' IDENTIFIED BY 'CHANGE_ME_strong_password';

GRANT SELECT ON ju_health.doctors TO 'ju_portal_app'@'localhost';
-- Add only what other features need, for example:
-- GRANT SELECT, INSERT, UPDATE ON ju_health.appointments TO 'ju_portal_app'@'localhost';

FLUSH PRIVILEGES;
