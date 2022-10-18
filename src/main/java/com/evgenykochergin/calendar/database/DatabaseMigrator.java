package com.evgenykochergin.calendar.database;

import org.flywaydb.core.Flyway;

import javax.sql.DataSource;

public class DatabaseMigrator {

    private final DataSource dataSource;

    public DatabaseMigrator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void migrate() {
        Flyway.configure().dataSource(dataSource).load().migrate();
    }
}
