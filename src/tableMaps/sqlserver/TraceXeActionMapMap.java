package tableMaps.sqlserver;

import sql.airJdbc.data.TableMap;

/**
 * Data from table: sqlserverdb.trace_xe_action_map
 * Create by sql.airJdbc.SqlService
 * At date: 2023-08-22 12:00:41
 * @see sql.airJdbc.service.SqlService#exportTableData(String, String, String, boolean)
 */
public class TraceXeActionMapMap extends TableMap {
    /**
     * Column names in table
     */
    /** smallint(5) **/
    public static String TRACE_COLUMN_ID = "trace_column_id";
    /** nvarchar(60) **/
    public static String PACKAGE_NAME = "package_name";
    /** nvarchar(60) **/
    public static String XE_ACTION_NAME = "xe_action_name";

    public TraceXeActionMapMap(){
        super();
        this.primaryKey = "";
    }
}
