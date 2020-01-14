package com.omid.query4j;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import com.omid.query4j.db.Operator;
import com.omid.query4j.db.QueryJ;
import com.omid.query4j.db.Sort;
import com.omid.query4j.db.Sort.Direction;
import com.omid.query4j.db.Sort.Order;
import com.omid.query4j.db.WhereClause;
import com.omid.query4j.db.WhereClause.QueryParam;

public class QueryJTest
{

    @Test
    public void testQ()
    {
        QueryJ j = new QueryJ((string, parameters) -> Assert.assertEquals("select * from ( select * from users   ) users", string), (string, parameters) -> {
            Assert.assertEquals("select count(*) from ( select * from users   ) users", string);
            return null;
        });
        j.setQuery("select * from users");
        j.execute(j.createQuery());
        j.getResultCount();
    }
    
    
    @Test
    public void testQWc()
    {
        QueryJ j = new QueryJ((string, parameters) -> System.out.println(string), (string, parameters) -> {
            System.out.println(string);
            return null;
        });
        j.setQuery("select * from users");
        
        WhereClause wc = new WhereClause(Arrays.asList(new QueryParam("user_name", "omid", Operator.EQUAL)));
        j.addWhereClause(wc);
        j.addOrderBy(new Sort(new Order(Direction.ASC, "username")));
        j.execute(j.createQuery());
        j.getResultCount();
    }

}
