package samples.oracle;

import samples.mariadb.BatchSample;
import sql.airJdbc.data.TableMap;
import sql.airJdbc.service.SqlService;
import sql.airJdbc.utils.SqlUtil;
import tableMaps.oracle.*;

import java.util.ArrayList;

public class SelectSample {
    private int logCount = 1;
    public SelectSample() {
        SqlService sqlService = SqlService.getInstance();
        Object result;
        ArrayList<TableMap> list;

        String sql = "delete from user_login where id > 0";
        sqlService.delete(sql);

        new BatchSample(3);

        sql = "select * from user_login";
        result = sqlService.select(sql, UserLoginMap.class);
        //1#
        this.print(result, false);

        sql = "select id, user_name  FROM user_login where id = ?";
        sql = SqlUtil.getParamsContent(sql, 2);
        result = sqlService.select(sql, UserLoginMap.class);
        //2#
        this.print(result, false);

        //sql = SELECT * FROM user_login WHERE id IN (1,2,3)
        sql = SqlUtil.getSelectContentIn(null, "user_login", "id", 1, 2, 3);
        //3#
        result = sqlService.select(sql, UserLoginMap.class);
        this.print(result, true);

        //sql = SELECT id,user_name  FROM user_login WHERE id IN (1,2,3)
        sql = SqlUtil.getSelectContentIn(new String[]{UserLoginMap.ID, UserLoginMap.LOGIN_COUNT},
                "user_login", "id", 1, 2, 3);
        result = sqlService.select(sql, UserLoginMap.class);
        //4#
        this.print(result, true);
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
