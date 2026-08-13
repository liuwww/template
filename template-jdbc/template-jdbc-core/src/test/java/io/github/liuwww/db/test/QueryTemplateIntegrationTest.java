package io.github.liuwww.db.test;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import io.github.liuwww.common.entity.TableEntity;
import io.github.liuwww.db.page.Page;
import io.github.liuwww.db.service.IQueryTemplate;
import io.github.liuwww.db.service.impl.QueryTemplate;

public class QueryTemplateIntegrationTest extends BaseTest {

    private IQueryTemplate queryTemplate = new QueryTemplate();

    @Test
    public void testGetBeanList() {
        assertNotNull(queryTemplate);
    }

    @Test
    public void testGetBean() {
        assertNotNull(queryTemplate);
    }

    @Test
    public void testGetPageWithEntity() {
        TestUserEntity entity = new TestUserEntity();
        Page page = new Page();
        page.setPageNum(1);
        page.setPages(10);

        assertNotNull(queryTemplate);
        assertNotNull(page);
    }

    @Test
    public void testGetPageWithTableName() {
        assertNotNull(queryTemplate);
    }

    @Test
    public void testGetMapList() {
        assertNotNull(queryTemplate);
    }

    @Test
    public void testGetCount() {
        assertNotNull(queryTemplate);
    }

    @Test
    public void testGetMap() {
        assertNotNull(queryTemplate);
    }

    @Test
    public void testGetBeanWithTableName() {
        assertNotNull(queryTemplate);
    }
}
