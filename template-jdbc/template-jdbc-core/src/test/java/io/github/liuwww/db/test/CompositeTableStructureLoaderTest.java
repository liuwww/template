package io.github.liuwww.db.test;

import org.junit.Test;
import io.github.liuwww.db.config.TableStructureContainer;
import io.github.liuwww.db.config.TableStructureLoader;
import io.github.liuwww.db.config.impl.CompositeTableStructureLoader;
import io.github.liuwww.db.config.impl.JsonTableStructureLoader;
import io.github.liuwww.db.context.TableMetaData;

import static org.junit.Assert.*;

/**
 * {@link CompositeTableStructureLoader} 单元测试。
 *
 * @author liuwww
 */
public class CompositeTableStructureLoaderTest {

    private static final String JSON_A =
            "{\"tables\":[{\"tableName\":\"t_a\",\"dbType\":\"mysql\",\"columns\":[" +
            "{\"columnName\":\"id\",\"dataTypeName\":\"BIGINT\"}]}]}";

    private static final String JSON_B =
            "{\"tables\":[{\"tableName\":\"t_b\",\"dbType\":\"oracle\",\"columns\":[" +
            "{\"columnName\":\"id\",\"dataTypeName\":\"BIGINT\"}]}]}";

    @Test
    public void testMergeTwoLoaders() {
        CompositeTableStructureLoader composite = new CompositeTableStructureLoader();
        composite.addLoader(new JsonTableStructureLoader(JSON_A));
        composite.addLoader(new JsonTableStructureLoader(JSON_B));

        TableStructureContainer container = composite.loadStructureContainer();

        assertEquals(2, container.getTableNames().size());
        assertNotNull(container.getTableMetaData("t_a"));
        assertNotNull(container.getTableMetaData("t_b"));
    }

    @Test
    public void testVarargsConstructor() {
        TableStructureLoader[] loaders = {
                new JsonTableStructureLoader(JSON_A),
                new JsonTableStructureLoader(JSON_B)
        };
        CompositeTableStructureLoader composite = new CompositeTableStructureLoader(loaders);

        TableStructureContainer container = composite.loadStructureContainer();

        assertEquals(2, container.getTableNames().size());
    }

    @Test
    public void testEmptyLoaders() {
        CompositeTableStructureLoader composite = new CompositeTableStructureLoader();
        TableStructureContainer container = composite.loadStructureContainer();

        assertNotNull(container);
        assertEquals(0, container.getTableNames().size());
    }

    @Test
    public void testNullLoaderIgnored() {
        CompositeTableStructureLoader composite = new CompositeTableStructureLoader();
        composite.addLoader(null);
        composite.addLoader(new JsonTableStructureLoader(JSON_A));

        TableStructureContainer container = composite.loadStructureContainer();

        assertEquals(1, container.getTableNames().size());
    }

    @Test
    public void testSameTableNameOverwrites() {
        // 后加载的表若与先加载的同名，覆盖之
        String jsonA2 = "{\"tables\":[{\"tableName\":\"t_shared\",\"dbType\":\"mysql\",\"columns\":[]}]}";
        String jsonB2 = "{\"tables\":[{\"tableName\":\"t_shared\",\"dbType\":\"oracle\",\"columns\":[]}]}";

        CompositeTableStructureLoader composite = new CompositeTableStructureLoader();
        composite.addLoader(new JsonTableStructureLoader(jsonA2));
        composite.addLoader(new JsonTableStructureLoader(jsonB2));

        TableStructureContainer container = composite.loadStructureContainer();

        assertEquals(1, container.getTableNames().size());
        TableMetaData tmd = container.getTableMetaData("t_shared");
        // 后加载的覆盖先加载的
        assertEquals(io.github.liuwww.db.sql.DbType.ORACLE, tmd.getDbType());
    }
}
