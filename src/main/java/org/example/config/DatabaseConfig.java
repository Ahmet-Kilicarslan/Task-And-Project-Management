package org.example.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.SQLException;


public class DatabaseConfig {

    final private static HikariDataSource dataSource;


    static {

        try {
            Dotenv dotenv = Dotenv.load();


            //Connection URL
            String jdbcUrl = String.format(
                    "jdbc:sqlserver://%s:%s;databaseName=%s;encrypt=false;trustServerCertificate=true",
                    dotenv.get("DB_HOST"),
                    dotenv.get("DB_PORT"),
                    dotenv.get("DB_NAME")
            );

            //Configure Pool
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(dotenv.get("DB_USER"));
            config.setPassword(dotenv.get("DB_PASSWORD"));
            config.setMaximumPoolSize(10);


            //Create Pool

            dataSource = new HikariDataSource(config);


        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database pool ", e);
        }


    }


    //get connection from pool
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    //close pool

    public static void closeConnection() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    private DatabaseConfig() {
    }

}