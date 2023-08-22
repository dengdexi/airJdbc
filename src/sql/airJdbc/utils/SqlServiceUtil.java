package sql.airJdbc.utils;

import sql.airJdbc.data.TableMap;
import sql.airJdbc.service.DatabaseType;
import sql.airJdbc.service.SqlService;

import java.math.BigInteger;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class SqlServiceUtil {
    //
    public static TableMap getFirst(Object result){
        if(result instanceof ArrayList<?>){
            ArrayList<TableMap> list = (ArrayList<TableMap>)result;
            if(list.size() > 0){
                return list.get(0);
            }
        }

        return null;
    }
    public static ArrayList<String> getDatabaseNameList(Connection connection){
        ArrayList<String> list = new ArrayList<>();
        try {
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            ResultSet resultSet = databaseMetaData.getCatalogs();
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();

            int count = resultSetMetaData.getColumnCount();
            while(resultSet.next()) {
                for (int i = 1; i <= count; i++) {
                    list.add(resultSet.getString(i));
                }
            }
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
    public static ArrayList<TableMap> getTableMetaData(SqlService sqlService, String catalog, String ...args){
        ArrayList<String> list = new ArrayList<>();

        if (DatabaseType.MARIADB == sqlService.databaseType || DatabaseType.MYSQL == sqlService.databaseType) {
            return getMysqlSchemaData(sqlService, "TABLES", catalog, null, 0, args);
        } else if (DatabaseType.ORACLE == sqlService.databaseType) {
            try {
                DatabaseMetaData databaseMetaData = sqlService.getConnection().getMetaData();
                ResultSet resultSet = sqlService.getTablesByMetaData(sqlService.getConnection(), databaseMetaData, catalog);

                while(resultSet.next()) {
                    String tableName = resultSet.getString("TABLE_NAME");
                    list.add(new String(tableName));
                }
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if (DatabaseType.SQL_SERVER == sqlService.databaseType) {
        } else if (DatabaseType.POSTGRE_SQL == sqlService.databaseType) {
        }

        return null;
    }
    public static BigInteger getTableRows(SqlService sqlService, String catalog, String table){
        BigInteger rows = null;
        String sql;
        ArrayList<String> list = new ArrayList<>();

        if (DatabaseType.MARIADB == sqlService.databaseType || DatabaseType.MYSQL == sqlService.databaseType) {
            //MARIADB 使用 DatabaseMetaData 无法获得系统数据库的表数据，通过元数据表查询
            TableMap tableMap = getMysqlSchemaData(sqlService, "TABLES", catalog, table, 1, "TABLE_ROWS").get(0);
            if(tableMap != null){
                BigInteger value = ((BigInteger) tableMap.get("TABLE_ROWS"));
                if(value != null){
                    rows = value;
                }
            }
        }

        return rows;
    }

    /**
     * 获取 mysql 的 information_schema 数据表数据
     * @param sqlService 数据库连接服务对象
     * @param catalog information_schema 中的数据库名
     * @param schemaTable information_schema 中的表名
     * @param args 字段名
     * @return
     */
    public static ArrayList<TableMap> getMysqlSchemaData(SqlService sqlService, String schemaTable, String catalog, String table, int limit, String ...args){
        String sql = "";
        int length = args.length;
        for (int i = 0; i < length; i ++ ){
            sql += args[i];
            if(i < length - 1){
                sql += ",";
            }
        }

        ArrayList<TableMap> maps = new ArrayList<>();
        //MARIADB 使用 DatabaseMetaData 无法获得系统数据库的表数据，通过元数据表查询
        sql = "select " + sql + " from information_schema." + schemaTable + " where table_schema = " + SqlUtil.getSqlValue(catalog);
        if(table != null){
            sql += " && table_name = " + SqlUtil.getSqlValue(table);
        }
        if(limit > 0){
            sql += " limit 1";
        }
        Object result = sqlService.select(sql, TableMap.class);
        if(result instanceof ArrayList<?>){
            maps = (ArrayList<TableMap>)result;
        }

        return maps;
    }
    public static HashMap<String, ArrayList<String>> getMapFieldList(ArrayList<TableMap> list){
        HashMap<String, ArrayList<String>> map = new HashMap<>();
        for (String field: list.get(0).keySet()) {
            map.put(field, new ArrayList<>());
        }
        int length = list.size();
        for (int i = 0; i < length; i++){
            for (String field: map.keySet()) {
                map.get(field).add((String) list.get(i).get(field));
            }
        }

        return map;
    }
    public static ArrayList<TableMap> getColumnMetaData(SqlService sqlService, String catalog, String table, String ...args){
        ArrayList<String> list = new ArrayList<>();

        if (DatabaseType.MARIADB == sqlService.databaseType || DatabaseType.MYSQL == sqlService.databaseType) {
            return getMysqlSchemaData(sqlService, "COLUMNS", catalog, table, 0, args);
        } else if (DatabaseType.ORACLE == sqlService.databaseType) {
            try {
                DatabaseMetaData databaseMetaData = sqlService.getConnection().getMetaData();
                ResultSet resultSet = sqlService.getTablesByMetaData(sqlService.getConnection(), databaseMetaData, catalog);

                while(resultSet.next()) {
                    String tableName = resultSet.getString("TABLE_NAME");
                    list.add(new String(tableName));
                }
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if (DatabaseType.SQL_SERVER == sqlService.databaseType) {
        } else if (DatabaseType.POSTGRE_SQL == sqlService.databaseType) {
        }

        return null;
    }
    public static ArrayList<String> getIndexNameList(SqlService sqlService, String catalog, String table){
        String sql;
        ArrayList<String> list = new ArrayList<>();

//        if (DatabaseType.MARIADB == sqlService.databaseType || DatabaseType.MYSQL == sqlService.databaseType) {
//            sql = "select index_name from information_schema.STATISTICS where table_schema = ? && table_name = ?";
//            sql = SqlUtil.getParamsContent(sql, catalog, table);
//            ArrayList<TableMap> maps = sqlService.select(sql, TableMap.class);
//            for(TableMap map:maps){
//                list.add((String) map.get("INDEX_NAME"));
//            }
//        } else if (DatabaseType.ORACLE == sqlService.databaseType) {
        try {
            DatabaseMetaData databaseMetaData = sqlService.getConnection().getMetaData();
            ResultSet resultSet = sqlService.getIndexByMetaData(databaseMetaData, catalog, table);

            while(resultSet.next()) {
                String indexName = resultSet.getString("INDEX_NAME") + "[" + resultSet.getString("COLUMN_NAME") + "]";
                list.add(indexName);
            }
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
//        } else if (DatabaseType.SQL_SERVER == sqlService.databaseType) {
//        } else if (DatabaseType.POSTGRE_SQL == sqlService.databaseType) {
//        }

        return list;
    }
}
