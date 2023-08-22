package sql.airJdbc.utils;

import sql.airJdbc.data.TableMap;
import sql.airJdbc.service.DatabaseType;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class SqlUtil {
    public static int databaseType = DatabaseType.MARIADB;
    private static String ORACLE_SELECT_DUAL = " SELECT * FROM dual";
    private static String ORACLE_DATE_FORMAT = "yyyy-mm-dd hh24:mi:ss";
    private static String ORACLE_TIMESTAMP_FORMAT = "yyyy-mm-dd hh24:mi:ssxff";
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    /**
     * sql 语句的字符串参数是否加上引号过滤列表，在此列表中的字符串将不加引号，直接与sql语句拼接
     * 如：oracle sql 语句的参数中若有 to_date() 函数，则将"to_date"加入列表，使 to_date() 函数在与sql语句拼接时不会加上引号
     * @see SqlUtil#getSqlValue(Object)
     * @see samples.oracle.InsertSample
     */
    public static HashMap<String, Integer> sqlValueFiltersMap = new HashMap<>();

    //

    /**
     * 使用参数替换占位符，仅能替换字段值占位符，不能替换表名等，类似于 PreparedStatement.setObject()
     * 仅支持简单数据类型，复杂数据类型和二进制使用 SqlService.setSqlParams()
     *
     * @param sql
     * @param args
     * @return
     */
    public static String getParamsContent(String sql, Object... args) {
        String reg;
        if (sql.indexOf("?") == -1) {
            reg = "\\{\\}";
        } else {
            reg = "\\?";
        }
        for (Object arg : args) {
            sql = sql.replaceFirst(reg, SqlUtil.getSqlValue(arg).toString());
        }
        return sql;
    }

    /**
     * 插入数据单条数据
     *
     * @return
     */
    public static String getInsertFromContent(String sourceTable, String targetTable, String fieldName, Object value) {
        String sql = "INSERT INTO " + targetTable + " SELECT * FROM " + sourceTable + " WHERE " + fieldName + "=" + getSqlValue(value);
        return sql;
    }

    /**
     * 插入数据单条数据
     *
     * @param table 表名
     * @param args  保存数据列表
     * @return
     */
    public static String getInsertOneContent(String table, Object... args) {
        final int length = args.length;

        String fields = "(";
        String values = "(";
        final int last = length - 2;

        for (int i = 0; i < length; i += 2) {
            fields += args[i];

            Object item = args[i + 1];
            values += SqlUtil.getSqlValue(item);

            if (i < last) {
                fields += ",";
                values += ",";
            }
        }

        fields += ")";
        values += ")";

        String sql = "INSERT INTO " + table + " " + fields + " VALUES " + values;
        return sql;
    }
    //

    /**
     * 插入数据单条数据
     *
     * @return
     */
    public static String getInsertOneContent(String tableName, TableMap tableMap) {
        Set<String> keys = tableMap.keySet();
        int length = keys.size();
        String fields = "(";
        String values = "(";
        final int last = length - 1;

        int i = 0;
        for (String key : keys) {
            fields += key;

            Object item = tableMap.get(key);
            values += SqlUtil.getSqlValue(item);

            if (i < last) {
                fields += ",";
                values += ",";
            }

            i++;
        }

        fields += ")";
        values += ")";

        String sql = "INSERT INTO " + tableName + " " + fields + " VALUES " + values;
        return sql;
    }

    //

    /**
     * 插入数据单条数据
     *
     * @param table 表名
     * @param args  保存数据列表
     * @return
     */
    public static String getInsertBatchContent(String table, String[] fieldNames, Object... args) {
        Object[] data = getFieldAndValues(table, fieldNames, args);
        String fields = (String) data[0];
        String[] values = (String[]) data[1];

        int length = fieldNames.length;
        int subLength = args.length / length;
        String content = "";
        int last = subLength - 1;

        for (int j = 0; j < subLength; j++) {
            content += values[j];
            if (j < last) {
                content += ",";
            }
        }

        String sql = "INSERT INTO " + table + " " + fields + " VALUES " + content;
        return sql;
    }

    public static String getInsertBatchContentOracle(String table, String[] fieldNames, Object... args) {
        Object[] data = getFieldAndValues(table, fieldNames, args);
        String fields = (String) data[0];
        String[] values = (String[]) data[1];

        int length = fieldNames.length;
        int subLength = args.length / length;
        String content = "";
        int last = subLength - 1;

        for (int j = 0; j < subLength; j++) {
            content += "INTO " + table + fields + " VALUES" + values[j];
            if (j < last) {
                content += " ";
            }
        }

        String sql = "INSERT ALL " + content + ORACLE_SELECT_DUAL;
        return sql;
    }

    private static Object[] getFieldAndValues(String table, String[] fieldNames, Object... args){
        int length = fieldNames.length;

        String fields = "(";
        int last = length - 1;
        int subLength = args.length / length;
        String[] values = new String[subLength];
        String content = "";

        for (int i = 0; i < length; i++) {
            fields += fieldNames[i];

            for (int j = 0; j < subLength; j++) {
                if (i == 0)
                    values[j] = "(";

                Object item = args[(j * length) + i];
                values[j] += SqlUtil.getSqlValue(item);

                if (i < last) {
                    values[j] += ",";
                } else if (i == last) {
                    values[j] += ")";
                }
            }

            if (i < last) {
                fields += ",";
            }
        }

        fields += ")";

        return new Object[]{fields, values};
    }
    //

    /**
     * 获取批量插入数据 sql 字符串
     * @param list 数据列表，需要保证列表中的每个 TableMap 实例字段总数量相同，如 list[0]有3个字段，则其他的实例也应都是3个字段
     * @return
     */
    public static String getInsertBatchContent(ArrayList<TableMap> list) {
        int size = list.size();
        String values = "";
        String fields = "(";
        int lastSize = size - 1;
        TableMap tableMap = list.get(0);
        String table = tableMap.tableName;

        for (int k = 0; k < size; k++) {
            tableMap = list.get(k);
            Set<String> keys = tableMap.keySet();
            int length = keys.size();
            String content = "(";
            int last = length - 1;

            int i = 0;
            for (String key : keys) {
                Object value = tableMap.get(key);
                if (k == 0)
                    fields += key;

                content += getSqlValue(value);

                if (i < last) {
                    if (k == 0)
                        fields += ",";
                    content += ",";
                }
                i++;
            }

            content += ")";
            values += content;
            if (k < lastSize) {
                values += ",";
            }
        }

        fields += ")";

        String sql = "INSERT INTO " + table + " " + fields + " VALUES " + values;
        return sql;
    }
    public static String getInsertBatchContentOracle(ArrayList<TableMap> list) {
        int size = list.size();
        String values = "";
        String fields = "(";
        int lastSize = size - 1;
        TableMap tableMap = list.get(0);
        String table = tableMap.tableName;

        for (int k = 0; k < size; k++) {
            tableMap = list.get(k);
            Set<String> keys = tableMap.keySet();
            int length = keys.size();
            String content = "(";
            int last = length - 1;

            int i = 0;
            for (String key : keys) {
                Object value = tableMap.get(key);
                if (k == 0)
                    fields += key;

                content += getSqlValue(value);

                if (i < last) {
                    if (k == 0)
                        fields += ",";
                    content += ",";
                }
                i++;
            }
            if (k == 0)
                fields += ")";

            content += ")";
            values += " INTO " + table + fields + " VALUES " + content;
        }

        String sql = "INSERT ALL" + values + ORACLE_SELECT_DUAL;
        return sql;
    }

    /**
     * 插入完整数据，可为多条数据
     *
     * @param table 表名
     * @param args  保存数据列表
     * @return
     */
    public static String getInsertBatchContentAll(String table, ArrayList<String> filedList, Object... args) {
        Object[] data = getFieldAndValuesAll(table, filedList, args);
        String fields = (String) data[0];
        String[] values = (String[]) data[1];

        int length = filedList.size();
        int subLength = args.length / length;
        String content = "";
        int last = subLength - 1;

        for (int j = 0; j < subLength; j++) {
            content += values[j];
            if (j < last) {
                content += ",";
            }
        }

        String sql = "INSERT INTO " + table + " " + fields + " VALUES " + content;
        return sql;
    }

    public static String getInsertBatchContentAllOracle(String table, ArrayList<String> filedList, Object... args) {
        Object[] data = getFieldAndValuesAll(table, filedList, args);
        String fields = (String) data[0];
        String[] values = (String[]) data[1];

        int length = filedList.size();
        int subLength = args.length / length;
        String content = "";
        int last = subLength - 1;

        for (int j = 0; j < subLength; j++) {
            content += "INTO " + table + fields + " VALUES" + values[j];
            if (j < last) {
                content += " ";
            }
        }

        String sql = "INSERT ALL " + content + ORACLE_SELECT_DUAL;
        return sql;
    }

    private static Object[] getFieldAndValuesAll(String table, ArrayList<String> filedList, Object... args){
        // 1,a,  2,b,  3,c
        String fields = "(";

        int length = filedList.size();
        int last = length - 1;
        int subLength = args.length / length;
        String[] values = new String[subLength];

        for (int i = 0; i < length; i++) {
            fields += filedList.get(i);

            for (int j = 0; j < subLength; j++) {
                if (i == 0)
                    values[j] = "(";

                Object item = args[(j * length) + i];
                values[j] += SqlUtil.getSqlValue(item);

                if (i < last) {
                    values[j] += ",";
                } else if (i == last) {
                    values[j] += ")";
                }
            }

            if (i < last) {
                fields += ",";
            }
        }

        fields += ")";

        return new Object[]{fields, values};
    }

    //
    public static String getDeleteContent(String table, String conditionField, String compareSign, Object conditionValue) {
        String sql = "DELETE FROM " + table + " WHERE " + conditionField + compareSign + getSqlValue(conditionValue);
        return sql;
    }

    //
    public static String getDeleteOneContent(String table, String conditionField, Object conditionValue) {
        String sql = getDeleteContent(table, conditionField, "=", conditionValue);
        return sql;
    }

    //
    public static String getDeleteBatchContent(String table, String field, Object... args) {
        String sql = "DELETE FROM " + table + " WHERE " + field + " IN " + getValuesContent(args);

        return sql;
    }

    //
    private static String getTableMapUpdateContent(TableMap tableMap) {
        Set<String> keys = tableMap.keySet();
        int length = keys.size();
        String content = "";
        final int last = length - 1;

        int i = 0;
        for (String key : keys) {
            if(!key.equals(tableMap.primaryKey)) {
                Object value = tableMap.get(key);
                if(value != null){
                    content += key + "=";
                    content += getSqlValue(value);

                    if(i < last){
                        content += ",";
                    }
                }
            }

            i++;
        }

        //当最后一个字段为空值时或为 primaryKey 时，字符串末尾有可能为逗号
        length = content.length() - 1;
        if(content.indexOf(",", length) == length){
            content = content.substring(0, length);
        }

        return content;
    }

    //
    public static String getUpdateContent(String table, String conditionField, String compareSign, Object conditionValue, Object... args) {
        final int length = args.length;

        String content = "";
        final int last = length - 2;

        for (int i = 0; i < length; i += 2) {
            content += args[i] + "=";
            content += getSqlValue(args[i + 1]);

            if (i < last) {
                content += ",";
            }
        }

        String sql = getUpdateSql(table, content, conditionField, compareSign, getSqlValue(conditionValue).toString());
        return sql;
    }

    //
    public static String getUpdateOneContent(TableMap tableMap) {
        String content = getTableMapUpdateContent(tableMap);
        String sql = getUpdateSql(tableMap.tableName, content, tableMap.primaryKey, "=", getSqlValue(tableMap.get(tableMap.primaryKey)).toString());
        return sql;
    }

    //
    public static String getUpdateBatchContent(String table, String conditionField, Object[] conditionValues, Object... args) {
        int length = args.length;

        String content = "";
        int last = length - 2;

        for (int i = 0; i < length; i += 2) {
            content += args[i] + "=";
            content += getSqlValue(args[i + 1]);

            if (i < last) {
                content += ",";
            }
        }

        String values = getValuesContent(conditionValues);
        String sql = getUpdateSql(table, content, conditionField, values);
        return sql;
    }

    //
//    public static String getUpdateBatchContent(String table, String conditionField, BaseVo ...args){
//        int length = args.length;
//        String content = "";
//        String values = "(";
//        int last = length - 1;
//
//        for(int i = 0; i < length; i ++){
//            if(content.length() == 0){
//                content += getVoUpdateContent(args[i]);
//            }
//
//            values += getSqlValue(args[i].get(conditionField));
//            if(i < last){
//                values += ",";
//            }
//        }
//
//        values += ")";
//
////        String sql = getUpdateSql(table, content, conditionField, values);
//        String sql = getInsertBatchContent(args);
//        return sql;
//    }
    //
    private static String getUpdateSql(String table, String content, String conditionField, String conditionValue) {
        String sql = "UPDATE " + table + " SET " + content + " WHERE " + conditionField + " IN " + conditionValue;
        return sql;
    }

    //
    private static String getUpdateSql(String table, String content, String conditionField, String compareSign, String conditionValue) {
        String sql = "UPDATE " + table + " SET " + content + " WHERE " + conditionField + compareSign + conditionValue;
        return sql;
    }

    //
    public static String getSelectContent(String table, String conditionField, String compareSign, Object conditionValue) {
        return getSelectContentByCondition(null, table, conditionField, compareSign, conditionValue, 0, 0, null, null);
    }

    //
    public static String getSelectContent(String[] selectFields, String table, String conditionField, String compareSign, Object conditionValue) {
        return getSelectContentByCondition(selectFields, table, conditionField, compareSign, conditionValue, 0, 0, null, null);
    }

    //
    public static String getSelectContent(String table, String conditionField, String compareSign, Object conditionValue, int limit, int offset) {
        return getSelectContentByCondition(null, table, conditionField, compareSign, conditionValue, limit, offset, null, null);
    }

    //
    public static String getSelectContent(String[] selectFields, String table, String conditionField, String compareSign, Object conditionValue, int limit, int offset) {
        return getSelectContentByCondition(selectFields, table, conditionField, compareSign, conditionValue, limit, offset, null, null);
    }

    //
    //此方法仅支持 mariadb/mysql
    public static String getSelectContentByCondition(String[] selectFields, String table, String conditionField, String compareSign, Object conditionValue, int limit, int offset, String[] orderFields, String[] orderValues) {
        String fields = getFieldsContent(selectFields);

        String sql = "SELECT " + fields + " FROM " + table;
        if (conditionField != null && !conditionField.equals("") && conditionValue != null) {
            sql += " WHERE " + conditionField + compareSign + getSqlValue(conditionValue);
        }

        sql += getOrderContent(orderFields, orderValues);
        sql += getLimitContent(limit, offset);

        return sql;
    }

    //
    public static String getSelectContentIn(String[] selectFields, String table, String field, Object... args) {
        return getSelectContentIn(selectFields, table, field, 0, 0, null, null, args);
    }

    //
    public static String getSelectContentIn(String[] selectFields, String table, String field, int limit, int offset, String[] orderFields, String[] orderValues, Object... args) {
        String fields = getFieldsContent(selectFields);
        String sql = "SELECT " + fields + " FROM " + table + " WHERE " + field + " IN " + getValuesContent(args);

        sql += getOrderContent(orderFields, orderValues);
        sql += getLimitContent(limit, offset);

        return sql;
    }

    //
    private static String getFieldsContent(String[] selectFields) {
        String fields;
        if (selectFields != null && selectFields.length > 0) {
            fields = "";
            int length = selectFields.length;
            int last = length - 1;
            for (int i = 0; i < length; i++) {
                fields += selectFields[i];
                if (i < last) {
                    fields += ",";
                }
            }
        } else {
            fields = "*";
        }

        return fields;
    }

    private static String getValuesContent(Object... args) {
        String values = "(";
        int length = args.length;
        int last = length - 1;

        for (int i = 0; i < length; i++) {
            values += getSqlValue(args[i]);

            if (i < last) {
                values += ",";
            }
        }

        values += ")";

        return values;
    }

    //

    /**
     * 将 java 对象转为 sql 语句值，仅支持简单数据类型，复杂数据类型和二进制使用 SqlService.setSqlParams()
     *
     * @param value java 对象
     * @return sql 字符串
     */
    /**
     * Convert Java objects to SQL statement values, only supporting simple data types, complex data types, and binary using SqlService. setSqlParams()
     * @param value Java objects
     * @return SQL string
     */

    public static Object getSqlValue(Object value) {
        if (value instanceof String) {
            if (DatabaseType.MARIADB == SqlUtil.databaseType || DatabaseType.MYSQL == SqlUtil.databaseType) {
            } else if (DatabaseType.ORACLE == SqlUtil.databaseType) {
                if(sqlValueFiltersMap != null && sqlValueFiltersMap.size() > 0){
                    for (String content: sqlValueFiltersMap.keySet()){
                        if(value != null && value.toString().indexOf(content) > -1){
                            return value;
                        }
                    }
                }
            } else if (DatabaseType.SQL_SERVER == SqlUtil.databaseType) {
            } else if (DatabaseType.POSTGRE_SQL == SqlUtil.databaseType) {
            }

            return "'" + value + "'";
        } else if (value instanceof Date) {
            if (DatabaseType.MARIADB == SqlUtil.databaseType || DatabaseType.MYSQL == SqlUtil.databaseType) {
            } else if (DatabaseType.ORACLE == SqlUtil.databaseType) {
                return getDateFormatOracle((Date)value);
            } else if (DatabaseType.SQL_SERVER == SqlUtil.databaseType) {
                getSqlValue(value.toString());
            } else if (DatabaseType.POSTGRE_SQL == SqlUtil.databaseType) {
            }

            synchronized (simpleDateFormat){
                return getSqlValue(simpleDateFormat.format(value));
            }
        } else if (value instanceof Timestamp) {
            if (DatabaseType.MARIADB == SqlUtil.databaseType || DatabaseType.MYSQL == SqlUtil.databaseType) {
            } else if (DatabaseType.ORACLE == SqlUtil.databaseType) {
                return getTimestampFormatOracle((Timestamp)value);
            } else if (DatabaseType.SQL_SERVER == SqlUtil.databaseType) {
            } else if (DatabaseType.POSTGRE_SQL == SqlUtil.databaseType) {
            }

            return getSqlValue(value.toString());
        } else if (value instanceof Boolean) {
            return value.equals(true) ? "1" : "0";
        } else if (value == null) {
            value = "NULL";
        }

        return value;
    }
    public static String getSqlField(String value) {
        //此单引号为数据库字段引号与字符串单引号不同，不能用键盘打出来，要复制使用，否则导入 mysql 数据库会失败
        return "`" + value + "`";

    }
    //
    public synchronized static String getDateFormatOracle(Date date){
        final String dateFormat = simpleDateFormat.format(date);

        return "to_date('" +  dateFormat + "','" + ORACLE_DATE_FORMAT + "'" + ")";
    }
    //
    public static String getTimestampFormatOracle(Timestamp timestamp){
        final String dateFormat = timestamp.toString();

        return "to_timestamp('" +  dateFormat + "','" + ORACLE_TIMESTAMP_FORMAT + "'" + ")";
    }

    //
    private static String getOrderContent(String[] orderFields, String[] orderValues) {
        String result = "";
        //排序字段
        if (orderFields != null && orderValues != null) {
            result += " ORDER BY ";

            int length = orderValues.length;
            int last = length - 1;
            for (int i = 0; i < length; i++) {
                result += orderFields[i] + " " + orderValues[i];

                if (i < last) {
                    result += ",";
                }
            }
        }

        return result;
    }

    //
    private static String getLimitContent(int limit, int offset) {
        String result = "";

        if (limit > 0) {
            result += " LIMIT " + limit;
            if (offset > 0) {
                result += " OFFSET " + offset;
            }
        }

        return result;
    }
}
