package io.github.liuwww.common.Idgen;

import io.github.liuwww.db.context.TableMetaData;

/**
 * 数据库自增主键策略。
 * <p>
 * 标记主键由数据库自动生成（MySQL {@code AUTO_INCREMENT}、PostgreSQL {@code SERIAL}、
 * Oracle 序列等）。{@link #nextKey(TableMetaData)} 返回 {@code null} 作为占位，
 * 实际 ID 由数据库在 insert 时生成并通过
 * {@code DatabaseExecutor.insertWithAutoGenKey(...)} 回填到实体/Row。
 * </p>
 * <p>
 * 启用此策略后，{@code DataTemplate} 的插入流程会走
 * {@code isCreatedByDatabase == true} 分支，调用
 * {@code insertWithAutoGenKey} / {@code batchInsertWithAutoGenKey} 拿回数据库生成的主键。
 * </p>
 *
 * @author liuwww
 * @since 2026-07
 */
public class DatabaseAutoIncrementIdGenerator implements IdGenerator {

    @Override
    public String nextKey(TableMetaData tableMetaData) {
        // 由数据库自增生成，应用层不提供主键值。
        // 返回 null 作为占位，DataTemplate 会走 insertWithAutoGenKey 分支。
        return null;
    }

    @Override
    public int nextIntKey(TableMetaData tableMetaData) {
        throw new UnsupportedOperationException("主键由数据库自增生成，应用层不生成 int 类型 ID");
    }

    @Override
    public long nextLongKey(TableMetaData tableMetaData) {
        throw new UnsupportedOperationException("主键由数据库自增生成，应用层不生成 long 类型 ID");
    }

    @Override
    public boolean isCreatedByDatabase(TableMetaData tableMetaData) {
        return true;
    }
}
