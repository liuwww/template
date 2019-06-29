# 简介
template jdbc 是基于spirng JdbcTemplate 封装的对数据库进行增删改查的一套api。实现了在项目启动加载时，加载配置的数据源中的表及视图的元数据，在对数据增删改查时，根据元数据进行sql语句的组装，实现数据的操作。api分为两个，一个是数据操作api,另一个是数据查询api。使用查询api，可以不在java代码中拼接sql,开发过程中，sql多数以视图给出，部分sql以xml文件形式给出，查询条件以接口形式组装，条件的有效性由接口中自动判断实现。在开发过程中，数据表字段常常变化，基础代码或者配置需要跟着改动，以元数据自动匹配的功能，不需要改动基础代码，省掉了很多麻烦。该套代码已实现mysql和h2的测试，在mysql和oracle环境中使用过。

# 使用,主要接口说明
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
 IQueryTemplate 简单查询<br/>
 QueryBean 获取指定结果集<br/>
 QueryBeanCompare 查询条件组装<br/>

 ### queryTemplate
 ```
 public QueryBean createQueryBean(String file, String tag, Object... params);
 public QueryBean createQueryBean(String tableName);
 public QueryBean createQueryBean(Table table);
```
需要指定数据源的情况，在上述方法参数中加上JdbcTemplate
其中entity中的有效值直接构建到查询条件中；talbeName 可以是表名或者视图，参数：file,tag,表示QueryBean 可以从xml 文件总构建，file表示xml 文件名，tag表示所属查询，例子/sql/test.xml：
<?xml version="1.0" encoding="UTF-8"?>
<sqls>
	<testQuery>
		<sql>select * from test_user where sts='A' </sql>
		<order-by>user_id asc</order-by>
	</testQuery>
	<testQuery2>
		<sql>select * from test_user2 where sts='A' </sql>
		<jdbcTemplate>jdbcTemplate1</jdbcTemplate>
		<order-by>user_id asc</order-by>
	</testQuery2>
	<testQuery3>
		<sql>select * from test_user2 where sts=? and sts_date>? </sql>
		<jdbcTemplate>jdbcTemplate1</jdbcTemplate>
		<order-by>user_id asc</order-by>
	</testQuery3>
</sqls>

```
queryTeamplge.createQuery("test","testQuery").getPage(new Page());
queryTeamplge.createQuery("test","testQuery2",sts,stsDate).getPage(new Page());
```

条件与参数需要匹配，如果xml总存在?参数,通常需要在构建时，以不定长数组给出。xml中可以指定jdbcTemplate以指定查询指定的数据源，orderBy参数也是有效的。
Table 也可以表示一个表或视图的查询，table.addField(field,alias).addFiled…… 表示查询的列，当为空时，查询所有字段，table 也实现了compare接口，可以构建灵活的查询条件，下面以QueryBeanCompare 统一说明。
Compare 中实现了：
```
T eq(String field, Object val);//=
T ne(String field, Object val);//!=
T le(String field, Object val);//<=
T lt(String field, Object val);//<
T gt(String field, Object val);//>
T ge(String field, Object val);//>=
T like(String field, Object val);//like 参数不字段加%
T likeAuto(String field, Object val);//查询有效时，加%val%
T likeR(String field, Object val);//查询有效时，加val%
T likeL(String field, Object val);//查询有效时，加%L
T addParamMap(Map<String, Object> paramMap);//map中的有效值做=条件
T notNull(String field);//不为空
T emptyStr(String field);//值为空字符串
```
其中T表示自身this,可以链式调用
```
 compare.eq(field,val).ge(f1,v2);
```
以上组合的条件以and 连接，以or 连接 需要在参数ConditionRel.OR,例如：
```
 compare.eq(field,val,ConditionRel.OR).ge(f1,v2,ConditionRel.OR);
```
如果需要带括号的查询条件，则使用GroupCondition group;compare.addCondition(group);GroupCondition实现了Compare接口，可以嵌套使用，group=new GroupCondition(ConditionRel.OR)表示or连接，默认时AND连接。
示例：
```
 QueryBean queryBean = queryTemplate.createQueryBean(……);
 queryBean.getCompare().eq(f1,v1).like(f2,v2).ge(f3,v3);
 GroupCondtion g1 = new GroupCondition();// new GroupCondition(ConditionRel.OR)
 g1.liekL(f4,val4).le(f5,val5).addCondition(new GroupCondition());//最后这个group 中没有值，是无效的
 queryBean.getBean();
 queryBean.getMap();
 queryBean.getPage(),
 queryBean.getBeanList(),
 queryBean.getMapList();
 //示例的链式调用，参数以上面给出的声明为准
 page= queryTemplage.createQueryBean(params).getCompare().eq(ps).le(ps).like(ps).addCondition(c).getQueryBean.getPage(page);
```
 
 
