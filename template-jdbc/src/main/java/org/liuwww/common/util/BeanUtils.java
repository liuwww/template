package org.liuwww.common.util;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * 扩展Apache Commons BeanUtils, 提供一些反射方面缺失功能的封装.
 */
public class BeanUtils extends org.apache.commons.beanutils.BeanUtils {

    protected static final Logger logger = LoggerFactory.getLogger(BeanUtils.class);

    static {
        ConvertUtils.register(new DateConverter(), Date.class);
    }

    private BeanUtils() {
    }

    /**
     * 循环向上转型,获取对象的DeclaredField.
     * 
     * @throws NoSuchFieldException
     *             如果没有该Field时抛出.
     */
    public static Field getDeclaredField(Object object, String propertyName) throws NoSuchFieldException {
        Assert.notNull(object);
        Assert.hasText(propertyName);
        return getDeclaredField(object.getClass(), propertyName);
    }

    /**
     * 循环向上转型,获取对象的DeclaredField.
     * 
     * @throws NoSuchFieldException
     *             如果没有该Field时抛出.
     */
    public static Field getDeclaredField(Class<?> clazz, String propertyName) throws NoSuchFieldException {
        Assert.notNull(clazz);
        Assert.hasText(propertyName);
        for (Class<?> superClass = clazz; superClass != Object.class; superClass = superClass.getSuperclass()) {
            try {
                return superClass.getDeclaredField(propertyName);
            }
            catch (NoSuchFieldException e) {
                // Field不在当前类定义,继续向上转型
            }
        }
        throw new NoSuchFieldException("No such field: " + clazz.getName() + '.' + propertyName);
    }

    /**
     * 暴力获取对象变量值,忽略private,protected修饰符的限制.
     * 
     * @throws NoSuchFieldException
     *             如果没有该Field时抛出.
     */
    public static Object forceGetProperty(Object object, String propertyName) throws NoSuchFieldException {
        Assert.notNull(object);
        Assert.hasText(propertyName);

        Field field = getDeclaredField(object, propertyName);

        boolean accessible = field.isAccessible();
        field.setAccessible(true);

        Object result = null;
        try {
            result = field.get(object);
        }
        catch (IllegalAccessException e) {
            if (logger.isInfoEnabled()) {
                logger.info("error wont' happen");
            }
        }
        field.setAccessible(accessible);
        return result;
    }

    /**
     * 暴力设置对象变量值,忽略private,protected修饰符的限制.
     * 
     * @throws NoSuchFieldException
     *             如果没有该Field时抛出.
     */
    public static void forceSetProperty(Object object, String propertyName, Object newValue)
            throws NoSuchFieldException {
        Assert.notNull(object);
        Assert.hasText(propertyName);

        Field field = getDeclaredField(object, propertyName);
        boolean accessible = field.isAccessible();
        field.setAccessible(true);
        try {
            field.set(object, newValue);
        }
        catch (IllegalAccessException e) {
            if (logger.isInfoEnabled()) {
                logger.info("Error won't happen");
            }
        }
        field.setAccessible(accessible);
    }

    /**
     * 暴力调用对象函数,忽略private,protected修饰符的限制.
     * 
     * @throws NoSuchMethodException
     *             如果没有该Method时抛出.
     */
    public static Object invokePrivateMethod(Object object, String methodName, Object... params)
            throws NoSuchMethodException {
        Assert.notNull(object);
        Assert.hasText(methodName);
        Class<?>[] types = new Class[params.length];
        for (int i = 0; i < params.length; i++) {
            types[i] = params[i].getClass();
        }

        Class<?> clazz = object.getClass();
        Method method = null;
        for (Class<?> superClass = clazz; superClass != Object.class; superClass = superClass.getSuperclass()) {
            try {
                method = superClass.getDeclaredMethod(methodName, types);
                break;
            }
            catch (NoSuchMethodException e) {
                // 方法不在当前类定义,继续向上转型
            }
        }

        if (method == null) {
            throw new NoSuchMethodException("No Such Method:" + clazz.getSimpleName() + methodName);
        }

        boolean accessible = method.isAccessible();
        method.setAccessible(true);
        Object result = null;
        try {
            result = method.invoke(object, params);
        }
        catch (Exception e) {
            ReflectionUtils.handleReflectionException(e);
        }
        method.setAccessible(accessible);
        return result;
    }

    /**
     * 按Filed的类型取得Field列表.
     */
    public static List<Field> getFieldsByType(Object object, Class<?> type) {
        List<Field> list = new ArrayList<Field>();
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getType().isAssignableFrom(type)) {
                list.add(field);
            }
        }
        return list;
    }

    /**
     * 按FiledName获得Field的类型.
     */
    public static Class<?> getPropertyType(Class<?> type, String name) {
        try {
            return getDeclaredField(type, name).getType();
        }
        catch (NoSuchFieldException e) {
            if (logger.isInfoEnabled()) {
                logger.info("获取字段类型异常", e);
            }
        }
        return null;
    }

    /**
     * 获得field的getter函数名称.
     */
    public static String getGetterName(Class<?> type, String fieldName) {
        Assert.notNull(type, "Type required");
        Assert.hasText(fieldName, "FieldName required");

        if (type.getName().equals("boolean")) {
            return "is" + StringUtil.capitalize(fieldName);
        }
        else {
            return "get" + StringUtil.capitalize(fieldName);
        }
    }

    /**
     * 获得field的getter函数,如果找不到该方法,返回null.
     */
    public static Method getGetterMethod(Class<?> type, String fieldName) {
        try {
            return type.getMethod(getGetterName(type, fieldName));
        }
        catch (NoSuchMethodException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 相同非空字段copy，注意效率
     */
    public static void copyNoNullProperties(Object dest, Object orig) {

        if (dest == null) {
            throw new IllegalArgumentException("No destination bean specified");
        }

        if (orig == null) {
            throw new IllegalArgumentException("No origin bean specified");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("BeanUtils.copyProperties(" + dest + ", " + orig + ")");
        }

        if ((orig instanceof DynaBean)) {
            DynaProperty[] origDescriptors = ((DynaBean) orig).getDynaClass().getDynaProperties();

            for (DynaProperty origDescriptor : origDescriptors) {
                String name = origDescriptor.getName();
                if (PropertyUtils.isWriteable(dest, name)) {
                    Object value = ((DynaBean) orig).get(name);
                    if (value != null) {
                        try {
                            copyProperty(dest, name, value);
                        }
                        catch (Exception e) {
                            if (logger.isInfoEnabled()) {
                                logger.info("bean字段值非空copy异常：", e);
                            }
                        }
                    }
                }
            }
        }
        else if ((orig instanceof Map)) {
            Iterator<?> names = ((Map<?, ?>) orig).keySet().iterator();
            while (names.hasNext()) {
                String name = (String) names.next();
                if (PropertyUtils.isWriteable(dest, name)) {
                    Object value = ((Map<?, ?>) orig).get(name);
                    if (value != null) {
                        try {
                            copyProperty(dest, name, value);
                        }
                        catch (Exception e) {
                            if (logger.isInfoEnabled()) {
                                logger.info("bean字段值非空copy异常：", e);
                            }
                        }
                    }
                }
            }
        }
        else {
            PropertyDescriptor[] origDescriptors = PropertyUtils.getPropertyDescriptors(orig);

            for (PropertyDescriptor origDescriptor : origDescriptors) {
                String name = origDescriptor.getName();
                if (!"class".equals(name)) {
                    if ((PropertyUtils.isReadable(orig, name)) && (PropertyUtils.isWriteable(dest, name))) {
                        try {
                            Object value = PropertyUtils.getSimpleProperty(orig, name);
                            if (value != null) {
                                copyProperty(dest, name, value);
                            }
                        }
                        catch (Exception e) {
                            if (logger.isInfoEnabled()) {
                                logger.info("bean字段值非空copy异常：", e);
                            }
                        }
                    }
                }
            }
        }
    }

    public static void copyProperties(Object dest, Object orig) {
        try {
            org.apache.commons.beanutils.BeanUtils.copyProperties(dest, orig);
        }
        catch (Exception e) {
            if (logger.isInfoEnabled()) {
                logger.info("bean字段值copy异常", e);
            }
        }
    }

    public static Object getPropertyValue(Object bean, String name) {
        try {
            return BeanUtilsBean.getInstance().getPropertyUtils().getProperty(bean, name);
        }
        catch (Exception e) {
            if (logger.isInfoEnabled()) {
                logger.info("获取bean字段值异常", e);
            }
        }
        return null;
    }

    public static String getProperty(Object bean, String name) {
        try {
            return org.apache.commons.beanutils.BeanUtils.getProperty(bean, name);
        }
        catch (Exception e) {
            if (logger.isInfoEnabled()) {
                logger.info("获取bean字段String类型值异常", e);
            }
        }
        return null;
    }

    public static void setProperty(Object bean, String name, Object value) {
        try {
            org.apache.commons.beanutils.BeanUtils.setProperty(bean, name, value);
        }
        catch (Exception e) {
            if (logger.isInfoEnabled()) {
                logger.info("bean字段设置值异常", e);
            }
        }
    }

}
