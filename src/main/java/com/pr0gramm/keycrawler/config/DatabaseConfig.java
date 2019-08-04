package com.pr0gramm.keycrawler.config;

import com.pr0gramm.keycrawler.config.properties.DatabaseProperties;
import io.r2dbc.h2.H2ConnectionConfiguration;
import io.r2dbc.h2.H2ConnectionFactory;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;

@Configuration
@EnableConfigurationProperties(DatabaseProperties.class)
@RequiredArgsConstructor
@Slf4j
public class DatabaseConfig extends AbstractR2dbcConfiguration {

    private final DatabaseProperties properties;

    @Override
    public ConnectionFactory connectionFactory() {
        if (properties.isInMemory()) {
            log.info("Using H2 database");
            return getH2ConnectionFactory();
        }
        log.info("Using PostgreSql database");
        return getPostgreSqlConnectionFactory();
    }

    /**
     * Required for Liquibase
     */
    @Bean
    public DataSource dataSource() {
        if (properties.isInMemory()) {
            return getH2DataSource();
        }
        return getPostgreSqlDatasource();
    }

    private H2ConnectionFactory getH2ConnectionFactory() {
        return new H2ConnectionFactory(H2ConnectionConfiguration.builder()
                .inMemory(properties.getName())
                .username(properties.getUser())
                .password(properties.getPassword())
                .build());
    }

    private DataSource getH2DataSource() {
        return new EmbeddedDatabaseBuilder()
                .setName(properties.getName())
                .setType(EmbeddedDatabaseType.H2)
                .continueOnError(false)
                .build();
    }

    private PostgresqlConnectionFactory getPostgreSqlConnectionFactory() {
        return new PostgresqlConnectionFactory(PostgresqlConnectionConfiguration.builder()
                .host(properties.getHost())
                .database(properties.getName())
                .username(properties.getUser())
                .password(properties.getPassword())
                .build());
    }

    private DataSource getPostgreSqlDatasource() {
        PGSimpleDataSource datasource = new PGSimpleDataSource();
        datasource.setServerName(properties.getHost());
        datasource.setDatabaseName(properties.getName());
        datasource.setUser(properties.getUser());
        datasource.setPassword(properties.getPassword());
        return datasource;
    }

}
