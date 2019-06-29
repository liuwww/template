package org.liuwww.db.test.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.liuwww.db.test.entity.TestUser2;
import org.springframework.jdbc.core.JdbcTemplate;

public class Update4MutiDs extends AbstractTest
{

    public Update4MutiDs(AbstractTest test)
    {
        super(test);
    }

    private void testUpdateEntity(JdbcTemplate jdbcTemplate)
    {
        TestUser2 user = queryTestUser2(jdbcTemplate1);
        user.setField1(getRandomStr());
        user.setField2(getRandomStr());
        user.setField3(getRandomStr());
        user.setField4(getRandomStr());
        dataTemplate.update(user, jdbcTemplate1);
        dataCompareUtil.testInsertSucccess(user, jdbcTemplate1);
    }

    public void testUpdateEntity()
    {
        testUpdateEntity(jdbcTemplate1);
        testUpdateEntity(jdbcTemplate2);
    }

    private void testUpdateMap(JdbcTemplate jdbcTemplate)
    {
        Map<String, Object> map = queryTestUser2Map(jdbcTemplate);
        map.put("field1", getRandomStr());
        map.put("field2", getRandomStr());
        map.put("field3", getRandomStr());
        map.put("field4", getRandomStr());
        map.put("field5", getRandomStr());
        dataTemplate.update("test_user2", map, jdbcTemplate);
        Map<String, Object> map2 = queryTemplate.createQueryBean("test_user2", jdbcTemplate).getCompare()
                .eq("userId", map.get("userId")).getQueryBean().getMap();
        dataCompareUtil.testMapEqual(map, map2);
    }

    public void testUpdateMap()
    {
        testUpdateMap(jdbcTemplate1);
        testUpdateMap(jdbcTemplate2);
    }

    private void testUpdateEntityList(JdbcTemplate jdbcTemplate)
    {
        List<TestUser2> list = queryTestUser2List(10, jdbcTemplate);
        for (TestUser2 u : list)
        {
            u.setField1(getRandomStr());
            u.setField2(getRandomStr());
            u.setField3(getRandomStr());
            u.setField4(getRandomStr());
            u.setField5(getRandomStr());
        }
        dataTemplate.update(list, jdbcTemplate);
        dataCompareUtil.testInsertSuccess(list, jdbcTemplate);
    }

    public void testUpdateEntityList()
    {
        testUpdateEntityList(jdbcTemplate1);
        testUpdateEntityList(jdbcTemplate2);
    }

    private void testUpdateMapList(JdbcTemplate jdbcTemplate)
    {
        List<Map<String, Object>> list = queryTestUser2MapList(10, jdbcTemplate);
        for (Map<String, Object> map : list)
        {
            map.put("field1", getRandomStr());
            map.put("field2", getRandomStr());
            map.put("field3", getRandomStr());
            map.put("field4", getRandomStr());
            map.put("field5", getRandomStr());
        }
        dataTemplate.update("test_user2", list, jdbcTemplate);
        dataCompareUtil.testMapListSuccess(list, "test_user2", jdbcTemplate);
    }

    public void testUpdateMapList()
    {
        testUpdateMapList(jdbcTemplate1);
        testUpdateMapList(jdbcTemplate2);
    }

    private void testUpdateRows(JdbcTemplate jdbcTemplate)
    {
        List<TestUser2> list = queryTestUser2List(10, jdbcTemplate);
        for (TestUser2 u : list)
        {
            u.setField1("t1");
            u.setField2("t2");
        }
        dataTemplate.update(list, jdbcTemplate);
        Map<String, Object> valMap = new HashMap<String, Object>();
        String ufieldVal = "testrows";
        valMap.put("field1", ufieldVal);
        valMap.put("field2", ufieldVal);
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("field1", "t1");
        paramMap.put("field2", "t2");
        dataTemplate.updateRows("test_user2", valMap, paramMap, jdbcTemplate);

        long n1 = queryTemplate.createQueryBean("test_user2", jdbcTemplate).getCompare().eq("field1", "t1")
                .eq("field2", "t2").getQueryBean().getCount();
        Assert.assertEquals(n1, 0);

        long n2 = queryTemplate.createQueryBean("test_user2", jdbcTemplate).getCompare().eq("field1", ufieldVal)
                .eq("field2", ufieldVal).getQueryBean().getCount();
        Assert.assertEquals(n2, 10);
    }

    public void testUpdateRows()
    {
        testUpdateRows(jdbcTemplate1);
        testUpdateRows(jdbcTemplate2);
    }

}
