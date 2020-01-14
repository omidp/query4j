package com.omid.query4j;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Vector;

/**
 * @author omidp
 *
 */
public class ReflectionUtil
{

    private static final Class<?>[] WRAPPER_TYPES = { int.class, long.class, short.class, float.class, double.class, byte.class,
            boolean.class, char.class };
    
    private ReflectionUtil()
    {
    }

    public static List<Field> getFields(Class clazz)
    {
        List<Field> fields = new ArrayList<Field>();
        for (Class superClass = clazz; superClass != Object.class; superClass = superClass.getSuperclass())
        {
            for (Field field : superClass.getDeclaredFields())
            {
                fields.add(field);
            }
        }
        return fields;
    }
    
    public static boolean isPrimitive(Class type)
    {
        return primitiveTypeFor(type) != null;
    }

    public static boolean isWrapper(Class<?> clazz)
    {
        if (clazz == null)
            throw new RuntimeException("null value");
        for (int i = 0; i < WRAPPER_TYPES.length; i++)
        {
            if (clazz == WRAPPER_TYPES[i])
                return true;
        }
        return false;
    }
    
    public static Class primitiveTypeFor(Class wrapper)
    {
        if (wrapper == Boolean.class)
            return Boolean.TYPE;
        if (wrapper == Byte.class)
            return Byte.TYPE;
        if (wrapper == Character.class)
            return Character.TYPE;
        if (wrapper == Short.class)
            return Short.TYPE;
        if (wrapper == BigDecimal.class)
            return BigDecimal.class;
        if (wrapper == Date.class)
            return Date.class;
        if (wrapper == java.sql.Date.class)
            return java.sql.Date.class;
        if (wrapper == Integer.class)
            return Integer.TYPE;
        if (wrapper == Long.class)
            return Long.TYPE;
        if (wrapper == Float.class)
            return Float.TYPE;
        if (wrapper == Double.class)
            return Double.TYPE;
        if (wrapper == Void.class)
            return Void.TYPE;
        if (wrapper == String.class)
            return String.class;
        return null;
    }

    public static boolean isArrayOrCollection(Class<?> clazz)
    {
        if (clazz == null)
            throw new RuntimeException("null value");
        return clazz.isArray() || isSubclass(clazz, Collection.class);
    }

    
    public static boolean isMap(Class<?> clazz)
    {
        if (clazz == null)
            throw new RuntimeException("null value");
        return isSubclass(clazz, Map.class);
    }

    public static boolean isEnum(Class<?> clazz)
    {
        if (clazz == null)
            throw new RuntimeException("null value");
        return clazz.isEnum();
    }

    public static boolean isSubclass(Class<?> class1, Class<?> class2)
    {
        List<Class<?>> superClasses = getAllSuperclasses(class1);
        List<Class<?>> superInterfaces = getAllInterfaces(class1);
        for (Class<?> c : superClasses)
        {
            if (class2 == c)
                return true;
        }
        for (Class<?> c : superInterfaces)
        {
            if (class2 == c)
                return true;
        }
        return false;
    }

    public static List getAllSuperclasses(Class cls)
    {
        if (cls == null)
        {
            return null;
        }
        List classes = new ArrayList();
        Class superclass = cls.getSuperclass();
        while (superclass != null)
        {
            classes.add(superclass);
            superclass = superclass.getSuperclass();
        }
        return classes;
    }

    public static List getAllInterfaces(Class cls)
    {
        if (cls == null)
        {
            return null;
        }
        List list = new ArrayList();
        while (cls != null)
        {
            Class[] interfaces = cls.getInterfaces();
            for (int i = 0; i < interfaces.length; i++)
            {
                if (list.contains(interfaces[i]) == false)
                {
                    list.add(interfaces[i]);
                }
                List superInterfaces = getAllInterfaces(interfaces[i]);
                for (Iterator it = superInterfaces.iterator(); it.hasNext();)
                {
                    Class intface = (Class) it.next();
                    if (list.contains(intface) == false)
                    {
                        list.add(intface);
                    }
                }
            }
            cls = cls.getSuperclass();
        }
        return list;
    }
    
    
    public static void set(Field field, Object target, Object value) 
    {
        try
        {
            field.set(target, value);
        }
        catch (IllegalArgumentException iae)
        {
            // target may be null if field is static so use
            // field.getDeclaringClass() instead
            String message = "Could not set field value by reflection: " + field + " on: " + field.getDeclaringClass().getName();
            if (value == null)
            {
                message += " with null value";
            }
            else
            {
                message += " with value: " + value.getClass();
            }
            throw new IllegalArgumentException(message, iae);
        }
        catch (IllegalAccessException e)
        {
            throw new IllegalArgumentException(e);
        }
    }
    
    public static Object toObject(Class clazz, Object value)
    {
        if (value == null)
            return null;
        if (value instanceof String)
        {
            String v = (String) value;
            if (v == null || v.length() == 0)
                return null;
        }
        if (Boolean.class == clazz || boolean.class == clazz)
            return (Boolean) value;
        if (Date.class == clazz)
        {
            if (value instanceof String)
            {
                throw new IllegalArgumentException("date as String can not be handled yet");
            }
            else
            {
                return (Date) value;
            }
        }
        if (Byte.class == clazz || byte.class == clazz)
            return Byte.parseByte(String.valueOf(value));
        if (Short.class == clazz || short.class == clazz)
            return Short.parseShort(String.valueOf(value));
        if (BigDecimal.class == clazz)
            return new BigDecimal(String.valueOf(value));
        if (Integer.class == clazz || int.class == clazz)
            return Integer.parseInt(String.valueOf(value));
        if (Long.class == clazz || long.class == clazz)
            return Long.parseLong(String.valueOf(value));
        if (Float.class == clazz || float.class == clazz)
            return Float.parseFloat(String.valueOf(value));
        if (Double.class == clazz || double.class == clazz)
            return Double.parseDouble(String.valueOf(value));
        if (String.class == clazz)
            return String.valueOf(value);
        return value;
    }

    public static Optional<Field> getField(Class<?> clz, String fieldName)
    {
        return getFields(clz).stream().filter(f->f.getName().equalsIgnoreCase(fieldName)).findFirst();
    }
    
    public static Collection<?> instantiateCollection(Class<?> t)
    {

        if (t == Set.class)
        {
            return new HashSet<Object>();
        }
        else if (t == List.class)
        {
            return new ArrayList<Object>();
        }
        else if (t == Map.class)
        {
            throw new RuntimeException("can not instantiate map");
        }
        else if (t == Vector.class)
        {
            throw new RuntimeException("can not instantiate vector");
        }
        else
            throw new RuntimeException("unknown type");
    }
    
    public static Class<?> getGenericFieldClassType(Field field) 
    {
        field.setAccessible(true);
        ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
        Class<?> genericClass = (Class<?>) parameterizedType.getActualTypeArguments()[0];
        return genericClass;
    }
    
}
