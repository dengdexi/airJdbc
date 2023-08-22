package samples.mariadb;

import sql.airJdbc.data.TableMap;
import sql.airJdbc.utils.SqlUtil;
import sql.airJdbc.service.SqlService;
import tableMaps.mariadb.*;

import java.util.ArrayList;

public class DeleteSample {
    public DeleteSample(){
        SqlService sqlService = SqlService.getInstance();
        Object result;
        ArrayList<TableMap> list;

        String sql = "delete from user_login where id > 0";
        sqlService.delete(sql);

        new BatchSample(30);

        //sql = DELETE FROM user_login WHERE id in (1, 2, 3)
        result = sqlService.deleteBatch("user_login", "id", 1, 2, 3);
        System.out.println("1# result = " + result);

        //sql = DELETE FROM user_login WHERE id in (4, 5, 6)
        result = sqlService.deleteBatch("user_login", "id", new Object[]{4, 5, 6});
        System.out.println("2# result = " + result);
        
        sql = "delete from user_login where id = 7";
        result = sqlService.delete(sql);
        System.out.println("3# result = " + result);

        //sql = DELETE FROM user_login WHERE id = 8
        sql = "delete from user_login where id = ?";
        sql = SqlUtil.getParamsContent(sql, 8);
        result = sqlService.delete(sql);
        System.out.println("4# result = " + result);
        
        //sql = DELETE FROM user_login WHERE id = 9
        UserLoginMap map = new UserLoginMap();
        map.tableName = "user_login";
        map.set(
                UserLoginMap.ID, 9
        );
        result = sqlService.deleteOne(map);
        System.out.println("5# result = " + result);

        //sql = SELECT * FROM user_login WHERE id = 10
        sql = "SELECT * FROM user_login WHERE id = 10";
        result = sqlService.select(sql, UserLoginMap.class);
        if (result instanceof ArrayList) {
            list = (ArrayList<TableMap>)result;
            if(list.size() > 0) {
                map = (UserLoginMap) list.get(0);
                System.out.println("map.id = " + map.get(UserLoginMap.ID));
                //sql = DELETE FROM user_login WHERE id = 10
                result = sqlService.deleteOne(map);
                System.out.println("6# result = " + result);
            }
        }

        //sql = SELECT * FROM user_login WHERE id < 15
        sql = "SELECT * FROM user_login WHERE id < 15";
        result = sqlService.select(sql, UserLoginMap.class);
        if (result instanceof ArrayList) {
            list = (ArrayList<TableMap>)result;
            if(list.size() > 0) {
                System.out.println("list.size = " + list.size());
                //sql = DELETE FROM user_login WHERE id in (xxx, xxx, xxx)
                result = sqlService.deleteBatch(list);
                System.out.println("7# result = " + result);
            }
        }
    }
}
