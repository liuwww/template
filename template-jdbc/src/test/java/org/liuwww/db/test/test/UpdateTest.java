package org.liuwww.db.test.test;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.liuwww.db.test.entity.TestUser;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UpdateTest extends AbstractTest
{

    public UpdateTest()
    {

    }

    // @Bean
    // public DbContext dbcontext()
    // {
    // return new DbContext();
    // }
    //
    // @Bean
    // public IQueryTemplate getQuerytemplate(DbContext dbContext)
    // {
    // return dbContext.getQueryTemplate();
    // }
    //
    // @Bean
    // public IDataTemplate getDatatemplate(DbContext dbContext)
    // {
    // return dbContext.getDataTemplate();
    // }
    //
    // @Bean
    // public BeanUtil beanUtil(DbContext dbContext)
    // {
    // return dbContext.getBeanUtil();
    // }

    public UpdateTest(AbstractTest test)
    {
        super(test);
    }

    public void testUpdateEntity()
    {
        TestUser user = queryTestUser();
        user.setField1(getRandomStr());
        user.setField2(getRandomStr());
        user.setField3(getRandomStr());
        user.setField4(getRandomStr());
        dataTemplate.update(user);
        dataCompareUtil.testSucccess(user);
    }

    public void testUpdateMap()
    {
        Map<String, Object> map = queryTestUser1Map();
        map.put("field1", getRandomStr());
        map.put("field2", getRandomStr());
        map.put("field3", getRandomStr());
        map.put("field4", getRandomStr());
        map.put("field5", getRandomStr());
        dataTemplate.update("test_user", map);
        Map<String, Object> map2 = queryTemplate.getMap("test_user", (Serializable) map.get("userId"));
        dataCompareUtil.testMapEqual(map, map2);
    }

    public void testUpdateEntityList()
    {
        List<TestUser> list = queryTestUserList(10);
        for (TestUser u : list)
        {
            u.setField1(getRandomStr());
            u.setField2(getRandomStr());
            u.setField3(getRandomStr());
            u.setField4(getRandomStr());
            u.setField5(getRandomStr());
        }
        dataTemplate.update(list);
        dataCompareUtil.testSuccess(list);
    }

    public void testUpdateMapList()
    {
        List<Map<String, Object>> list = queryTestUserMapList(10);
        for (Map<String, Object> map : list)
        {
            map.put("field1", getRandomStr());
            map.put("field2", getRandomStr());
            map.put("field3", getRandomStr());
            map.put("field4", getRandomStr());
            map.put("field5", getRandomStr());
        }
        dataTemplate.update("test_user", list);
        dataCompareUtil.testMapListSuccess(list, "test_user", null);
    }

    public void testUpdateRows()
    {
        List<TestUser> list = queryTestUserList(10);
        for (TestUser u : list)
        {
            u.setField1("t1");
            u.setField2("t2");
        }
        dataTemplate.update(list);
        Map<String, Object> valMap = new HashMap<String, Object>();
        String ufieldVal = "testrows";
        valMap.put("field1", ufieldVal);
        valMap.put("field2", ufieldVal);
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("field1", "t1");
        paramMap.put("field2", "t2");
        dataTemplate.updateRows("test_user", valMap, paramMap);

        long n1 = queryTemplate.createQueryBean("test_user").getCompare().eq("field1", "t1").eq("field2", "t2")
                .getQueryBean().getCount();
        Assert.assertEquals(n1, 0);

        long n2 = queryTemplate.createQueryBean("test_user").getCompare().eq("field1", ufieldVal)
                .eq("field2", ufieldVal).getQueryBean().getCount();
        Assert.assertEquals(n2, 10);
    }

}
