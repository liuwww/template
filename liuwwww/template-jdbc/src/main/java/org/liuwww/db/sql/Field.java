package org.liuwww.db.sql;

public class Field
{
    private String field;

    private String alias;

    public Field(String field)
    {
        super();
        this.field = field;
    }

    public Field(String field, String alias)
    {
        super();
        this.field = field;
        this.alias = alias;
    }

    public String getField()
    {
        return field;
    }

    public void setField(String field)
    {
        this.field = field;
    }

    public String getAlias()
    {
        return alias;
    }

    public void setAlias(String alias)
    {
        this.alias = alias;
    }

}
