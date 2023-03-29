package org.liuwww.db.test.test;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
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

    public DeleteTest(AbstractTest test)
    {
        super(test);
    }

    public void testDeleteEntity()
    {
        TestUser user = queryTestUser();
        dataTemplate.delete(user);
        user = queryTemplate.getBean(user);
        Assert.assertNull(user);

        TestUser2 u2 = queryTestUser2(jdbcTemplate1);
        dataTemplate.delete(u2, jdbcTemplate1);
        u2 = queryTemplate.createQueryBean(u2.tableName(), jdbcTemplate1).getCompare().eq("userId", u2.getUserId())
                .getQueryBean().getBean(TestUser2.class);
        Assert.assertNull(u2);

        u2 = queryTestUser2(jdbcTemplate2);
        dataTemplate.delete(u2, jdbcTemplate2);
        u2 = queryTemplate.createQueryBean(u2.tableName(), jdbcTemplate2).getCompare().eq("userId", u2.getUserId())
                .getQueryBean().getBean(TestUser2.class);
        Assert.assertNull(u2);

    }

    public void testDeleteEntityList()
    {
        List<TestUser> list = queryTestUserList(10);
        dataTemplate.delete(list);
        checkDeleteSuccess("test_user", list, null);

        List<TestUser2> list2 = queryTestUser2List(10, jdbcTemplate1);
        dataTemplate.delete(list2, jdbcTemplate1);
        checkDeleteSuccess("test_user2", list2, jdbcTemplate1);

        list2 = queryTestUser2List(10, jdbcTemplate2);
        dataTemplate.delete(list2, jdbcTemplate2);
        checkDeleteSuccess("test_user2", list2, jdbcTemplate2);
    }

    protected void checkDeleteSuccess(String tableName, List<? extends Object> list, JdbcTemplate jdbcTemplate)
    {
        QueryBeanCompare compare = queryTemplate.createQueryBean(tableName).getCompare();
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
        long n = compare.getQueryBean().getCount();
        Assert.assertEquals(n, 0);
    }

    public void testDeleteMap()
    {
        Map<String, Object> user = queryTestUser1Map();
        Serializable userId = (Serializable) user.get("userId");
        dataTemplate.delete("test_user", user);
        user = queryTemplate.getMap("test_user", userId);
        Assert.assertNull(user);

        user = queryTestUser2Map(jdbcTemplate1);
        dataTemplate.delete("test_user2", user, jdbcTemplate1);
        userId = (Serializable) user.get("userId");
        user = queryTemplate.createQueryBean("test_user2", jdbcTemplate1).getCompare().eq("userId", userId)
                .getQueryBean().getMap();
        Assert.assertNull(user);

        user = queryTestUser2Map(jdbcTemplate2);
        dataTemplate.delete("test_user2", user, jdbcTemplate2);
        userId = (Serializable) user.get("userId");
        user = queryTemplate.createQueryBean("test_user2", jdbcTemplate2).getCompare().eq("userId", userId)
                .getQueryBean().getMap();
        Assert.assertNull(user);
    }

    public void testDeleteBean()
    {
        TestUser user = queryTestUser();
        dataTemplate.createDeleteBean("test_user").addCondition("userId", user.getUserId()).delete();
        user = queryTemplate.getBean("test_user", user.getUserId(), TestUser.class);
        Assert.assertNull(user);
    }

    public void testDeleteMapList()
    {
        List<Map<String, Object>> list = queryTestUserMapList(10);
        dataTemplate.delete("test_user", list);
        checkDeleteSuccess("test_user", list, null);

        list = queryTestUser2MapList(10, jdbcTemplate1);
        dataTemplate.delete("test_user2", list);
        checkDeleteSuccess("test_user2", list, jdbcTemplate1);

        list = queryTestUser2MapList(10, jdbcTemplate2);
        dataTemplate.delete("test_user2", list, jdbcTemplate2);
        checkDeleteSuccess("test_user2", list, jdbcTemplate2);
    }

    public void testDeleteById()
    {
        TestUser user = queryTestUser();
        dataTemplate.delete(user.tableName(), user.getUserId());
        Map<String, Object> um = queryTemplate.getMap(user.tableName(), user.getUserId());
        Assert.assertNull(um);

        List<TestUser> list = queryTestUserList(5);
        Serializable[] ids = new Serializable[5];
        int i = 0;
        for (TestUser u : list)
        {
            ids[i++] = u.getUserId();
        }
        dataTemplate.delete("test_user", ids);
        checkDeleteSuccess("test_user", list, null);

        TestUser2 u2 = queryTestUser2(jdbcTemplate1);
        dataTemplate.delete("test_user2", u2.getUserId(), jdbcTemplate1);
        um = queryTemplate.getMap(u2.tableName(), user.getUserId());
        Assert.assertNull(um);

        u2 = queryTestUser2(jdbcTemplate2);
        dataTemplate.delete("test_user2", u2.getUserId(), jdbcTemplate2);
        um = queryTemplate.getMap(u2.tableName(), user.getUserId());
        Assert.assertNull(um);

        List<TestUser2> list2 = queryTestUser2List(5, jdbcTemplate1);
        i = 0;
        for (TestUser2 u : list2)
        {
            ids[i++] = u.getUserId();
        }
        dataTemplate.delete("test_user2", ids, jdbcTemplate1);

        list2 = queryTestUser2List(5, jdbcTemplate2);
        i = 0;
        for (TestUser2 u : list2)
        {
            ids[i++] = u.getUserId();
        }
        dataTemplate.delete("test_user2", ids, jdbcTemplate2);
    }

    public void testDeleteRows()
    {
        TestUser user = queryTestUser();
        Map<String, Object> paramMap = new HashMap<String, Object>(2);
        paramMap.put("userId", user.getUserId());
        paramMap.put("field1", user.getField1());
        dataTemplate.deleteRows(user.tableName(), paramMap);
        user = queryTemplate.getBean(user.tableName(), user.getUserId(), user.getClass());
        Assert.assertNull(user);

        TestUser2 u2 = queryTestUser2(jdbcTemplate1);
        paramMap.put("userId", u2.getUserId());
        paramMap.put("field1", u2.getField1());
        dataTemplate.deleteRows(u2.tableName(), paramMap, jdbcTemplate1);
        u2 = queryTemplate.createQueryBean(u2.tableName(), jdbcTemplate1).getCompare().eq("userId", u2.getUserId())
                .getQueryBean().getBean(u2.getClass());
        Assert.assertNull(u2);

        u2 = queryTestUser2(jdbcTemplate2);
        paramMap.put("userId", u2.getUserId());
        paramMap.put("field1", u2.getField1());
        dataTemplate.deleteRows(u2.tableName(), paramMap, jdbcTemplate2);
        u2 = queryTemplate.createQueryBean(u2.tableName(), jdbcTemplate2).getCompare().eq("userId", u2.getUserId())
                .getQueryBean().getBean(u2.getClass());
        Assert.assertNull(u2);
    }

    public void deleteAll()
    {
        List<TestUser> list = queryTemplate.createQueryBean("test_user", jdbcTemplate1).getBeanList(TestUser.class);
        dataTemplate.delete(list, jdbcTemplate1);

        List<TestUser2> list2 = queryTemplate.createQueryBean("test_user2", jdbcTemplate1).getBeanList(TestUser2.class);
        dataTemplate.delete(list2, jdbcTemplate1);

        List<TestUser2> list3 = queryTemplate.createQueryBean("test_user2", jdbcTemplate2).getBeanList(TestUser2.class);
        dataTemplate.delete(list3, jdbcTemplate2);

    }

}
