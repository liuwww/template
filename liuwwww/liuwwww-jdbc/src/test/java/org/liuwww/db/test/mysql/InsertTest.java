package org.liuwww.db.test.mysql;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.liuwww.common.util.DbNameConverter;
import org.liuwww.common.util.StringUtil;
import org.liuwww.db.service.IDataService;
import org.liuwww.db.service.IQueryService;
import org.liuwww.db.sql.Row;
import org.liuwww.db.test.entity.TestUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alibaba.fastjson.JSONObject;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations =
{ "classpath:spring/spring.xml" })
public class InsertTest
{

    @Autowired
    private IDataService dataService;

    @Autowired
    private IQueryService queryService;

    @Test
    public void testInsertEntity() throws Throwable
    {
        try
        {
            TestUser user = TestUserFactory.createEntity();
            dataService.insert(user);
            Assert.assertNotNull(user.getUserCode());
            testInsertSucccess(user);
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
            Row row = dataService.insert("test_user", user);
            row.getRowValueMap();
            testInsertSucccess(row);
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
            dataService.insert(list);
            testInsertSuccess(list);
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
        List<Row> rl = dataService.insert("test_user", list);
        testInsertSuccess4Map(rl);
    }

    private void testInsertSuccess4Map(List<Row> rl)
    {
        for (Row r : rl)
        {
            testInsertSucccess(r);
        }
    }

    private void testInsertSuccess(List<TestUser> list)
    {
        for (TestUser u : list)
        {
            testInsertSucccess(u);
        }
    }

    private void testInsertSucccess(Row row)
    {
        String id = row.getIdValue();
        Assert.assertNotNull(id);
        Map<String, Object> map2 = queryService.getMap(row.getTableName(), row.getIdValue());
        testMapEqual(row.getRowValueMap(), map2);
    }

    @SuppressWarnings(
    { "rawtypes", "unchecked" })
    private void testInsertSucccess(TestUser user)
    {
        TestUser u = queryService.createQueryBean(user.tableName()).getCompare().eq("userId", user.getUserId())
                .getQueryBean().getBean(TestUser.class);
        Assert.assertNotNull(u);

        testMapEqual((Map) JSONObject.toJSON(user), (Map) JSONObject.toJSON(u));
    }

    private void testMapEqual(Map<String, Object> map1, Map<String, Object> map2)
    {
        for (Entry<String, Object> en : map1.entrySet())
        {
            String key = en.getKey();
            if (key.indexOf("_") > 0)
            {
                key = DbNameConverter.convert(en.getKey());
            }
            Object val1 = en.getValue();
            Object val2 = map2.get(key);
            if (val2 == null && val1 != null)
            {
                throw new RuntimeException("保存:[" + key + "]字段不一致");
            }
            else if (val2 != null)
            {
                if ("stsDate".equals(key) || "createDate".equals(key))
                {
                    // if (val2 instanceof Date)
                    // {
                    // val2 = DateUtil.format((Date) val2,
                    // DateUtil.datetimePattern);
                    // }
                    // else if (val2 instanceof String)
                    // {
                    // val2 = val2.toString().split("\\.")[0];
                    // }
                    //
                    // if (val1 != null)
                    // {
                    // val1 = val1.toString().split("\\.")[0];
                    // }

                    // 存库时毫秒可能进一位导致比对不通过
                    continue;
                }
                if (!StringUtil.equals(val1.toString(), val2.toString()))
                {
                    throw new RuntimeException("保存字段[" + key + "]不一致");
                }
            }

        }
    }

}
