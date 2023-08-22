package sql.airJdbc.data;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据对象基类 对应数据库记录
 * @author ddx <br/>
 * 2015-8-13
 */

public class TableMap extends HashMap<String, Object>{
	//共享数据字段数据数组
	private static HashMap<Object, String[]> fieldsMap = new HashMap<>();
	private String[] tableKeys;
	//
	/**
	 * 表名，有可能多个表数据结构相同，因此需要设置
	 */
	public String tableName;
	//
	/**
	 * 表主键字段
	 */
	public String primaryKey;

	public TableMap() {
		Class<?> clazz = this.getClass();
		tableKeys = fieldsMap.get(clazz);

		int length;

		if(tableKeys == null){
			//使每个实例共享数据字段，不用每次生成新数组，提高性能
			Field[] fields = this.getClass().getFields();
			length = fields.length;
			tableKeys = new String[length];

			int count = 0;
			Field f;
			int i;

			for(i = 0; i < length; i++){
				f = fields[i];
				//去掉非静态字段
				if(Modifier.isStatic(f.getModifiers())){
					try {
						tableKeys[count] = (String) f.get(f.getName());
					} catch (Exception e) {
						System.out.println(this.getClass().getName() + e);
					}

					count ++;
				}
			}

			String[] temp = new String[count];

			System.arraycopy(tableKeys, 0, temp, 0, count);

			tableKeys = temp;

			fieldsMap.put(clazz, tableKeys);
		}
	}
	//
	public TableMap getValueMap(String ...args){
		TableMap map = new TableMap();
		for(String value:args){
			map.put(value, this.get(value));
		}

		return map;
	}
	public TableMap getValueMapByFilters(String ...args){
		TableMap map = new TableMap();
		TableMap result = new TableMap();

		for(String value:args){
			map.put(value, 1);
		}

		for(String key:tableKeys){
			if(map.containsKey(key)){
				continue;
			}

			result.put(key, this.get(key));
		}

		return result;
	}
	//
	public static void copyProperties(HashMap<String, Object> src, HashMap<String, Object> target, String ...args){
		for (String key : args) {
			target.put(key, src.get(key));
		}
	}
	//
	public static void copyProperties(TableMap src, HashMap<String, Object> target, String ...args){
		for (String key : args) {
			target.put(key, src.get(key));
		}
	}
	//

	/**
	 * 设置数据表映射类字段值
	 * @param field 数据表字段
	 * @param value 字段值
	 */
	public void set(String field, Object value){
//		tableMaps.tableMap.put(field, value);
		this.put(field, value);
	}
	//

	/**
	 * 设置数据表映射类字段值
	 * @param args 数据表字段和值列表，如：column1, column1_value, column2, column2_value ...
	 */
	public void set(Object ...args){
		final int length = args.length;
		if(length % 2 != 0){
			System.out.println("参数数量应为偶数：args.length = " + length);
			return;
		}

		String key;

		for(int i = 0; i < length; i += 2){
			key = (String) args[i];
			this.set(key, args[i + 1]);
		}
	}
	//
	/**
	 * 设置字段数据，
	 * @param map 字段和数据列表，字段名称必须和数据库字段相同
	 */
	public void setByMap(Map map){
		for(Object key:map.keySet()){
			Object value = map.get(key);
			this.put((String) key, value);
		}
	}
	/**
	 * 设置所有字段数据，参数必须按照类中的字段的顺序传递并且必须包含所有字段数据
	 * @param args
	 */
	public void setAll(Object ...args){
		final int length = tableKeys.length;

		if(args.length != length){
			System.out.println("参数数量错误：args.length = " + args.length);
			return;
		}

		String key;

		for(int i = 0; i < length; i ++){
			key = (String) tableKeys[i];
			if(args[i] != null)
				this.put(key, args[i]);
		}
	}

	/**
	 * 使用 ResultSet 设置数据
	 * @param set
	 * @param metaData
	 * @param count
	 * @param tableName
	 */
	public void setTableDataBy(ResultSet set, ResultSetMetaData metaData, int count, String tableName){
		try {
			//列索引从 1 开 始
			this.tableName = tableName;
			for (int i = 1; i <= count; i++) {
				String columnName = metaData.getColumnName(i);

				this.set(columnName, set.getObject(columnName));
			}
		} catch (SQLException e) {
			System.out.println("setTableDataBy()设置数据表字段错误");
			System.out.println(e);
		}
	}
	//
	public Object get(String field){
		return super.get(field);
	}

	//
	public void clearFields(String ...args){
		for (String value:args){
			this.remove(value);
		}
	}
	//
	public void increase(String ...args){
		for (String key : args) {
			this.set(key, (int)this.get(key) + 1);
		}
	}
	//
	public void reduce(String ...args){
		for (String key : args) {
			this.set(key, (int)this.get(key) - 1);
		}
	}
	//
	public void setFieldsBySingleValue(Object value, String ...args){
		for (String key : args) {
			this.set(key, value);
		}
	}

	//
	public String toString(){
		String result = this.getClass().getName() + "{";
		int i = 0;
		int last = this.keySet().size() - 1;

		for (String key : this.keySet()) {
			result += key + "=" + this.get(key);

			if(i < last){
				result += ", ";
			}
			i ++;
		}

		result += "}";

		return result;
	}
	//
	public void destroy() {
		this.clear();
	}
}
