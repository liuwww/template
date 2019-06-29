# 简介
template jdbc 是基于spirng JdbcTemplate 封装的对数据库进行增删改查的一套api。实现了在项目启动加载时，加载配置的数据源中的表及视图的元数据，在对数据增删改查时，根据元数据进行sql语句的组装，实现数据的操作。api分为两个，一个是数据操作api,另一个是数据查询api。

# 使用
## 数据api
 IDataTemplate 主要方法：<br/>
 ### 新增
 ```
   public Row insert(String tableName, Map<String, Object> fieldVals);
   public List<Row> insert(String tableName, List<Map<String, Object>> fieldValList);
   public List<Row> insert(String tableName, List<Map<String, Object>> fieldValList);
   public <T> List<T> insert(List<? extends TableEntity<T>> entityList);
```
 map 中的key 可以是表字段名称或者是JavaBean的字段名称 如 user_name,USER_NAME,userName,表字段名称不区分大小写，sql组装时根据元数据中的字段名称组装，参数大小写没有影响。使用bean做参数时 bean需要实现接口TableEntity并实现方法 tableName,返回该表的表名称。如果存在多个数据源，并且多个数据源存在表面一致的表，在进行方法调用时，应增加参数Jdbctemplate 以指明使用哪一个数据源，未指明时使用匹配到的第一个数据源。
 #### ID生成策略
  默认使用的是Snowflake ID 的策略，使用long类型表示，目前不支持自定义的其他策略。
 
 ### 修改
 ```
    public int update(String tableName, Map<String, Object> fieldVals);
    public int update(String tableName, List<Map<String, Object>> fieldMapList);
    public <T> int update(TableEntity<T> entity);
    public <T> int update(List<? extends TableEntity<T>> entityList);
    public int updateRows(String tableName, Map<String, Object> valMap, Map<String, Object> paramMap);
 ```
 修改同样支持指定数据源。使用map作为参数时，key匹配上的字段作为有效修改字段；bean作为参数时，field匹配上并且value 不为null的字段作为有效修改字段。
 updateRows方法可以做批量修改，paramMap 作为字段条件，valMap做为要修改的字段。
 
 ### 删除
 ```
    public int delete(String tableName, Map<String, Object> idMap);
    public int delete(String tableName, List<Map<String, Object>> idMapList);
    public int deleteRows(String tableName, Map<String, Object> paramMap);
    public <T> int delete(TableEntity<T> entity);
    public <T> int delete(List<? extends TableEntity<T>> enityList);
    public int delete(String table, Serializable id);
    public int delete(String table, Serializable[] ids);
 ```
 删除支持指定数据源。map或bean作为参数时，必须含有id字段，以id字段作为条件删除。deleteRows 可以使用条件删除。
 
 ## 查询api
 IQueryTemplate 简单查询
 QueryBean 获取指定结果集
 QueryBeanCompare 查询条件组装
 下面进一步说明


 
 
