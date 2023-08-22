package samples.mariadb;

import sql.airJdbc.data.TableMap;
import sql.airJdbc.service.*;
import sql.airJdbc.utils.SqlUtil;
import tableMaps.mariadb.UserLoginMap;

import java.util.ArrayList;

public class UtilSample {
    private int logCount = 1;
    public UtilSample(){
        SqlService sqlService = SqlService.getInstance();
        String sql;
        Object result;
        ArrayList<TableMap> list;

        //SELECT * FROM user_login WHERE id > 1
        sql = SqlUtil.getSelectContent("user_login", "id", ">", 1);
        result = sqlService.select(sql, UserLoginMap.class);
        //3#
        this.print(result, false);

        //SELECT id, user FROM user_login WHERE user = "ub"
        sql = SqlUtil.getSelectContent(new String[]{"id", UserLoginMap.USER_NAME},
                "user_login", UserLoginMap.USER_NAME, "=", "ub");
        result = sqlService.select(sql, UserLoginMap.class);
        //4#
        this.print(result, false);

        //SELECT id,user FROM user_login WHERE user = "ub" ORDER BY id desc, user asc LIMIT 2 OFFSET 1
        sql = SqlUtil.getSelectContentByCondition(new String[]{UserLoginMap.ID, UserLoginMap.LOGIN_COUNT},
                "user_login", UserLoginMap.USER_NAME, "=", "ub", 2, 1,
                new String[]{UserLoginMap.ID, UserLoginMap.LOGIN_COUNT}, new String[]{"desc", "asc"});
        result = sqlService.select(sql, UserLoginMap.class);
        //5#
        this.print(result, true);

        //sql = SELECT * FROM user_login WHERE id < 1252
        sql = SqlUtil.getSelectContent("user_login", "id", "<", 1252);
        result = sqlService.select(sql, TableMap.class);
        if (result instanceof ArrayList) {
            list = (ArrayList<TableMap>)result;
            if(list.size() > 0) {
                System.out.println("list.size = " + list.size());
                //sql = INSERT INTO user_login (login_count, id, create_date, user_name) VALUES (3, 2628, '2022-11-28 12:15:00.0', 'u5'), (8, 2842, '2022-11-28 12:15:00.0', 'u0')
                for (Object tableMap : list) {
                    ((TableMap)tableMap).set(
                            UserLoginMap.ID, (int) (Math.random() * 1000) + 2000,
                            UserLoginMap.USER_NAME, "u" + (int) (Math.random() * 10),
                            UserLoginMap.LOGIN_COUNT, (int) (Math.random() * 10)
                    );
                }
                result = sqlService.insertBatch((ArrayList<TableMap>)result);
                System.out.println("10# result = " + result);
            }
        }

        //sql = DELETE FROM user_login WHERE id = 1002
        sql = SqlUtil.getDeleteOneContent("user_login", "id", 1002);
        result = sqlService.delete(sql);
        System.out.println("5# result = " + result);

        //sql = DELETE FROM user_login WHERE id = 1003
        sql = SqlUtil.getDeleteContent("user_login", "id", "=", 1003);
        result = sqlService.delete(sql);
        System.out.println("6# result = " + result);

        //sql = DELETE FROM user_login WHERE id < 1218
        sql = SqlUtil.getDeleteContent("user_login", "id", "<", 1218);
        result = sqlService.delete(sql);
        System.out.println("7# result = " + result);
    }
    //
    private void print(Object result, boolean isAll){
        System.out.println(logCount + "# -------------------------------");
        logCount ++;
        if(result instanceof ArrayList){
            ArrayList<TableMap> list = (ArrayList<TableMap>) result;
            System.out.println("size = " + list.size());

            if(isAll){
                for (TableMap tableMap : list) {
                    System.out.println("id = " + tableMap.get(UserLoginMap.ID));
                    System.out.println(tableMap.toString());
                }
            }else if(list.size() > 0) {
                UserLoginMap userLoginMap = (UserLoginMap)list.get(0);
                System.out.println("id = " + userLoginMap.get(userLoginMap.ID));
                System.out.println(userLoginMap.toString());
            }
        }else {
            System.out.println("sql error");
        }
    }
}
