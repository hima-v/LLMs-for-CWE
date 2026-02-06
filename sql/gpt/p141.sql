-- config.sql

-- Store config (DON'T store plaintext passwords in real systems; use secrets manager / vault)
CREATE TABLE IF NOT EXISTS app_config (
  config_key   TEXT PRIMARY KEY,
  config_value TEXT NOT NULL
);

INSERT INTO app_config (config_key, config_value) VALUES
  ('server_name',   'localhost'),
  ('database_name', 'appdb'),
  ('database_user', 'appuser')
ON CONFLICT (config_key) DO UPDATE SET config_value = excluded.config_value;

-- A view to read non-sensitive config
CREATE VIEW IF NOT EXISTS app_config_public AS
SELECT config_key, config_value
FROM app_config
WHERE config_key IN ('server_name', 'database_name', 'database_user');
