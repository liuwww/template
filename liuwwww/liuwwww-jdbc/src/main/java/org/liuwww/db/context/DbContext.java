package org.liuwww.db.context;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.liuwww.db.dao.IDataDao;
import org.liuwww.db.dao.IQueryDao;
import org.liuwww.db.dao.impl.DataDao;
import org.liuwww.db.dao.impl.QueryDao;
import org.liuwww.db.service.IDataService;
import org.liuwww.db.service.IQueryService;
import org.liuwww.db.service.impl.DataService;
import org.liuwww.db.service.impl.QueryService;
import org.liuwww.db.sql.DbType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import org.liuwww.common.execption.SysException;
import org.liuwww.common.util.BeanUtil;

@Component
public class DbContext
{

    private static DbContext instance;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private List<DataSource> dsList;

    @Autowired
    private List<JdbcTemplate> jtList;

    private TableMetaContext tmc;

    private DbContext()
    {
        if (instance != null)
        {
            throw new SysException("DbContext不可多次实例化！");
        }
        instance = this;
    }

    @PostConstruct
    public void init()
    {
        tmc = new DefaultTableMetaContext(dsList, applicationContext);
        tmc.init();
    }

    public void refresh()
    {
        TableMetaContext tmc = new DefaultTableMetaContext(dsList, applicationContext);
        tmc.init();
        this.tmc = tmc;
    }

    public static TableMetaContext getTableMetaContext()
    {
        return instance.tmc;
    }

    public static JdbcTemplate getJdbcTemplateForTable(String tableName)
    {
        TableMetaData tmd = instance.tmc.getTableMetaData(tableName);
        if (tmd == null)
        {
            throw new SysException("table[" + tableName + "]不存在");
        }
        DataSource ds = tmd.getDataSource();
        for (JdbcTemplate jt : instance.jtList)
        {
            if (ds == jt.getDataSource())
            {
                return jt;
            }
        }
        return null;
    }

    public static List<JdbcTemplate> getJdbcTemplateList()
    {
        return instance.jtList;
    }

    public static DbType getDbTypeForTable(String tableName)
    {
        TableMetaData tmd = instance.tmc.getTableMetaData(tableName);
        DbType dbtype = tmd.getDbType();
        return dbtype;
    }

    public static DbType getDbTypeForJdbcTemlate(String jdbcTemplateBeanName)
    {
        JdbcTemplate jdbcTemplate = BeanUtil.getBean(jdbcTemplateBeanName, JdbcTemplate.class);
        if (jdbcTemplate == null)
        {
            throw new SysException("没有beanName为" + jdbcTemplateBeanName + "的JdbcTemplate实例");
        }
        return instance.tmc.getDbType(jdbcTemplate.getDataSource());
    }

    public static DbType getDbTypeForJdbcTemlate(JdbcTemplate jdbcTemplate)
    {
        return instance.tmc.getDbType(jdbcTemplate.getDataSource());
    }

    public static DbType getDbType(String tableName, JdbcTemplate jdbcTemplate)
    {
        DbType type = null;
        if (jdbcTemplate != null)
        {
            type = getDbTypeForJdbcTemlate(jdbcTemplate);
        }
        else
        {
            type = getDbTypeForTable(tableName);
        }
        return type;
    }

    public static TableMetaData getTableMetaData(String tableName, JdbcTemplate jdbcTemplate)
    {
        TableMetaData tmd = null;
        if (jdbcTemplate == null)
        {
            tmd = instance.tmc.getTableMetaData(tableName);
        }
        else
        {
            tmd = instance.tmc.getTableMetaData(tableName, jdbcTemplate.getDataSource());
        }
        if (tmd == null)
        {
            throw new SysException("表[" + tableName + "]不存在或没加载！");
        }
        return tmd;
    }

    @Bean
    public IDataDao getDataDao()
    {
        return new DataDao();
    }

    @Bean
    public IQueryDao getQueryDao()
    {
        return new QueryDao();
    }

    @Bean
    public IDataService getDataService()
    {
        return new DataService();
    }

    @Bean
    public IQueryService getQueryService()
    {
        return new QueryService();
    }

}
