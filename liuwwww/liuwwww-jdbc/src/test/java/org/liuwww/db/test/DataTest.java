package org.liuwww.db.test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.liuwww.db.service.IDataService;
import org.liuwww.db.service.IQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alibaba.fastjson.JSON;
import org.liuwww.common.execption.SysException;
import org.liuwww.common.util.BeanUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations =
{ "classpath:spring/spring.xml" })
public class DataTest
{
    @Autowired
    private IDataService dataService;

    @Autowired
    private IQueryService queryService;

    public void test1()
    {
        try
        {
            Staff staff = new Staff();
            staff.setWcode("test002");
            staff.setStaffName("test001");
            staff.setPassword("111111");
            staff.setDepId("1");
            staff.setOperaStaffId("0");
            JdbcTemplate jdbcTemplate = BeanUtil.getBean("jdbcTemplate2", JdbcTemplate.class);
            dataService.insert(staff, jdbcTemplate);
            System.out.println(JSON.toJSON(staff));
        }
        catch (SysException e)
        {
            e.printStackTrace();
        }
    }

    public Staff getEData()
    {
        Staff staff = new Staff();
        staff.setWcode("test002");
        staff.setStaffName("test001");
        staff.setPassword("111111");
        staff.setDepId("1");
        staff.setOperaStaffId("0");
        return staff;
    }

    public List<Staff> getStaffList()
    {
        List<Staff> list = new ArrayList<Staff>(10);
        for (int i = 0; i < 10; i++)
        {
            Staff staff = new Staff();
            staff.setWcode("test01" + i);
            staff.setStaffName("test01" + i);
            staff.setPassword("111111");
            staff.setDepId("1");
            staff.setOperaStaffId("0");
            list.add(staff);
        }
        return list;
    }

    public Map<String, Object> getMap()
    {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("wcode", "test1");
        map.put("staffName", "test1");
        map.put("password", "111111");
        map.put("depId", "1");
        map.put("operaStaffId", "0");
        return map;
    }

    public List<Map<String, Object>> getMapList()
    {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>(10);
        for (int i = 0; i < 10; i++)
        {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("wcode", "test02" + i);
            map.put("staffName", "test02" + i);
            map.put("password", "111111");
            map.put("depId", "1");
            map.put("operaStaffId", "0");
            list.add(map);
        }
        return list;
    }

    public void test2()
    {
        JdbcTemplate jdbcTemplate = BeanUtil.getBean("jdbcTemplate2", JdbcTemplate.class);
        dataService.insert(getStaffList(), jdbcTemplate);
    }

    @Test
    public void testInsert3()
    {
        dataService.insert("staff", getMap());
        dataService.insert("staff", getMapList());
    }

    @Test
    public void testInsert4()
    {
        JdbcTemplate jdbcTemplate = BeanUtil.getBean("jdbcTemplate2", JdbcTemplate.class);
        dataService.insert("staff", getMap(), jdbcTemplate);
        dataService.insert("staff", getMapList(), jdbcTemplate);
    }

    public void test5()
    {
        Staff f = getEData();
        f.setStaffId("228561456214310912");
        f.setCertCode("test");
        f.setBirthday("2019-01-01");
        dataService.update(f);
    }

    public void test6()
    {
        Staff f = getEData();
        f.setStaffId("228561456214310912");
        f.setCertCode("test");
        f.setBirthday("2019-01-01");
        JdbcTemplate jdbcTemplate = BeanUtil.getBean("jdbcTemplate2", JdbcTemplate.class);
        dataService.update(f, jdbcTemplate);
    }

    public void test7()
    {
        Map<String, Object> map = getMap();
        map.put("staffId", 228561196708528128L);
        map.put("wcode", "test123");
        map.put("certCode", "test1");
        dataService.update("staff", map);

        JdbcTemplate jdbcTemplate = BeanUtil.getBean("jdbcTemplate2", JdbcTemplate.class);
        map.put("staffId", 228561456663101440L);
        map.put("certCode", "test2");
        dataService.update("staff", map, jdbcTemplate);
    }

    public void test8()
    {
        List<Staff> list = queryService.createQueryBean("staff").getCompare().eq("depId", "1").eq("sts", "A")
                .getQueryBean().getBeanList(Staff.class);
        for (Staff f : list)
        {
            f.setCertCode("11111111");
        }
        dataService.update(list);
    }

    public void test9()
    {
        JdbcTemplate jdbcTemplate = BeanUtil.getBean("jdbcTemplate2", JdbcTemplate.class);
        List<Staff> list = queryService.createQueryBean("staff", jdbcTemplate).getCompare().eq("depId", "1")
                .eq("sts", "A").getQueryBean().getBeanList(Staff.class);
        for (Staff f : list)
        {
            f.setCertCode("111111113");
        }
        dataService.update(list, jdbcTemplate);
    }

    public void test10()
    {
        JdbcTemplate jdbcTemplate = BeanUtil.getBean("jdbcTemplate2", JdbcTemplate.class);
        List<Map<String, Object>> list = queryService.createQueryBean("staff", jdbcTemplate).getCompare()
                .eq("depId", "1").eq("sts", "A").getQueryBean().getMapList();
        for (Map<String, Object> f : list)
        {
            f.put("certCode", "1211111113");
        }
        dataService.update("staff", list, jdbcTemplate);
    }

    public List<Staff> queryStaffList(JdbcTemplate jdbcTemplate)
    {
        List<Staff> list = queryService.createQueryBean("staff", jdbcTemplate).getCompare().eq("depId", "1")
                .eq("sts", "A").getQueryBean().getBeanList(Staff.class);
        return list;
    }

    public List<Map<String, Object>> queryMapList(JdbcTemplate jdbcTemplate)
    {
        List<Map<String, Object>> list = queryService.createQueryBean("staff", jdbcTemplate).getCompare()
                .eq("depId", "1").eq("sts", "A").getQueryBean().getMapList();
        return list;
    }

    @Test
    public void testDelete1()
    {
        System.out.println("测试delete(Entity<Staff> entity)");
        List<Staff> list = queryStaffList(null);
        System.out.println("数据数量:" + list.size());
        dataService.delete(list.get(0));
        list = queryStaffList(null);
        System.out.println("数据数量:" + list.size());
    }

    @Test
    public void testDelete2()
    {
        System.out.println("测试delete(String tableName, Map<String, Object> idMap) ");
        List<Map<String, Object>> list = queryMapList(null);
        System.out.println("数据数量:" + list.size());
        dataService.delete("staff", list.get(0));
        list = queryMapList(null);
        System.out.println("数据数量:" + list.size());
    }

    @Test
    public void testDelete3()
    {
        System.out.println("测试delete(List<? extends Entity<Staff>> enityList)  ");
        List<Staff> list = queryStaffList(null);
        System.out.println("数据数量:" + list.size());
        dataService.delete(list);
        list = queryStaffList(null);
        System.out.println("数据数量:" + list.size());
        testInsert3();
    }

    public void testDelete4()
    {
        System.out.println("测试delete(Entity<T> entity, JdbcTemplate jdbcTemplate)");
        JdbcTemplate jdbcTemplate = getJdbcTemplate();
        List<Staff> list = queryStaffList(jdbcTemplate);
        System.out.println("数据数量:" + list.size());
        dataService.delete(list.get(0), jdbcTemplate);
        list = queryStaffList(jdbcTemplate);
        System.out.println("数据数量:" + list.size());
    }

    public void testDelete5()
    {
        System.out.println("测试delete(List<? extends Entity<Staff>> enityList, JdbcTemplate jdbcTemplate)");
        List<Staff> list = queryStaffList(getJdbcTemplate());
        System.out.println("数据数量:" + list.size());
        dataService.delete(list, getJdbcTemplate());
        list = queryStaffList(getJdbcTemplate());
        System.out.println("数据数量:" + list.size());
        testInsert4();
    }

    public void testDelete6()
    {
        System.out.println("测试delete(String tableName, List<Map<String, Object>> idMapList)");
        List<Map<String, Object>> list = queryMapList(null);
        System.out.println("数据数量:" + list.size());
        dataService.delete("staff", list);
        list = queryMapList(null);
        System.out.println("数据数量:" + list.size());
        testInsert3();
    }

    public void testDelete7()
    {
        System.out.println("测试delete(String table, Serializable id) ");
        List<Staff> list = queryStaffList(null);
        System.out.println("数据数量:" + list.size());
        dataService.delete("staff", list.get(0).getStaffId());
        list = queryStaffList(null);
        System.out.println("数据数量:" + list.size());
    }

    public void testDelete8()
    {
        System.out.println("测试delete(String table, Serializable[] ids) ");
        List<Staff> list = queryStaffList(null);
        System.out.println("数据数量:" + list.size());
        List<Serializable> idList = new ArrayList<Serializable>(list.size());
        for (Staff f : list)
        {
            idList.add(f.getStaffId());
        }
        dataService.delete("staff", idList.toArray(new Serializable[0]));
        list = queryStaffList(null);
        System.out.println("数据数量:" + list.size());
        testInsert3();
    }

    public void testDelete9()
    {
        System.out
                .println("测试delete(String tableName, List<Map<String, Object>> idMapList, JdbcTemplate jdbcTemplate) ");
        List<Map<String, Object>> list = queryMapList(getJdbcTemplate());
        System.out.println("数据数量:" + list.size());
        dataService.delete("staff", list, getJdbcTemplate());
        list = queryMapList(getJdbcTemplate());
        System.out.println("数据数量:" + list.size());
        testInsert4();
    }

    public void testDelete10()
    {
        System.out.println("测试delete(String tableName, Map<String, Object> idMap, JdbcTemplate jdbcTemplate)");
        List<Map<String, Object>> list = queryMapList(getJdbcTemplate());
        System.out.println("数据数量:" + list.size());
        dataService.delete("staff", list.get(0), getJdbcTemplate());
        list = queryMapList(getJdbcTemplate());
        System.out.println("数据数量:" + list.size());
    }

    public void testDelete11()
    {
        System.out.println("测试delete(String tableName, Serializable id, JdbcTemplate jdbcTemplate)");
        List<Staff> list = queryStaffList(getJdbcTemplate());
        System.out.println("数据数量:" + list.size());
        dataService.delete("staff", list.get(0).getStaffId(), getJdbcTemplate());
        list = queryStaffList(getJdbcTemplate());
        System.out.println("数据数量:" + list.size());
    }

    public void testDelete12()
    {
        System.out.println("测试delete(String table, Serializable[] ids, JdbcTemplate jdbcTemplate) ");
        List<Staff> list = queryStaffList(getJdbcTemplate());
        System.out.println("数据数量:" + list.size());
        List<Serializable> idList = new ArrayList<Serializable>(list.size());
        for (Staff f : list)
        {
            idList.add(f.getStaffId());
        }
        dataService.delete("staff", idList.toArray(new Serializable[0]), getJdbcTemplate());
        list = queryStaffList(getJdbcTemplate());
        System.out.println("数据数量:" + list.size());
        testInsert4();
    }

    public void testDelete13()
    {
        System.out.println("测试deleteRows(String tableName, Map<String, Object> paramMap)");
        List<Staff> list = queryStaffList(null);
        System.out.println("数据数量:" + list.size());
        Map<String, Object> paramMap = new HashMap<String, Object>(2);
        paramMap.put("sts", "A");
        paramMap.put("depId", "1");
        dataService.deleteRows("staff", paramMap);
        list = queryStaffList(null);
        System.out.println("数据数量:" + list.size());
    }

    public void testDelete14()
    {
        System.out.println("测试deleteRows(String tableName, Map<String, Object> paramMap, JdbcTemplate jdbcTemplate)");
        List<Staff> list = queryStaffList(getJdbcTemplate());
        System.out.println("数据数量:" + list.size());
        Map<String, Object> paramMap = new HashMap<String, Object>(2);
        paramMap.put("sts", "A");
        paramMap.put("depId", "1");
        dataService.deleteRows("staff", paramMap, getJdbcTemplate());
        list = queryStaffList(getJdbcTemplate());
        System.out.println("数据数量:" + list.size());
    }

    public JdbcTemplate getJdbcTemplate()
    {
        return BeanUtil.getBean("jdbcTemplate2", JdbcTemplate.class);
    }

    @Test
    public void test()
    {
        try
        {
            testDelete1();
            testDelete2();
            testDelete3();
            testDelete4();
            testDelete5();
            testDelete6();
            testDelete7();
            testDelete8();
            testDelete9();
            testDelete10();
            testDelete11();
            testDelete12();
            testDelete13();
            testDelete14();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    @Test
    public void tttt()
    {
        queryMapList(null);
        System.out.println("-----------------------------------");
    }
}
