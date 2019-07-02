package org.liuwww.db.test.test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.runner.RunWith;
import org.liuwww.db.service.IDataTemplate;
import org.liuwww.db.service.IQueryTemplate;
import org.liuwww.db.test.entity.TestUser;
import org.liuwww.db.test.entity.TestUser2;
import org.liuwww.db.test.util.DataCompareUtil;
import org.liuwww.db.test.util.TestUserFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations =
{ "classpath:spring/spring.xml" })
public abstract class AbstractTest
{

    @Autowired
    protected IDataTemplate dataTemplate;

    @Autowired
    protected DataCompareUtil dataCompareUtil;

    @Qualifier("jdbcTemplate")
    @Autowired
    protected JdbcTemplate jdbcTemplate1;

    @Qualifier("jdbcTemplate2")
    @Autowired
    protected JdbcTemplate jdbcTemplate2;

    @Autowired
    protected IQueryTemplate queryTemplate;

    public AbstractTest()
    {
        super();
    }

    public AbstractTest(AbstractTest test)
    {
        this.dataTemplate = test.dataTemplate;
        this.dataCompareUtil = test.dataCompareUtil;
        this.jdbcTemplate1 = test.jdbcTemplate1;
        this.jdbcTemplate2 = test.jdbcTemplate2;
        this.queryTemplate = test.queryTemplate;
    }

    protected TestUser queryTestUser()
    {
        List<TestUser> list = queryTemplate.createQueryBean("test_user").getBeanList(TestUser.class);
        System.out.println(list.size());
        Assert.assertNotEquals(0, list.size());
        return list.get(0);
    }

    protected Map<String, Object> queryTestUser1Map()
    {
        List<Map<String, Object>> list = queryTemplate.createQueryBean("test_user").getMapList();
        System.out.println(list.size());
        Assert.assertNotEquals(0, list.size());
        return list.get(0);
    }

    protected TestUser2 queryTestUser2(JdbcTemplate jdbcTemplate)
    {
        List<TestUser2> list = queryTemplate.createQueryBean("test_user2", jdbcTemplate).getBeanList(TestUser2.class);
        System.out.println(list.size());
        Assert.assertNotEquals(0, list.size());
        return list.get(0);
    }

    protected Map<String, Object> queryTestUser2Map(JdbcTemplate jdbcTemplate)
    {
        List<Map<String, Object>> list = queryTemplate.createQueryBean("test_user2", jdbcTemplate).getMapList();
        System.out.println(list.size());
        Assert.assertNotEquals(0, list.size());
        return list.get(0);
    }

    protected List<TestUser> queryTestUserList(int n)
    {
        return queryTemplate.createQueryBean("test_user").getBeanList(TestUser.class, n);
    }

    protected List<TestUser2> queryTestUser2List(int n, JdbcTemplate jdbcTemplate)
    {
        return queryTemplate.createQueryBean("test_user2", jdbcTemplate).getBeanList(TestUser2.class, n);
    }

    protected List<Map<String, Object>> queryTestUserMapList(int n)
    {
        return queryTemplate.createQueryBean("test_user").getMapList(n);
    }

    protected List<Map<String, Object>> queryTestUser2MapList(int n, JdbcTemplate jdbcTemplate)
    {
        return queryTemplate.createQueryBean("test_user2", jdbcTemplate).getMapList(n);
    }

    protected String getRandomStr()
    {
        return TestUserFactory.getRandomStr();
    }

    public void showDataCount()
    {
        long c = 0;
        c = queryTemplate.createQueryBean("test_user").getCount();
        System.out.println("test_user:" + c);

        c = queryTemplate.createQueryBean("test_user2", jdbcTemplate1).getCount();
        System.out.println("j1:test_user2:" + c);

        c = queryTemplate.createQueryBean("test_user2", jdbcTemplate2).getCount();
        System.out.println("j2:test_user2:" + c);

    }

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
                    System.out.println("method:" + name);
                    m.invoke(this, new Object[0]);
                    showDataCount();
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
