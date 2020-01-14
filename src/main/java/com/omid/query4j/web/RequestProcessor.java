package com.omid.query4j.web;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import com.omid.query4j.ReflectionUtil;
import com.omid.query4j.web.RequestProcessor.ModelValueHolder;

/**
 * @author omidp
 *
 * @param <E>
 */
public abstract class RequestProcessor<E>
{

    private static final Logger logger = Logger.getLogger(RequestProcessor.class.getName());

    protected E instance;
    private Class<E> entityClass;
    HttpServletRequest request;

    public RequestProcessor(HttpServletRequest request)
    {
        this.request = request;
        initInstance();
    }

    private void initInstance()
    {
        this.entityClass = getEntityClass();
        if (this.entityClass == null)
            throw new IllegalArgumentException("Generic Entity can not be empty");
        this.instance = (E) quietInstantiation(entityClass);
    }

    private static Object quietInstantiation(Class<?> type)
    {
        try
        {
            return type.newInstance();
        }
        catch (InstantiationException | IllegalAccessException e)
        {
            logger.info("IllegalAccessException");
            return null;
        }
    }

    private Class<E> getEntityClass()
    {
        if (entityClass == null)
        {
            Type type = getClass().getGenericSuperclass();
            if (type instanceof ParameterizedType)
            {
                ParameterizedType paramType = (ParameterizedType) type;
                if (paramType.getActualTypeArguments().length == 2)
                {
                    // likely dealing with -> new
                    // EntityHome<Person>().getEntityClass()
                    if (paramType.getActualTypeArguments()[1] instanceof TypeVariable)
                    {
                        throw new IllegalArgumentException("Could not guess entity class by reflection");
                    }
                    // likely dealing with -> new Home<EntityManager, Person>()
                    // { ... }.getEntityClass()
                    else
                    {
                        entityClass = (Class<E>) paramType.getActualTypeArguments()[1];
                    }
                }
                else
                {
                    // likely dealing with -> new PersonHome().getEntityClass()
                    // where PersonHome extends EntityHome<Person>
                    entityClass = (Class<E>) paramType.getActualTypeArguments()[0];
                }
            }
            else
            {
                throw new IllegalArgumentException("Could not guess entity class by reflection");
            }
        }
        return entityClass;
    }

    public static class RequestField
    {

        private Field field;
        private Class<?> type;
        private Object instance;

        public RequestField(Field field, Object instance)
        {
            this.field = field;
            this.type = field.getType();
            this.instance = instance;
        }

        public void setValue(Object value)
        {
            field.setAccessible(true);
            if (isPrimitiveOrWrapper())
            {
                ReflectionUtil.set(field, instance, ReflectionUtil.toObject(type, value));
            }

        }

        public boolean isPrimitiveOrWrapper()
        {
            return ReflectionUtil.isPrimitive(type) || ReflectionUtil.isWrapper(type);
        }
    }

    private class RequestParameterFieldProcessor
    {
        public void process(List<Field> fields, Object userInstance)
        {
            Map<String, String[]> parameterMap = request.getParameterMap();
            Map<String, Object> nestedObjectMap = new WeakHashMap<>();
            List<ModelValueHolder> mvlas = new ArrayList<>();
            for (Map.Entry<String, String[]> param : parameterMap.entrySet())
            {
                String key = param.getKey();
                String val = param.getValue()[0];
                new ModelProcessor(key).process(new ParamProcessor() {

                    @Override
                    public void processNestedModel(String modelFieldName, String propertyName)
                    {
                        fields.stream().filter(f -> f.getName().equalsIgnoreCase(modelFieldName)).findFirst().ifPresent(field -> {
                            Object nestedObject = nestedObjectMap.get(field.getName());
                            if (nestedObject == null)
                            {
                                nestedObject = quietInstantiation(field.getType());
                                nestedObjectMap.put(field.getName(), nestedObject);
                            }
                            mvlas.add(new ModelValueHolder(field, propertyName, val, 0));
                        });

                    }

                    @Override
                    public void processListModel(String nestedFiledName, String nestedPropertyName, int nestedIndex)
                    {
                        fields.stream().filter(f -> f.getName().equalsIgnoreCase(nestedFiledName)).findFirst().ifPresent(field -> {
                            Object nestedObject = nestedObjectMap.get(field.getName());
                            if (nestedObject == null)
                            {
                                Class<?> genericFieldClassType = ReflectionUtil.getGenericFieldClassType(field);
                                nestedObject = quietInstantiation(genericFieldClassType);
                                nestedObjectMap.put(field.getName(), nestedObject);
                            }
                            mvlas.add(new ModelValueHolder(field, nestedPropertyName, val, nestedIndex));
                        });
                    }

                    @Override
                    public void processField(String key)
                    {
                        fields.stream().filter(f -> f.getName().equalsIgnoreCase(key)).findFirst()
                                .ifPresent(field -> new RequestField(field, userInstance).setValue(val));
                    }
                });
            }
            //
            processSimpleModel(mvlas.stream().filter(f->!ReflectionUtil.isArrayOrCollection(f.getBaseField().getType())).collect(Collectors.toList()), nestedObjectMap);
            processListModel(mvlas.stream().filter(f->ReflectionUtil.isArrayOrCollection(f.getBaseField().getType())).collect(Collectors.toList()), nestedObjectMap);
            
        }

    }
    
    
    private void processListModel(List<ModelValueHolder> list, Map<String, Object> nestedObjectMap)
    {
        Collections.sort(list, (o1, o2) -> {
            int x = o1.getIndex();
            int y = o2.getIndex();
            return (x < y) ? -1 : ((x == y) ? 0 : 1);
        });
        Set<Entry<Field,List<ModelValueHolder>>> entrySet = list.stream().collect(Collectors.groupingBy(ModelValueHolder::getBaseField)).entrySet();
        Collection collectionInstance = null;        
        for (Entry<Field, List<ModelValueHolder>> entry : entrySet)
        {
            Field key = entry.getKey();
            collectionInstance = ReflectionUtil.instantiateCollection(key.getType());
            List<ModelValueHolder> mvals = entry.getValue();
            int paramIndex = 0;
            Object object = nestedObjectMap.get(key.getName());
            if(object != null)
            {
                for (ModelValueHolder item : mvals)
                {
                    if(paramIndex != item.getIndex())
                    {
                        collectionInstance.add(object);
                        ReflectionUtil.set(key, instance, collectionInstance);
                        object = quietInstantiation(object.getClass());
                    }
                    final Object nestedObject = object; 
                    ReflectionUtil.getField(object.getClass(), item.getFieldName()).ifPresent(f -> {
                        new RequestField(f, nestedObject).setValue(item.getValue());
                    });
                    paramIndex = item.getIndex();
                }
                if(object != null)
                {
                    collectionInstance.add(object);
                    ReflectionUtil.set(key, instance, collectionInstance);
                    object = quietInstantiation(object.getClass());
                }
            }
        }
    }
    
    private void processSimpleModel(List<ModelValueHolder> list, Map<String, Object> nestedObjectMap)
    {
        for (ModelValueHolder mv : list)
        {
            Object nestedObject = nestedObjectMap.get(mv.getBaseField().getName());
            if (nestedObject != null)
            {
               
                    ReflectionUtil.getField(nestedObject.getClass(), mv.getFieldName()).ifPresent(f -> {
                        new RequestField(f, nestedObject).setValue(mv.getValue());
                    });
                    ReflectionUtil.set(mv.getBaseField(), instance, nestedObject);
               
            }
        }
    }

    public static class ModelValueHolder
    {
        private Field baseField;
        private String fieldName;
        private String value;
        private int index;

        public ModelValueHolder(Field baseField, String fieldName, String value, int index)
        {
            this.baseField = baseField;
            this.fieldName = fieldName;
            this.value = value;
            this.index = index;
        }

        public int getIndex()
        {
            return index;
        }

        public void setIndex(int index)
        {
            this.index = index;
        }

        public Field getBaseField()
        {
            return baseField;
        }

        public void setBaseField(Field baseField)
        {
            this.baseField = baseField;
        }

        public String getFieldName()
        {
            return fieldName;
        }

        public void setFieldName(String fieldName)
        {
            this.fieldName = fieldName;
        }

        public String getValue()
        {
            return value;
        }

        public void setValue(String value)
        {
            this.value = value;
        }

    }

    public static class ModelProcessor
    {
        private String key;

        public ModelProcessor(String key)
        {
            this.key = key;
        }

        public void process(ParamProcessor pp)
        {
            if (isNestedModel())
            {
                String nestedFiledName = key.substring(0, key.indexOf(".")); // model
                String nestedPropertyName = key.substring(key.indexOf(".") + 1); // id
                pp.processNestedModel(nestedFiledName, nestedPropertyName);
            }
            else if (isListModel())
            {
                // handle roles[0].rolename
                String nestedFiledName = key.substring(0, key.indexOf("[")); // roles
                String nestedPropertyName = key.substring(key.indexOf(".") + 1); // rolename
                Matcher matcher = Pattern.compile("\\[(.)\\]").matcher(key);
                int nestedIndex = 0;
                if (matcher.find())
                    nestedIndex = Integer.parseInt(matcher.group(1));
                pp.processListModel(nestedFiledName, nestedPropertyName, nestedIndex);
            }
            else
            {
                pp.processField(key);
            }
        }

        private boolean isNestedModel()
        {
            // nested object model.id
            return key.indexOf(".") > 0 && key.indexOf("[") < 1;
        }

        private boolean isListModel()
        {
            return key.indexOf("[") > 0;
        }

    }

    public interface ParamProcessor
    {
        void processNestedModel(String modelFieldName, String propertyName);

        void processListModel(String nestedFiledName, String nestedPropertyName, int nestedIndex);

        void processField(String key);
    }

    public E processRequest()
    {
        List<Field> fields = ReflectionUtil.getFields(entityClass);
        new RequestParameterFieldProcessor().process(fields, instance);
        return instance;
    }

    public E getInstance()
    {
        return instance;
    }

}