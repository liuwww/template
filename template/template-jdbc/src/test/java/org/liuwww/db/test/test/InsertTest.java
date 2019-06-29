package org.liuwww.db.test.test;

import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.liuwww.db.sql.Row;
import org.liuwww.db.test.entity.TestUser;
import org.liuwww.db.test.util.TestUserFactory;

public class InsertTest extends AbstractTest
{

    public InsertTest(AbstractTest test)
    {
        super(test);
    }

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

    public void testInsertEntityList() throws Throwable
    {
        try
        {
            List<TestUser> list = TestUserFactory.createEntityList(20);
            dataTemplate.insert(list);
            dataCompareUtil.testSuccess(list);
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw e;
        }
    }

    public void testInsertMapList()
    {
        List<Map<String, Object>> list = TestUserFactory.createMapList(20);
        List<Row> rl = dataTemplate.insert("test_user", list);
        dataCompareUtil.testInsertSuccess4Map(rl);
    }

}
