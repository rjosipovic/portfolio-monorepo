-- Check if database exists
SELECT 'CREATE DATABASE "gamification-manager-db"'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'gamification-manager-db')\gexec

-- Check if user exists
DO $$
DECLARE
    pwd text := current_setting('my.gamification_manager_password');
BEGIN
    IF NOT EXISTS (SELECT FROM pg_user WHERE usename = 'gamification-manager-user') THEN
	EXECUTE format('CREATE USER "gamification-manager-user" WITH ENCRYPTED PASSWORD %L', pwd);
    END IF;
END $$;

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE "gamification-manager-db" TO "gamification-manager-user";

-- Connect to the new database to set schema permissions
\c "gamification-manager-db";
GRANT ALL ON SCHEMA public TO "gamification-manager-user";
