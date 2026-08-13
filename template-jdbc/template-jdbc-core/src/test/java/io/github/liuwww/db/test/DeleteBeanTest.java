package io.github.liuwww.db.test;

import static org.junit.Assert.*;

import org.junit.Test;
import io.github.liuwww.db.update.DefaultDeleteBean;
import io.github.liuwww.db.update.DeleteBean;

/**
 * DeleteBean 测试类
 */
public class DeleteBeanTest {

    @Test
    public void testDeleteBeanInstantiation() {
        DeleteBean deleteBean = new DefaultDeleteBean("test_user", null);
        assertNotNull(deleteBean);
    }

    @Test
    public void testDeleteBeanChainCall() {
        DeleteBean deleteBean = new DefaultDeleteBean("test_user", null);
        
        DeleteBean result = deleteBean
            .addCondition("id", "1")
            .addCondition("status", 0);
        
        assertNotNull(result);
    }
}
