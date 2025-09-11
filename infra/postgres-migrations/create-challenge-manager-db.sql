-- Check if database exists
SELECT 'CREATE DATABASE "challenge-manager-db"'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'challenge-manager-db')\gexec

-- Check if user exists
DO $$
DECLARE
    pwd text := current_setting('my.challenge_manager_password');
BEGIN
    IF NOT EXISTS (SELECT FROM pg_user WHERE usename = 'challenge-manager-user') THEN
	EXECUTE format('CREATE USER "challenge-manager-user" WITH ENCRYPTED PASSWORD %L', pwd);
    END IF;
END $$;

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE "challenge-manager-db" TO "challenge-manager-user";

-- Connect to the new database to set schema permissions
\c "challenge-manager-db";
GRANT ALL ON SCHEMA public TO "challenge-manager-user";
