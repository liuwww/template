package io.github.liuwww.db.test;

import io.github.liuwww.common.entity.TableEntity;

/**
 * Spring 测试用实体类
 *
 * @author liuwww
 */
public class SpringTestUserEntity implements TableEntity<SpringTestUserEntity> {

    private String id;
    private String name;
    private Integer age;
    private String email;
    private Integer status;

    @Override
    public String tableName() {
        return "test_user";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "SpringTestUserEntity{id='" + id + "', name='" + name + "', age=" + age + "}";
    }
}
