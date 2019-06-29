package org.liuwww.db.dao;

import java.util.List;
import java.util.Map;

import org.liuwww.db.page.Page;
import org.liuwww.db.sql.SqlBean;

import org.liuwww.common.execption.SysException;

public interface IQueryDao
{
    public List<Map<String, Object>> getList(SqlBean sqlBean) throws SysException;

    public <T> T getBean(SqlBean sqlBean, Class<T> clazz) throws SysException;

    public <T> List<T> getBeanList(SqlBean sqlBean, Class<T> clazz) throws SysException;

    public Map<String, Object> getMap(SqlBean sqlBean) throws SysException;

    public <T> Page getPage(SqlBean bean, Page pageInfo, Class<T> clazz) throws SysException;

    public Page getPage(SqlBean bean, Page pageInfo) throws SysException;

    public long getCount(SqlBean bean) throws SysException;
}
