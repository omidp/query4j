package com.omid.query4j;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Transient;

import org.junit.Before;
import org.junit.Test;

import com.omid.query4j.db.QueryMapper;

import junit.framework.Assert;

public class QueryMapperTest
{

    List<Map<String, Object>> result;

    @Before
    public void setUp()
    {
        result = new ArrayList<Map<String, Object>>();
        Map<String, Object> item = new HashMap<String, Object>();
        item.put("user_name", "vata2999");
        item.put("name", "Omid");
        result.add(item);

    }

    @Test
    public void testToList()
    {
        List<User> userList = QueryMapper.toList(result, User.class);
        Assert.assertEquals(userList.size(), 1);
        System.out.println(userList.iterator().next().getFullName());
    }

    @Test
    public void testToObject()
    {
        User user = QueryMapper.toObject(result, User.class);
        Assert.assertNotNull(user);
    }
    
    public static class User implements EntityModel
    {

        private Long id;

        private String name;

        private String userName;

        @Transient
        public String getFullName()
        {
            return getUserName() + " " + getName();
        }

        @Column(name = "user_name")
        public String getUserName()
        {
            return userName;
        }

        public void setUserName(String userName)
        {
            this.userName = userName;
        }

        @Column(name = "name")
        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        @Column(name = "id")
        public Long getId()
        {
            return id;
        }

        public void setId(Long id)
        {
            this.id = id;
        }

    }


}
