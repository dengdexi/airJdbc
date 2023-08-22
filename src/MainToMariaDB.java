import samples.mariadb.*;
import sql.airJdbc.service.SqlService;

import java.sql.SQLException;

public class MainToMariaDB {
    public static void main(String[] args) {
        System.out.println("MainToMariaDB start");

        //MariaDB/mysql
        SqlService.getInstance().init(
                "com.mysql.jdbc.Driver",
                "jdbc:mysql://localhost:3306/picwebdb?allowMultiQueries=true",
                "root",
                "root8@my");

        //table for test in db
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

//        SqlService.getInstance().exportTableData("src/","src/tableMaps/mariadb/", "Map", false);

        //表数据映射对象示例
//        new TableMapSample();
        //插入数据示例
//        new InsertSample();
        //查询数据示例
//        new SelectSample();
        //更新数据示例
//        new UpdateSample();
        //删除数据示例
//        new DeleteSample();
        //批量数据处理用时示例
        new BatchSample();
        //json 转换示例
//        new JsonSample();
        //二进制数据处理示例
//        new BinarySample();
        //表连接示例
//        new JoinUnionSample();
        //分组示例
//        new GroupBySample();
        //函数示例
//        new FunctionSample();
        //多实例示例
//        new MultipleInstanceSample();

        SqlService.getInstance().closeConnection();
    }
}
