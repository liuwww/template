package org.liuwww.db.dao;

import java.util.List;
import java.util.Map;

import org.liuwww.db.page.Page;
import org.liuwww.db.sql.SqlBean;

import org.liuwww.common.execption.DbException;

public interface IQueryDao
{
    public List<Map<String, Object>> getList(SqlBean sqlBean) throws DbException;

    public <T> T getBean(SqlBean sqlBean, Class<T> clazz) throws DbException;

    public <T> List<T> getBeanList(SqlBean sqlBean, Class<T> clazz) throws DbException;

    public Map<String, Object> getMap(SqlBean sqlBean) throws DbException;

    public <T> Page getPage(SqlBean bean, Page pageInfo, Class<T> clazz) throws DbException;

    public Page getPage(SqlBean bean, Page pageInfo) throws DbException;

    public long getCount(SqlBean bean) throws DbException;
}
