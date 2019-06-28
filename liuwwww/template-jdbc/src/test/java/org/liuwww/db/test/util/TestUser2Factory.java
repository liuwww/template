package org.liuwww.db.test.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.liuwww.db.test.entity.TestUser2;

public class TestUser2Factory
{
    public static TestUser2 createEntity()
    {
        TestUser2 user = new TestUser2();
        user.setField1(getRandomStr());
        user.setField2(getRandomStr());
        user.setField3(getRandomStr());
        user.setField4(getRandomStr());
        user.setField5(getRandomStr());
        user.setName(getRandomStr());
        user.setPassword(getRandomStr());
        user.setUserCode(getRandomStr());
        return user;
    }

    public static List<TestUser2> createEntityList(int n)
    {
        if (n > 0)
        {
            List<TestUser2> list = new ArrayList<TestUser2>(n);
            for (int i = 0; i < n; i++)
            {
                list.add(createEntity());
            }
            return list;
        }
        else
        {
            return Collections.emptyList();
        }
    }

    public static Map<String, Object> createMap()
    {
        Map<String, Object> user = new HashMap<String, Object>();
        user.put("field1", getRandomStr());
        user.put("field2", getRandomStr());
        user.put("field3", getRandomStr());
        user.put("field4", getRandomStr());
        user.put("field5", getRandomStr());
        user.put("name", getRandomStr());
        user.put("password", getRandomStr());
        user.put("userCode", getRandomStr());
        return user;
    }

    public static List<Map<String, Object>> createMapList(int n)
    {
        if (n > 0)
        {
            List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
            for (int i = 0; i < n; i++)
            {
                list.add(createMap());
            }
            return list;
        }
        else
        {
            return Collections.emptyList();
        }
    }

    private static char[] digital = "0123456789ABCDEF".toCharArray();

    public static String getRandomChar(int c)
    {
        Random random = new Random();
        char[] arr = new char[c];
        for (int i = 0; i < c; i++)
        {
            int index = random.nextInt(16);
            arr[i] = digital[index];
        }
        return new String(arr);
    }

    public static String getRandomStr()
    {
        Random random = new Random();
        int c = random.nextInt(16) + 10;
        return getRandomChar(c);
    }

    public static void main(String[] args)
    {
        System.out.println(getRandomChar(10));

    }

}
