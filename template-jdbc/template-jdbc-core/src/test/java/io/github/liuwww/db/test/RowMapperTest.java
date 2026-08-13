package io.github.liuwww.db.test;

import static org.junit.Assert.*;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Test;
import io.github.liuwww.db.executor.RowMapper;

public class RowMapperTest {

    @Test
    public void testRowMapperInterface() {
        RowMapper<String> mapper = new RowMapper<String>() {
            @Override
            public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                return "Row " + rowNum;
            }
        };
        
        assertNotNull(mapper);
    }

    @Test
    public void testRowMapperImplementation() {
        RowMapper<TestEntity> mapper = new RowMapper<TestEntity>() {
            @Override
            public TestEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
                TestEntity entity = new TestEntity();
                entity.setId(rowNum);
                entity.setName("Test" + rowNum);
                return entity;
            }
        };
        
        assertNotNull(mapper);
    }

    @Test
    public void testRowMapperWithTypeInference() {
        RowMapper<Integer> intMapper = (rs, rowNum) -> rowNum * 10;
        
        assertNotNull(intMapper);
    }

    public static class TestEntity {
        private int id;
        private String name;
        
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}
