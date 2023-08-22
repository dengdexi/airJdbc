package samples.sqlserver;

import sql.airJdbc.data.TableMap;
import sql.airJdbc.service.SqlService;
import tableMaps.sqlserver.*;

import java.util.ArrayList;

public class TableMapSample {
    public TableMapSample(){
        SqlService sqlService = SqlService.getInstance();
        Object result;
        ArrayList<TableMap> list;

        String sql = "delete from user_login where id > 0";
        sqlService.delete(sql);

        new BatchSample(3);

        //use TableMap
        sql = "select * from user_login";
        result = sqlService.select(sql, TableMap.class);
        if (result instanceof ArrayList) {
            list = (ArrayList<TableMap>)result;
            System.out.println("list.size = " + list.size());

            if(list.size() > 0){
                TableMap tableMap = (TableMap) list.get(0);
                System.out.println("1# user_name = " + tableMap.get("user_name"));
            }
        }else {
            System.out.println("sql error");
        }

        //use UserLoginMap
        result = sqlService.select(sql, UserLoginMap.class);
        if (result instanceof ArrayList) {
            list = (ArrayList<TableMap>)result;
            System.out.println("list.size = " + list.size());

            if(list.size() > 0){
                UserLoginMap userLoginMap = (UserLoginMap)list.get(0);
                System.out.println("2# user_name = " + userLoginMap.get(UserLoginMap.USER_NAME));
            }
        }else {
            System.out.println("sql error");
        }

    }
}
