package samples.oracle;

import samples.mariadb.BatchSample;
import sql.airJdbc.data.TableMap;
import sql.airJdbc.service.SqlService;
import tableMaps.oracle.*;

import java.util.ArrayList;

public class JoinUnionSample {
    public JoinUnionSample(){
        SqlService sqlService = SqlService.getInstance();
        Object result;
        ArrayList<TableMap> list;

        String sql = "delete from user_login where id > 0";
        sqlService.delete(sql);

        new BatchSample(3);

        //add new column nickname
        sqlService.executeSql("drop table user_login_clone");
        sqlService.executeSql("create table user_login_clone as select * from user_login where rownum<4");
        sqlService.executeSql("alter table user_login_clone add NICKNAME varchar2(30) default 'air'");

        //join
        sql = "select a.id,a.user_name,b.nickname from user_login a join user_login_clone b on a.id=b.id";
        result = sqlService.select(sql, UserLoginMap.class);
        if (result instanceof ArrayList) {
            list = (ArrayList<TableMap>)result;
            if(list.size() > 0) {
                System.out.println("size = " + list.size());
                System.out.println("list = " + list);

                //get new column nickname
                UserLoginMap map = (UserLoginMap) list.get(0);
                System.out.println("1# nickname = " + map.get("NICKNAME"));
                System.out.println("----------------------------------------------");
            }
        }

        //union
        sql = "select id,user_name from user_login union all select id,user_name from user_login_clone";
        result = sqlService.select(sql, UserLoginMap.class);
        if (result instanceof ArrayList) {
            list = (ArrayList<TableMap>)result;
            if(list.size() > 0) {
                System.out.println("size = " + list.size());
                System.out.println("list = " + list);

                UserLoginMap map = (UserLoginMap) list.get(0);
                System.out.println("2# user_name = " + map.get(UserLoginMap.USER_NAME));
                System.out.println("----------------------------------------------");
            }
        }
    }
}

