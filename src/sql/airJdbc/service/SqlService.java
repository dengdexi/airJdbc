/**
 * SPDX-License-Identifier: MIT
 * <p>
 * Copyright (C) 2022 dexi Deng(邓德喜 China)
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package sql.airJdbc.service;

import sql.airJdbc.data.TableMap;
import sql.airJdbc.utils.FileUtil;
import sql.airJdbc.utils.SqlServiceUtil;
import sql.airJdbc.utils.SqlUtil;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 数据库 sql 服务类，非线程安全，可使用多实例处理多线程的任务
 * Database SQL service class, non thread safe, can use multiple instances to handle multithreaded tasks
 */

public class SqlService {
    private static volatile HashMap<String, SqlService> instancesMap = new HashMap<>();
    private static int FIELD_NAME_LENGTH = 64;
    private static int SQL_DEFAULT_LENGTH = 30;
    private static int SQL_LENGTH_PRINT = 200;
    private static boolean isLoadDriver = false;

    /**
     * 以下5个字段可能不是最新值，需要调用 SqlService.readMetaData() 更新
     * The following 5 fields may not be the latest values and need to be updated by calling SqlService.readMetaData()
     */
    public ArrayList<String> tables = new ArrayList<>();
    public ArrayList<String> primaryKeys = new ArrayList<>();
    public HashMap<String, ArrayList<String>> columnsMap = new HashMap<>();
    public HashMap<String, ArrayList<String>> columnsTypeMap = new HashMap<>();
    public HashMap<String, ArrayList<Integer>> columnsSizeMap = new HashMap<>();

    /**
     * 是否显示日志，false:单条日志长度超过200则不显示
     * Whether to display logs, false: If the length of a single log exceeds 200, it will not be displayed
     */
    public boolean isPrintLog = false;
    private String name;

    private Connection connection;
    private String driverName;
    private String url;
    public String databaseName;
    public String databaseProductName;
    public int databaseType;
    private String user;
    private String password;
    /**
     * 导出的数据表映射类字段名称后面是否显示数据表中的字段类型
     * Is the field type in the exported data table displayed after the mapping class field name
     */
    private boolean hasFieldType = false;
    /**
     * sql.airJDBC 包的路径前缀 默认为 "src/"，表示 sql.airJDBC 包放在 src/ 路径下
     * 若 sql.airJDBC 包放在 src/com/ 路径下，则 sqlPackagePath 设置为 "src/com/"
     * The default path prefix for the sql.airJDBC package is "src/", which means that the sql.airJDBC package is placed under the src/path. If the sql.airJDBC package is placed under the src/com/path, then the sqlPackagePath is set to "src/com/"
     */
    private String sqlPackagePath = "src/";
    /**
     * 导出的数据库表映射类路径 默认为 src/tableMaps/mariadb
     * The exported database table mapping class path defaults to src/tableMaps/mariadb
     */
    private String mapPath = "src/tableMaps/mariadb";
    /**
     * 导出的数据库表映射类后缀名称，如 user 表导出类 UserMap.java
     * The suffix name of the exported database table mapping class, such as the user table exporting class UserMap.java
     */
    private String mapSuffix = "Map";
    /**
     * 设置即将执行的 sql 参数，即提交给 PreparedStatement 的参数，
     * 参数的顺序与 PreparedStatement.setObject() 的顺序相同，
     * 第1个参数将执行 PreparedStatement.setObject(1, args[0])，
     * 其余参数类似，参数只使用一次，SqlService 执行 sql 后清除所有参数
     * Set the SQL parameters to be executed, that is, the parameters submitted to PreparedStatement,
     * The order of the parameters is the same as that of PreparedStatement. setObject (),
     * The first parameter will execute PreparedStatement. setObject (1, args [0]),
     * The other parameters are similar, they are only used once, and SqlService clears all parameters after executing SQL
     *
     */
    private Object[] sqlParams;
    /**
     * 批量处理sql时，单次提交的 sql 语句最大长度，默认：8 K，此值执行效率接近最佳，超过此值执行效率逐渐降低
     * When batch processing SQL, the maximum length of a single submitted SQL statement is 8 K by default. This value is close to the optimal execution efficiency, but the execution efficiency gradually decreases beyond this value
     * @see samples.mariadb.BatchSample
     */
    public int maxSqlLength = 1024 * 8;
    /**
     * ms sql Server 主键自增表是否开启插入主键，若需要插入主键设置为 true
     * 默认为 false 插入主键导致错误: 当 IDENTITY_INSERT 设置为 OFF 时，不能为表 'xxx' 中的标识列插入显式值
     * Is the MS SQL Server primary key autoincrement table enabled for inserting primary keys? If inserting primary keys is required, set it to true
     * The default is false. Inserting the primary key causes an error: when Identity_ When Insert is set to OFF, explicit values cannot be inserted for the identity column in table 'xxx'
     */
    public boolean sqlServerIdentityInsert = false;
    private static String IDENTITY_INSERT_ON = "SET IDENTITY_INSERT #tableName# ON ";
    private static String IDENTITY_INSERT_OFF = " SET IDENTITY_INSERT #tableName# OFF ";
    private static String INVALIDATE_ARGUMENTS = "Invalidate Arguments";
    private static String SQL_SERVICE_NOT_INITIALIZED = "SqlService not initialized.";
    public boolean isInit = false;
    /**
     * 批量处理分组执行时的进度百分比
     * Progress percentage during batch processing group execution
     */
    public float batchPercent = 1;
    private SimpleDateFormat logFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private Date logDate = new Date();

    //
    public SqlService() {
    }
    public static HashMap<String, SqlService> getInstancesMap() {
        return instancesMap;
    }

    //
    public static SqlService getInstance() {
        return getInstance("default");
    }

    //

    /**
     * 获取实例
     * Obtain an instance
     * @param name 实例名称 instance name
     * @return
     */
    public static synchronized SqlService getInstance(String name) {
        SqlService instance = instancesMap.get(name);
        if (instance == null) {
            instance = new SqlService();
            instance.name = name;
            instancesMap.put(name, instance);
        }
        return instance;
    }
    //
    public static boolean hasInstance(String name){
        return instancesMap.containsKey(name);
    }
    //
    public static void setDatabaseEnvironment(int databaseType){
        SqlUtil.databaseType = databaseType;
    }

    //
    public String toString() {
        return "SqlService[" + this.name + "]";
    }

    //
    public String getVersion() {
        return "airJdbc-0.1.0";
    }

    //
    public Object init() {
        return this.init(instancesMap.get("default"));
    }

    //
    public Object init(SqlService sqlService) {
        if (sqlService == null) {
            this.print("error", "Init fail：sqlService = " + sqlService);
            return "fail";
        }

        return this.init(sqlService.driverName, sqlService.url, sqlService.user, sqlService.password);
    }

    //
    public Object init(String driverName, String url) {
        return this.init(driverName, url, null, null);
    }
    //

    /**
     * 初始化 sql 处理服务
     * @param driverName 驱动名称
     * @param url 数据库地址
     * @param user 数据库用户名
     * @param password 数据库密码
     * @return 返回 Connection 对象表示: 初始化且连接数据库成功，
     * 返回字符串表示: 错误消息
     */
    /**
     * Initialize SQL processing service
     * @param driverName Driver Name
     * @param url Database address
     * @param user User name
     * @param password Password
     * @return
     * Returning the Connection object indicates that initialization and successful connection to the database,
     * Return string representation: error message
     */
    public synchronized Object init(String driverName, String url, String user, String password) {
        if (connection != null) {
            this.print("error", "Cannot re-initialize");
            return "Cannot re-initialize";
        }

        this.driverName = driverName;
        this.url = url;
        this.user = user;
        this.password = password;

        String[] urlArray;
        if (url.indexOf("mysql") > -1) {
            int index = url.indexOf("?");

            urlArray = url.split(":");
            url = urlArray[urlArray.length - 1];
            urlArray = url.split("\\?");
            url = urlArray[0];

            urlArray = url.split("/");
            if(urlArray.length > 1){
                this.databaseName = urlArray[urlArray.length - 1];
            }

            this.databaseType = DatabaseType.MARIADB;

            //mysql8.0+ nullCatalogMeansCurrent is now false by default
            if(index > -1){
                if(this.url.indexOf("nullCatalogMeansCurrent") == -1)
                    this.url += "&nullCatalogMeansCurrent=true";
            }else {
                this.url += "?nullCatalogMeansCurrent=true";
            }
        } else if (url.indexOf("oracle") > -1) {
            if(url.indexOf("/") > -1)
                urlArray = url.split("/");
            else if(url.indexOf("service_name") > -1)
                urlArray = url.split("service_name=");
            else
                urlArray = url.split(":");
            this.databaseName = urlArray[urlArray.length - 1].replaceAll("\\)", "");

            this.databaseType = DatabaseType.ORACLE;
        } else if (url.indexOf("sqlserver") > -1) {
            if(url.indexOf("databaseName") > -1)
                urlArray = url.split("databaseName=");
            else
                urlArray = url.split("database=");
            if(urlArray.length > 1){
                urlArray = urlArray[urlArray.length - 1].split(";");
                this.databaseName = urlArray[0];
            }

            this.databaseType = DatabaseType.SQL_SERVER;
        }

        this.print("info", "databaseName = " + this.databaseName);

        setDatabaseEnvironment(this.databaseType);
        this.setDatabaseDefaultValue();

        if (!isLoadDriver) {
            try {
                //加载驱动程序
                Class.forName(driverName);
                isLoadDriver = true;
                this.print("info", "Successfully loaded sql driver, driverName = " + driverName);
            } catch (ClassNotFoundException e1) {
                this.print("error", "Failed to load sql driver, driverName = " + driverName);
                e1.printStackTrace();
                isLoadDriver = false;
                return e1.getMessage();
            }
        }

//        Properties properties = new Properties();
//        properties.put("user", this.user);
//        properties.put("password", this.password);
//        properties.put("ResultSetMetaDataOptions", "1");

        try {
            this.print("info", "Connecting to database, url = " + this.url + ", user = " + this.user);
            if (user != null) {
                connection = DriverManager.getConnection(this.url, this.user, this.password);
//                connection = DriverManager.getConnection(this.url, properties);
            } else {
                connection = DriverManager.getConnection(this.url);
            }

            isInit = true;

            this.print("info", "Connection to database succeeded, url = " + this.url + ", user = " + this.user);

            if(DatabaseType.MARIADB == this.databaseType || DatabaseType.MYSQL == this.databaseType){
                String version = this.getDatabaseVersion();
                if(version != null){
                    if(!version.toLowerCase().contains("MariaDB".toLowerCase())){
                        this.databaseType = DatabaseType.MYSQL;
                    }
                }
            }
        } catch (SQLException e) {
            isInit = false;
            this.print("error", "Failed to connect to database, url = " + this.url + ", user = " + this.user);
            e.printStackTrace();
            return this.getResult(e);
        }

        return connection;
    }

    public void setDatabaseDefaultValue(){

    }

    public Connection getConnection() {
        return connection;
    }
    //

    /**
     * 当前连接状态是否有效
     * Is the current connection status valid
     * @return
     */
    public boolean isConnected(){
        //此处不能使用 connection.isClosed() 判断，此值不准确
        if(this.connection == null) return false;

        Object result = this.getDatabaseVersion();

        return result != null;
    }

    //

    /**
     * 关闭数据库连接
     * Close database connection
     */
    public Object closeConnection() {
        Object result = 0;
        try {
            if(connection != null)
                connection.close();
            this.print("info", "Successfully closed database connection, url = " + this.url + ", user = " + this.user);
        } catch (SQLException e) {
            this.print("error", "Error closing database connection：" + e);
            e.printStackTrace();
            result = this.getResult(e);
        }finally {
            instancesMap.remove(this.name);
            connection = null;
            isInit = false;
        }

        return result;
    }
    //

    /**
     * 关闭数据集合
     * Close Dataset
     *
     * @param set
     */
    public void closeResultSet(ResultSet set) {
        if (set == null) return;

        Statement s;
        try {
            s = set.getStatement();
            set.close();
            s.close();
        } catch (SQLException e) {
            this.print("error", "Error closing data result set：" + e);
            e.printStackTrace();
        }
    }
    /**
     * 关闭数据集合
     * Close Dataset
     * @param statement
     */
    public void closeStatement(PreparedStatement statement) {
        if (statement == null) return;

        try {
            statement.close();
        } catch (SQLException e) {
            this.print("error", "Error closing data result statement：" + e);
            e.printStackTrace();
        }
    }

    //

    /**
     * 导出数据库中的表为 java 映射类
     * @param sqlPackagePath  sql.airJDBC 包的路径前缀，默认为 "src/"，表示 sql.airJDBC 包放在 src/ 路径下，
     *                        若 sql.airJDBC 包放在 src/com/ 路径下，则 sqlPackagePath 设置为 "src/com/"
     * @param mapPath      导出文件路径，相对于 src 目录的路径，默认路径 src/tableMaps/mariadb/
     * @param mapSuffix    数据表类名称后缀，默认为 Map，若表名为 user ，则默认导出文件名 UserMap.java
     * @param hasFieldType 导出的数据类中的字段是否包含字段的数据类型，
     *                    若表中字段为 id ，类型为 int，此参数为 true 则字段名称导出为 ID_INTEGER = "id"
     */
    /**
     * Export tables in the database as Java mapping classes
     * @param sqlPackagePath The path prefix of the sql.airJDBC package, which defaults to 'src/',
     *                       indicates that the sql.airJDBC package is placed under the src/path,
     *                       If the sql.airJDBC package is placed in the src/com/path, then sqlPackagePath is set to "src/com/"
     * @param mapPath The exported database table mapping class path defaults to src/tableMaps/mariadb
     * @param mapSuffix The suffix name of the exported database table mapping class, such as the user table exporting class UserMap.java
     * @param hasFieldType Does the field in the exported data class contain the data type of the field,
     *                     If the field in the table is id and the type is int, and this parameter is true, the field name is exported as ID_ INTEGER="id
     */
    public void exportTableData(String sqlPackagePath, String mapPath, String mapSuffix, boolean hasFieldType) {
        this.hasFieldType = hasFieldType;
        if (mapPath != null && !mapPath.equals("")) {
            mapPath = mapPath.replaceAll("/", ".");
            if (mapPath.lastIndexOf(".") == mapPath.length() - 1) {
                mapPath = mapPath.substring(0, mapPath.length() - 1);
            }
            if (mapPath.indexOf("src.") > -1) {
                mapPath = mapPath.substring(4);
            }
            this.mapPath = mapPath;
        }
        if (mapSuffix != null) {
            this.mapSuffix = mapSuffix;
        }
        if (sqlPackagePath != null) {
            this.sqlPackagePath = sqlPackagePath;
        }

        this.readMetaData(true);
    }

    //

    /**
     * 获取数据库元数据，导出数据库中的表为 java 映射类
     * @param isExportMap 是否导出数据表映射类
     */
    /**
     *  Obtain database metadata and export tables in the database as Java mapping classes
     * @param isExportMap Export Data Table Mapping Class
     */
    public void readMetaData(boolean isExportMap) {
        if(!this.isInit){
            this.print("error", SQL_SERVICE_NOT_INITIALIZED);
            return;
        }

        try {
            DatabaseMetaData databaseMetaData = this.connection.getMetaData();
            this.databaseProductName = databaseMetaData.getDatabaseProductName();

            File dir = null;
            String dateFormat = null;
            String userDir = System.getProperty("user.dir");
//            String mapPath = this.getMapPath();
            String mapPath = this.mapPath;
            if (isExportMap) {
                this.print("info", this.toString());
                this.print("info", "Start reading database metadata");
                this.print("info", "Database products：" + this.databaseProductName);
                this.print("info", "Database driver：" + databaseMetaData.getDriverName());
                this.print("info", "Database username：" + databaseMetaData.getUserName());

                dir = new File(userDir + "/src/" + this.getMapPath());
                if (!dir.exists()) {
                    dir.mkdir();
                }
                dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis()));
            }

            //maven
            mapPath = mapPath.replace("main.java.", "");
            this.tables.clear();
            this.columnsMap.clear();
            this.columnsTypeMap.clear();
            this.columnsSizeMap.clear();
            this.primaryKeys.clear();

            //获取所有表
//            ResultSet resultSet = databaseMetaData.getTables(null, null, "%", new String[]{"TABLE"});
            ResultSet resultSet = this.getTablesByMetaData(this.connection, databaseMetaData, null);
            while (resultSet.next()) {
                String catalog = resultSet.getString("TABLE_CAT");
                String schema = resultSet.getString("TABLE_SCHEM");
                String tableName = resultSet.getString("TABLE_NAME");
                String primaryKey = "";

                //因不同数据库表名称大小写不一致，使用两个key，不区分大小写都可以获取
                this.tables.add(tableName.toLowerCase());
                this.tables.add(tableName.toUpperCase());

                ArrayList<String> columnsList = new ArrayList<>();
                this.columnsMap.put(tableName.toLowerCase(), columnsList);
                this.columnsMap.put(tableName.toUpperCase(), columnsList);

                ArrayList<String> columnsTypeList = new ArrayList<>();
                this.columnsTypeMap.put(tableName.toLowerCase(), columnsTypeList);
                this.columnsTypeMap.put(tableName.toUpperCase(), columnsTypeList);

                ArrayList<Integer> columnsSizeList = new ArrayList<>();
                this.columnsSizeMap.put(tableName.toLowerCase(), columnsSizeList);
                this.columnsSizeMap.put(tableName.toUpperCase(), columnsSizeList);

                if (isExportMap) {
                    this.print("info", "Table: " + tableName);
                }

                //获取表主键
                ResultSet primaryKeysSet = databaseMetaData.getPrimaryKeys(catalog, schema, tableName);
                while (primaryKeysSet.next()) {
                    primaryKey = primaryKeysSet.getString("COLUMN_NAME");
                    this.primaryKeys.add(primaryKey);
                }
                primaryKeysSet.close();
                if (isExportMap) {
                    this.print("info", "Primary key: " + primaryKey);
                }

                //获取所有列
//                ResultSet columnsSet = databaseMetaData.getColumns(null, this.connection.getCatalog(), tableName, null);
                ResultSet columnsSet = this.getColumnsByMetaData(this.connection, databaseMetaData, tableName, null);
                ResultSetMetaData columnsMetaData = columnsSet.getMetaData();
                while (columnsSet.next()) {
                    //列名称
                    columnsList.add(columnsSet.getString("COLUMN_NAME"));
                    //列长度
                    columnsSizeList.add(columnsSet.getInt("COLUMN_SIZE"));
                    //列的 sql 数据类型
                    columnsTypeList.add(JDBCType.valueOf(Integer.parseInt(columnsSet.getString("DATA_TYPE"))).getName());
                }
                columnsSet.close();
                if (isExportMap) {
                    this.print("info", "Columns list: " + columnsList);
                }

                //导出数据表映射类 Map
                if (isExportMap) {
                    File file = new File(userDir + "/" + this.sqlPackagePath + "sql/airJdbc/template/TableClassStaticTemplate.txt");

                    String mapName = this.getReadableName(tableName) + this.mapSuffix;
                    String content = FileUtil.readFile(file);
                    String fieldLinePattern = this.getFieldContent(content, false);
                    String fieldLine = fieldLinePattern.replaceAll("#", "");

                    content = content.replaceFirst("%package%", mapPath);
                    content = content.replaceFirst("%table%", this.databaseName + "." + tableName);
                    content = content.replaceFirst("%date%", dateFormat);
                    content = content.replaceAll("%class%", mapName);

                    String fieldContent = "";
                    int length = columnsList.size();
                    for (int i = 0; i < length; i++) {
                        String column = columnsList.get(i);
                        String temp = fieldLine.replaceFirst("%FIELD%", column.toUpperCase());
                        String fieldType = "";
                        if (this.hasFieldType) {
                            fieldType = "_" + columnsTypeList.get(i).toUpperCase();
                        }
                        temp = temp.replaceFirst("%columnType%", columnsTypeList.get(i).toLowerCase() + "(" + columnsSizeList.get(i) + ")");
                        temp = temp.replaceFirst("%TYPE%", fieldType);
                        temp = temp.replaceFirst("%field%", column);
                        fieldContent += temp;
                        if (i < length - 1)
                            fieldContent += System.getProperty("line.separator");
                    }

                    fieldLinePattern = fieldLinePattern.replaceAll("\\*", "\\\\*");
                    content = content.replaceFirst(fieldLinePattern, fieldContent);
                    content = content.replaceFirst("%primaryKey%", primaryKey);

                    file = new File(dir.getPath() + "/" + mapName + ".java");

                    FileUtil.writeFile(file, content);
                    if (file.exists()) {
                        this.print("info", "Export file: " + file.getPath());
                    }

                    this.print("", "-----------------------------------");
                }
            }

            //导出数据表名
            if (isExportMap) {
                File file = new File(userDir + "/" + this.sqlPackagePath + "sql/airJdbc/template/TableNameTemplate.txt");
                String content = FileUtil.readFile(file);
                String fieldLinePattern = this.getFieldContent(content, true);
                String fieldLine = fieldLinePattern.replaceAll("#", "");

                String fieldContent = "";
                int length = this.tables.size();
                for (int i = 0; i < length; i += 2) {
                    String column = this.tables.get(i);
                    String temp = fieldLine.replaceFirst("%FIELD%", column.toUpperCase());
                    temp = temp.replaceFirst("%field%", column);
                    fieldContent += temp;
                    if (i < length - 1)
                        fieldContent += System.getProperty("line.separator");
                }

                String fileName = "TableName";
                content = content.replaceFirst("%package%", mapPath);
                content = content.replaceFirst("%database%", this.databaseName);
                content = content.replaceFirst("%date%", dateFormat);
                content = content.replaceFirst("%class%", fileName);
                content = content.replaceFirst("%tableCount%", length + "");
                content = content.replaceFirst(fieldLinePattern, fieldContent);

                file = new File(dir.getPath() + "/" + fileName + ".java");

                FileUtil.writeFile(file, content);
                if (file.exists()) {
                    this.print("info", "Export file: " + file.getPath());
                }
            }

            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    //
    public String getDatabaseVersion(){
        Object result = null;
        String version = null;

        if (DatabaseType.MARIADB == this.databaseType || DatabaseType.MYSQL == this.databaseType) {
            result = this.selectOne("SELECT VERSION()", TableMap.class);
            if(result instanceof TableMap){
                version = ((TableMap) result).get("VERSION()").toString();
            }
        } else if (DatabaseType.ORACLE == this.databaseType) {
            result = this.selectOne("SELECT * FROM v$version", TableMap.class);
            if(result instanceof TableMap){
                version = ((TableMap) result).get("BANNER_FULL").toString();
            }
        } else if (DatabaseType.SQL_SERVER == this.databaseType) {
            result = this.selectOne("select @@version", TableMap.class);
            if(result instanceof TableMap){
                version = ((TableMap) result).get("").toString();
            }
        } else if (DatabaseType.POSTGRE_SQL == this.databaseType) {
            result = this.selectOne("SELECT VERSION()", TableMap.class);
            if(result instanceof TableMap){
                version = ((TableMap) result).get("VERSION()").toString();
            }
        }

        return version;
    }
    //
    public String getPrimaryKey(String catalog, String table){
        String primaryKey = null;
        ResultSet primaryKeysSet = null;
        try {
            //未选择数据库时必须传 catalog 参数才能读取到数据，若已选择数据库 catalog 可以使用 "TABLE_CAT"
            primaryKeysSet = this.connection.getMetaData().getPrimaryKeys(catalog, "TABLE_SCHEM", table);
            while (primaryKeysSet.next()) {
                primaryKey = primaryKeysSet.getString("COLUMN_NAME");
            }
            primaryKeysSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return primaryKey;
    }

    //
    public ResultSet getTablesByMetaData(Connection connection, DatabaseMetaData databaseMetaData, String catalog) throws SQLException {
        ResultSet resultSet = null;
        if (DatabaseType.MARIADB == this.databaseType || DatabaseType.MYSQL == this.databaseType) {
            resultSet = databaseMetaData.getTables(catalog, "%", "%", new String[]{"TABLE"});
        } else if (DatabaseType.ORACLE == this.databaseType) {
            resultSet = databaseMetaData.getTables(catalog, databaseMetaData.getUserName(), "%", null);
        } else if (DatabaseType.SQL_SERVER == this.databaseType) {
            resultSet = databaseMetaData.getTables(catalog, null, "%", new String[]{"TABLE"});
        } else if (DatabaseType.POSTGRE_SQL == this.databaseType) {
            resultSet = databaseMetaData.getTables(connection.getCatalog(), "public", "%", new String[]{"TABLE"});
        }

        return resultSet;
    }
    //
    private ResultSet getColumnsByMetaData(Connection connection, DatabaseMetaData databaseMetaData, String tableName, String catalog) throws SQLException {
        if(catalog == null){
            //若未选中数据库则 catalog 必须传值，否则部分数据库无法获取数据
            catalog = this.connection.getCatalog();
        }
        ResultSet resultSet = null;
        if (DatabaseType.MARIADB == this.databaseType || DatabaseType.MYSQL == this.databaseType) {
            resultSet = databaseMetaData.getColumns(catalog, catalog, tableName, null);
        } else if (DatabaseType.ORACLE == this.databaseType) {
            resultSet = databaseMetaData.getColumns(catalog, catalog, tableName, null);
        } else if (DatabaseType.SQL_SERVER == this.databaseType) {
            resultSet = databaseMetaData.getColumns(null, null, tableName, null);
        } else if (DatabaseType.POSTGRE_SQL == this.databaseType) {
            resultSet = databaseMetaData.getColumns(catalog, catalog, tableName, null);
        }

        return resultSet;
    }
    //
    public ResultSet getIndexByMetaData(DatabaseMetaData databaseMetaData, String catalog, String table) throws SQLException {
        if(catalog == null){
            //若未选中数据库则 catalog 必须传值，否则无法获取数据
            catalog = this.connection.getCatalog();
        }
        ResultSet resultSet = databaseMetaData.getIndexInfo(catalog, catalog, table, false, false);

        return resultSet;
    }

    //
    private String getMapPath() {
        String content = this.mapPath.replaceAll("\\.", "/");
        return content;
    }

    //
    private String getFieldContent(String value, boolean isTable) {
        String expression;
        if (isTable) {
            //单行
            expression = "#\\s*.*\\s*.*\\s*#";
        } else {
            //多行
            expression = "#\\s*\\/\\*\\*\\s*.*\\s*\\*\\*\\/.*\\s*.*\\s*#";
        }

        Pattern pattern = Pattern.compile(expression);

        Matcher matcher = pattern.matcher(value);
        if (matcher.find()) {
            return matcher.group(0);
        }

        return null;
    }

    //
    private String getReadableName(String name) {
        if (name == null || name.equals("")) return name;

        if (DatabaseType.MARIADB == this.databaseType || DatabaseType.MYSQL == this.databaseType) {
        } else if (DatabaseType.ORACLE == this.databaseType) {
            name = name.toLowerCase();
        } else if (DatabaseType.SQL_SERVER == this.databaseType) {
        } else if (DatabaseType.POSTGRE_SQL == this.databaseType) {
        }

        String[] array = name.split("_");
        int length = array.length;
        String result = "";

        for (int i = 0; i < length; i++) {
            String first = array[i].substring(0, 1);
            array[i] = array[i].replaceFirst(first, first.toUpperCase());
            result += array[i];
        }

        return result;
    }
    //

    /**
     * 设置即将执行的 sql 参数，即提交给 PreparedStatement 的参数，
     * 参数的顺序与 PreparedStatement.setObject() 的顺序相同，
     * 第1个参数将执行 PreparedStatement.setObject(1, args[0])，
     * 第2个参数将执行 PreparedStatement.setObject(2, args[1])，
     * 其余参数类似，参数只使用一次，SqlService 执行下一条 sql 后清除所有参数
     *
     * @param args 参数列表
     */
    //外部使用 synchronized 锁定此对象后，此方法同时被锁定

    /**
     * Set the SQL parameters to be executed, that is, the parameters submitted to PreparedStatement,
     * The order of the parameters is the same as that of PreparedStatement.setObject (),
     * The first parameter will execute PreparedStatement.setObject (1, args [0]),
     * The second parameter will execute PreparedStatement.setObject (2, args [1]),
     * The other parameters are similar. The parameters are only used once, and SqlService clears them after executing the next SQL statement
     * @param args parameter list
     */
    public synchronized void setSqlParams(Object... args) {
        this.sqlParams = args;
    }

    //外部使用 synchronized 锁定此对象后，此方法同时被锁定
    private synchronized void setSqlParamsToStatement(PreparedStatement preStatement) throws SQLException {
        if (this.sqlParams != null && this.sqlParams.length > 0) {
            for (int i = 0; i < this.sqlParams.length; i++) {
                preStatement.setObject(i + 1, this.sqlParams[i]);

                if (this.sqlParams[i].toString().length() < SQL_LENGTH_PRINT)
                    this.print("info", "sql params[" + i + "] = " + this.sqlParams[i]);
            }

            this.sqlParams = null;
        }
    }
    //

    /**
     * 插入数据
     *
     * @param sql sql 语句
     * @return 查询成功时返回 Integer 表示影响的行数，查询失败返回字符串 "ErrorCode:xxx ..."，参数错误时返回字符串  "Invalidate Arguments"
     */
    /**
     * insert data
     * @param sql SQL statement
     * @return When the query is successful, an Integer is returned indicating the number of rows affected. When the query fails, the string "ErrorCode: xxx..." is returned. When the parameter is incorrect, the string "Invalidate Arguments" is returned
     */
    public Object insert(String sql) {
        return this.executeSql(sql);
    }
    //

    /**
     * 插入单行数据
     * @param tableMap 数据表映射类实例，需要设置 tableName 字段和数据表字段的值
     * @return 查询成功时返回 Integer 表示影响的行数，查询失败返回字符串 "ErrorCode:xxx ..."，参数错误时返回字符串  "Invalidate Arguments"
     */
    /**
     * Insert Single Row Data
     * @param tableMap The data table mapping class instance requires setting the values of the tableName field and the data table field
     * @return When the query is successful, an Integer is returned indicating the number of rows affected. When the query fails, the string "ErrorCode: xxx..." is returned. When the parameter is incorrect, the string "Invalidate Arguments" is returned
     */
    public Object insertOne(TableMap tableMap) {
        String sql = SqlUtil.getInsertOneContent(tableMap.tableName, tableMap);
        return executeSql(sql);
    }
    //

    /**
     * 插入单行数据
     * @param table 数据表名
     * @param args 插入的字段和值列表，格式：column1,value1,column2,value2，如："user","abc","count",1
     * @return 查询成功时返回 Integer 表示影响的行数，查询失败返回字符串 "ErrorCode:xxx ..."，参数错误时返回字符串  "Invalidate Arguments"
     */
    /**
     * Insert Single Row Data
     * @param table Table name
     * @param args List of inserted fields and values, format: column1, value1, column2, value2, such as "user", "abc", "count", 1
     * @return When the query is successful, an Integer is returned indicating the number of rows affected. When the query fails, the string "ErrorCode: xxx..." is returned. When the parameter is incorrect, the string "Invalidate Arguments" is returned
     */
    public Object insertOne(String table, Object... args) {
        if (args.length == 0) {
            this.print("error", ".insertOne()No inserted data args.length = " + args.length);
            return INVALIDATE_ARGUMENTS;
        }

        String sql = SqlUtil.getInsertOneContent(table, args);
        return executeSql(sql);
    }
    //

    /**
     * 插入单行或多行数据，分组插入时若发生错误，则停止后续插入
     * @param table 数据表名
     * @param columnNames 插入的列名数组，格式：String[]{column1,column2}
     * @param args 插入的值列表，对应 columnNames 顺序，可以有无限组，sql 长度超过 maxSqlLength 时自动分组插入
     *             格式：column1_value1,column2_value1,column1_value2,column2_value2 ...
     * @return 查询成功时返回 Integer 表示影响的行数，查询失败返回字符串 "ErrorCode:xxx ..."，参数错误时返回字符串  "Invalidate Arguments"
     */
    /**
     * Insert single or multiple rows of data. If an error occurs during group insertion, stop subsequent insertion
     * @param table Table name
     * @param columnNames Array of inserted column names, format: String [] {column1, column2}
     * @param args The list of inserted values corresponds to the order of columnNames, which can have infinite groups. When the SQL length exceeds maxSqlLength, it will be automatically grouped and inserted
     * Format: column1_ Value1, column2_ Value1, column1_ Value2, column2_ Value2
     * @return When the query is successful, an Integer is returned indicating the number of rows affected. When the query fails, the string "ErrorCode: xxx..." is returned. When the parameter is incorrect, the string "Invalidate Arguments" is returned
     */
    public Object insertBatch(String table, String[] columnNames, Object... args) {
        final int size = args.length;
        if (size == 0) {
            this.print("error", ".insertBatch()No inserted data");
            return INVALIDATE_ARGUMENTS;
        }
        if (size % columnNames.length != 0) {
            this.print("error", ".insertBatch()The number of data does not match the number of fields args.length = " + size);
            return INVALIDATE_ARGUMENTS;
        }

        //估算值，并非准确值，假定用于更新的字段名称最长为64位，另外计算空格和逗号
        int rowLength = table.length()
                + (FIELD_NAME_LENGTH + args[1].toString().length()) * size
                + FIELD_NAME_LENGTH * columnNames.length + SQL_DEFAULT_LENGTH;
        int count = (int) Math.ceil((double) rowLength / maxSqlLength);

        Object result = 0;
        int resultSuccess = 0;
        String sql;

        //超过最大数量时，拆分插入
        if (count > 1) {
            int subCount = (int) Math.ceil((double) size / count);
            if (subCount % columnNames.length != 0) {
                subCount += columnNames.length - subCount % columnNames.length;
            }
            count = (int) Math.ceil((double) size / subCount);

            Object[] subList;

            int index = 0;
            int i = 0;

            while (index < size && subCount > 0) {
                subList = Arrays.copyOfRange(args, index, index + subCount);

                sql = getInsertBatchContent(table, columnNames, subList);

                i++;
                this.printBatchLog("insertBatch()", subCount, size, sql.length(), i, count);

                result = executeSql(sql);
                if(result instanceof Integer){
                    resultSuccess += (int)result;
                    result = resultSuccess;
                }else {
                    break;
                }

                index += subCount;
                subCount = Math.min(subCount, size - index);
            }
        } else {
            sql = getInsertBatchContent(table, columnNames, args);

            this.printBatchLog("insertBatch()", size, size, sql.length(), 1, 1);

            result = executeSql(sql);
        }

        return result;
    }
    //
    private String getInsertBatchContent(String table, String[] columnNames, Object... args){
        String sql = null;
        if (DatabaseType.MARIADB == this.databaseType || DatabaseType.MYSQL == this.databaseType) {
            sql = SqlUtil.getInsertBatchContent(table, columnNames, args);
        } else if (DatabaseType.ORACLE == this.databaseType) {
            sql = SqlUtil.getInsertBatchContentOracle(table, columnNames, args);
        } else if (DatabaseType.SQL_SERVER == this.databaseType) {
            sql = SqlUtil.getInsertBatchContent(table, columnNames, args);
            sql = this.getIdentityInsertSql(sql, table);
        } else if (DatabaseType.POSTGRE_SQL == this.databaseType) {
        }

        return sql;
    }
    //
    private String getIdentityInsertSql(String sql, String table){
        if(this.sqlServerIdentityInsert){
            sql = IDENTITY_INSERT_ON.replaceFirst("#tableName#", table) + sql + IDENTITY_INSERT_OFF.replaceFirst("#tableName#", table);
        }
        return sql;
    }
    //

    /**
     * 插入单行或多行数据，包括数据库表一条记录的所有列值，分组插入时若发生错误，则停止后续插入
     * @param table 数据表名
     * @param args 插入的值列表，对应 columnNames 顺序，可以有无限组，sql 长度超过 maxSqlLength 时自动分组插入
     * 格式：column1_value1,column2_value1,column1_value2,column2_value2 ...
     * @return 查询成功时返回 Integer 表示影响的行数，查询失败返回字符串 "ErrorCode:xxx ..."，参数错误时返回字符串  "Invalidate Arguments"
     */
    /**
     * Insert single or multiple rows of data, including all column values of a record in the database table. If an error occurs during group insertion, stop subsequent insertion
     * @param table Table name
     * @param args The list of inserted values corresponds to the order of columnNames, which can have infinite groups. When the SQL length exceeds maxSqlLength, it will be automatically grouped and inserted
     * *Format: column1_ Value1, column2_ Value1, column1_ Value2, column2_ Value2
     * @return When the query is successful, an Integer is returned indicating the number of rows affected. When the query fails, the string "ErrorCode: xxx..." is returned. When the parameter is incorrect, the string "Invalidate Arguments" is returned
     */
    public Object insertBatchAllFields(String table, Object... args) {
        int size = args.length;
        if (size == 0) {
            this.print("error", ".insertBatchAllFields()No inserted data");
            return INVALIDATE_ARGUMENTS;
        }

        //更新 columnsMap
        this.readMetaData(false);
        ArrayList<String> filedList = this.columnsMap.get(table);
        if (filedList == null || filedList.size() == 0) {
            this.print("error", ".insertBatchAllFields()Data table does not exist： table = " + table);
            return INVALIDATE_ARGUMENTS;
        }
        if (size % filedList.size() != 0) {
            this.print("error", ".insertBatchAllFields()The number of data does not match the number of table fields: args.length = " + size + ", filedList.size() = " + filedList.size());
            return INVALIDATE_ARGUMENTS;
        }

        //估算值，并非准确值，假定用于更新的字段名称最长为64位，另外计算空格和逗号
        int rowLength = table.length()
                + (FIELD_NAME_LENGTH + args[1].toString().length()) * size
                + FIELD_NAME_LENGTH * filedList.size() + SQL_DEFAULT_LENGTH;
        int count = (int) Math.ceil((double) rowLength / maxSqlLength);

        Object result = 0;
        int resultSuccess = 0;
        String sql;

//        filedList.remove(filedList.size() - 1);

        //超过最大数量时，拆分插入
        if (count > 1) {
            int subCount = (int) Math.ceil((double) size / count);
            if (subCount % filedList.size() != 0) {
                subCount += filedList.size() - subCount % filedList.size();
            }
            count = (int) Math.ceil((double) size / subCount);

            Object[] subList;

            int index = 0;
            int i = 0;

            while (index < size && subCount > 0) {
                subList = Arrays.copyOfRange(args, index, index + subCount);

                sql = getInsertBatchContentAll(table, filedList, subList);

                i++;
                this.printBatchLog("insertBatchAllFields()", subCount, size, sql.length(), i, count);

                result = executeSql(sql);
                if(result instanceof Integer){
                    resultSuccess += (int)result;
                    result = resultSuccess;
                }else {
                    break;
                }

                index += subCount;
                subCount = Math.min(subCount, size - index);
            }
        } else {
            sql = getInsertBatchContentAll(table, filedList, args);

            this.printBatchLog("insertBatchAllFields()", size, size, sql.length(), 1, 1);

            result = executeSql(sql);
        }

        return result;
    }
    //
    private String getInsertBatchContentAll(String table, ArrayList<String> filedList, Object... args){
        String sql = null;
        if (DatabaseType.MARIADB == this.databaseType || DatabaseType.MYSQL == this.databaseType) {
            sql = SqlUtil.getInsertBatchContentAll(table, filedList, args);
        } else if (DatabaseType.ORACLE == this.databaseType) {
            sql = SqlUtil.getInsertBatchContentAllOracle(table, filedList, args);
        } else if (DatabaseType.SQL_SERVER == this.databaseType) {
            sql = SqlUtil.getInsertBatchContentAll(table, filedList, args);
            sql = this.getIdentityInsertSql(sql, table);
        } else if (DatabaseType.POSTGRE_SQL == this.databaseType) {
        }

        return sql;
    }

    //

    /**
     * 插入多行数据，使用面对对象编程，分组插入时若发生错误，则停止后续插入
     * @param list 数据列表，需要保证列表中的每个 TableMap 实例已设置值的字段总数量相同，如 list[0]有3个字段有值，
     *             则其他的实例也应都是3个字段有值
     * @return 查询成功时返回 Integer 表示影响的行数，查询失败返回字符串 "ErrorCode:xxx ..."，参数错误时返回字符串  "Invalidate Arguments"
     */
    /**
     * Insert multiple rows of data using object-oriented programming. If an error occurs during group insertion, stop subsequent insertion
     * @param list For a data list, it is necessary to ensure that the total number of fields with values set for each TableMap instance in the list is the same. For example, there are three fields with values in the list [0],
     * Then all other instances should also have 3 fields with values
     * @return When the query is successful, an Integer is returned indicating the number of rows affected. When the query fails, the string "ErrorCode: xxx..." is returned. When the parameter is incorrect, the string "Invalidate Arguments" is returned
     */
    public Object insertBatch(ArrayList<TableMap> list) {
        if (list == null) {
            this.print("error", ".insertBatch()No inserted  data: list = " + list);
            return INVALIDATE_ARGUMENTS;
        }
        int size = list.size();
        if (size == 0) {
            this.print("error", ".insertBatch()No inserted  data: list.size() = " + size);
            return INVALIDATE_ARGUMENTS;
        }

        TableMap tableMap = list.get(0);
        String table = tableMap.tableName;
        String conditionField = tableMap.primaryKey;

        if (tableMap.get(conditionField) == null) {
            for (String key : tableMap.keySet()) {
                Object value = tableMap.get(key);
                if (value != null) {
                    conditionField = key;
                    break;
                }
            }
        }

        //估算值，并非准确值，假定用于更新的字段名称最长为64位，另外计算空格和逗号
        int rowLength = table.length() + conditionField.length()
                + (FIELD_NAME_LENGTH + tableMap.get(conditionField).toString().length()) * size
                + FIELD_NAME_LENGTH * size + SQL_DEFAULT_LENGTH;//为语句本身的长度，另外计算空格和括号
        int count = (int) Math.ceil((double) rowLength / maxSqlLength);

        Object result = 0;
        int resultSuccess = 0;
        String sql;

        //超过最大数量时，拆分插入
        if (count > 1) {
            int subCount = (int) Math.ceil((double) size / count);
            count = (int) Math.ceil((double) size / subCount);
            ArrayList<TableMap> subList;

            int index = 0;
            int i = 0;

            while (index < size && subCount > 0) {
                subList = new ArrayList<>();
                for (int k = 0; k < subCount; k++) {
                    subList.add(list.get(k + index));
                }

                sql = getInsertBatchContent(subList);

                i++;
                this.printBatchLog("insertBatch()", subCount, size, sql.length(), i, count);

                result = executeSql(sql);
                if(result instanceof Integer){
                    resultSuccess += (int)result;
                    result = resultSuccess;
                }else {
                    break;
                }

                index += subCount;
                subCount = Math.min(subCount, size - index);
            }
        } else {
            sql = getInsertBatchContent(list);

            this.printBatchLog("insertBatch()", size, size, sql.length(), 1, 1);

            result = this.executeSql(sql);
        }

        return result;
    }

    private String getInsertBatchContent(ArrayList<TableMap> list){
        String sql = null;
        if (DatabaseType.MARIADB == this.databaseType || DatabaseType.MYSQL == this.databaseType) {
            sql = SqlUtil.getInsertBatchContent(list);
        } else if (DatabaseType.ORACLE == this.databaseType) {
            sql = SqlUtil.getInsertBatchContentOracle(list);
        } else if (DatabaseType.SQL_SERVER == this.databaseType) {
            sql = SqlUtil.getInsertBatchContent(list);
            sql = this.getIdentityInsertSql(sql, list.get(0).tableName);
        } else if (DatabaseType.POSTGRE_SQL == this.databaseType) {
        }

        return sql;
    }

    //

    /**
     * 删除数据记录
     * @param sql sql 语句
     * @return 查询成功时返回 Integer 表示影响的行数，查询失败返回字符串 "ErrorCode:xxx ..."，参数错误时返回字符串  "Invalidate Arguments"
     */
    /**
     * Delete Data Record
     * @param sql SQL statement
     * @return When the query is successful, an Integer is returned indicating the number of rows affected. When the query fails, the string "ErrorCode: xxx..." is returned. When the parameter is incorrect, the string "Invalidate Arguments" is returned
     */
    public Object delete(String sql) {
        return executeSql(sql);
    }

    //

    /**
     * 删除一条记录，使用面对对象编程
     * @param tableMap 表映射类实例对象，需要设置 tableMap.tableName, tableMap.primaryKey 字段值
     * @return 查询成功时返回 Integer 表示影响的行数，查询失败返回字符串 "ErrorCode:xxx ..."，参数错误时返回字符串  "Invalidate Arguments"
     */
    /**
     * Delete a record and use object-oriented programming
     * @param tableMap Table mapping class instance objects require setting tableMap. tableName, tableMap. primaryKey field values
     * @return When the query is successful, an Integer is returned indicating the number of rows affected. When the query fails, the string "ErrorCode: xxx..." is returned. When the parameter is incorrect, the string "Invalidate Arguments" is returned
     */
    public Object deleteOne(TableMap tableMap) {
        String sql = SqlUtil.getDeleteOneContent(tableMap.tableName, tableMap.primaryKey, tableMap.get(tableMap.primaryKey));
        return executeSql(sql);
    }

    //

    /**
     * 删除多行数据记录，使用面对对象编程，分组插入时若发生错误，则停止后续插入
     * @param list 表映射类实例对象列表，需要设置 tableMap.tableName, tableMap.primaryKey 字段值
     * @return 查询成功时返回 Integer 表示影响的行数，查询失败返回字符串 "ErrorCode:xxx ..."，参数错误时返回字符串  "Invalidate Arguments"
     */
    /**
     * Delete multiple rows of data records, use object-oriented programming, and if an error occurs during group insertion, stop subsequent insertion
     * @param list Table mapping class instance object list, requires setting tableMap. tableName, tableMap. primaryKey field values
     * @return When the query is successful, an Integer is returned indicating the number of rows affected. When the query fails, the string "ErrorCode: xxx..." is returned. When the parameter is incorrect, the string "Invalidate Arguments" is returned
     */
    public Object deleteBatch(ArrayList<TableMap> list) {
        if (list == null) {
            this.print("error", ".deleteBatch()No deleted data: list = " + list);
            return INVALIDATE_ARGUMENTS;
        }
        if (list.size() == 0) {
            this.print("error", ".deleteBatch()No deleted data: list.size() = " + list.size());
            return INVALIDATE_ARGUMENTS;
        }

        String table = null;
        String field = null;
        int length = list.size();
        Object[] idList = new Object[length];
        for (int i = 0; i < length; i++) {
            TableMap tableMap = list.get(i);
            if (table == null) {
                table = tableMap.tableName;
                field = tableMap.primaryKey;
            }
            idList[i] = tableMap.get(tableMap.primaryKey);
        }
        return deleteBatch(table, field, idList);
    }

    //

    /**
     * 删除多行数据记录，分组插入时若发生错误，则停止后续插入
     * @param table 数据库表名
     * @param conditionField 用于删除记录的条件字段
     * @param args 用于删除记录的条件字段值列表，如 conditionField 为主键字段，则参数列表为主键值列表
     * @return  查询成功时返回 Integer 表示影响的行数，查询失败返回字符串 "ErrorCode:xxx ..."，参数错误时返回字符串  "Invalidate Arguments"
     */
    /**
     * Delete multiple rows of data records. If an error occurs during group insertion, stop subsequent insertion
     * @param table Table name
     * @param conditionField Condition fields for deleting records
     * @param args A list of condition field values used to delete records. If conditionField is a primary key field, then the parameter list is a primary key value list
     * @return When the query is successful, an Integer is returned indicating the number of rows affected. When the query fails, the string "ErrorCode: xxx..." is returned. When the parameter is incorrect, the string "Invalidate Arguments" is returned
     */
    public Object deleteBatch(String table, String conditionField, Object... args) {
        int size = args.length;

        if (size == 0) {
            this.print("error", ".deleteBatch()No deleted data: size = " + size);
            return INVALIDATE_ARGUMENTS;
        }

        //估算值，并非准确值，假定用于更新的字段名称最长为64位，另外计算空格和逗号
        int rowLength = table.length() + conditionField.length()
                + FIELD_NAME_LENGTH * size + SQL_DEFAULT_LENGTH;//为语句本身的长度，另外计算空格和括号
        int count = (int) Math.ceil((double) rowLength / maxSqlLength);

        Object result = 0;
        int resultSuccess = 0;
        String sql;

        //超过最大数量时，拆分插入
        if (count > 1) {
            int subCount = (int) Math.ceil((double) size / count);
            count = (int) Math.ceil((double) size / subCount);
            Object[] subList;

            int index = 0;
            int i = 0;

            while (index < size && subCount > 0) {
                subList = Arrays.copyOfRange(args, index, index + subCount);

                sql = SqlUtil.getDeleteBatchContent(table, conditionField, subList);

                i++;
                this.printBatchLog("deleteBatch()", subCount, size, sql.length(), i, count);

                result = executeSql(sql);
                if(result instanceof Integer){
                    resultSuccess += (int)result;
                    result = resultSuccess;
                }else {
                    break;
                }

                index += subCount;
                subCount = Math.min(subCount, size - index);
            }
        } else {
            sql = SqlUtil.getDeleteBatchContent(table, conditionField, args);

            this.printBatchLog("deleteBatch()", size, size, sql.length(), 1, 1);

            result = executeSql(sql);
        }

        return result;
    }

    //
    /**
     * 批量执行除了 select 之外的所有 sql 语句，如 insert ..., delete ...，sql 语句总长度超过最大值时自动拆分为多次执行，sql 语句较多时使用，
     * 分组执行时发生错误，仍然会执行后续的语句
     * @param sqls sql 语句列表，语句不包含分号，多条语句同时提交，连接数据库时需要开启多条语句查询设置，
     *             如 mysql 的 url : "jdbc:mysql://localhost:3306/dbName?allowMultiQueries=true"
     * @return  new Object[]{successList, errorList} successList和errorList 每个元素表示单次查询结果:成功时返回 Integer 表示影响的行数，查询失败返回字符串 "ErrorCode:xxx ..."，参数错误时返回字符串  "Invalidate Arguments"
     */
    /**
     * Batch execute all SQL statements except select, such as insert, Delete, Automatically split into multiple executions when the total length of SQL statements exceeds the maximum value, used when there are many SQL statements,An error occurred during group execution, and subsequent statements will still be executed
     * @param sqls SQL statement list, statement does not contain semicolons, multiple statements are submitted simultaneously, and multiple statement query settings need to be enabled when connecting to the database,
     * For example, the URL of MySQL is: "jdbc: mysql://localhost:3306/dbName?allowMultiQueries=true “
     * @return New Object [] {successList, errorList} successList and errorList Each element represents a single query result: on success, an Integer is returned indicating the number of rows affected, on query failure, the string "ErrorCode: xxx..." is returned, and on parameter errors, the string "Invalidate Arguments" is returned
     */
    public Object[] executeSqlBatch(String... sqls){
        String content = "";
        Object result;
        ArrayList<Integer> successList = new ArrayList<>();
        ArrayList<String> errorList = null;

        for (int i = 0, length = sqls.length; i < length; i++) {
            if(sqls[i] == null || sqls[i].length() == 0) continue;;

            if(content.length() + sqls[i].length() < this.maxSqlLength){
                content += sqls[i] + ";";
            }else {
                if(content.length() == 0)
                    content = sqls[i];

                this.printBatchLog("executeSqlBatch()", i, length, content.length(), i, length);
                result = this.executeSql(content);

                if(result instanceof Integer){
                    successList.add((Integer) result);
                }else {
                    if(errorList == null){
                        errorList = new ArrayList<>();
                    }
                    errorList.add(result.toString());
                }

                content = "";
            }
        }

        return new Object[]{successList, errorList};
    }
    /**
     * 执行除了 select 之外的所有 sql 语句，如 insert ..., delete ...，sql 语句较少时使用
     * @param sqls sql 语句列表，语句不包含分号，多条语句同时提交，连接数据库时需要开启多条语句查询设置，
     *             如 mysql 的 url : "jdbc:mysql://localhost:3306/dbName?allowMultiQueries=true"
     * @return  查询成功时返回 Integer 表示影响的行数，查询失败返回字符串 "ErrorCode:xxx ..."，参数错误时返回字符串  "Invalidate Arguments"
     */
    /**
     * Execute all SQL statements except select, such as insert, Delete, Use when there are fewer SQL statements
     * @param sqls SQL statement list, statement does not contain semicolons, multiple statements are submitted simultaneously, and multiple statement query settings need to be enabled when connecting to the database,
     * For example, the URL of MySQL is: "jdbc: mysql://localhost:3306/dbName?allowMultiQueries=true “
     * @return When the query is successful, an Integer is returned indicating the number of rows affected. When the query fails, the string "ErrorCode: xxx..." is returned. When the parameter is incorrect, the string "Invalidate Arguments" is returned
     */
    public Object executeSql(String... sqls) {
        String content = "";
        for (int i = 0; i < sqls.length; i++) {
            content += sqls[i] + ";";
        }

        return this.executeSql(content);
    }

    //

    /**
     * 执行一条或多条除了 select 之外的所有 sql 语句，如 insert ..., delete ...
     * @param sql sql 语句，若为多条语句，需要使用分号分隔语句，并且连接数据库时需要开启多条语句查询设置，
     *      *             如 mysql 的 url : "jdbc:mysql://localhost:3306/dbName?allowMultiQueries=true"
     * @return  查询成功时返回 Integer 表示影响的行数，查询失败返回字符串 "ErrorCode:xxx ..."，参数错误时返回字符串  "Invalidate Arguments"
     * @see SqlService#setSqlParams
     */
    /**
     * Execute one or more SQL statements other than select, such as insert, Delete
     * @param sql SQL statements, if there are multiple statements, need to use semicolons to separate the statements, and when connecting to the database, multiple statement query settings need to be enabled,
     * *For example, the URL of MySQL is "jdbc: mysql://localhost:3306/dbName?allowMultiQueries=true “
     * @return When the query is successful, an Integer is returned indicating the number of rows affected. When the query fails, the string "ErrorCode: xxx..." is returned. When the parameter is incorrect, the string "Invalidate Arguments" is returned
     * @see SqlService#setSqlParams
     */
    public Object executeSql(String sql) {
        if(!this.isInit){
            this.print("error", SQL_SERVICE_NOT_INITIALIZED);
            return SQL_SERVICE_NOT_INITIALIZED;
        }

        PreparedStatement preStatement = null;
        Object result;
        try {
            preStatement = connection.prepareStatement(sql);
            this.setSqlParamsToStatement(preStatement);
            result = preStatement.executeUpdate();

            if (isPrintLog || sql.length() < SQL_LENGTH_PRINT)
                this.print("info", ".executeSql()Update data table succeeded：sql.length() = " + sql.length() + ", sql = " + sql);
        } catch (SQLException e) {
            if (isPrintLog || sql.length() < SQL_LENGTH_PRINT)
                this.print("error", ".executeSql()Error updating data table：sql.length() = " + sql.length() + ", sql = " + sql);
            e.printStackTrace();

            result = getResult(e);
        }finally {
            this.closeStatement(preStatement);
        }

        return result;
    }
    //
    private String getResult(SQLException e){
        return "ErrorCode:" + e.getErrorCode() + "; SQLState:" + e.getSQLState() + "; Message:" + e.getMessage();
    }

    //
    /**
     * 执行更新数据库的 sql 语句
     * @param sql sql 语句
     * @return  查询成功时返回 Integer 表示影响的行数，查询失败返回字符串 "ErrorCode:xxx ..."，参数错误时返回字符串  "Invalidate Arguments"
     * @see SqlService#setSqlParams
     */
    /**
     * Execute SQL statements to update the database
     * @param sql SQL statement
     * @return When the query is successful, an Integer is returned indicating the number of rows affected. When the query fails, the string "ErrorCode: xxx..." is returned. When the parameter is incorrect, the string "Invalidate Arguments" is returned
     * @see SqlService#setSqlParams
     */
    public Object update(String sql) {
        return this.executeSql(sql);
    }

    //

    /**
     * 使用参数更新数据库表的一条记录，使用面对对象编程
     * @param tableMap 表映射类实例对象，需要设置 tableMap.tableName, tableMap.primaryKey 字段值，作为更新sql语句的表名和 where 条件值
     * @return  查询成功时返回 Integer 表示影响的行数，查询失败返回字符串 "ErrorCode:xxx ..."，参数错误时返回字符串  "Invalidate Arguments"
     */
    /**
     * Using parameters to update a record in a database table, using object-oriented programming
     * @param tableMap Table mapping class instance objects require setting the tableMap. tableName, tableMap. primaryKey field values as the table name and where condition values for updating SQL statements
     * @return When the query is successful, an Integer is returned indicating the number of rows affected. When the query fails, the string "ErrorCode: xxx..." is returned. When the parameter is incorrect, the string "Invalidate Arguments" is returned
     */
    public Object updateOne(TableMap tableMap) {
        String sql = SqlUtil.getUpdateOneContent(tableMap);
        return this.executeSql(sql);
    }
    /**
     * 使用参数更新数据库表的一条记录，使用面对对象编程
     * @param table 表名
     * @param conditionField sql 语句的 where 条件字段
     * @param conditionValue sql 语句的 where 条件字段值
     * @param args 需要更新的字段和值列表，如 column1, column1_value, column2, column2_value ...
     * @return  查询成功时返回 Integer 表示影响的行数，查询失败返回字符串 "ErrorCode:xxx ..."，参数错误时返回字符串  "Invalidate Arguments"
     */
    /**
     * Using parameters to update a record in a database table, using object-oriented programming
     * @param table Table name
     * @param conditionField Where condition fields in SQL statements
     * @param conditionValue The where condition field value of the statement
     * @param args List of fields and values that need to be updated, such as column1, column1_ Value, column2, column2_ Value
     * @return When the query is successful, an Integer is returned indicating the number of rows affected. When the query fails, the string "ErrorCode: xxx..." is returned. When the parameter is incorrect, the string "Invalidate Arguments" is returned
     */
    public Object updateOne(String table, String conditionField, Object conditionValue, Object... args) {
        String sql = SqlUtil.getUpdateContent(table, conditionField, "=", conditionValue, args);
        return this.executeSql(sql);
    }

    /**
     * 使用数据库表映射对象列表更新数据库表，使用面对对象编程，分组插入时若发生错误，则停止后续插入
     * @param list 数据库表映射对象数组，需要设置 tableMap.tableName, tableMap.primaryKey 字段值，作为更新sql语句的表名和 where 条件值
     * @param args 需要更新的字段和值列表，如 column1, column1_value, column2, column2_value ...
     * @return  查询成功时返回 Integer 表示影响的行数，查询失败返回字符串 "ErrorCode:xxx ..."，参数错误时返回字符串  "Invalidate Arguments"
     */
    /**
     * Update database tables using database table mapping object lists, and use object-oriented programming, and if an error occurs during group insertion, stop subsequent insertion
     * @param list Database table mapping object array requires setting tableMap. tableName, tableMap. primaryKey field values as table names and where condition values for updating SQL statements
     * @param args List of fields and values that need to be updated, such as column1, column1_ Value, column2, column2_ Value
     * @return When the query is successful, an Integer is returned indicating the number of rows affected. When the query fails, the string "ErrorCode: xxx..." is returned. When the parameter is incorrect, the string "Invalidate Arguments" is returned
     */
    public Object updateBatch(ArrayList<TableMap> list, Object... args) {
        if (list == null) {
            this.print("error", ".updateBatch()No update data: values = " + list);
            return INVALIDATE_ARGUMENTS;
        }
        if (list.size() == 0) {
            this.print("error", ".updateBatch()No update data: size = " + list.size());
            return INVALIDATE_ARGUMENTS;
        }

        return this.updateBatch(list.get(0).tableName, list.get(0).primaryKey, list, args);
    }

    /**
     * 使用数据库表映射对象列表更新数据库表，分组插入时若发生错误，则停止后续插入
     * @param table 表名
     * @param conditionField sql 语句的 where 条件字段
     * @param list 数据库表映射对象数组，数组中的对象必须包含表的 conditionField 与值，作为更新sql语句的 where 条件值
     * @param args 需要更新的字段和值列表，如 column1, column1_value, column2, column2_value ...
     * @return  查询成功时返回 Integer 表示影响的行数，查询失败返回字符串 "ErrorCode:xxx ..."，参数错误时返回字符串  "Invalidate Arguments"
     */
    /**
     * Update database tables using database table mapping object list, and if an error occurs during group insertion, stop subsequent insertion
     * @param table Table name
     * @param conditionField Where condition fields in SQL statements
     * @param list Database table mapping object array, the objects in the array must contain the conditionField and value of the table as the where condition value for updating SQL statements
     * @param args List of fields and values that need to be updated, such as column1, column1_ Value, column2, column2_ Value
     * @return When the query is successful, an Integer is returned indicating the number of rows affected. When the query fails, the string "ErrorCode: xxx..." is returned. When the parameter is incorrect, the string "Invalidate Arguments" is returned
     */
    public Object updateBatch(String table, String conditionField, ArrayList<TableMap> list, Object... args) {
        if (list == null) {
            this.print("error", ".updateBatch()No update data: list = " + list);
            return INVALIDATE_ARGUMENTS;
        }
        if (list.size() == 0) {
            this.print("error", ".updateBatch()No update data: list.size() = " + list.size());
            return INVALIDATE_ARGUMENTS;
        }

        int length = list.size();
        Object[] conditionValues = new Object[length];
        for (int i = 0; i < length; i++) {
            conditionValues[i] = list.get(i).get(conditionField);
        }

        return this.updateBatch(table, conditionField, conditionValues, args);
    }

    //

    /**
     * 使用参数更新数据库表，分组插入时若发生错误，则停止后续插入
     * @param table 表名
     * @param conditionField sql 语句的 where 条件字段
     * @param conditionValues sql 语句的 where 条件字段值列表
     * @param args 需要更新的字段和值列表，如 column1, column1_value, column2, column2_value ...
     * @return  查询成功时返回 Integer 表示影响的行数，查询失败返回字符串 "ErrorCode:xxx ..."，参数错误时返回字符串  "Invalidate Arguments"
     */
    /**
     * Updating database tables with parameters, and if an error occurs during group insertion, stop subsequent insertion
     * @param table Table name
     * @param conditionField Where condition fields in SQL statements
     * @param conditionValues Where conditional field value list for SQL statements
     * @param args List of fields and values that need to be updated, such as column1, column1_ Value, column2, column2_ Value
     * @return When the query is successful, an Integer is returned indicating the number of rows affected. When the query fails, the string "ErrorCode: xxx..." is returned. When the parameter is incorrect, the string "Invalidate Arguments" is returned
     */
    public Object updateBatch(String table, String conditionField, Object[] conditionValues, Object... args) {
        int size = args.length;

        if (size == 0) {
            this.print("error", ".updateBatch()No update data: size = " + size);
            return INVALIDATE_ARGUMENTS;
        }

        //估算值，并非准确值，假定用于更新的字段名称最长为64位，另外计算空格和逗号
        int rowLength = table.length() + conditionField.length()
                + (FIELD_NAME_LENGTH + args[1].toString().length()) * size
                + FIELD_NAME_LENGTH * conditionValues.length + SQL_DEFAULT_LENGTH;//为语句本身的长度，另外计算空格和括号
        int count = (int) Math.ceil((double) rowLength / maxSqlLength);

        Object result = 0;
        int resultSuccess = 0;
        String sql;

        //超过最大数量时，拆分插入
        if (count > 1) {
            size = conditionValues.length;
            int subCount = (int) Math.ceil((double) size / count);
            count = (int) Math.ceil((double) size / subCount);

            Object[] subList;

            int index = 0;
            int i = 0;

            while (index < size && subCount > 0) {
                subList = Arrays.copyOfRange(conditionValues, index, index + subCount);

                sql = SqlUtil.getUpdateBatchContent(table, conditionField, subList, args);

                i++;
                this.printBatchLog("updateBatch()", subCount, size, sql.length(), i, count);

                result = executeSql(sql);
                if(result instanceof Integer){
                    resultSuccess += (int)result;
                    result = resultSuccess;
                }else {
                    break;
                }

                index += subCount;
                subCount = Math.min(subCount, size - index);
            }
        } else {
            sql = SqlUtil.getUpdateBatchContent(table, conditionField, conditionValues, args);

            this.printBatchLog("updateBatch()", conditionValues.length, conditionValues.length, sql.length(), 1, 1);

            result = executeSql(sql);
        }

        return result;
    }
    //

    /**
     * 查询数据
     *
     * @param sql 查询语句
     * @return 查询成功返回 ResultSet 数据集，需要手动关闭此集合，查询失败返回字符串 "ErrorCode:xxx ..."
     * @see #closeResultSet(ResultSet)
     */
    /**
     * query data
     * @param sql SQL Query Statement
     * @return The query successfully returns the ResultSet dataset, which needs to be manually closed. If the query fails, the string 'ErrorCode: xxx...' will be returned
     * @see #closeResultSet(ResultSet)
     */
    public Object select(String sql) {
        if(!this.isInit){
            this.print("error", SQL_SERVICE_NOT_INITIALIZED);
            return SQL_SERVICE_NOT_INITIALIZED;
        }

        Object result;
        ResultSet resultSet;
        try {
            final PreparedStatement preStatement = connection.prepareStatement(sql);
            this.setSqlParamsToStatement(preStatement);
            resultSet = preStatement.executeQuery();
            result = resultSet;

            if (isPrintLog || sql.length() < SQL_LENGTH_PRINT)
                this.print("info", ".select()Query database succeeded：sql.length() = " + sql.length() + ", sql = " + sql);
        } catch (SQLException e) {
            if (isPrintLog || sql.length() < SQL_LENGTH_PRINT)
                this.print("error", ".select()Query database error：sql.length() = " + sql.length() + ", sql = " + sql);
            e.printStackTrace();
            result = this.getResult(e);
        }

        return result;
    }

    /**
     * 查询数据
     * @param sql 查询语句
     * @param mapClass 数据表映射类，一条记录即一个映射类实例，其他数据库的TableMap实例的 tableName 可能不准确，
     *      除mysql/mariadb外，其他数据库不支持从 ResultSetMetaData 获取表名，目前从sql语句获取，若sql语句存在错误，tableName 可能不准确
     * @return 查询成功返回 ArrayList<mapClass>，查询失败返回字符串 "ErrorCode:xxx ..."
     */
    /**
     * query data
     * @param sql SQL Query Statement
     * @param mapClass A data table mapping class, where one record is an instance of a mapping class, may have inaccurate tableNames for TableMap instances in other databases,
     * Except for MySQL/mariadb, other databases do not support obtaining table names from ResultSetMetaData. Currently, obtaining table names from SQL statements may result in inaccurate tableNames if there are errors in the SQL statements
     * @return A successful query returns ArrayList<mapClass>, while a failed query returns the string 'ErrorCode: xxx...'
     */
    public Object select(String sql, Class mapClass) {
        Object result = this.select(sql);

        ResultSet resultSet;
        if(result instanceof ResultSet){
            resultSet = (ResultSet)result;
        }else {
            return result;
        }

        ArrayList<TableMap> list = new ArrayList<>();
        try {
            final ResultSetMetaData metaData = resultSet.getMetaData();

            String tableName = metaData.getTableName(1);
            //除mysql/mariadb外，其他数据库不支持从 ResultSetMetaData 获取表名，目前从sql语句获取，若sql语句存在错误，tableName 可能不准确
            if("".equals(tableName) || tableName == null){
                Pattern p = Pattern.compile(".*\\s+from\\s+\\w*?\\.?(\\w+).*",
                        Pattern.CASE_INSENSITIVE);
                Matcher m = p.matcher(sql);
                if (m.matches())
                    tableName = m.group(1).toLowerCase();
            }
            final int count = metaData.getColumnCount();

            while (resultSet.next()) {
                TableMap tableMap = (TableMap) mapClass.getDeclaredConstructor().newInstance();
                tableMap.tableName = tableName;
                for (int i = 1; i <= count; i++) {
                    String columnName = metaData.getColumnName(i);
                    tableMap.set(columnName, resultSet.getObject(i));
                }

                list.add(tableMap);
            }

            result = list;
        } catch (SQLException | InstantiationException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
            this.print("error", ".select()Query database error：" + e);
            e.printStackTrace();

            if(e instanceof SQLException){
                result = this.getResult((SQLException)e);
            }
        }finally {
            this.closeResultSet(resultSet);
        }

        return result;
    }
    //
    /**
     * 查询数据
     *
     * @param sql 查询语句
     * @param mapClass 数据表映射类，一条记录即一个映射类实例，除mysql/mariadb外，其他数据库的TableMap实例的 tableName 可能不准确，
     *                 除mysql/mariadb外，其他数据库不支持从 ResultSetMetaData 获取表名，从sql语句获取，若sql语句存在错误，tableName 可能不准确
     * @return 返回 null 为 sql 操作错误，否则返回 ArrayList，无数据时 ArrayList.size = 0
     */
    /**
     * query data
     * @param sql SQL Query Statement
     * @param mapClass A data table mapping class, where one record is an instance of a mapping class, may have inaccurate tableNames for TableMap instances in other databases,
     * Except for MySQL/mariadb, other databases do not support obtaining table names from ResultSetMetaData. Currently, obtaining table names from SQL statements may result in inaccurate tableNames if there are errors in the SQL statements
     * @return Returns null as an SQL operation error, otherwise returns ArrayList. When there is no data, ArrayList. size=0
     */
    public ArrayList<TableMap> select2(String sql, Class mapClass) {
        Object result = this.select(sql, mapClass);
        if(result instanceof ArrayList<?>){
            return (ArrayList<TableMap>)result;
        }

        return null;
    }
    //
    /**
     * 查询一条数据
     *
     * @param sql 查询语句，语句应包含 limit 1 等单条查询限制关键字，否则在返回的数据中只取第一条数据返回
     * @param mapClass 数据表映射类，一条记录即一个映射类实例，除mysql/mariadb外，其他数据库的TableMap实例的 tableName 可能不准确，
     *                 除mysql/mariadb外，其他数据库不支持从 ResultSetMetaData 获取表名，从sql语句获取，若sql语句存在错误，tableName 可能不准确
     * @return 查询成功时返回 mapClass 对象实例，无数据返回 null，查询失败返回字符串 "ErrorCode:xxx ..."，参数错误时返回字符串  "Invalidate Arguments"
     */
    /**
     * Query a piece of data
     * @param sql An SQL query statement should contain a single query restriction keyword such as limit 1, otherwise only the first data will be returned in the returned data
     * @param mapClass A data table mapping class, where one record is an instance of a mapping class, may have inaccurate tableNames for TableMap instances in other databases,
     * Except for MySQL/mariadb, other databases do not support obtaining table names from ResultSetMetaData. Currently, obtaining table names from SQL statements may result in inaccurate tableNames if there are errors in the SQL statements
     * @return When the query is successful, a mapClass object instance is returned. If there is no data, null is returned. If the query fails, the string "ErrorCode: xxx..." is returned. If the parameter is incorrect, the string "Invalidate Arguments" is returned
     */
    public Object selectOne(String sql, Class mapClass) {
        Object result = this.select(sql, mapClass);

        if(result instanceof ArrayList){
            return SqlServiceUtil.getFirst(result);
        }

        return result;
    }

    //
    private void printBatchLog(String name, int count, int size, int length, int index, int total) {
        this.print("info", "." + name + " progress: count/total = " + count + "/" + size +
                ", sql length = " + length + ", index/total = " + index + "/" + total);

        this.batchPercent = (float)index / total;
    }

    //
    private void print(String type, String content){
        logDate.setTime(System.currentTimeMillis());
        System.out.println("[" + type.toUpperCase() + "] " + logFormat.format(logDate) + " " + this.toString() + content);
    }
}
