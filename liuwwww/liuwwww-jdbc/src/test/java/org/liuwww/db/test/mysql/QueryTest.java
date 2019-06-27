package org.liuwww.db.test.mysql;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.liuwww.db.condition.Compare;
import org.liuwww.db.condition.CompareOpe;
import org.liuwww.db.condition.GroupCondition;
import org.liuwww.db.condition.Condition.ConditionRel;
import org.liuwww.db.condition.OneCondition;
import org.liuwww.db.page.Page;
import org.liuwww.db.query.conditon.QueryBeanCompare;
import org.liuwww.db.sql.DefaultSqlBean;
import org.liuwww.db.sql.SqlBean;
import org.liuwww.db.sql.Table;
import org.liuwww.db.test.entity.TestUser;

public class QueryTest extends AbstractTest
{

    public void t2estQueryGet()
    {
        TestUser u = queryTestUser();
        u = queryTemplate.getBean(u);
        Assert.assertNotNull(u);

        List<TestUser> l1 = queryTemplate.getBeanList(u);
        Assert.assertEquals(1, l1.size());

        long n = queryTemplate.getCount(u);
        Assert.assertEquals(1, n);

        u = queryTemplate.getBean(u.tableName(), u.getUserId(), TestUser.class);
        Assert.assertNotNull(u);

        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("userId", u.getUserId());
        List<Map<String, Object>> l2 = queryTemplate.getMapList(u.tableName(), paramMap);
        Assert.assertEquals(1, l2.size());

        Map<String, Object> um = queryTemplate.getMap(u.tableName(), u.getUserId());
        Assert.assertNotNull(um);

        Page page = new Page(1, 10);
        page = queryTemplate.getPage(u, page);

        Assert.assertEquals(1, page.getTotal());
        Assert.assertEquals(1, page.getRows().size());

        page = queryTemplate.getPage(u.tableName(), paramMap);
        Assert.assertEquals(1, page.getTotal());
        Assert.assertEquals(1, page.getRows().size());

        page = queryTemplate.getPage(u.tableName(), page, paramMap);
        Assert.assertEquals(1, page.getTotal());
        Assert.assertEquals(1, page.getRows().size());

    }

    /**
     * @desc:主要观察非有效查询条件有没有被过滤掉、查询sql有没有被正确组装
     * @author liuwww
     */
    public void testQueryBean4Single()
    {
        TestUser u = queryTestUser();
        QueryBeanCompare compare = queryTemplate.createQueryBean(u).getCompare();
        compare.addCondition(new OneCondition("field1", CompareOpe.emptyStr, null));
        compare.addCondition(new OneCondition("field2", CompareOpe.eq, "test", ConditionRel.OR));
        compare.emptyStr("ttttt", ConditionRel.OR);
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("t1", "t");
        paramMap.put("t2", "t");
        paramMap.put("field3", "t");
        paramMap.put("field4", "t");
        paramMap.put("user_code", "t");
        paramMap.put("userId", "t");
        compare.addParamMap(paramMap);

        compare.eq("test", "33");
        compare.eq("field1", "");
        compare.ge("field1", null);
        compare.ge("field2", "");
        compare.ge("field3", "2");
        compare.ge("field3", "2", ConditionRel.OR);

        compare.gt("test", "33");
        compare.gt("field1", "");
        compare.gt("field1", null);
        compare.gt("field2", "");
        compare.gt("field3", "2");
        compare.gt("field3", "2", ConditionRel.OR);

        compare.lt("test", "33");
        compare.lt("field1", "");
        compare.lt("field1", null);
        compare.lt("field2", "");
        compare.lt("field3", "2");
        compare.lt("field3", "2", ConditionRel.OR);

        compare.le("test", "33");
        compare.le("field1", "");
        compare.le("field1", null);
        compare.le("field2", "");
        compare.le("field3", "2");
        compare.le("field3", "2", ConditionRel.OR);

        compare.like("test", "33");
        compare.like("field1", "");
        compare.like("field1", null);
        compare.like("field2", "");
        compare.like("field3", "2");
        compare.like("field3", "2", ConditionRel.OR);

        compare.likeAuto("test", "33");
        compare.likeAuto("field1", "");
        compare.likeAuto("field1", null);
        compare.likeAuto("field2", "");
        compare.likeAuto("field3", "2");
        compare.likeAuto("field3", "2", ConditionRel.OR);

        compare.likeL("test", "33");
        compare.likeL("field1", "");
        compare.likeL("field1", null);
        compare.likeL("field2", "");
        compare.likeL("field3", "2");
        compare.likeL("field3", "2", ConditionRel.OR);

        compare.likeR("test", "33");
        compare.likeR("field1", "");
        compare.likeR("field1", null);
        compare.likeR("field2", "");
        compare.likeR("field3", "2");
        compare.likeR("field3", "2", ConditionRel.OR);

        compare.ne("test", "33");
        compare.ne("field1", "");
        compare.ne("field1", null);
        compare.ne("field2", "");
        compare.ne("field3", "2");
        compare.ne("field3", "2", ConditionRel.OR);

        compare.notNull("test");
        compare.notNull("field1");
        compare.notNull("field3", ConditionRel.OR);

        List<TestUser> l1 = compare.getQueryBean().getBeanList(TestUser.class);
        System.out.println(l1.size());

        List<Map<String, Object>> l2 = compare.getQueryBean().getMapList();
        System.out.println(l2.size());

        long total = compare.getQueryBean().getCount();

        Assert.assertEquals(total, l1.size());

        l1 = compare.getQueryBean().getBeanList(TestUser.class, 10);

        Assert.assertTrue(l1.size() <= 10);

        l2 = compare.getQueryBean().getMapList(10);

        Assert.assertTrue(l2.size() <= 10);

        Page page = new Page(1, 10);
        page = compare.getQueryBean().getPage(page);
        Assert.assertEquals(page.getTotal(), total);
        Assert.assertEquals(page.getPageSize(), page.getRows().size());

        compare.getQueryBean().getPage(page, TestUser.class);
    }

    @SuppressWarnings("rawtypes")
    private void addCondition(Compare compare)
    {
        compare.ge("userCode", "1234");
        GroupCondition g1 = new GroupCondition();
        GroupCondition g2 = new GroupCondition(ConditionRel.OR);
        GroupCondition g11 = new GroupCondition();
        GroupCondition g12 = new GroupCondition(ConditionRel.OR);

        g1.eq("userCode", "t1");
        g1.le("field1", "123");
        g1.addCondition(g11);
        g1.addCondition(g12);

        g11.eq("field2", "t111");
        g11.eq("field3", "t111");
        g11.eq("field4", "t111", ConditionRel.OR);

        g12.ge("field1", "1");
        g12.ge("field2", "1");

        g2.likeAuto("field1", "1d");
        g2.likeR("filed2", "2");
        g2.likeL("filed2", "3");
        // g1,g2
        // 最好在加入之前处理好，在该方法中对condition作了一些校验，如果在该方法后g1，g2中添加条件的话，无法对类似userCode字段作user_code转换，field值只能时user_code
        compare.addCondition(g1).addCondition(g2).addCondition(new GroupCondition());
    }

    /**
     * @desc:测试组合条件下，查询条件的过滤和sql的组装
     * @author liuwww
     */
    public void testQuery4Group()
    {
        QueryBeanCompare compare = queryTemplate.createQueryBean("test_user").getCompare();
        addCondition(compare);
        List<TestUser> list = compare.getQueryBean().getBeanList(TestUser.class);
        System.out.println(list.size());

    }

    public void testQueryMore1()
    {
        QueryBeanCompare compare = queryTemplate.createQueryBean(new TestUser()).getCompare();
        addCondition(compare);
        long c = compare.getQueryBean().getCount();
        System.out.println(c);
    }

    /**
     * @desc:未知的表或视图 查询时，字段需要是表字段名，不能是bean的字段名，字段不能拼错
     * @Date:2019年6月27日下午10:29:15
     * @author liuwww
     */
    public void testQueryMore2()
    {
        SqlBean sqlbean = new DefaultSqlBean("select * from test_user where field1>? and field2>? and field3 <?",
                new Object[]
                { "1", "2", 99 });
        QueryBeanCompare compare = queryTemplate.createQueryBean(sqlbean).getCompare();
        compare.ge("user_code", "1234");
        GroupCondition g1 = new GroupCondition();
        GroupCondition g2 = new GroupCondition(ConditionRel.OR);
        GroupCondition g11 = new GroupCondition();
        GroupCondition g12 = new GroupCondition(ConditionRel.OR);

        g1.eq("user_code", "t1");
        g1.le("field1", "123");
        g1.addCondition(g11);
        g1.addCondition(g12);

        g11.eq("field2", "t111");
        g11.eq("field3", "t111");
        g11.eq("field4", "t111", ConditionRel.OR);

        g12.ge("field1", "1");
        g12.ge("field2", "1");

        g2.likeAuto("field1", "1d");
        g2.likeR("field4", "2");
        g2.likeL("field2", "3");
        // g1,g2
        // 最好在加入之前处理好，在该方法中对condition作了一些校验，如果在该方法后g1，g2中添加条件的话，无法对类似userCode字段作user_code转换，field值只能时user_code
        compare.addCondition(g1).addCondition(g2).addCondition(new GroupCondition());
        Page page = compare.getQueryBean().getPage(new Page());
        System.out.println(page.getTotal());
    }

    public void testQueryMore3()
    {
        Table table = new Table("test_user");
        table.addField("field1", "f1").addField("field1").addField("userId");
        table.ge("field1", "111").le("field4", "999");
        QueryBeanCompare compare = queryTemplate.createQueryBean(table).getCompare();
        addCondition(compare);
        Page page = compare.getQueryBean().getPage(new Page());
        System.out.println(page.getTotal());
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
