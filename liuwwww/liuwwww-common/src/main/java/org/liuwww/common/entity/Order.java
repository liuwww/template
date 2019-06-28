package org.liuwww.common.entity;

public class Order
{
    private String field;

    private String sort;

    public Order(String field, String sort)
    {
        super();
        this.field = field;
        this.sort = sort;
    }

    public String getField()
    {
        return field;
    }

    public void setField(String field)
    {
        this.field = field;
    }

    public String getSort()
    {
        return sort;
    }

    public void setSort(String sort)
    {
        this.sort = sort;
    }

}
