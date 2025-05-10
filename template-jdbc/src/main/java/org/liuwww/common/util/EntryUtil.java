package org.liuwww.common.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.liuwww.common.execption.BusinessExecption;
import org.liuwww.common.execption.DbException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntryUtil
{
    private static volatile EntryUtil instance = new EntryUtil();

    private static Logger logger = LoggerFactory.getLogger(EntryUtil.class);

    public static Set<Class<?>> simpleClasses = new HashSet<Class<?>>(Arrays.asList(short.class, char.class, int.class,
            long.class, byte.class, float.class, double.class, boolean.class, Short.class, Character.class,
            Integer.class, Long.class, Byte.class, Float.class, Boolean.class, String.class, CharSequence.class));

    public static class EntryProps
    {
        protected EntryProps(Field field)
        {
            this.field = field;
            this.name = field.getName();
            this.type = field.getType();

            String _mName = DbNameConverter.setUpperCaseForFirstLetter(name);
            if (StringUtil.equals(name, "getSerialVersionUID"))
            {
                return;
            }
            String getMethodName = "get" + _mName;
            try
            {
                this.getMethod = field.getDeclaringClass().getDeclaredMethod(getMethodName, new Class[0]);

                if (this.getMethod != null)
                {
                    // if (this.getMethod.getAnnotation(Transient.class) !=
                    // null)
                    {
                        // this.isTransient = true;
                    }
                }
            }
            catch (Exception e)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("class：{},not have mehtod：{}", field.getClass(), getMethodName);
                }
            }
            String setMethodName = "set" + _mName;
            try
            {
                this.setMethod = field.getDeclaringClass().getDeclaredMethod(setMethodName, this.type);
            }
            catch (Exception e)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("class：{},not have mehtod：{}", field.getClass(), getMethodName);
                }
            }

        }

        private String name;

        private Field field;

        private Method getMethod;

        private Method setMethod;

        private Class<?> type;

        private boolean isTransient = false;

        public boolean isTransient()
        {
            return isTransient;
        }

        public String getName()
        {
            return name;
        }

        public Field getField()
        {
            return field;
        }

        public Method getGetMethod()
        {
            return getMethod;
        }

        public Method getSetMethod()
        {
            return setMethod;
        }

        public Class<?> getType()
        {
            return type;
        }
    }

    private Map<Class<?>, Map<String, EntryProps>> entryMap = new LinkedHashMap<Class<?>, Map<String, EntryProps>>();

    private Map<Class<?>, Map<String, List<Method>>> methodMap = new HashMap<Class<?>, Map<String, List<Method>>>();

    private EntryUtil()
    {
    }

    private static Map<Class<?>, Map<String, EntryProps>> getEntryMap()
    {
        return instance.entryMap;
    }

    public void setEntryMap(Map<Class<?>, Map<String, EntryProps>> entryMap)
    {
        this.entryMap = entryMap;
    }

    public static Map<String, EntryProps> getEntryProps(Class<?> clazz)
    {
        Map<String, EntryProps> props = getEntryMap().get(clazz);
        if (props == null)
        {
            synchronized (clazz)
            {
                props = getEntryMap().get(clazz);
                if (props == null)
                {
                    props = new HashMap<String, EntryProps>();
                    for (Class<?> supperClazz = clazz; supperClazz != Object.class; supperClazz = supperClazz
                            .getSuperclass())
                    {
                        for (Field field : supperClazz.getDeclaredFields())
                        {
                            EntryProps ep = new EntryProps(field);
                            props.put(ep.name, ep);
                        }
                    }
                    getEntryMap().put(clazz, props);
                }
            }
        }
        return props;
    }

    public static Field getField(Class<?> clazz, String field)
    {
        EntryProps entryprops = getEntryProps(clazz).get(field);
        if (entryprops != null)
        {
            return entryprops.field;
        }
        return null;
    }

    public static Object getFieldValue(Object entry, String field)
    {
        if (entry instanceof Map)
        {
            return getMapValue(entry, field);
        }
        checkField(entry.getClass(), field);
        EntryProps entryprops = getEntryProps(entry.getClass()).get(field);
        Method getter = entryprops.getMethod;
        if (getter != null)
        {
            return getValue(getter, entry);
        }
        entryprops.field.setAccessible(true);
        try
        {
            return entryprops.field.get(entry);
        }
        catch (Exception e)
        {
            throw new DbException("get field value throuth field self error", e);
        }
    }

    private static Object getMapValue(Object obj, Object key)
    {
        Map<?, ?> map = (Map<?, ?>) obj;
        return map.get(key);
    }

    private static Object getValue(Method method, Object entry)
    {
        try
        {
            return method.invoke(entry, new Object[0]);
        }
        catch (Exception e)
        {
            throw new DbException("get field value throuth Getter error", e);
        }
    }

    public static Class<?> getFieldType(Class<?> clazz, String field)
    {
        checkField(clazz, field);
        EntryProps entryprops = getEntryProps(clazz).get(field);
        return entryprops.type;
    }

    private static void checkField(Class<?> clazz, String field)
    {
        EntryProps entryprops = getEntryProps(clazz).get(field);
        if (entryprops == null)
        {
            throw new DbException("there is no field:" + field + " in Class:" + clazz);
        }
    }

    public static List<String> getFieldList(Class<?> clazz)
    {
        Map<String, EntryProps> map = getEntryProps(clazz);
        List<String> list = new ArrayList<String>(map.size());
        for (String field : map.keySet())
        {
            list.add(field);
        }
        return list;
    }

    /**
     * @desc:把bean转换成功map
     * @Date:2018年5月22日下午10:03:14
     * @author liuwww
     * @param obj
     * @param keepNull false时 value 为null时值被丢弃
     * @return
     */
    public static Map<String, Object> getMap(Object obj, boolean keepNull)
    {
        Map<String, Object> map = new HashMap<String, Object>();
        for (String field : getEntryProps(obj.getClass()).keySet())
        {
            Object val = getFieldValue(obj, field);
            if (keepNull || val != null)
            {
                map.put(field, val);
            }
        }
        return map;
    }

    /**
     * 简单类型字段获取，复杂的或略
     * @param obj
     * @param keepNull
     * @return
     */
    public static Map<String, Object> getSimpleValMap(Object obj, boolean keepNull)
    {
        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, EntryProps> ep = getEntryProps(obj.getClass());

        for (String field : ep.keySet())
        {
            Class<?> type = ep.get(field).field.getType();
            if (isSimpleType(type))
            {
                Object val = getFieldValue(obj, field);
                if (keepNull || val != null)
                {
                    map.put(field, val);
                }
            }
        }
        return map;
    }

    private static boolean isSimpleType(Class<?> type)
    {
        return simpleClasses.contains(type) || CharSequence.class.isAssignableFrom(type)
                || Date.class.isAssignableFrom(type);
    }

    public static Map<String, Object> getNotBlankValueMap(Object obj)
    {
        Map<String, Object> map = new HashMap<String, Object>();
        for (String field : getEntryProps(obj.getClass()).keySet())
        {
            Object val = getFieldValue(obj, field);
            if (val != null && StringUtil.isNotBlank(val.toString()))
            {
                map.put(field, val);
            }
        }
        return map;

    }

    public static Method getMethod(Class<?> clazz, String methodName, Class<?>[] classes)
    {
        Map<String, List<Method>> map = getMehtod(clazz);
        List<Method> list = map.get(methodName);
        if (list != null)
        {
            for (Method method : list)
            {
                if (Arrays.equals(method.getParameterTypes(), classes))
                {
                    return method;
                }
            }
        }
        return null;
    }

    private static Map<String, List<Method>> getMehtod(Class<?> clazz)
    {
        Map<String, List<Method>> map = instance.methodMap.get(clazz);
        if (map == null)
        {
            synchronized (EntryUtil.class)
            {
                map = instance.methodMap.get(clazz);
                if (map == null)
                {
                    map = new HashMap<String, List<Method>>();
                    for (Class<?> supperClazz = clazz; supperClazz != Object.class; supperClazz = supperClazz
                            .getSuperclass())
                    {
                        Method[] methods = clazz.getMethods();
                        for (Method method : methods)
                        {
                            String name = method.getName();
                            List<Method> list = map.get(name);
                            if (list == null)
                            {
                                list = new ArrayList<Method>();
                                map.put(name, list);
                            }
                            list.add(method);
                        }
                    }
                    instance.methodMap.put(clazz, map);
                }
            }
        }
        return map;
    }

    public static void setFieldValue(Object entry, String field, Object val)
    {
        if (entry instanceof Map)
        {
            setMapValue(entry, field, val);
        }
        else
        {
            checkField(entry.getClass(), field);
            EntryProps entryprops = getEntryProps(entry.getClass()).get(field);
            Method setter = entryprops.setMethod;
            if (val != null)
            {
                Converter converter = null;
                converter = ConvertUtils.lookup(entryprops.type);
                if (converter == null && entryprops.type != Object.class)
                {
                    BeanUtils.setProperty(entry, field, val);
                    return;
                }
                else if (converter != null)
                {
                    val = converter.convert(entryprops.type, val);
                }
            }
            if (setter != null)
            {
                setValue(setter, entry, val);
            }
            else
            {
                boolean accessible = entryprops.field.isAccessible();
                entryprops.field.setAccessible(true);
                try
                {
                    entryprops.field.set(entry, val);
                    entryprops.field.setAccessible(accessible);
                }
                catch (Exception e)
                {
                    throw new DbException("set field value throuth field self error", e);
                }
            }

        }
    }

    private static void setValue(Method setter, Object entry, Object val)
    {
        try
        {
            setter.invoke(entry, new Object[]
            { val });
        }
        catch (Exception e)
        {
            throw new DbException("set field value throuth Getter error", e);
        }

    }

    /**
     * @param rlist
     * @param isSimple 是否忽略复杂字段 true:忽略
     * @return
     */
    public static List<Map<String, Object>> getMapListFromEntity(List<?> rlist, boolean isSimple)
    {
        if (rlist == null)
        {
            return null;
        }
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>(rlist.size());
        if (isSimple)
        {
            for (Object o : rlist)
            {
                list.add(getSimpleValMap(o, true));
            }
        }
        else
        {
            for (Object o : rlist)
            {
                list.add(getMap(o, true));
            }
        }
        return list;
    }

    @SuppressWarnings(
    { "unchecked", "rawtypes" })
    private static void setMapValue(Object obj, Object key, Object val)
    {
        Map map = (Map) obj;
        map.put(obj, val);
    }

    public static boolean hasField(Object obj, String field)
    {
        return getEntryProps(obj.getClass()).containsKey(field);
    }

    public static boolean hasField(Class<?> clazz, String field)
    {
        return getEntryProps(clazz).containsKey(field);
    }

    /**
     * @desc:orig中的字段有值，才会赋值,并且类型不会自动匹配
     * @Date:2018年6月2日下午7:30:18
     * @author liuwww
     * @param dest 不可是Map
     * @param orig
     */
    public static void copyProperties(Object dest, Object orig)
    {
        if (dest instanceof Map)
        {
            throw new BusinessExecption("dest can not be a map");
        }
        if (orig instanceof Map)
        {
            Map<String, EntryProps> dMap = getEntryProps(dest.getClass());
            Map<?, ?> map = (Map<?, ?>) orig;
            for (Object key : map.keySet())
            {
                Object val = getMapValue(orig, key);
                if (val != null && dMap.get(key.toString()) != null)
                {
                    setFieldValue(dest, key.toString(), val);
                }
            }
        }
        else
        {
            Map<String, EntryProps> dMap = null;
            if (dest instanceof Map)
            {
                dMap = getEntryProps(dest.getClass());
            }
            Map<String, EntryProps> fMap = getEntryProps(orig.getClass());

            for (String key : getFieldList(dest.getClass()))
            {

                EntryProps entryprops = fMap.get(key);
                if (entryprops == null || (dMap != null && dMap.get(key) == null))
                {
                    continue;
                }
                Method getter = entryprops.getMethod;
                Object val = null;
                if (getter != null)
                {
                    val = getValue(getter, orig);
                }
                else
                {
                    entryprops.field.setAccessible(true);
                    try
                    {
                        val = entryprops.field.get(orig);
                    }
                    catch (Exception e)
                    {
                        throw new DbException("get field value throuth field self error", e);
                    }
                }

                setFieldValue(dest, key, val);
            }

        }
    }

}
