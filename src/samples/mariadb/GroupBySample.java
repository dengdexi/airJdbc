package samples.mariadb;

import sql.airJdbc.data.TableMap;
import sql.airJdbc.service.SqlService;
import tableMaps.mariadb.*;

import java.util.ArrayList;

public class GroupBySample {
    public GroupBySample(){
        SqlService sqlService = SqlService.getInstance();
        Object result;
        ArrayList<TableMap> list;

        String sql = "delete from user_login where id > 0";
        sqlService.delete(sql);

        new BatchSample(3);

        //group by
        sql = "select id,user_name,count(*) from user_login group by user_name with rollup";
        result = sqlService.select(sql, UserLoginMap.class);
        if (result instanceof ArrayList) {
            list = (ArrayList<TableMap>)result;
            if(list.size() > 0) {
                System.out.println("size = " + list.size());
                System.out.println("list = " + list);

                UserLoginMap map = (UserLoginMap) list.get(list.size() - 1);
                System.out.println("count(*) = " + map.get("count(*)"));
                System.out.println("----------------------------------------------");
            }
        }
    }
}
