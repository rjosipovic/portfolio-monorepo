-- Check if database exists
SELECT 'CREATE DATABASE "user-manager-db"'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'user-manager-db')\gexec

-- Check if user exists
DO $$
DECLARE
    pwd text := current_setting('my.user_manager_password');
BEGIN
    IF NOT EXISTS (SELECT FROM pg_user WHERE usename = 'user-manager-user') THEN
	EXECUTE format('CREATE USER "user-manager-user" WITH ENCRYPTED PASSWORD %L', pwd);
    END IF;
END $$;

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE "user-manager-db" TO "user-manager-user";

-- Connect to the new database to set schema permissions
\c "user-manager-db";
GRANT ALL ON SCHEMA public TO "user-manager-user";
