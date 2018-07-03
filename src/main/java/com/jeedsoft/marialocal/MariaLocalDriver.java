package com.jeedsoft.marialocal;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.LoggerFactory;

/**
 * A local driver for MariaDB4j
 * 
 * MariaDB4j starts MariaDB instances which accept URL in remote-style format
 * (jdbc:mysql://<host>:<port>/<database>). To supports URL in local-style format,
 * we create this driver.
 * 
 * JDBC URL format: jdbc:marialocal:<file-path>, file-path can be a relative or absolute
 * path. Examples:
 * 
 * jdbc:marialocal:data/school (data/school is a relative path)
 * jdbc:marialocal:/opt/data/school (/opt/data/school is an absolute path for Linux)
 * jdbc:marialocal:C:/data/school (C:/data/school is an absolute path for Windows)
 */
public class MariaLocalDriver implements Driver
{
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(MariaLocalDriver.class);

    static final Pattern urlPattern = Pattern.compile("^jdbc:marialocal:(.+)$");

    static
    {
        try {
            DriverManager.registerDriver(new MariaLocalDriver());
        }
        catch (SQLException e) {
            throw new RuntimeException("Failed to register MariaLocalDriver", e);
        }
        
        // Try to register MariaDB driver.
        // The MariaDB driver (or MySQL driver) is required by URL jdbc:mysql://server/database
        try {
            Class.forName("org.mariadb.jdbc.Driver");
        }
        catch (Exception e) {
            logger.error("Failed to initiailize MariaDB JDBC driver", e);
        }
    }
   
    @Override
    public boolean acceptsURL(String url) throws SQLException
    {
        return urlPattern.matcher(url).matches();
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException
    {
        Matcher matcher = urlPattern.matcher(url);
        if (!matcher.matches()) {
            return null;
        }
        try {
            String remoteUrl = MariaLocalManager.toRemoteUrl(url);
            return DriverManager.getConnection(remoteUrl, info);
        }
        catch (SQLException e) {
            throw e;
        }
        catch (Exception e) {
            throw new SQLException("Failed to connect to MariaDB4j", e);
        }
    }

    @Override
    public int getMajorVersion()
    {
        return 1;
    }

    @Override
    public int getMinorVersion()
    {
        return 0;
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException
    {
        return new DriverPropertyInfo[0];
    }

    @Override
    public boolean jdbcCompliant()
    {
        return false;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException
    {
        throw new SQLFeatureNotSupportedException("MariaLocalDriver does not support getParentLogger() method.");
    }
}
