package com.jeedsoft.marialocal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.junit.Assert;
import org.junit.Test;

public class MariaLocalDriverTest
{
    @Test
    public void testAcceptsUrl() throws Exception
    {
        MariaLocalDriver driver = new MariaLocalDriver();
        Assert.assertEquals(true, driver.acceptsURL("jdbc:marialocal:C:/data/school"));
        Assert.assertEquals(true, driver.acceptsURL("jdbc:marialocal:data/school"));
        Assert.assertEquals(true, driver.acceptsURL("jdbc:marialocal:/opt/data/school"));
        Assert.assertEquals(false, driver.acceptsURL("jdbc:mariadb://127.0.0.1/test"));
    }

    @Test
    public void testDriver() throws Exception
    {
        Class.forName(MariaLocalDriver.class.getName());
        String url = "jdbc:marialocal:build/data/test";
        try (Connection cn = DriverManager.getConnection(url, "root", "")) {
            QueryRunner qr = new QueryRunner();
            qr.update(cn, "create table if not exists hello (world varchar(50))");
            qr.update(cn, "delete from hello");
            qr.update(cn, "insert into hello values ('Hello, world')");
            List<String> results = qr.query(cn, "select * from hello", new ColumnListHandler<>());
            Assert.assertEquals(1, results.size());
            Assert.assertEquals("Hello, world", results.get(0));
        }
        finally {
            MariaLocalManager.shutdown();
        }
    }
}
