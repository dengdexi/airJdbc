# airJdbc
update@2023.8.22

#### introduce
High performance, lighter, and simpler to use OOP JDBC framework, replacing native JDBC with cumbersome code, as well as frameworks such as Mybatis and Hibernate with cumbersome configuration

#### characteristic
1. Insert, delete, update, and select can be completed with just one sentence of code
2. High performance, batch insertion of 10000 records takes only 37-53 ms
3. Support OOP programming
4. Support SQL statement programming
5. Support exporting data tables to map Java class objects
6. Support for easily completing multithreaded tasks through multiple instances
7. Supports MariaDB/MySql, Oracle, and SqlServer databases, and will support more mainstream databases in the future
8. Reduce the amount of jdbc code by 80%


#### Installation Tutorial

1.  Download the code or jar package and use it immediately. For examples of MariaDB/MySql, Oracle, and SqlServer databases, refer to the code in the src/samples package

#### Simple example, adding, deleting, modifying, and checking can be completed with just one sentence of code

1.  SQL statement programming, MariaDB/MySql addition, deletion, modification, and query examples

```
//MariaDB/mysql Initialize and connect to the database
SqlService.getInstance().init(
                "com.mysql.jdbc.Driver",
                "jdbc:mysql://localhost:3306/picwebdb?allowMultiQueries=true",
                "root",
                "root8@my");
```

```
//Create Table user_login
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
//insert data
Object result = SqlService.getInstance().insert("insert into user_login(user_name, login_count) values('ua', 1), ('ub', 1)");
```

```
//delete data
Object result = SqlService.getInstance().delete("delete from user_login where id > 0");
```

```
//update data
Object result = SqlService.getInstance().update("update user_login set user_name = 'uk' where id = 1");
```

```
//select data
Object result = SqlService.getInstance().select("select * from user_login", TableMap.class);
```

2.  OOP Programming Example

```
//insert data
TableMap map = new TableMap();
map.tableName = "user_login";
map.set(
                "user_name", "ud",
                "login_count", 4
        );
Object result = SqlService.getInstance().insertOne(map);
```

```
//insert Batch insertion of data, Mainstream computer configurations require only 37-53 ms to insert 10000 records
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

3.  Export the data table to map Java objects and convert them to Java class objects during queries
```
SqlService.getInstance().exportTableData("src/","src/tableMaps/mariadb/", "Map", false);
```

As shown in the above table, user_ Login export UserLoginMap. java:
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
//select Query data, convert it into Java class objects, and the result contains the UserLoginMap object instance
Object result = SqlService.getInstance().select("select * from user_login", UserLoginMap.class);
```

```
//Using Java mapping classes to batch insert data
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

4.  Easily complete multithreaded tasks through multiple instances

```
//Thread 1
new Thread(new Runnable(){
            @Override
            public void run() {
                Object result = SqlService.getInstance().select("select * from user_login", UserLoginMap.class);
            }
        }).run();

//Thread 2
SqlService.getInstance("instance2").init();
new Thread(new Runnable(){
            @Override
            public void run() {
                Object result = SqlService.getInstance("instance2").select("select * from user_login", UserLoginMap.class);
            }
        }).run();
```

