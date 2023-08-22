import sql.airJdbc.data.TableMap;
import sql.airJdbc.service.SqlService;
import tableMaps.mariadb.UserLoginMap;

import java.util.ArrayList;

public class MainToTest {
    public static void main(String[] args) {
        System.out.println("MainToTest start");

        //MariaDB/mysql 初始化并连接数据库
        SqlService.getInstance().init(
                "com.mysql.jdbc.Driver",
                "jdbc:mysql://localhost:3306/picwebdb?allowMultiQueries=true",
                "root",
                "root8@my");

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
        System.out.println("----------------------------------------------");

        //insert 插入数据
        Object result = SqlService.getInstance().insert("insert into user_login(user_name, login_count) values('ua', 1), ('ub', 1)");

        //update 更新数据
        result = SqlService.getInstance().update("update user_login set user_name = 'uk' where id = 1");

        //select 查询数据
        result = SqlService.getInstance().select("select * from user_login", TableMap.class);

        //delete 删除数据
        result = SqlService.getInstance().delete("delete from user_login where id > 0");

        //OOP编程示例 insert 插入数据
        TableMap map = new TableMap();
        map.tableName = "user_login";
        map.set(
                "user_name", "ud",
                "login_count", 4
        );
        result = SqlService.getInstance().insertOne(map);

        //select 查询数据，转换为 Java 类对象，result 中包含有 UserLoginMap 对象实例
        result = SqlService.getInstance().select("select * from user_login", UserLoginMap.class);

        //delete 删除数据
        result = SqlService.getInstance().delete("delete from user_login where id > 0");

        //使用 Java 映射类，批量插入数据
        ArrayList<TableMap> list = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            UserLoginMap userLoginMap = new UserLoginMap();
            userLoginMap.tableName = "user_login";
            userLoginMap.set(UserLoginMap.ID, i + 1);
            userLoginMap.set(UserLoginMap.USER_NAME, "uname" + i);
            userLoginMap.set(UserLoginMap.LOGIN_COUNT, i + 1);
            userLoginMap.set("create_date", new java.sql.Date(System.currentTimeMillis()));

            list.add(userLoginMap);
        }
        result = SqlService.getInstance().insertBatch(list);

        //通过多实例轻松完成多线程任务
        SqlService.getInstance("instance2").init();

        //线程 1
        new Thread(new Runnable(){
            @Override
            public void run() {
                Object result = SqlService.getInstance("instance2").select("select * from user_login", UserLoginMap.class);
            }
        }).run();

        //线程 2
        new Thread(new Runnable(){
            @Override
            public void run() {
                Object result = SqlService.getInstance().select("select * from user_login", UserLoginMap.class);
            }
        }).run();
    }
}
