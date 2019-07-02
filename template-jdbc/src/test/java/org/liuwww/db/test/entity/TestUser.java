package org.liuwww.db.test.entity;

import org.liuwww.common.entity.TableEntity;
import org.springframework.stereotype.Component;

@Component
public class TestUser implements TableEntity<TestUser>
{

    private String userId;

    private String name;

    private String userCode;

    private String password;

    private String createDate;

    private String stsDate;

    private String sts;

    private String field1;

    private String field2;

    private String field3;

    private String field4;

    private String field5;

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getUserCode()
    {
        return userCode;
    }

    public void setUserCode(String userCode)
    {
        this.userCode = userCode;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getCreateDate()
    {
        return createDate;
    }

    public void setCreateDate(String createDate)
    {
        this.createDate = createDate;
    }

    public String getStsDate()
    {
        return stsDate;
    }

    public void setStsDate(String stsDate)
    {
        this.stsDate = stsDate;
    }

    public String getSts()
    {
        return sts;
    }

    public void setSts(String sts)
    {
        this.sts = sts;
    }

    public String getField1()
    {
        return field1;
    }

    public void setField1(String field1)
    {
        this.field1 = field1;
    }

    public String getField2()
    {
        return field2;
    }

    public void setField2(String field2)
    {
        this.field2 = field2;
    }

    public String getField3()
    {
        return field3;
    }

    public void setField3(String field3)
    {
        this.field3 = field3;
    }

    public String getField4()
    {
        return field4;
    }

    public void setField4(String field4)
    {
        this.field4 = field4;
    }

    public String getField5()
    {
        return field5;
    }

    public void setField5(String field5)
    {
        this.field5 = field5;
    }

    @Override
    public String tableName()
    {
        return "test_user";
    }

}
