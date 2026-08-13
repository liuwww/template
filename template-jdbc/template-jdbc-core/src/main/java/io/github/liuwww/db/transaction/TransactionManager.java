package io.github.liuwww.db.transaction;

import javax.sql.DataSource;

public interface TransactionManager {

    void init(DataSource dataSource);

    void begin();

    void commit();

    void rollback();

    <T> T executeInTransaction(TransactionCallback<T> callback);

    boolean isInTransaction();
}
