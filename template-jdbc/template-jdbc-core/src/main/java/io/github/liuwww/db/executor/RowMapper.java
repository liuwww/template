package io.github.liuwww.db.executor;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 结果集行映射器接口
 * 
 * @author liuwww
 * @date 2026-03-01
 * 
 * @param <T> 映射结果类型
 */
public interface RowMapper<T> {

    /**
     * 将结果集中的当前行映射为对象
     * 
     * @param rs     结果集
     * @param rowNum 当前行号（从0开始）
     * @return 映射后的对象
     * @throws SQLException SQL异常
     */
    T mapRow(ResultSet rs, int rowNum) throws SQLException;
}