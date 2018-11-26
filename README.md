# JOM v1.0
-
Supported Named Query, Store Procedure and Java Object with JOM annotations  

Supported Callback



User Guide
-
`Entity`
-

```java
@Table(name = "jom", schema = "public")
@Queries(queries = {
            @Query(name = "insertTest", statement = "insert into jom (id,name) values (?,?)"),
            @Query(name = "selectAll", statement = "select * from jom"),
            @Query(name = "procedure", statement = "{call jomprocedure(?,?)}", type = DefineQueryType.PROCEDURE)
})
public class Test {
   @Column(name = "id", id = true)
   Long id;
   @Column(name = "name")
   String name;
   
   //getter and setter....
}
```

`Init JOM EntityManager`
-
```java
public class Main{
    public static void main(String[] args){
            private static DataSource dataSource;
            
            public static void main(String[] args) {
                if (null == dataSource){
                    try {
                        dataSource = new BasicDataSource();
                        ((BasicDataSource) dataSource).setDriverClassName("org.h2.Driver");
                        ((BasicDataSource) dataSource).setUrl("jdbc:h2:file:~/embedded/jom_db");
                        ((BasicDataSource) dataSource).setUsername("sa");
                        ((BasicDataSource) dataSource).setPassword("");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
        
                EntityManager em = new EntityManagerImpl(dataSource);
                }
             }
    }
}
```

`Callback Transaction`
-
```java
public class Callback{
    private EntityManager em;
    
    {...}
    
    public void call(){
        final Test callback = new Test();
        
        em.select(Test,new CallBack<List<Test>>(
            @Override
            public void completed(List<Test> result) {
                 {...} // result
            }
            
             @Override
             public void exception(Throwable throwable) {
                {...} // exception
             }      
        ));
    }
}
```

`Named Query Call`
-
```java
public class NamedQuery{
    private EntityManager em;
    {...}
    public void named(){
        em.named("insertTest", new Object[]{(long) (Math.random() * 1000), "JOMNamedQuery"}, Test.class);
    }
}
```

JOM with frameworks
-
`JOM With Spring`
-
```xml
<?xml version = "1.0" encoding = "UTF-8"?>
<beans xmlns = "http://www.springframework.org/schema/beans"
   xmlns:xsi = "http://www.w3.org/2001/XMLSchema-instance"
   xsi:schemaLocation = "http://www.springframework.org/schema/beans
   http://www.springframework.org/schema/beans/spring-beans-3.0.xsd">

   <!-- Definition for JOM Persistence bean -->
   <bean id = "jom" class = "com.kelvin.jom.persistence.impl.EntityManagerImpl">
      <constructor-arg  ref = "dataSource" />
   </bean>

   <!-- Definition for DataSource bean -->
   <bean name="dataSource" class="org.springframework.jdbc.datasource.DriverManagerDataSource">
       <property name="driverClassName" value="com.mysql.jdbc.Driver" />
       <property name="url" value="jdbc:mysql://localhost:3306/jom" />
       <property name="username" value="sa" />
       <property name="password" value="" />
   </bean>
</beans>
```

```java
public class SpringDAO{
    @Autowired
    private EntityManager em;
    
    public void save(Entity entity){
        em.insert(entity);
    }
    // another transaction method like update,delete
}
```
    
`JOM with CDI`
-
```java
public class Instance{
    // create data source class
    private DataSource ds;
    
    private DataSource dataSource(){
        // init datasource
    }
    
    
    @Named("entityManager")
    public EntityManager entityManager(){
        return new EntityManagerImpl(dataSource());
    }
}

```

```java
public class CdiDAO{
    @Inject
    private EntityManager em;
    
    public void save(Entity entity){
        em.insert(entity);
    }
    // another transaction method like update,delete
}
```

v1.0 Supported Functions
-
```java
     <T> T update(T entity);

     <T> void insert(T entity);

     <T> void delete(T entity);

     <T> Collection<T> select(T entity);
    
     <T> void update(T entity, CallBack callBack);
    
     <T> void select(T entity, CallBack callBack);
    
     <T> void delete(T entity, CallBack callBack);
    
     <T> void insert(T entity, CallBack callBack);
        
     <R, T> R named(String name, Object[] params, Class<T> clazz);
        
     <R, T> R named(String name, Object[] params, CallBack<R> callback, Class<T> clazz);
        
     <R, T> R named(String name, Object[] params, EntityTransform<R> transform, CallBack<R> callBack, Class<T> clazz);
    
     <R, T> R procedure(String name, Object[] params, Class<T> clazz);
     
     <R, T> R procedure(String name, Object[] params, CallBack<R> callback, Class<T> clazz);
     
     <R, T> R procedure(String name, Object[] params, EntityTransform<R> transform, CallBack<R> callBack, Class<T> clazz);
```


