package samples.oracle;

import sql.airJdbc.data.TableMap;
import sql.airJdbc.service.SqlService;
import sql.airJdbc.utils.SqlUtil;
import tableMaps.oracle.TableName;
import tableMaps.oracle.*;

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

        sql = "insert into user_login(id, user_name, login_count) values(1, 'ua', 1)";
        result = sqlService.insert(sql);
        System.out.println("result = " + result);
        if(result instanceof Integer){
            System.out.println("insert success:" + result);
        }else {
            System.out.println("sql error:" + result);
        }

        String dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis()));
        String dateContent = "to_date('" +  dateFormat + "','yyyy-mm-dd hh24:mi:ss'" + ")";
        //sql = insert into user_login(id, user_name, login_count, create_date) values(0, 'uname', 2, '22-12-1 下午5:59')
        sql = "insert into user_login(id, user_name, login_count, create_date) values(?, ?, ?, ?)";
        SqlUtil.sqlValueFiltersMap.put("to_date", 1);
        sql = SqlUtil.getParamsContent(sql, 0, "uname", 2, dateContent);
        result = sqlService.insert(sql);
        System.out.println("0# result = " + result);

        sql = "insert into user_login(id, user_name, login_count, create_date) values(?, ?, ?, ?)";
        sqlService.setSqlParams(2, "u2", 2, new Date(System.currentTimeMillis()));
        result = sqlService.insert(sql);
        System.out.println("1# result = " + result);

        //sql = INSERT INTO user_login (ID,USER_NAME,LOGIN_COUNT) VALUES (3,'uc',3)
        result = sqlService.insertOne
                ("user_login",
                        UserLoginMap.ID, 3, UserLoginMap.USER_NAME, "uc", UserLoginMap.LOGIN_COUNT, 3
                );
        System.out.println("2# result = " + result);

        //sql = INSERT ALL INTO user_login(ID,USER_NAME,LOGIN_COUNT) VALUES(4,'uy',1) INTO user_login(ID,USER_NAME,LOGIN_COUNT) VALUES(5,'ue',2) SELECT * FROM dual
        result = sqlService.insertBatch
                ("user_login",
                        new String[]{UserLoginMap.ID, UserLoginMap.USER_NAME, UserLoginMap.LOGIN_COUNT},
                        4, "uy", 1,
                        5, "ue", 2
                );
        System.out.println("3# result = " + result);

        //sql = INSERT ALL INTO user_login(ID,USER_NAME,LOGIN_COUNT,CREATE_DATE,VAR_CONTENT) VALUES(101,'uc',1,to_date('2023-02-17 11:26:03','yyyy-mm-dd hh24:mi:ss'),NULL) INTO user_login(ID,USER_NAME,LOGIN_COUNT,CREATE_DATE,VAR_CONTENT) VALUES(102,'uc',2,to_date('2023-02-17 11:26:03','yyyy-mm-dd hh24:mi:ss'),NULL) INTO user_login(ID,USER_NAME,LOGIN_COUNT,CREATE_DATE,VAR_CONTENT) VALUES(103,'uc',2,to_timestamp('2023-02-17 11:26:03.326','yyyy-mm-dd hh24:mi:ssxff'),NULL) SELECT * FROM dual
        result = sqlService.insertBatchAllFields
                ("user_login",
                        101, "uc", 1, new Date(System.currentTimeMillis()), null,
                        102, "uc", 2, new Date(System.currentTimeMillis()), null,
                        103, "uc", 2, new Timestamp(System.currentTimeMillis()), null
                );
        System.out.println("4# result = " + result);

        //sql = INSERT INTO user_login (ID,USER_NAME,LOGIN_COUNT) VALUES (104,'ud',4)
        UserLoginMap map = new UserLoginMap();
        map.tableName = TableName.USER_LOGIN;
        map.set(
                UserLoginMap.ID, 104,
                UserLoginMap.USER_NAME, "ud",
                UserLoginMap.LOGIN_COUNT, 4
        );
        result = sqlService.insertOne(map);
        System.out.println("5# result = " + result);

        //sql = SELECT * FROM user_login WHERE id < 1252
        sql = "SELECT * FROM user_login WHERE id < 1252";
        result = sqlService.select(sql, UserLoginMap.class);
        if (result instanceof ArrayList) {
            list = (ArrayList<TableMap>)result;
            if(list.size() > 0) {
                System.out.println("list.size = " + list.size());
                //sql = INSERT ALL INTO user_login(CREATE_DATE,VAR_CONTENT,ID,USER_NAME,LOGIN_COUNT) VALUES (NULL,NULL,2990,'u6',4) INTO user_login(CREATE_DATE,VAR_CONTENT,ID,USER_NAME,LOGIN_COUNT) VALUES (to_timestamp('2023-02-20 10:05:15.0','yyyy-mm-dd hh24:mi:ssxff'),NULL,2084,'u8',7) INTO user_login(CREATE_DATE,VAR_CONTENT,ID,USER_NAME,LOGIN_COUNT) VALUES (to_timestamp('2023-02-20 10:05:15.0','yyyy-mm-dd hh24:mi:ssxff'),NULL,2871,'u3',4) INTO user_login(CREATE_DATE,VAR_CONTENT,ID,USER_NAME,LOGIN_COUNT) VALUES (NULL,NULL,2611,'u3',4) INTO user_login(CREATE_DATE,VAR_CONTENT,ID,USER_NAME,LOGIN_COUNT) VALUES (NULL,NULL,2988,'u7',8) INTO user_login(CREATE_DATE,VAR_CONTENT,ID,USER_NAME,LOGIN_COUNT) VALUES (NULL,NULL,2357,'u2',9) INTO user_login(CREATE_DATE,VAR_CONTENT,ID,USER_NAME,LOGIN_COUNT) VALUES (to_timestamp('2023-02-20 10:05:15.0','yyyy-mm-dd hh24:mi:ssxff'),NULL,2782,'u7',8) INTO user_login(CREATE_DATE,VAR_CONTENT,ID,USER_NAME,LOGIN_COUNT) VALUES (to_timestamp('2023-02-20 10:05:15.0','yyyy-mm-dd hh24:mi:ssxff'),NULL,2601,'u5',6) INTO user_login(CREATE_DATE,VAR_CONTENT,ID,USER_NAME,LOGIN_COUNT) VALUES (to_timestamp('2023-02-20 10:05:15.0','yyyy-mm-dd hh24:mi:ssxff'),NULL,2551,'u7',1) INTO user_login(CREATE_DATE,VAR_CONTENT,ID,USER_NAME,LOGIN_COUNT) VALUES (NULL,NULL,2546,'u8',8) SELECT * FROM dual
                for (TableMap tableMap : list) {
                    //oracle 不支持从 ResultSetMetaData 获取表名，tableName 可能不准确，所以手动设置，保证准确性
                    tableMap.tableName = TableName.USER_LOGIN;
                    tableMap.set(
                            UserLoginMap.ID, (int) (Math.random() * 1000) + 20000,
                            UserLoginMap.USER_NAME, "u" + (int) (Math.random() * 10),
                            UserLoginMap.LOGIN_COUNT, (int) (Math.random() * 10)
                    );
                }
                result = sqlService.insertBatch(list);
                System.out.println("6# result = " + result);
            }
        }

        list = new ArrayList<>();
        //sql = INSERT ALL INTO user_login(ID,USER_NAME,LOGIN_COUNT) VALUES (3744,'u3',9) INTO user_login(ID,USER_NAME,LOGIN_COUNT) VALUES (3932,'u0',6) INTO user_login(ID,USER_NAME,LOGIN_COUNT) VALUES (3046,'u3',9) SELECT * FROM dual
        for (int i = 0; i < 3; i++) {
            UserLoginMap userLoginMap = new UserLoginMap();
            userLoginMap.tableName = "user_login";
            userLoginMap.set(
                    UserLoginMap.ID, (int) (Math.random() * 2000) + 30000,
                    UserLoginMap.USER_NAME, "u" + (int) (Math.random() * 10),
                    UserLoginMap.LOGIN_COUNT, (int) (Math.random() * 10)
            );
            list.add(userLoginMap);
        }
        result = sqlService.insertBatch(list);
        System.out.println("7# result = " + result);
    }
}
