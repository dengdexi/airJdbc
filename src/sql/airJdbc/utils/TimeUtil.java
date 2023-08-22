package sql.airJdbc.utils;

import java.util.HashMap;

/**
 * 计时工具
 * @author Dempsey <br/>
 * 2016年2月19日
 */

public class TimeUtil {
	//计时map
	private static final HashMap<String, Long> hashMap = new HashMap<>();
	
	/**
	 * 记录并输出上次开始时经过的时间长度，若为第一次调用 isReset = true 则开始记录时间，后面再调用时 isReset = false 则输出时长
	 * @param key 计时key
	 * @param isReset 是否重置时长数据，重新开始计时
	 * @return
	 */
	public static long recordTime(String key, boolean isReset){
		if(isReset)
			reset(key);
		
		long startTime;
		long endTime = System.currentTimeMillis();
		long value = 0;
		
		if(hashMap.containsKey(key)){
			startTime = hashMap.get(key);
			value = endTime - startTime;
			
			System.out.println("[key = " + key + "]程序用时为：" +  value + " ms");
		}
		hashMap.put(key, endTime);
		
		return value;
	}
	//
	/**
	 * 开始记录程序运行时长
	 * @param key 计时key
	 * @return
	 */
	public static long beginRecordTime(String key){
		return recordTime(key, true);
	}
	//
	/**
	 * 输出程序运行经过的时长
	 * @param key 计时key
	 * @return
	 */
	public static long endRecordTime(String key){
		return recordTime(key, false);
	}
	//
	/**
	 * 重置记录时长，若要重新开始计算经过的时长，则调用此方法
	 * @param key 计时key
	 */
	public static void reset(String key){
		hashMap.remove(key);
	}
}
