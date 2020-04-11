package org.liuwww.db.test.test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Assert;
import org.liuwww.common.Idgen.IdGenerator;
import org.liuwww.db.context.TableMetaData;
import org.liuwww.db.sql.Row;
import org.liuwww.db.test.entity.TestUser;
import org.liuwww.db.test.util.TestUserFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InsertTest extends AbstractTest
{

    public InsertTest()
    {

    }

    public InsertTest(AbstractTest test)
    {
        super(test);
    }

    // @Bean
    public IdGenerator idGenerator()
    {
        return new IdGenerator()
        {

            private AtomicLong atomicLong = new AtomicLong(1);

            @Override
            public String nextKey(String tableName, TableMetaData tmd)
            {
                return String.valueOf(atomicLong.addAndGet(1));
            }

            @Override
            public boolean isCreatedByDatabase(String tableName, TableMetaData tmd)
            {
                if ("test_user".equals(tableName))
                {
                    return true;
                }
                return false;
            }
        };
    }

    public void testInsertEntity() throws Throwable
    {
        try
        {
            TestUser user = TestUserFactory.createEntity();
            dataTemplate.insert(user);
            Assert.assertNotNull(user.getUserId());
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
