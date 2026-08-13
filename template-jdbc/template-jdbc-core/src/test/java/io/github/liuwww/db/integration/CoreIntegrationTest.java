package io.github.liuwww.db.integration;

import org.junit.Test;
import io.github.liuwww.db.dao.IQueryDao;
import io.github.liuwww.db.dao.impl.QueryDao;

/**
 * Core模块集成测试
 *
 * @author liuwww
 * @date 2026-03-01
 */
public class CoreIntegrationTest {

    @Test
    public void testQueryDaoInstantiation() {
        // 测试QueryDao是否可以正常实例化
        IQueryDao queryDao = new QueryDao();
        assert (queryDao != null);
    }
}
