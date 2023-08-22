package samples.mariadb;

import sql.airJdbc.data.TableMap;
import sql.airJdbc.service.SqlService;
import tableMaps.mariadb.*;

import java.util.ArrayList;

public class FunctionSample {
    public FunctionSample(){
        SqlService sqlService = SqlService.getInstance();
        Object result;
        ArrayList<TableMap> list;

        String sql = "delete from user_login where id > 0";
        sqlService.delete(sql);

        new BatchSample(3);

        sql = "select *,count(*),avg(login_count),sum(login_count) from user_login";
        result = sqlService.select(sql, UserLoginMap.class);
        if (result instanceof ArrayList) {
            list = (ArrayList<TableMap>)result;
            if(list.size() > 0) {
                System.out.println("size = " + list.size());
                System.out.println("list = " + list);

                UserLoginMap map = (UserLoginMap) list.get(list.size() - 1);
                System.out.println("count(*) = " + map.get("count(*)"));
                System.out.println("avg(login_count) = " + map.get("avg(login_count)"));
                System.out.println("sum(login_count) = " + map.get("sum(login_count)"));
                System.out.println("----------------------------------------------");
            }
        }
    }
}
