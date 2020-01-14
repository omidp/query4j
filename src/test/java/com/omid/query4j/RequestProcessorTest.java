package com.omid.query4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.omid.query4j.web.RequestProcessor;

public class RequestProcessorTest
{

    @Before
    public void setUp()
    {

    }

    @Test
    @Ignore
    public void testSimple()
    {
        Map<String, String[]> params = new HashMap<>();
        params.put("id", new String[] { "100" });
        User processRequest = new RequestProcessor<User>(new TestServletReq(params)) {
        }.processRequest();
        System.out.println(processRequest.getId());
    }

    @Test
    @Ignore
    public void testNestedModel()
    {
        Map<String, String[]> params = new HashMap<>();
        params.put("pname", new String[] { "omid profile" });
        params.put("user.id", new String[] { "100" });
        params.put("user.name", new String[] { "user name" });
        UserProfile processRequest = new RequestProcessor<UserProfile>(new TestServletReq(params)) {
        }.processRequest();
        System.out.println(processRequest.getUser().getId());
        System.out.println(processRequest.getUser().getName());
        System.out.println(processRequest.getPname());
    }

    @Test
    public void testListModel()
    {
        Map<String, String[]> params = new HashMap<>();
        params.put("pname", new String[] { "omid profile" });
        params.put("users[0].id", new String[] { "100" });
        params.put("users[0].name", new String[] { "user name" });
        params.put("users[1].id", new String[] { "200" });
        params.put("users[1].name", new String[] { "user name2" });
        params.put("profiles[0].pname", new String[] { "user profile" });
        UserProfileList processRequest = new RequestProcessor<UserProfileList>(new TestServletReq(params)) {
        }.processRequest();        
        System.out.println(processRequest.getPname());
        processRequest.getUsers().forEach(System.out::println);
        processRequest.getProfiles().forEach(System.out::println);
    }

    public static class UserProfileList
    {
        List<User> users;
        List<UserProfile> profiles;
        String pname;
        
        

        public List<UserProfile> getProfiles()
        {
            return profiles;
        }

        public void setProfiles(List<UserProfile> profiles)
        {
            this.profiles = profiles;
        }

        public List<User> getUsers()
        {
            return users;
        }

        public void setUsers(List<User> users)
        {
            this.users = users;
        }

        public String getPname()
        {
            return pname;
        }

        public void setPname(String pname)
        {
            this.pname = pname;
        }

    }

    public static class User
    {
        Long id;
        String name;

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public Long getId()
        {
            return id;
        }

        public void setId(Long id)
        {
            this.id = id;
        }
        
        @Override
        public String toString()
        {
            return "id : " +getId() + " , NAME : " + getName();
        }

    }

    public static class UserProfile
    {
        User user;
        String pname;

        public User getUser()
        {
            return user;
        }

        public void setUser(User user)
        {
            this.user = user;
        }

        public String getPname()
        {
            return pname;
        }

        public void setPname(String pname)
        {
            this.pname = pname;
        }
        
        @Override
        public String toString()
        {
            return "pname : " + getPname();
        }

    }

    public static class TestServletReq implements HttpServletRequest
    {
        Map<String, String[]> params;

        public TestServletReq(Map<String, String[]> params)
        {
            this.params = params;
        }

        @Override
        public Object getAttribute(String name)
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Enumeration getAttributeNames()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getCharacterEncoding()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setCharacterEncoding(String env) throws UnsupportedEncodingException
        {
            // TODO Auto-generated method stub

        }

        @Override
        public int getContentLength()
        {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public String getContentType()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ServletInputStream getInputStream() throws IOException
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getParameter(String name)
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Enumeration getParameterNames()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String[] getParameterValues(String name)
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Map getParameterMap()
        {
            return params;
        }

        @Override
        public String getProtocol()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getScheme()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getServerName()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int getServerPort()
        {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public BufferedReader getReader() throws IOException
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getRemoteAddr()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getRemoteHost()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setAttribute(String name, Object o)
        {
            // TODO Auto-generated method stub

        }

        @Override
        public void removeAttribute(String name)
        {
            // TODO Auto-generated method stub

        }

        @Override
        public Locale getLocale()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Enumeration getLocales()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean isSecure()
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public RequestDispatcher getRequestDispatcher(String path)
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getRealPath(String path)
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int getRemotePort()
        {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public String getLocalName()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getLocalAddr()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int getLocalPort()
        {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public String getAuthType()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Cookie[] getCookies()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getDateHeader(String name)
        {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public String getHeader(String name)
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Enumeration getHeaders(String name)
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Enumeration getHeaderNames()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int getIntHeader(String name)
        {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public String getMethod()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getPathInfo()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getPathTranslated()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getContextPath()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getQueryString()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getRemoteUser()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean isUserInRole(String role)
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public Principal getUserPrincipal()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getRequestedSessionId()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getRequestURI()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public StringBuffer getRequestURL()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getServletPath()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public HttpSession getSession(boolean create)
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public HttpSession getSession()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean isRequestedSessionIdValid()
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isRequestedSessionIdFromCookie()
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isRequestedSessionIdFromURL()
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isRequestedSessionIdFromUrl()
        {
            // TODO Auto-generated method stub
            return false;
        }

    }

}
