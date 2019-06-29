package org.liuwww.common.entity;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

@SuppressWarnings("serial")
public abstract class AbstractTableEntity<T> implements Serializable, TableEntity<T>
{
    @Override
    public String toString()
    {
        return ReflectionToStringBuilder.reflectionToString(this);
    }

    @Override
    public boolean equals(Object o)
    {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
