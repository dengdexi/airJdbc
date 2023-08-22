package samples.oracle;

import samples.mariadb.BatchSample;
import sql.airJdbc.data.TableMap;
import sql.airJdbc.service.SqlService;
import tableMaps.oracle.*;

import java.util.ArrayList;

public class FunctionSample {
    public FunctionSample(){
        SqlService sqlService = SqlService.getInstance();
        Object result;
        ArrayList<TableMap> list;

        String sql = "delete from user_login where id > 0";
        sqlService.delete(sql);

        new BatchSample(3);

        sql = "select count(*),avg(login_count),sum(login_count) from user_login";
        result = sqlService.select(sql, UserLoginMap.class);
        if (result instanceof ArrayList) {
            list = (ArrayList<TableMap>)result;
            if(list.size() > 0) {
                System.out.println("size = " + list.size());
                System.out.println("list = " + list);

                UserLoginMap map = (UserLoginMap) list.get(list.size() - 1);
                System.out.println("count(*) = " + map.get("COUNT(*)"));
                System.out.println("avg(login_count) = " + map.get("AVG(LOGIN_COUNT)"));
                System.out.println("sum(login_count) = " + map.get("SUM(LOGIN_COUNT)"));
                System.out.println("----------------------------------------------");
            }
        }
    }
}
