package com.crawler.law.core;

import com.crawler.law.util.ResourceUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionPool {
    private static HikariConfig config = new HikariConfig();
    private static HikariDataSource ds;

    static {
        config.setPoolName("HikariCP");
        config.setConnectionTestQuery("SELECT 1");
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);

        config.setDriverClassName(ResourceUtil.getValue("datasource.driver-class-name"));
        config.setJdbcUrl(ResourceUtil.getValue("datasource.url"));
        config.setUsername(ResourceUtil.getValue("datasource.username"));
        config.setPassword(ResourceUtil.getValue("datasource.password"));

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");

        ds = new HikariDataSource(config);
    }

    private ConnectionPool() { }

    public static Connection getTransactional() throws SQLException {
        return ds.getConnection();
    }
}

