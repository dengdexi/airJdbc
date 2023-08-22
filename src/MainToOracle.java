import samples.oracle.*;
import sql.airJdbc.service.SqlService;

import java.sql.SQLException;

public class MainToOracle {
    public static void main(String[] args) {
        System.out.println("MainToOracle start");

        //oracle
        // jdbc:oracle:thin:@localhost:1521/ORAGLEDBS
        // jdbc:oracle:thin:@localhost:1521:ORAGLEDBS
        // jdbc:oracle:thin:@(description=(address_list=(address=(host=127.0.0.1)(protocol=tcp)(port=1521)))(connect_data=(service_name=ORAGLEDBS)))
        SqlService.getInstance().init(
                "oracle.jdbc.driver.OracleDriver",
                "jdbc:oracle:thin:@localhost:1521/ORAGLEDBS",
                "C##admin",
                "Develop8");

        //table for test in db
        SqlService.getInstance().executeSql( "drop table user_login");
        SqlService.getInstance().executeSql(
                        "CREATE TABLE user_login (" +
                                "id NUMBER(10) NOT NULL," +
                                "user_name VARCHAR2(255)," +
                                "login_count NUMBER(10)," +
                                "create_date DATE," +
                                "var_content BLOB," +
                                "PRIMARY KEY (id)" +
                                ")"
        );

        System.out.println("----------------------------------------------");

        SqlService.getInstance().exportTableData("src/","src/tableMaps/oracle/", "Map", false);

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
