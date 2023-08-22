package samples.mariadb;

import sql.airJdbc.data.TableMap;
import sql.airJdbc.service.SqlService;
import tableMaps.mariadb.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.util.ArrayList;

public class BinarySample {
    public BinarySample() {
        SqlService sqlService = SqlService.getInstance();
        ArrayList<TableMap> list;

        String sql = "delete from user_login where id > 0";
        sqlService.delete(sql);

        //insert bytes
        sql = "insert into user_login(user_name, login_count, create_date, var_content) values(?, ?, ?, ?)";

        sqlService.setSqlParams(
                "u2",
                2,
                new Date(System.currentTimeMillis()),
                "hello".getBytes(StandardCharsets.UTF_8)
        );
        Object result = sqlService.insert(sql);
        System.out.println("1# result = " + result);

        sql = "select * from user_login";
        result = sqlService.select(sql, UserLoginMap.class);
        if (result instanceof ArrayList) {
            list = (ArrayList<TableMap>)result;
            if(list.size() > 0){
                UserLoginMap map = (UserLoginMap) list.get(list.size() - 1);
                byte[] bytes = (byte[]) map.get(UserLoginMap.VAR_CONTENT);
                System.out.println("var_content = " + new String(bytes));
                System.out.println("----------------------------------------------");
            }
        }

        //insert FileInputStream
        try {
            FileInputStream fileInputStream =new FileInputStream( new File(System.getProperty("user.dir") + "/src/sql/airJdbc/template/TableNameTemplate.txt"));

            sql = "insert into user_login(user_name, login_count, create_date, var_content) values(?, ?, ?, ?)";
            sqlService.setSqlParams(
                    "u2",
                    2,
                    new Date(System.currentTimeMillis()),
                    fileInputStream
            );
            result = sqlService.insert(sql);
            System.out.println("2# result = " + result);

            sql = "select * from user_login";
            result = sqlService.select(sql, UserLoginMap.class);
            if (result instanceof ArrayList) {
                list = (ArrayList<TableMap>)result;
                if(list.size() > 0){
                    UserLoginMap map = (UserLoginMap) list.get(list.size() - 1);
                    byte[] bytes = (byte[]) map.get(UserLoginMap.VAR_CONTENT);
                    System.out.println("TableNameTemplate.txt = \n" + new String(bytes));
                    System.out.println("----------------------------------------------");
                }
            }else {
                System.out.println("sql error");
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
