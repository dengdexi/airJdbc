package tableMaps.oracle;

import sql.airJdbc.data.TableMap;

/**
 * Data from table: ORAGLEDBS.USER_LOGIN
 * Create by sql.airJdbc.SqlService
 * At date: 2023-08-21 16:23:11
 * @see sql.airJdbc.service.SqlService#exportTableData(String, String, String, boolean)
 */
public class UserLoginMap extends TableMap {
    /**
     * Column names in table
     */
    /** numeric(10) **/
    public static String ID = "ID";
    /** varchar(255) **/
    public static String USER_NAME = "USER_NAME";
    /** numeric(10) **/
    public static String LOGIN_COUNT = "LOGIN_COUNT";
    /** timestamp(7) **/
    public static String CREATE_DATE = "CREATE_DATE";
    /** blob(4000) **/
    public static String VAR_CONTENT = "VAR_CONTENT";

    public UserLoginMap(){
        super();
        this.primaryKey = "ID";
    }
}
