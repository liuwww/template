package io.github.liuwww.common.Idgen;

import java.util.UUID;

import io.github.liuwww.db.context.TableMetaData;

public class UUIDGenerator implements IdGenerator {
    @Override
    public String nextKey(TableMetaData tableMetaData) {
        return UUID.randomUUID().toString().replace("-", "");
    }

    @Override
    public int nextIntKey(TableMetaData tableMetaData) {
        throw new UnsupportedOperationException("仅仅支持字符串类型的key，不支持int类型");
    }

    @Override
    public long nextLongKey(TableMetaData tableMetaData) {
        throw new UnsupportedOperationException("仅仅支持字符串类型的key，不支持long类型");
    }

    @Override
    public boolean isCreatedByDatabase(TableMetaData tableMetaData) {
        return false;
    }
}
