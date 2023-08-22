package samples.sqlserver;

import sql.airJdbc.data.TableMap;
import sql.airJdbc.service.SqlService;
import sql.airJdbc.utils.SqlUtil;
import tableMaps.sqlserver.*;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class InsertSample {
    public InsertSample() {
        SqlService sqlService = SqlService.getInstance();
        Object result;
        ArrayList<TableMap> list;

        String sql = "delete from user_login where id > 0";
        sqlService.delete(sql);

        sql = "insert into user_login(user_name, login_count) values('ua', 1), ('ub', 1)";
        result = sqlService.insert(sql);
        System.out.println("1# result = " + result);
        if(result instanceof Integer){
            System.out.println("insert success:" + result);
        }else {
            System.out.println("sql error:" + result);
        }

        //sql = insert into user_login(user_name, login_count, create_date) values('uname', 2, '22-12-1 下午5:59')
        sql = "insert into user_login(user_name, login_count, create_date) values(?, ?, ?)";
        sqlService.setSqlParams("u2", 2, new Timestamp(System.currentTimeMillis()));
        result = sqlService.insert(sql);
        System.out.println("2# result = " + result);

        //sql = insert into user_login(user_name, login_count, create_date) values('uname', 2, '22-12-1 下午5:59')
        sql = "insert into user_login(user_name, login_count, create_date) values(?, ?, ?)";
        sql = SqlUtil.getParamsContent(sql, "uname", 2, new Date(System.currentTimeMillis()));
        result = sqlService.insert(sql);
        System.out.println("3# result = " + result);

        //sql = insert into user_login(user_name, login_count, create_date) values('uz', 3, '22-12-1 下午5:59')
        sql = "insert into user_login(user_name, login_count, create_date) values({}, {}, {})";
        sql = SqlUtil.getParamsContent(sql, "uz", 3, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));
        result = sqlService.insert(sql);
        System.out.println("4# result = " + result);

        //sql = INSERT INTO user_login (user_name,login_count) VALUES ('uc',3)
        result = sqlService.insertOne
                ("user_login",
                        UserLoginMap.USER_NAME, "uc", UserLoginMap.LOGIN_COUNT, 3
                );
        System.out.println("5# result = " + result);

        //sql = INSERT INTO user_login (user_name, login_count) VALUES ('uy', 1), ('ue', 2)
        result = sqlService.insertBatch
                ("user_login",
                        new String[]{UserLoginMap.USER_NAME, UserLoginMap.LOGIN_COUNT},
                        "uy", 1,
                        "ue", 2
                );
        System.out.println("6# result = " + result);

        sqlService.sqlServerIdentityInsert = true;
        //sql = INSERT INTO user_login (id, user_name, login_count, create_date) VALUES (101, 'uc', 1, '22-12-1 下午5:59'), (102, 'uc', 2, '22-12-1 下午5:59')
        result = sqlService.insertBatchAllFields
                ("user_login",
                        101, "uc", 1, new Date(System.currentTimeMillis()), null,
                        102, "uc", 1, new Timestamp(System.currentTimeMillis()), null,
                        103, "uc", 2, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())), null
                );
        System.out.println("7# result = " + result);
        sqlService.sqlServerIdentityInsert = false;

        sqlService.sqlServerIdentityInsert = true;
        //sql = INSERT INTO user_login (id, user_name, login_count, create_date) VALUES (1003, 'uc', 1, '22-12-1 下午5:59'), (1004, 'uc', 2, '22-12-1 下午5:59')
        Object[] values = new Object[]{
                1003, "uc", 1, new Date(System.currentTimeMillis()), null,
                1004, "uc", 2, new Date(System.currentTimeMillis()), null
        };
        result = sqlService.insertBatchAllFields
                ("user_login",
                        values
                );
        System.out.println("8# result = " + result);
        sqlService.sqlServerIdentityInsert = false;

        //sql = INSERT INTO user_login (login_count,user_name) VALUES (4,'ud')
        UserLoginMap map = new UserLoginMap();
        map.tableName = TableName.USER_LOGIN;
        map.set(
                UserLoginMap.USER_NAME, "ud",
                UserLoginMap.LOGIN_COUNT, 4
        );
        result = sqlService.insertOne(map);
        System.out.println("9# result = " + result);

        //sql = SELECT * FROM user_login WHERE id < 1252
        sql = "SELECT * FROM user_login WHERE id < 1252";
        result = sqlService.select(sql, UserLoginMap.class);
        if (result instanceof ArrayList) {
            list = (ArrayList<TableMap>)result;
            if(list.size() > 0) {
                System.out.println("list.size = " + list.size());
                //sql = INSERT INTO user_login (login_count, id, create_date, user_name) VALUES (3, 2628, '2022-11-28 12:15:00.0', 'u5'), (8, 2842, '2022-11-28 12:15:00.0', 'u0')
                for (TableMap tableMap : list) {
                    tableMap.set(
                            UserLoginMap.ID, (int) (Math.random() * 100000) + 2000,
                            UserLoginMap.USER_NAME, "u" + (int) (Math.random() * 10),
                            UserLoginMap.LOGIN_COUNT, (int) (Math.random() * 10)
                    );
                }
                sqlService.sqlServerIdentityInsert = true;
                result = sqlService.insertBatch(list);
                System.out.println("10# result = " + result);
            }
        }
        sqlService.sqlServerIdentityInsert = false;

        list = new ArrayList<>();
        //sql = INSERT INTO user_login (login_count, id, user_name) VALUES (0, 3623, 'u3'), (6, 3784, 'u7'), (6, 3101, 'u4')
        for (int i = 0; i < 3; i++) {
            UserLoginMap userLoginMap = new UserLoginMap();
            userLoginMap.tableName = "user_login";
            userLoginMap.set(
                    UserLoginMap.ID, (int) (Math.random() * 1000) + 3000,
                    UserLoginMap.USER_NAME, "u" + (int) (Math.random() * 10),
                    UserLoginMap.LOGIN_COUNT, (int) (Math.random() * 10)
            );
            list.add(userLoginMap);
        }
        sqlService.sqlServerIdentityInsert = true;
        result = sqlService.insertBatch(list);
        System.out.println("11# result = " + result);
        sqlService.sqlServerIdentityInsert = false;
    }
}
