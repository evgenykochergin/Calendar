package com.evgenykochergin.calendar.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class DataSourceProvider {

    private final DataSource dataSource;

    public DataSourceProvider() {
        final var config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:calendar");
        config.setUsername("sa");
        dataSource = new HikariDataSource(config);
    }

    public DataSource getDataSource() {
        return dataSource;
    }

}
