package io.github.liuwww.db.config;

import javax.sql.DataSource;

/**
 * 表结构加载器接口
 * 定义表结构加载行为
 *
 * @author liuwww
 */
public interface TableStructureLoader {

    /**
     * 加载表结构
     *
     * @return 表结构配置容器
     */
    TableStructureContainer loadStructureContainer();
}