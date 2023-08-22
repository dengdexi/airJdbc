package samples.oracle;

import oracle.sql.BLOB;
import sql.airJdbc.data.TableMap;
import sql.airJdbc.service.SqlService;
import tableMaps.oracle.UserLoginMap;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;

public class BinarySample {
    public BinarySample() {
        SqlService sqlService = SqlService.getInstance();
        ArrayList<TableMap> list;

        String sql = "delete from user_login where id > 0";
        sqlService.delete(sql);

        //insert bytes
        sql = "insert into user_login(id, user_name, login_count, create_date, var_content) values(?, ?, ?, ?, ?)";

        sqlService.setSqlParams(
                1,
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
            if(list.size() > 0) {
                UserLoginMap map = (UserLoginMap) list.get(list.size() - 1);
                try {
                    InputStream inputStream = ((Blob) map.get(UserLoginMap.VAR_CONTENT)).getBinaryStream();
                    byte[] bytes = new byte[inputStream.available()];
                    inputStream.read(bytes);
                    System.out.println("var_content = " + new String(bytes));
                    System.out.println("----------------------------------------------");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        //insert FileInputStream
        try {
            FileInputStream fileInputStream =new FileInputStream( new File(System.getProperty("user.dir") + "/src/sql/airJdbc/template/TableNameTemplate.txt"));
            byte[] bytes = new byte[fileInputStream.available()];
            fileInputStream.read(bytes);

            sql = "insert into user_login(id, user_name, login_count, create_date, var_content) values(?, ?, ?, ?, ?)";
            sqlService.setSqlParams(
                    2,
                    "u2",
                    2,
                    new Date(System.currentTimeMillis()),
                    bytes
            );
            result = sqlService.insert(sql);
            System.out.println("2# result = " + result);

            sql = "select * from user_login";
            result = sqlService.select(sql, UserLoginMap.class);
            if (result instanceof ArrayList) {
                list = (ArrayList<TableMap>)result;
                if(list.size() > 0){
                    UserLoginMap map = (UserLoginMap) list.get(list.size() - 1);
                    InputStream inputStream = ((Blob) map.get(UserLoginMap.VAR_CONTENT)).getBinaryStream();
                    bytes = new byte[inputStream.available()];
                    inputStream.read(bytes);
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
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
//    private String clob2String(BLOB clob) throws SQLException, IOException
//    {
//        String reString = "";
////        Reader is = clob.getCharacterStream();// 得到流
//        InputStream is = clob.getBinaryStream();
//        BufferedReader br = new BufferedReader(is);
//        String s = br.readLine();
//        StringBuffer sb = new StringBuffer();
//        while (s != null)
//        {
//            // 执行循环将字符串全部取出付值给StringBuffer由StringBuffer转成STRING
//            sb.append(s);
//            s = br.readLine();
//        }
//        reString = sb.toString();
//        return reString;
//    }
}
