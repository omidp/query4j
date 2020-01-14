package com.omid.query4j.db;

/**
 * @author omid
 *
 */
public interface Function
{

    public String parseColumn(String columnName);
    public Object parseColumnValue(Object columnValue);
    
}
