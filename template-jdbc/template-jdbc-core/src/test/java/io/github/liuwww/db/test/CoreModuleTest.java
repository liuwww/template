package io.github.liuwww.db.test;

import org.junit.Test;
import io.github.liuwww.db.dao.IQueryDao;
import io.github.liuwww.db.dao.impl.QueryDao;

import static org.junit.Assert.*;

/**
 * Core模块基本功能测试
 *
 * @author liuwww
 * @date 2026-03-01
 */
public class CoreModuleTest {

    @Test
    public void testQueryDaoInstantiation() {
        // 测试QueryDao能否正常实例化（不含Spring依赖）
        IQueryDao queryDao = new QueryDao();
        assertNotNull("QueryDao should be instantiated successfully", queryDao);
    }

    @Test
    public void testInterfaceCompatibility() {
        // 测试接口兼容性
        IQueryDao queryDao = new QueryDao();
        assertTrue(queryDao instanceof IQueryDao);
    }
}
