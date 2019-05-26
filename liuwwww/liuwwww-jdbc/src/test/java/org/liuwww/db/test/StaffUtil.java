package org.liuwww.db.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaffUtil
{
    public static List<Staff> getStaffList(int n)
    {
        List<Staff> list = new ArrayList<Staff>(n);
        for (int i = 0; i < n; i++)
        {
            Staff staff = new Staff();
            staff.setWcode("test01" + i);
            staff.setStaffName("test01" + i);
            staff.setPassword("111111");
            staff.setDepId("1");
            staff.setOperaStaffId("0");
            list.add(staff);
        }
        return list;
    }

    public static Staff getStaff()
    {
        return getStaffList(1).get(0);
    }

    public static List<Map<String, Object>> getMapList(int n)
    {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>(n);
        for (int i = 0; i < n; i++)
        {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("wcode", "test02" + i);
            map.put("staffName", "test02" + i);
            map.put("password", "111111");
            map.put("depId", "1");
            map.put("operaStaffId", "0");
            list.add(map);
        }
        return list;
    }

    public static Map<String, Object> getMap()
    {
        return getMapList(1).get(0);
    }

}
