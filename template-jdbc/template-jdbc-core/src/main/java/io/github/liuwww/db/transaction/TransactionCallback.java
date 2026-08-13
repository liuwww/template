package io.github.liuwww.db.transaction;

@FunctionalInterface
public interface TransactionCallback<T> {
    T doInTransaction();
}
