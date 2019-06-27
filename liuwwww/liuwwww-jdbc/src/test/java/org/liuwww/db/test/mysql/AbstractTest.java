package org.liuwww.db.test.mysql;

import java.util.List;
import java.util.Map;

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

    protected TestUser queryTestUser()
    {
        return queryTemplate.createQueryBean("test_user").getBeanList(TestUser.class, 1).get(0);
    }

    protected Map<String, Object> queryTestUser1Map()
    {
        return queryTemplate.createQueryBean("test_user").getMapList(1).get(0);
    }

    protected TestUser2 queryTestUser2(JdbcTemplate jdbcTemplate)
    {
        return queryTemplate.createQueryBean("test_user2", jdbcTemplate).getBeanList(TestUser2.class, 1).get(0);
    }

    protected Map<String, Object> queryTestUser2Map(JdbcTemplate jdbcTemplate)
    {
        return queryTemplate.createQueryBean("test_user2", jdbcTemplate).getMapList(1).get(0);
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

}
