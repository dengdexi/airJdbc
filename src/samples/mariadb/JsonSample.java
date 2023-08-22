package samples.mariadb;

import com.google.gson.Gson;
import sql.airJdbc.data.TableMap;
import sql.airJdbc.service.SqlService;
import tableMaps.mariadb.*;

import java.util.ArrayList;

public class JsonSample {
    public JsonSample(){
        SqlService sqlService = SqlService.getInstance();

        String sql = "delete from user_login where id > 0";
        sqlService.delete(sql);

        new BatchSample(3);

        Object result = SqlService.getInstance().select("select * from user_login", UserLoginMap.class);
        if(result instanceof ArrayList){
            System.out.println("list = " + result);
            System.out.println("list to json = " + new Gson().toJson(result));
        }
    }
}
