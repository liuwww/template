package org.liuwww.db.context;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.liuwww.db.sql.DbType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import org.liuwww.common.util.PropertyUtil;

public class DefaultTableMetaContext implements TableMetaContext
{

    private static Logger logger = LoggerFactory.getLogger(DefaultTableMetaContext.class);

    private List<DataSource> dsList;

    private List<DataSourceContext> dscList;

    private static String tablePropName = "table_config";

    private Map<String, String> prop;

    private ApplicationContext applicationContext;

    protected DefaultTableMetaContext(List<DataSource> dsList, ApplicationContext applicationContext)
    {
        this.dsList = dsList;
        this.applicationContext = applicationContext;
        dscList = new ArrayList<DataSourceContext>(dsList.size());
        try
        {
            prop = PropertyUtil.tryGetPropMap(tablePropName);
        }
        catch (Exception e)
        {
            logger.debug("table_config.properties配置文件不存在");
        }

    }

    @Override
    public void init()
    {
        boolean debug = logger.isDebugEnabled();
        if (debug)
        {
            logger.debug("开始加载数据库表元数据……");
        }
        int n = 1;
        for (DataSource ds : dsList)
        {
            if (debug)
            {
                logger.debug("开始加载数据源" + (n++) + "数据库表元数据……");
            }

            String includeTabels = getIncludeTables(ds);
            String excludeTables = getExcludeTables(ds);
            DataSourceContext dsc = new DefaultDataSourceContext(ds, includeTabels, excludeTables);
            dsc.init();
            dscList.add(dsc);

            if (debug)
            {
                logger.debug("数据源" + (n++) + "数据库表元数据加载完成");
            }

        }
        if (debug)
        {
            logger.debug("数据库表元数据加载完成");
        }
    }

    public String getIncludeTables(DataSource ds)
    {
        if (prop == null)
        {
            return null;
        }
        for (String key : prop.keySet())
        {
            String[] keys = key.split("\\.");
            if (keys.length == 2 && keys[1] == "includeTables")
            {
                try
                {
                    Object ds2 = applicationContext.getBean(keys[0]);
                    if (ds2 == ds)
                    {
                        return prop.get(key);
                    }
                }
                catch (BeansException e)
                {
                }
            }
        }
        return null;

    }

    public String getExcludeTables(DataSource ds)
    {
        if (prop == null)
        {
            return null;
        }
        for (String key : prop.keySet())
        {
            String[] keys = key.split("\\.");
            if (keys.length == 2 && keys[1] == "excludeTables")
            {
                try
                {
                    Object ds2 = applicationContext.getBean(keys[0]);
                    if (ds2 == ds)
                    {
                        return prop.get(key);
                    }
                }
                catch (BeansException e)
                {
                }
            }
        }
        return null;
    }

    @Override
    public TableMetaData getTableMetaData(String table)
    {
        for (DataSourceContext dsc : dscList)
        {
            TableMetaData tmd = dsc.getTableMetaData(table);
            if (tmd != null)
            {
                return tmd;
            }
        }
        return null;
    }

    @Override
    public TableMetaData getTableMetaData(String table, DataSource ds)
    {
        for (DataSourceContext dsc : dscList)
        {
            if (ds == dsc.getDataSource())
            {
                return dsc.getTableMetaData(table);
            }
        }
        return null;
    }

    @Override
    public TableMetaData getTableMetaData(String table, String dsBeanName)
    {
        DataSource ds = (DataSource) applicationContext.getBean(dsBeanName);
        if (ds != null)
        {
            return getTableMetaData(table, ds);
        }
        return null;
    }

    @Override
    public DataSource getDataSource(String dataSourceBeanName)
    {
        DataSource ds = (DataSource) applicationContext.getBean(dataSourceBeanName);
        if (ds != null)
        {
            return ds;
        }
        return null;
    }

    @Override
    public DbType getDbType(String dataSourceBeanName)
    {
        DataSource ds = (DataSource) applicationContext.getBean(dataSourceBeanName);
        if (ds != null)
        {
            for (DataSourceContext dsc : dscList)
            {
                if (ds == dsc.getDataSource())
                {
                    return dsc.getDbType();
                }
            }
        }
        return null;
    }

    @Override
    public DbType getDbType(DataSource ds)
    {
        if (ds != null)
        {
            for (DataSourceContext dsc : dscList)
            {
                if (ds == dsc.getDataSource())
                {
                    return dsc.getDbType();
                }
            }
        }
        return null;
    }

}
