package samples.mariadb;

import sql.airJdbc.data.TableMap;
import sql.airJdbc.service.SqlService;
import tableMaps.mariadb.*;

import java.util.ArrayList;

public class MultipleInstanceSample {
    public MultipleInstanceSample(){
        SqlService sqlService = SqlService.getInstance();

        String sql = "delete from user_login where id > 0";
        sqlService.delete(sql);

        new BatchSample(3);

        Object result = SqlService.getInstance().select("select * from user_login", UserLoginMap.class);
        System.out.println("1# result = " + result);
        System.out.println("----------------------------------------------");

        SqlService.getInstance("instance2").init();

        result = SqlService.getInstance("instance2").select("select * from user_login", UserLoginMap.class);
        System.out.println("2# result = " + result);
        System.out.println("----------------------------------------------");

        SqlService.getInstance("instance2").closeConnection();
    }
}
