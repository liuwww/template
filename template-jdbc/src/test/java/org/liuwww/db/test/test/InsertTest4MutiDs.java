package org.liuwww.db.test.test;

import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.liuwww.db.sql.Row;
import org.liuwww.db.test.entity.TestUser2;
import org.liuwww.db.test.util.TestUser2Factory;

/**
 * @desc:测试存在多数据源的情况下，存在相同表的情况
 * @Date:2019年6月3日下午9:07:04
 * @author liuwww
 */

public class InsertTest4MutiDs extends AbstractTest
{

    public InsertTest4MutiDs()
    {

    }

    public InsertTest4MutiDs(AbstractTest test)
    {
        super(test);
    }

    public void testInsertEntity() throws Throwable
    {
        try
        {
            TestUser2 user = TestUser2Factory.createEntity();
            dataTemplate.insert(user, jdbcTemplate1);
            Assert.assertNotNull(user.getUserCode());
            dataCompareUtil.testInsertSucccess(user, jdbcTemplate1);

            TestUser2 user2 = TestUser2Factory.createEntity();
            dataTemplate.insert(user2, jdbcTemplate2);
            Assert.assertNotNull(user2.getUserCode());
            dataCompareUtil.testInsertSucccess(user2, jdbcTemplate2);

        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw e;
        }
    }

    public void testInsertMap() throws Throwable
    {
        try
        {
            Map<String, Object> user = TestUser2Factory.createMap();
            Row row = dataTemplate.insert("test_user2", user, jdbcTemplate1);
            dataCompareUtil.testInsertSucccess(row, jdbcTemplate1);

            Map<String, Object> user2 = TestUser2Factory.createMap();
            Row row2 = dataTemplate.insert("test_user2", user2, jdbcTemplate2);
            dataCompareUtil.testInsertSucccess(row2, jdbcTemplate2);

        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testInsertEntityList() throws Throwable
    {
        try
        {
            List<TestUser2> list = TestUser2Factory.createEntityList(20);
            dataTemplate.insert(list, jdbcTemplate1);
            // dataCompareUtil.testInsertSuccess(list, jdbcTemplate1);

            List<TestUser2> list2 = TestUser2Factory.createEntityList(20);
            dataTemplate.insert(list2, jdbcTemplate2);
            // dataCompareUtil.testInsertSuccess(list2, jdbcTemplate2);

        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw e;
        }
    }

    public void testInsertMapList()
    {
        List<Map<String, Object>> list = TestUser2Factory.createMapList(20);
        List<Row> rl = dataTemplate.insert("test_user2", list, jdbcTemplate1);
        dataCompareUtil.testInsertSuccess4Map(rl, jdbcTemplate1);

        List<Map<String, Object>> list2 = TestUser2Factory.createMapList(20);
        List<Row> rl2 = dataTemplate.insert("test_user2", list2, jdbcTemplate2);
        dataCompareUtil.testInsertSuccess4Map(rl2, jdbcTemplate2);

    }

}
