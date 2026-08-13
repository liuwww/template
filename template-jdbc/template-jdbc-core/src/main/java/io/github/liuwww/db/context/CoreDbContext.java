package io.github.liuwww.db.context;

import javax.sql.DataSource;

import io.github.liuwww.db.config.DatabaseConfigCenter;
import io.github.liuwww.db.config.impl.GenericDatabaseConfigCenter;
import io.github.liuwww.db.executor.DatabaseExecutor;
import io.github.liuwww.db.service.IDataTemplate;
import io.github.liuwww.db.service.IQueryTemplate;
import io.github.liuwww.db.service.impl.DataTemplate;
import io.github.liuwww.db.service.impl.QueryTemplate;
import io.github.liuwww.db.sql.DbType;
import io.github.liuwww.db.sql.TableDefaultValue;
import io.github.liuwww.db.sql.TableDefaultValueImpl;
import io.github.liuwww.db.transaction.TransactionCallback;
import io.github.liuwww.db.transaction.TransactionManager;
import io.github.liuwww.db.transaction.impl.JdbcTransactionManager;
import io.github.liuwww.common.Idgen.IdGenerator;
import io.github.liuwww.common.execption.DbException;

public class CoreDbContext {

    private DatabaseConfigCenter configCenter;
    private IdGenerator idGenerator;
    private volatile IDataTemplate dataTemplate;
    private volatile IQueryTemplate queryTemplate;
    private TableDefaultValue tableDefaultValue;
    private volatile TransactionManager transactionManager;
    private volatile boolean initialized = false;

    public CoreDbContext() {
        this.configCenter = new GenericDatabaseConfigCenter();
        this.tableDefaultValue = new TableDefaultValueImpl();
    }

    public CoreDbContext(DatabaseConfigCenter configCenter) {
        this.configCenter = configCenter;
        this.tableDefaultValue = new TableDefaultValueImpl();
    }

    public void init() {
        configCenter.init();
        this.initialized = true;
    }

    public void addDataSource(DataSource dataSource) {
        if (configCenter instanceof GenericDatabaseConfigCenter) {
            ((GenericDatabaseConfigCenter) configCenter).addDataSource(dataSource);
        }
        if (this.transactionManager == null) {
            synchronized (this) {
                if (this.transactionManager == null) {
                    this.transactionManager = new JdbcTransactionManager();
                    this.transactionManager.init(dataSource);
                }
            }
        }
    }

    public TransactionManager getTransactionManager() {
        return this.transactionManager;
    }

    public <T> T executeInTransaction(TransactionCallback<T> callback) {
        if (this.transactionManager == null) {
            throw new DbException("事务管理器未初始化，请先调用addDataSource");
        }
        return this.transactionManager.executeInTransaction(callback);
    }

    public void setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public void setDatabaseExecutor(DatabaseExecutor databaseExecutor) {
        configCenter.setDatabaseExecutor(databaseExecutor);
    }

    public DbType getDbTypeForTable(String tableName) {
        TableMetaData tmd = configCenter.getTableMetaData(tableName);
        if (tmd == null) {
            throw new DbException("table[" + tableName + "]不存在");
        }
        return tmd.getDbType();
    }

    public IDataTemplate getDataTemplate() {
        if (this.dataTemplate == null) {
            synchronized (this) {
                if (this.dataTemplate == null) {
                    this.dataTemplate = new DataTemplate(configCenter, idGenerator, transactionManager);
                }
            }
        }
        // 若 dataTemplate 已缓存但 transactionManager 后续才初始化，补注入
        if (transactionManager != null && this.dataTemplate instanceof DataTemplate) {
            ((DataTemplate) this.dataTemplate).setTransactionManager(transactionManager);
        }
        return this.dataTemplate;
    }

    public IQueryTemplate getQueryTemplate() {
        if (this.queryTemplate == null) {
            synchronized (this) {
                if (this.queryTemplate == null) {
                    this.queryTemplate = new QueryTemplate(configCenter);
                }
            }
        }
        return this.queryTemplate;
    }

    public TableDefaultValue getTableDefaultValue() {
        return this.tableDefaultValue;
    }

    public void refresh() {
        configCenter.refresh();
    }

    public DatabaseConfigCenter getConfigCenter() {
        return configCenter;
    }

    public boolean isInitialized() {
        return initialized;
    }
}
