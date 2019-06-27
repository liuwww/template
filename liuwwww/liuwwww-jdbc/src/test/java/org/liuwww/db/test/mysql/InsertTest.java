package org.liuwww.db.test.mysql;

import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.liuwww.db.sql.Row;
import org.liuwww.db.test.entity.TestUser;
import org.liuwww.db.test.util.TestUserFactory;

public class InsertTest extends AbstractTest
{

    @Test
    public void testInsertEntity() throws Throwable
    {
        try
        {
            TestUser user = TestUserFactory.createEntity();
            dataTemplate.insert(user);
            Assert.assertNotNull(user.getUserCode());
            dataCompareUtil.testSucccess(user);
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testInsertMap() throws Throwable
    {
        try
        {
            Map<String, Object> user = TestUserFactory.createMap();
            Row row = dataTemplate.insert("test_user", user);
            dataCompareUtil.testInsertSucccess(row);
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
            List<TestUser> list = TestUserFactory.createEntityList(10);
            dataTemplate.insert(list);
            dataCompareUtil.testSuccess(list);
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testInsertMapList()
    {
        List<Map<String, Object>> list = TestUserFactory.createMapList(10);
        List<Row> rl = dataTemplate.insert("test_user", list);
        dataCompareUtil.testInsertSuccess4Map(rl);
    }

}
