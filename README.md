# query4j

An object-oriented native query builder for java with the ability to map request to your models.

## sample code

```
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
```

## Request Processor

please visit https://github.com/omidp/query4j/blob/master/src/test/java/com/omid/query4j/RequestProcessorTest.java

## QueryMapperTest

please visit https://github.com/omidp/query4j/blob/master/src/test/java/com/omid/query4j/QueryMapperTest.java
