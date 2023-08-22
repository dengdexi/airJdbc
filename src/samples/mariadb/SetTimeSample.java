package samples.mariadb;

import sql.airJdbc.utils.TimeUtil;

import java.util.ArrayList;
import java.util.HashMap;

public class SetTimeSample {
    public SetTimeSample(){
        int length = 1000000;

        //jre 1.8
        //870-1014ms
        TimeUtil.beginRecordTime("MapTest");
        HashMap<String, Object> map = new HashMap<>();
        for(int i = 0; i < length; i++){
            map.put(i + "", new Object());
        }
        TimeUtil.endRecordTime("MapTest");

        //30-48 ms
        TimeUtil.beginRecordTime("KeyTest");
        for (String s : map.keySet()) {
            map.get(s);
        }
        TimeUtil.endRecordTime("KeyTest");

        //6-7 ms
        TimeUtil.beginRecordTime("ArrTest");
        Object[] array = new Object[length];
        for(int i = 0; i < length; i++){
            array[i] = new Object();
        }
        TimeUtil.endRecordTime("ArrTest");

        //12-13 ms
        TimeUtil.beginRecordTime("ListTest");
        ArrayList<Object> list = new ArrayList<>(length);
        for(int i = 0; i < length; i++){
            list.add(new Object());
        }
        TimeUtil.endRecordTime("ListTest");

        //156 ms  效率极低
        length = 100;
        TimeUtil.beginRecordTime("indexOfTest");
        for(int i = 0; i < length; i++){
            list.indexOf(i);
        }
        TimeUtil.endRecordTime("indexOfTest");
    }
}
