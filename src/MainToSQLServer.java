import samples.sqlserver.*;
import sql.airJdbc.service.SqlService;

import java.sql.SQLException;

public class MainToSQLServer {
    public static void main(String[] args) {
        System.out.println("MainToSQLServer start");

        //sql server
        //if not set databaseName=xxx, use default database master
        SqlService.getInstance().init(
                "com.microsoft.sqlserver.jdbc.SQLServerDriver",
                "jdbc:sqlserver://localhost:1433;databaseName=sqlserverdb",
                "admin",
                "develop8");

        //table for test in db
        SqlService.getInstance().executeSql("drop table user_login");
        SqlService.getInstance().executeSql(
                "CREATE TABLE user_login(" +
                        "id bigint IDENTITY(1,1) NOT NULL," +
                        "user_name varchar(50) NULL," +
                        "login_count int NULL," +
                        "create_date datetime NULL," +
                        "var_content varbinary(max) NULL," +
                        "CONSTRAINT PK_user_login PRIMARY KEY CLUSTERED(id))"
        );
        System.out.println("----------------------------------------------");

        SqlService.getInstance().exportTableData("src/","src/tableMaps/sqlserver/", "Map", false);

        //表数据映射对象示例
        new TableMapSample();
        //插入数据示例
//        new InsertSample();
        //查询数据示例
//        new SelectSample();
        //更新数据示例
//        new UpdateSample();
        //删除数据示例
//        new DeleteSample();
        //批量数据处理用时示例
//        new BatchSample();
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
