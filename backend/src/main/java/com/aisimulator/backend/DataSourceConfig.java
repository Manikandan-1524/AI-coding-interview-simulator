package com.aisimulator.backend;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * DataSource configuration for Railway deployment.
 * 
 * Railway provides DATABASE_URL in format: postgresql://user:pass@host:port/dbname
 * Spring Boot expects: jdbc:postgresql://host:port/dbname with separate username/password
 * 
 * This configuration:
 * 1. Detects if DATABASE_URL environment variable is set (Railway)
 * 2. Parses the URL to extract connection details
 * 3. Configures HikariCP connection pool
 * 4. Falls back to local MySQL if DATABASE_URL not set
 */
@Configuration
public class DataSourceConfig {

    /**
     * Configuration for production profile (Railway deployment)
     */
    @Configuration
    @Profile("production")
    static class ProductionDataSourceConfig {

        /**
         * Creates a PostgreSQL DataSource configured for Railway.
         * Parses DATABASE_URL environment variable and creates proper JDBC URL.
         */
        @Bean
        @Primary
        public DataSource productionDataSource() {
            String databaseUrl = System.getenv("DATABASE_URL");
            
            if (databaseUrl == null || databaseUrl.isEmpty()) {
                throw new IllegalStateException(
                    "DATABASE_URL environment variable is not set. " +
                    "Railway should auto-provide this for PostgreSQL service."
                );
            }

            try {
                // Parse Railway's DATABASE_URL format: postgresql://user:pass@host:port/dbname
                URI dbUri = new URI(databaseUrl);
                
                String username = dbUri.getUserInfo().split(":")[0];
                String password = dbUri.getUserInfo().split(":")[1];
                String host = dbUri.getHost();
                int port = dbUri.getPort() != -1 ? dbUri.getPort() : 5432;
                String database = dbUri.getPath().substring(1); // Remove leading slash

                // Construct proper JDBC URL for PostgreSQL
                String jdbcUrl = String.format(
                    "jdbc:postgresql://%s:%d/%s",
                    host, port, database
                );

                // Configure HikariCP connection pool
                HikariConfig hikariConfig = new HikariConfig();
                hikariConfig.setJdbcUrl(jdbcUrl);
                hikariConfig.setUsername(username);
                hikariConfig.setPassword(password);
                hikariConfig.setMaximumPoolSize(5);
                hikariConfig.setMinimumIdle(2);
                hikariConfig.setConnectionTimeout(10000);
                hikariConfig.setIdleTimeout(600000);
                hikariConfig.setMaxLifetime(1800000);
                hikariConfig.setAutoCommit(true);
                
                // Enable connection testing
                hikariConfig.setConnectionTestQuery("SELECT 1");

                System.out.println("✓ Configured PostgreSQL DataSource for Railway");
                System.out.println("  Database: " + database);
                System.out.println("  Host: " + host + ":" + port);
                
                return new HikariDataSource(hikariConfig);

            } catch (URISyntaxException e) {
                throw new IllegalStateException(
                    "Failed to parse DATABASE_URL: " + databaseUrl, e
                );
            } catch (Exception e) {
                throw new IllegalStateException(
                    "Failed to configure PostgreSQL DataSource for Railway: " + e.getMessage(), e
                );
            }
        }
    }

    /**
     * Configuration for default/local profile (local MySQL development)
     */
    @Configuration
    @Profile("!production")
    static class LocalDataSourceConfig {

        /**
         * Creates a MySQL DataSource for local development.
         * Uses hardcoded localhost values configured in application.properties
         */
        @Bean
        @Primary
        public DataSource localDataSource(
                org.springframework.boot.autoconfigure.jdbc.DataSourceProperties properties) {
            System.out.println("✓ Configured MySQL DataSource for local development");
            System.out.println("  Database: " + properties.getUrl());
            
            return DataSourceBuilder.create()
                    .driverClassName(properties.getDriverClassName())
                    .url(properties.getUrl())
                    .username(properties.getUsername())
                    .password(properties.getPassword())
                    .build();
        }
    }
}
