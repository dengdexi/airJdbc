package tableMaps.sqlserver;

import sql.airJdbc.data.TableMap;

/**
 * Data from table: sqlserverdb.user_login
 * Create by sql.airJdbc.SqlService
 * At date: 2023-08-22 12:00:41
 * @see sql.airJdbc.service.SqlService#exportTableData(String, String, String, boolean)
 */
public class UserLoginMap extends TableMap {
    /**
     * Column names in table
     */
    /** bigint(19) **/
    public static String ID = "id";
    /** varchar(50) **/
    public static String USER_NAME = "user_name";
    /** integer(10) **/
    public static String LOGIN_COUNT = "login_count";
    /** timestamp(23) **/
    public static String CREATE_DATE = "create_date";
    /** varbinary(2147483647) **/
    public static String VAR_CONTENT = "var_content";

    public UserLoginMap(){
        super();
        this.primaryKey = "id";
    }
}
