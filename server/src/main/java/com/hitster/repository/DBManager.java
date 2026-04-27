package com.hitster.repository;

import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;

public class DBManager {
    final static String CONFIG_FILE = "application.yml";

    private static Connection connection = null;

    @SuppressWarnings("unchecked")
    public static Connection connect() {
        try {
            if (connection == null || connection.isClosed()) {

                Yaml yaml = new Yaml();
                InputStream inputStream = DBManager.class.getClassLoader()
                        .getResourceAsStream(CONFIG_FILE);

                Map<String, Object> config = yaml.load(inputStream);
                Map<String, Object> dbConfig = (Map<String, Object>) config.get("database");

                String host = (String) dbConfig.get("host");
                int port = (Integer) dbConfig.get("port");
                String name = (String) dbConfig.get("name");
                String user = (String) dbConfig.get("user");
                String password = (String) dbConfig.get("password");

                Map<String, Object> sslConfig = (Map<String, Object>) dbConfig.get("ssl");
                String sslMode = (String) sslConfig.get("mode");
                String caCertPath = (String) sslConfig.get("ca_cert");

                String dbUrl = String.format("jdbc:mysql://%s:%d/%s", host, port, name);

                Properties connectionProps = new Properties();
                connectionProps.put("user", user);
                connectionProps.put("password", password);
                connectionProps.put("sslMode", sslMode);
                connectionProps.put("useUnicode", "true");
                connectionProps.put("characterEncoding", "UTF-8");
                connectionProps.put("connectionCollation", "utf8mb4_unicode_ci");

                if (!"DISABLED".equalsIgnoreCase(sslMode) && caCertPath != null && !caCertPath.isEmpty()) {
                    connectionProps.put("trustCertificateKeyStoreUrl", "file:" + caCertPath);
                    connectionProps.put("trustCertificateKeyStoreType", "PEM");
                }

                connection = DriverManager.getConnection(dbUrl, connectionProps);
                configureUtf8mb4(connection);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

        return connection;
    }

    private static void configureUtf8mb4(Connection connection) {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci");
            stmt.execute("ALTER TABLE Songs CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        } catch (SQLException e) {
            System.err.println("Could not verify utf8mb4 database encoding: " + e.getMessage());
        }
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error while closing the connection, " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        connect();
    }
}
