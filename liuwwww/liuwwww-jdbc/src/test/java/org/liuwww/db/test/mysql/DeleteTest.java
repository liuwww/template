package org.liuwww.db.test.mysql;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.liuwww.common.util.EntryUtil;
import org.liuwww.db.condition.GroupCondition;
import org.liuwww.db.condition.Condition.ConditionRel;
import org.liuwww.db.context.DbContext;
import org.liuwww.db.context.TableMetaData;
import org.liuwww.db.query.conditon.QueryBeanCompare;
import org.liuwww.db.test.entity.TestUser;
import org.liuwww.db.test.entity.TestUser2;
import org.springframework.jdbc.core.JdbcTemplate;

public class DeleteTest extends AbstractTest
{

    public void testDeleteEntity()
    {
        TestUser user = queryTestUser();
        dataService.delete(user);
        user = queryService.getBean(user);
        Assert.assertNull(user);

        TestUser2 u2 = queryTestUser2(jdbcTemplate1);
        dataService.delete(u2, jdbcTemplate1);
        u2 = queryService.createQueryBean(u2.tableName(), jdbcTemplate1).getCompare().eq("userId", u2.getUserId())
                .getQueryBean().getBean(TestUser2.class);
        Assert.assertNull(u2);

        u2 = queryTestUser2(jdbcTemplate2);
        dataService.delete(u2, jdbcTemplate2);
        u2 = queryService.createQueryBean(u2.tableName(), jdbcTemplate2).getCompare().eq("userId", u2.getUserId())
                .getQueryBean().getBean(TestUser2.class);
        Assert.assertNull(u2);

    }

    public void testDeleteEntityList()
    {
        List<TestUser> list = queryTestUserList(10);
        dataService.delete(list);
        checkDeleteSuccess("test_user", list, null);

        List<TestUser2> list2 = queryTestUser2List(10, jdbcTemplate1);
        dataService.delete(list2);
        checkDeleteSuccess("test_user2", list2, jdbcTemplate1);

        list2 = queryTestUser2List(10, jdbcTemplate2);
        dataService.delete(list2);
        checkDeleteSuccess("test_user2", list2, jdbcTemplate2);
    }

    protected void checkDeleteSuccess(String tableName, List<? extends Object> list, JdbcTemplate jdbcTemplate)
    {
        QueryBeanCompare compare = queryService.createQueryBean(tableName).getCompare();
        TableMetaData tmd = DbContext.getTableMetaData(tableName, jdbcTemplate);
        String idColumnName = tmd.getIdColumn().getColumnName();
        String idName = tmd.getIdColumn().getName();
        GroupCondition group = new GroupCondition();
        for (Object u : list)
        {
            Object id = EntryUtil.getFieldValue(u, idName);
            group.eq(idColumnName, id, ConditionRel.OR);
        }
        compare.addCondition(group);
        int n = compare.getQueryBean().getCount();
        Assert.assertEquals(n, 0);
    }

    public void testDeleteMap()
    {
        Map<String, Object> user = queryTestUser1Map();
        Serializable userId = (Serializable) user.get("userId");
        dataService.delete("test_user", user);
        user = queryService.getMap("test_user", userId);
        Assert.assertNull(user);

        user = queryTestUser2Map(jdbcTemplate1);
        dataService.delete("test_user2", user, jdbcTemplate1);
        userId = (Serializable) user.get("userId");
        user = queryService.createQueryBean("test_user2", jdbcTemplate1).getCompare().eq("userId", userId)
                .getQueryBean().getMap();
        Assert.assertNull(user);

        user = queryTestUser2Map(jdbcTemplate2);
        dataService.delete("test_user2", user, jdbcTemplate2);
        userId = (Serializable) user.get("userId");
        user = queryService.createQueryBean("test_user2", jdbcTemplate2).getCompare().eq("userId", userId)
                .getQueryBean().getMap();
        Assert.assertNull(user);
    }

    public void testDeleteMapList()
    {
        List<Map<String, Object>> list = queryTestUserMapList(10);
        dataService.delete("test_user", list);
        checkDeleteSuccess("test_user", list, null);

        list = queryTestUser2MapList(10, jdbcTemplate1);
        dataService.delete("test_user2", list);
        checkDeleteSuccess("test_user2", list, jdbcTemplate1);

        list = queryTestUser2MapList(10, jdbcTemplate2);
        dataService.delete("test_user2", list);
        checkDeleteSuccess("test_user2", list, jdbcTemplate2);
    }

    public void testDeleteById()
    {
        TestUser user = queryTestUser();
        dataService.delete(user.tableName(), user.getUserId());
        Map<String, Object> um = queryService.getMap(user.tableName(), user.getUserId());
        Assert.assertNull(um);

        List<TestUser> list = queryTestUserList(5);
        Serializable[] ids = new Serializable[5];
        int i = 0;
        for (TestUser u : list)
        {
            ids[i++] = u.getUserId();
        }
        dataService.delete("test_user", ids);
        checkDeleteSuccess("test_user", list, null);

        TestUser2 u2 = queryTestUser2(jdbcTemplate1);
        dataService.delete("test_user2", u2.getUserId(), jdbcTemplate1);
        um = queryService.getMap(u2.tableName(), user.getUserId());
        Assert.assertNull(um);

        u2 = queryTestUser2(jdbcTemplate2);
        dataService.delete("test_user2", u2.getUserId(), jdbcTemplate2);
        um = queryService.getMap(u2.tableName(), user.getUserId());
        Assert.assertNull(um);

        List<TestUser2> list2 = queryTestUser2List(5, jdbcTemplate1);
        i = 0;
        for (TestUser2 u : list2)
        {
            ids[i++] = u.getUserId();
        }
        dataService.delete("test_user2", ids, jdbcTemplate1);

        list2 = queryTestUser2List(5, jdbcTemplate2);
        i = 0;
        for (TestUser2 u : list2)
        {
            ids[i++] = u.getUserId();
        }
        dataService.delete("test_user2", ids, jdbcTemplate2);
    }

    public void testDeleteRows()
    {
        TestUser user = queryTestUser();
        Map<String, Object> paramMap = new HashMap<String, Object>(2);
        paramMap.put("userId", user.getUserId());
        paramMap.put("field1", user.getField1());
        dataService.deleteRows(user.tableName(), paramMap);
        user = queryService.getEntity(user.tableName(), user.getUserId(), user.getClass());
        Assert.assertNull(user);

        TestUser2 u2 = queryTestUser2(jdbcTemplate1);
        paramMap.put("userId", u2.getUserId());
        paramMap.put("field1", u2.getField1());
        dataService.deleteRows(u2.tableName(), paramMap, jdbcTemplate1);
        u2 = queryService.createQueryBean(u2.tableName(), jdbcTemplate1).getCompare().eq("userId", u2.getUserId())
                .getQueryBean().getBean(u2.getClass());
        Assert.assertNull(u2);

        u2 = queryTestUser2(jdbcTemplate2);
        paramMap.put("userId", u2.getUserId());
        paramMap.put("field1", u2.getField1());
        dataService.deleteRows(u2.tableName(), paramMap, jdbcTemplate2);
        u2 = queryService.createQueryBean(u2.tableName(), jdbcTemplate2).getCompare().eq("userId", u2.getUserId())
                .getQueryBean().getBean(u2.getClass());
        Assert.assertNull(u2);
    }

    @Test
    public void test() throws Throwable
    {
        try
        {
            Method[] ms = this.getClass().getMethods();
            for (Method m : ms)
            {

                String name = m.getName();
                if (name.startsWith("test") && name.length() > 4)
                {
                    m.setAccessible(true);
                    m.invoke(this, new Object[0]);
                }
            }
        }
        catch (Throwable e)
        {
            e = e.getCause();
            if (e != null)
            {
                e.printStackTrace();
                throw e;
            }
        }
    }

}
