package org.liuwww.demo.entity;

import java.io.Serializable;

public class TestUser implements Serializable
{
    private long id;

    private String name;

    private int age;

    public TestUser()
    {
        super();
    }

    public TestUser(long id, String name, int age)
    {
        super();
        this.id = id;
        this.name = name;
        this.age = age;
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getAge()
    {
        return age;
    }

    public void setAge(int age)
    {
        this.age = age;
    }

}
