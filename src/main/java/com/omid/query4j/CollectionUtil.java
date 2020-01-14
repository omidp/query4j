package com.omid.query4j;

import java.util.Collection;

public class CollectionUtil
{

    public static boolean isNotEmpty(Collection<?> input)
    {
        return input != null && input.size() > 0;
    }

    public static boolean isEmpty(Collection<?> input)
    {
        return isNotEmpty(input) == false;
    }
    
}
