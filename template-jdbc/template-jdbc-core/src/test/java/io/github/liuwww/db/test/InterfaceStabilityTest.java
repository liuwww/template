package io.github.liuwww.db.test;

import java.lang.reflect.Method;
import java.util.Map;

import org.junit.Test;
import io.github.liuwww.common.entity.TableEntity;
import io.github.liuwww.db.page.Page;
import io.github.liuwww.db.query.QueryBean;
import io.github.liuwww.db.query.QueryBeanFactory;
import io.github.liuwww.db.query.conditon.QueryBeanCompare;
import io.github.liuwww.db.service.IDataTemplate;
import io.github.liuwww.db.service.IQueryTemplate;
import io.github.liuwww.db.sql.SqlBean;
import org.junit.Assert;

public class InterfaceStabilityTest extends Assert {

    @Test
    public void testIDataTemplateInterface() throws Exception {
        Class<?> clazz = IDataTemplate.class;

        assertNotNull(clazz.getMethod("insert", String.class, Map.class));
        assertNotNull(clazz.getMethod("insert", String.class, java.util.List.class));
        assertNotNull(clazz.getMethod("insert", TableEntity.class));
        assertNotNull(clazz.getMethod("insert", java.util.List.class));

        assertNotNull(clazz.getMethod("update", String.class, Map.class));
        assertNotNull(clazz.getMethod("update", String.class, java.util.List.class));
        assertNotNull(clazz.getMethod("update", TableEntity.class));
        assertNotNull(clazz.getMethod("update", java.util.List.class));
        assertNotNull(clazz.getMethod("updateRows", String.class, Map.class, Map.class));

        assertNotNull(clazz.getMethod("delete", String.class, Map.class));
        assertNotNull(clazz.getMethod("delete", String.class, java.util.List.class));
        assertNotNull(clazz.getMethod("delete", String.class, java.io.Serializable.class));
        assertNotNull(clazz.getMethod("delete", String.class, java.io.Serializable[].class));
        assertNotNull(clazz.getMethod("delete", TableEntity.class));
        assertNotNull(clazz.getMethod("delete", java.util.List.class));
        assertNotNull(clazz.getMethod("deleteRows", String.class, Map.class));

        assertNotNull(clazz.getMethod("createUpdateBean", String.class));
        assertNotNull(clazz.getMethod("createDeleteBean", String.class));
    }

    @Test
    public void testIQueryTemplateInterface() throws Exception {
        Class<?> clazz = IQueryTemplate.class;

        assertNotNull(clazz.getMethod("getBeanList", TableEntity.class));
        assertNotNull(clazz.getMethod("getBean", TableEntity.class));
        assertNotNull(clazz.getMethod("getPage", TableEntity.class, Page.class));
        assertNotNull(clazz.getMethod("getPage", String.class, Map.class));
        assertNotNull(clazz.getMethod("getPage", String.class, Page.class, Map.class));
        assertNotNull(clazz.getMethod("getMapList", String.class, Map.class));
        assertNotNull(clazz.getMethod("getCount", TableEntity.class));
        assertNotNull(clazz.getMethod("getMap", String.class, java.io.Serializable.class));
        assertNotNull(clazz.getMethod("getBean", String.class, java.io.Serializable.class, Class.class));
    }

    @Test
    public void testQueryBeanInterface() throws Exception {
        Class<?> clazz = QueryBean.class;

        assertNotNull(clazz.getMethod("getBean", Class.class));
        assertNotNull(clazz.getMethod("getBeanList", Class.class));
        assertNotNull(clazz.getMethod("getBeanList", Class.class, int.class));
        assertNotNull(clazz.getMethod("getMap"));
        assertNotNull(clazz.getMethod("getMapList"));
        assertNotNull(clazz.getMethod("getMapList", int.class));
        assertNotNull(clazz.getMethod("getPage", Page.class));
        assertNotNull(clazz.getMethod("getPage", Page.class, Class.class));
        assertNotNull(clazz.getMethod("setTheOrderBy", String.class));
        assertNotNull(clazz.getMethod("addOrderBy", String.class));
        assertNotNull(clazz.getMethod("getCompare"));
        assertNotNull(clazz.getMethod("getCount"));
        assertNotNull(clazz.getMethod("getSqlBean"));
    }

    @Test
    public void testQueryBeanFactoryInterface() throws Exception {
        Class<?> clazz = QueryBeanFactory.class;

        assertNotNull(clazz.getMethod("createQueryBean", TableEntity.class));
        assertNotNull(clazz.getMethod("createQueryBean", TableEntity.class, io.github.liuwww.db.executor.DatabaseExecutor.class));
        assertNotNull(clazz.getMethod("createQueryBean", String.class, String.class));
        assertNotNull(clazz.getMethod("createQueryBean", String.class, String.class, io.github.liuwww.db.executor.DatabaseExecutor.class));
        assertNotNull(clazz.getMethod("createQueryBean", String.class, String.class, Object[].class));
        assertNotNull(clazz.getMethod("createQueryBean", String.class, String.class, io.github.liuwww.db.executor.DatabaseExecutor.class, Object[].class));
        assertNotNull(clazz.getMethod("createQueryBean", String.class));
        assertNotNull(clazz.getMethod("createQueryBean", String.class, io.github.liuwww.db.executor.DatabaseExecutor.class));
        assertNotNull(clazz.getMethod("createQueryBean", SqlBean.class));
        assertNotNull(clazz.getMethod("createQueryBean", io.github.liuwww.db.sql.Table.class));
    }

    @Test
    public void testNoDeprecatedMethods() {
        checkNoDeprecated(IDataTemplate.class);
        checkNoDeprecated(IQueryTemplate.class);
        checkNoDeprecated(QueryBean.class);
        checkNoDeprecated(QueryBeanFactory.class);
    }

    private void checkNoDeprecated(Class<?> clazz) {
        for (Method method : clazz.getMethods()) {
            assertFalse("方法 " + method.getName() + " 不应标记为 @Deprecated",
                    method.isAnnotationPresent(Deprecated.class));
        }
    }
}