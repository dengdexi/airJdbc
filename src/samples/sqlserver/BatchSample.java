package samples.sqlserver;

import sql.airJdbc.data.TableMap;
import sql.airJdbc.service.SqlService;
import sql.airJdbc.utils.TimeUtil;
import tableMaps.sqlserver.*;

import java.sql.Date;
import java.util.ArrayList;

public class BatchSample {
    SqlService sqlService = SqlService.getInstance();
    int count = 40000;

    public BatchSample(int count){
        sqlService.isPrintLog = false;

        //insertBatch
        TimeUtil.beginRecordTime("insertBatch");
        ArrayList<TableMap> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            UserLoginMap map = new UserLoginMap();
            map.tableName = "user_login";
            map.set(UserLoginMap.ID, i + 1);
            map.set(UserLoginMap.USER_NAME, "uname" + i);
            map.set(UserLoginMap.LOGIN_COUNT, i + 1);
            map.set(UserLoginMap.CREATE_DATE, new Date(System.currentTimeMillis()));

            list.add(map);
        }
        sqlService.sqlServerIdentityInsert = true;
        sqlService.insertBatch(list);
        sqlService.sqlServerIdentityInsert = false;
        TimeUtil.endRecordTime("insertBatch");
    }
    public BatchSample() {
        sqlService.isPrintLog = false;
        ArrayList<TableMap> list;
        Object result;
        String sql;

        System.out.println("1#");
        sql = "delete from user_login where id > 0";
        sqlService.delete(sql);

        System.out.println("2#");
        //insertBatch time:218ms
        new BatchSample(count / 4);

        System.out.println("3#");
        //insertBatch time:157ms
        TimeUtil.beginRecordTime("insertBatch");
        Object[] values = new Object[count];
        for (int i = 0; i < count; i+= 4) {
            values[i] = i + count;
            values[i + 1] = "uname" + i;
            values[i + 2] = i + 1;
            values[i + 3] = new Date(System.currentTimeMillis());
        }
//        sqlService.sqlServerIdentityInsert = true;
        sqlService.insertBatch("user_login", new String[]{"id", "user_name", "login_count", "create_date"}, values);
        sqlService.sqlServerIdentityInsert = false;
        TimeUtil.endRecordTime("insertBatch");

        System.out.println("4#");
        //select time:31ms
        TimeUtil.beginRecordTime("selectTimeTest");
        sql = "select * from user_login";
        result = sqlService.select(sql, UserLoginMap.class);
        if (result instanceof ArrayList)
            System.out.println("list.size = " + ((ArrayList<UserLoginMap>)result).size());
        TimeUtil.endRecordTime("selectTimeTest");

        System.out.println("5#");
        //updateBatch time:390ms
        sql = "select * from user_login";
        result = sqlService.select(sql, UserLoginMap.class);
        TimeUtil.beginRecordTime("updateTimeTest");
        if (result instanceof ArrayList)
            System.out.println("list.size = " + ((ArrayList<TableMap>)result).size());
        sqlService.updateBatch((ArrayList<TableMap>)result, UserLoginMap.USER_NAME, "ui", UserLoginMap.LOGIN_COUNT, 99);
        TimeUtil.endRecordTime("updateTimeTest");

        System.out.println("6#");
        //deleteBatch time:344ms
        sql = "select * from user_login";
        result = sqlService.select(sql, UserLoginMap.class);
        TimeUtil.beginRecordTime("deleteTimeTest");
        if (result instanceof ArrayList)
            sqlService.deleteBatch((ArrayList<TableMap>)result);
        TimeUtil.endRecordTime("deleteTimeTest");

    }

}
