package io.github.liuwww.db.test;

import static org.junit.Assert.*;

import org.junit.Test;
import io.github.liuwww.db.update.DefaultUpdateBean;
import io.github.liuwww.db.update.UpdateBean;

/**
 * UpdateBean 测试类
 */
public class UpdateBeanTest {

    @Test
    public void testUpdateBeanInstantiation() {
        // 注意：这需要 IDataTemplate 实例
        // 这里只测试基本的链式调用结构
        UpdateBean updateBean = new DefaultUpdateBean("test_user", null);
        assertNotNull(updateBean);
    }

    @Test
    public void testUpdateBeanChainCall() {
        UpdateBean updateBean = new DefaultUpdateBean("test_user", null);
        
        UpdateBean result = updateBean
            .addValue("name", "张三")
            .addValue("age", 25)
            .addCondition("id", "1");
        
        assertNotNull(result);
    }
}
