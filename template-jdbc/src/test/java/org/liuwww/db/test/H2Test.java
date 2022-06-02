package org.liuwww.db.test;

import org.junit.Before;
import org.junit.Test;
import org.liuwww.db.context.DbContext;
import org.liuwww.db.test.test.AbstractTest;
import org.liuwww.db.test.test.DeleteTest;
import org.liuwww.db.test.test.InsertTest;
import org.liuwww.db.test.test.InsertTest4MutiDs;
import org.liuwww.db.test.test.QueryTest;
import org.liuwww.db.test.test.Update4MutiDs;
import org.liuwww.db.test.test.UpdateTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;

public class H2Test extends AbstractTest
{

    @Autowired
    private DbContext dbContext;

    @Before
    public void init()
    {
        try
        {
            String csql1 = "CREATE TABLE `test_user` (\r\n" + "  `USER_ID` bigint(20) NOT NULL AUTO_INCREMENT,\r\n"
                    + "  `NAME` varchar(255) DEFAULT NULL,\r\n" + "  `USER_CODE` varchar(255) DEFAULT NULL,\r\n"
                    + "  `PASSWORD` varchar(255) DEFAULT NULL,\r\n" + "  `CREATE_DATE` datetime DEFAULT NULL,\r\n"
                    + "  `STS_DATE` datetime DEFAULT NULL ,\r\n" + "  `STS` char(1) DEFAULT NULL,\r\n"
                    + "  `FIELD1` varchar(255) DEFAULT NULL,\r\n" + "  `FIELD2` varchar(255) DEFAULT NULL,\r\n"
                    + "  `FIELD3` varchar(255) DEFAULT NULL,\r\n" + "  `FIELD4` varchar(255) DEFAULT NULL,\r\n"
                    + "  `FIELD5` varchar(255) DEFAULT NULL,\r\n" + "  PRIMARY KEY (`USER_ID`)\r\n" + ") CHARSET=utf8";
            String csql2 = "CREATE TABLE `test_user2` (\r\n" + "  `USER_ID` bigint(20) NOT NULL AUTO_INCREMENT,\r\n"
                    + "  `NAME` varchar(255) DEFAULT NULL,\r\n" + "  `USER_CODE` varchar(255) DEFAULT NULL,\r\n"
                    + "  `PASSWORD` varchar(255) DEFAULT NULL,\r\n" + "  `CREATE_DATE` datetime DEFAULT NULL ,\r\n"
                    + "  `STS_DATE` datetime DEFAULT NULL,\r\n" + "  `STS` char(1) DEFAULT NULL,\r\n"
                    + "  `FIELD1` varchar(255) DEFAULT NULL,\r\n" + "  `FIELD2` varchar(255) DEFAULT NULL,\r\n"
                    + "  `FIELD3` varchar(255) DEFAULT NULL,\r\n" + "  `FIELD4` varchar(255) DEFAULT NULL,\r\n"
                    + "  `FIELD5` varchar(255) DEFAULT NULL,\r\n" + "  PRIMARY KEY (`USER_ID`)\r\n" + ") CHARSET=utf8";

            String vsql = "create or replace view test_user2_view as  select * from test_user2";

            jdbcTemplate1.execute(csql1);
            jdbcTemplate1.execute(csql2);
            jdbcTemplate1.execute(vsql);

            jdbcTemplate2.execute(csql2);
            jdbcTemplate2.execute(vsql);
            System.out.println("------------------------------");
            dbContext.refresh();
            System.out.println("------------------------------");
        }
        catch (DataAccessException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    @Override
    public void test() throws Throwable
    {
        try
        {
            new InsertTest(this).test();
            new InsertTest4MutiDs(this).test();
            new UpdateTest(this).test();
            new Update4MutiDs(this).test();
            new QueryTest(this).test();
            new DeleteTest(this).test();// 放最后防止数据没了可能出现数组越界
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw e;
        }
        finally
        {
            new DeleteTest(this).deleteAll();
        }
    }

}
