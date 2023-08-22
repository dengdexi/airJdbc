package samples.sqlserver;

import sql.airJdbc.data.TableMap;
import sql.airJdbc.service.SqlService;
import tableMaps.sqlserver.*;

import java.util.ArrayList;

public class UpdateSample {
    public UpdateSample(){
        SqlService sqlService = SqlService.getInstance();
        Object result;
        ArrayList<TableMap> list;

        String sql = "delete from user_login where id > 0";
        sqlService.delete(sql);

        new BatchSample(3);

        sql = "update user_login set user_name = 'uk' where id = 1";
        result = sqlService.update(sql);
        System.out.println("1# result = " + result);

        //sql = UPDATE user_login SET user_name = 'ug', login_count = 3 WHERE id < 3
        sql = "UPDATE user_login SET user_name = 'ug', login_count = 3 WHERE id < 3";
        result = sqlService.update(sql);
        System.out.println("2# result = " + result);

        //sql = UPDATE user_login SET user_name = 'u3', login_count = 5 WHERE id in (1, 2, 3)
        result = sqlService.updateBatch(
                "user_login", "id", new Object[]{1, 2, 3},
                UserLoginMap.USER_NAME, "u3",
                UserLoginMap.LOGIN_COUNT, 5
        );
        System.out.println("3# result = " + result);

        //sql = UPDATE user_login SET user_name = 'ug',login_count = 3 WHERE id = 3
        sql = "UPDATE user_login SET user_name = 'ug',login_count = 3 WHERE id = 3";
        result = sqlService.update(sql);
        System.out.println("4# result = " + result);

        //sql = UPDATE user_login SET login_count = 2, user_name = "u1" WHERE id = 1
        UserLoginMap map = new UserLoginMap();
        map.tableName = "user_login";
        map.set(
                UserLoginMap.ID, 1,
                UserLoginMap.USER_NAME, "u1",
                UserLoginMap.LOGIN_COUNT, 2
        );
        result = sqlService.updateOne(map);
        System.out.println("5# result = " + result);
        result = sqlService.updateOne(TableName.USER_LOGIN, UserLoginMap.ID, 2, UserLoginMap.USER_NAME, "uu");
        System.out.println("5# result = " + result);

        //sql = SELECT * FROM user_login WHERE id = 2
        sql = "SELECT * FROM user_login WHERE id = 2";
        result = sqlService.select(sql, UserLoginMap.class);
        if (result instanceof ArrayList) {
            list = (ArrayList<TableMap>)result;
            if(list.size() > 0) {
                map = (UserLoginMap) list.get(0);
                System.out.println("map.id = " + map.get(UserLoginMap.ID));
                //sql = UPDATE user_login SET login_count = 4, user_name = "u2" WHERE id = 2
                map.set(
                        UserLoginMap.USER_NAME, "u2",
                        UserLoginMap.LOGIN_COUNT, 4
                );
                result = sqlService.updateOne(map);
                System.out.println("6# result = " + result);
            }
        }

        //sql = SELECT * FROM user_login WHERE id < 1252
        sql = "SELECT * FROM user_login WHERE id < 1252";
        result = sqlService.select(sql, UserLoginMap.class);
        if (result instanceof ArrayList) {
            list = (ArrayList<TableMap>)result;
            if(list.size() > 0) {
                System.out.println("list.size = " + list.size());
                if (result instanceof ArrayList) {
                    result = sqlService.updateBatch(list, UserLoginMap.USER_NAME, "ui", UserLoginMap.LOGIN_COUNT, 99);
                    System.out.println("7# result = " + result);
                }
            }
        }
    }
}
