# airJdbc 
update@2023.8.22

#### 介绍
高性能，更轻快，更简单易用的 OOP JDBC 框架，替代代码繁琐的原生 JDBC，以及配置繁琐的 Mybatis，Hibernate 等框架

#### 特性
1.  增删改查只需要一句代码即可完成
2.  高性能，批量插入10000条数据只需要 37-53 ms
3.  支持使用 OOP 编程
4.  支持 sql 语句编程
5.  支持导出数据表映射 Java 类对象
6.  支持通过多实例轻松完成多线程任务
7.  支持 MariaDB/MySql, Oracle, SqlServer 数据库，未来将支持更多主流数据库
8.  jdbc代码量减少80%


#### 安装教程

1.  下载代码或jar包即用，MariaDB/MySql, Oracle, SqlServer 各数据库示例，参阅 src/samples 包中代码

#### 简单示例，增删改查只需要一句代码代即可完成

1.  sql语句编程，MariaDB/MySql 增删改查示例

```
//MariaDB/mysql 初始化并连接数据库
SqlService.getInstance().init(
                "com.mysql.jdbc.Driver",
                "jdbc:mysql://localhost:3306/picwebdb?allowMultiQueries=true",
                "root",
                "root8@my");
```

```
//创建表 user_login
SqlService.getInstance().executeSql(
                "drop table if exists user_login;" +
                "create table user_login(" +
                        "id bigint(30) auto_increment," +
                        "user_name varchar(40)," +
                        "login_count int(10)," +
                        "create_date datetime," +
                        "var_content blob," +
                        "primary key(id)" +
                        ") engine = myisam default charset=utf8"
        );
```

```
//insert 插入数据
Object result = SqlService.getInstance().insert("insert into user_login(user_name, login_count) values('ua', 1), ('ub', 1)");
```

```
//delete 删除数据
Object result = SqlService.getInstance().delete("delete from user_login where id > 0");
```

```
//update 更新数据
Object result = SqlService.getInstance().update("update user_login set user_name = 'uk' where id = 1");
```

```
//select 查询数据
Object result = SqlService.getInstance().select("select * from user_login", TableMap.class);
```

2.  OOP编程示例

```
//insert 插入数据
TableMap map = new TableMap();
map.tableName = "user_login";
map.set(
                "user_name", "ud",
                "login_count", 4
        );
Object result = SqlService.getInstance().insertOne(map);
```

```
//insert 批量插入数据，主流电脑配置插入10000条数据只需要 37-53 ms
ArrayList<TableMap> list = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            TableMap map = new TableMap ();
            map.tableName = "user_login";
            map.set("id", i + 1);
            map.set("user_name", "uname" + i);
            map.set("login_count", i + 1);
            map.set("create_date", new java.sql.Date(System.currentTimeMillis()));

            list.add(map);
        }
Object result = SqlService.getInstance().insertBatch(list);
```

3.  导出数据表映射 Java 对象，并在查询时转换为 Java 类对象
```
SqlService.getInstance().exportTableData("src/","src/tableMaps/mariadb/", "Map", false);
```

  如上述表 user_login 导出 UserLoginMap.java:
```
package tableMaps.mariadb;

import sql.airJdbc.data.TableMap;

/**
 * Data from table: picwebdb.user_login
 * Create by sql.airJdbc.SqlService
 * At date: 2023-08-21 16:01:47
 * @see sql.airJdbc.service.SqlService#exportTableData(String, String, String, boolean)
 */
public class UserLoginMap extends TableMap {
    /**
     * Column names in table
     */
    /** bigint(19) **/
    public static String ID = "id";
    /** varchar(40) **/
    public static String USER_NAME = "user_name";
    /** integer(10) **/
    public static String LOGIN_COUNT = "login_count";
    /** timestamp(19) **/
    public static String CREATE_DATE = "create_date";
    /** longvarbinary(65535) **/
    public static String VAR_CONTENT = "var_content";

    public UserLoginMap(){
        super();
        this.primaryKey = "id";
    }
}
```

```
//select 查询数据，转换为 Java 类对象，result 中包含有 UserLoginMap 对象实例
Object result = SqlService.getInstance().select("select * from user_login", UserLoginMap.class);
```

```
//使用 Java 映射类，批量插入数据
ArrayList<TableMap> list = new ArrayList<>();
for (int i = 0; i < 10000; i++) {
   UserLoginMap map = new UserLoginMap();
   map.tableName = "user_login";
   map.set(UserLoginMap.ID, i + 1);
   map.set(UserLoginMap.USER_NAME, "uname" + i);
   map.set(UserLoginMap.LOGIN_COUNT, i + 1);
   map.set(UserLoginMap.CREATE_DATE, new java.sql.Date(System.currentTimeMillis()));

   list.add(map);
}
Object result = SqlService.getInstance().insertBatch(list);
```

4.  通过多实例轻松完成多线程任务

```
//线程 1
new Thread(new Runnable(){
            @Override
            public void run() {
                Object result = SqlService.getInstance().select("select * from user_login", UserLoginMap.class);
            }
        }).run();

//线程 2
SqlService.getInstance("instance2").init();
new Thread(new Runnable(){
            @Override
            public void run() {
                Object result = SqlService.getInstance("instance2").select("select * from user_login", UserLoginMap.class);
            }
        }).run();
```
