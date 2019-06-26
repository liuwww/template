package org.liuwww.db.test.util;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Assert;
import org.liuwww.common.util.DateUtil;
import org.liuwww.common.util.DbNameConverter;
import org.liuwww.common.util.StringUtil;
import org.liuwww.db.context.DbContext;
import org.liuwww.db.context.TableMetaData;
import org.liuwww.db.service.IQueryTemplate;
import org.liuwww.db.sql.Row;
import org.liuwww.db.test.entity.TestUser;
import org.liuwww.db.test.entity.TestUser2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

@Component
public class DataCompareUtil
{
    @Autowired
    private IQueryTemplate queryService;

    public void testInsertSuccess4Map(List<Row> rl)
    {
        for (Row r : rl)
        {
            testInsertSucccess(r);
        }
    }

    public void testSuccess(List<TestUser> list)
    {
        for (TestUser u : list)
        {
            testSucccess(u);
        }
    }

    public void testInsertSucccess(Row row)
    {
        String id = row.getIdValue();
        Assert.assertNotNull(id);
        Map<String, Object> map2 = queryService.getMap(row.getTableName(), row.getIdValue());
        testMapEqual(DataUtil.convertMapKey(row.getRowValueMap()), map2);
    }

    @SuppressWarnings(
    { "rawtypes", "unchecked" })
    public void testSucccess(TestUser user)
    {
        TestUser u = queryService.createQueryBean(user.tableName()).getCompare().eq("userId", user.getUserId())
                .getQueryBean().getBean(TestUser.class);
        Assert.assertNotNull(u);

        testMapEqual((Map) JSONObject.toJSON(user), (Map) JSONObject.toJSON(u));
    }

    public void testMapEqual(Map<String, Object> map1, Map<String, Object> map2)
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
                    if (val2 instanceof Date)
                    {
                        val2 = DateUtil.format((Date) val2, DateUtil.datetimePattern);
                    }
                    else if (val2 instanceof String)
                    {
                        val2 = val2.toString().split("\\.")[0];
                    }

                    if (val1 != null)
                    {
                        val1 = val1.toString().split("\\.")[0];
                    }

                    continue;
                }
                if (!StringUtil.equals(val1.toString(), val2.toString()))
                {
                    throw new RuntimeException("保存字段[" + key + "]不一致");
                }
            }

        }
    }

    public void testInsertSuccess4Map(List<Row> rl, JdbcTemplate jdbcTemplate)
    {
        for (Row r : rl)
        {
            testInsertSucccess(r, jdbcTemplate);
        }
    }

    public void testInsertSuccess(List<TestUser2> list, JdbcTemplate jdbcTemplate)
    {
        for (TestUser2 u : list)
        {
            testInsertSucccess(u, jdbcTemplate);
        }
    }

    public void testInsertSucccess(Row row, JdbcTemplate jdbcTemplate)
    {
        String id = row.getIdValue();
        Assert.assertNotNull(id);
        Map<String, Object> map2 = queryService.createQueryBean(row.getTableName(), jdbcTemplate).getCompare()
                .eq(row.getIdName(), row.getIdValue()).getQueryBean().getMap();
        testMapEqual(DataUtil.convertMapKey(row.getRowValueMap()), map2);
    }

    @SuppressWarnings(
    { "rawtypes", "unchecked" })
    public void testInsertSucccess(TestUser2 user, JdbcTemplate jdbcTemplate)
    {
        TestUser2 u = queryService.createQueryBean(user.tableName(), jdbcTemplate).getCompare()
                .eq("userId", user.getUserId()).getQueryBean().getBean(TestUser2.class);
        Assert.assertNotNull(u);

        testMapEqual((Map) JSONObject.toJSON(user), (Map) JSONObject.toJSON(u));
    }

    public void testMapListSuccess(List<Map<String, Object>> list, String tableName, JdbcTemplate jdbcTemplate)
    {
        for (Map<String, Object> m : list)
        {
            TableMetaData tmd = DbContext.getTableMetaData(tableName, jdbcTemplate);
            Serializable id = (Serializable) m.get(tmd.getIdColumn().getName());
            Map<String, Object> map = queryService.createQueryBean(tableName, jdbcTemplate).getCompare()
                    .eq(tmd.getIdColumn().getName(), id).getQueryBean().getMap();
            if (map == null)
            {
                System.out.println(id);
            }
            testMapEqual(m, map);
        }
    }
}
