package tableMaps.sqlserver;

import sql.airJdbc.data.TableMap;

/**
 * Data from table: sqlserverdb.trace_xe_event_map
 * Create by sql.airJdbc.SqlService
 * At date: 2023-08-22 12:00:41
 * @see sql.airJdbc.service.SqlService#exportTableData(String, String, String, boolean)
 */
public class TraceXeEventMapMap extends TableMap {
    /**
     * Column names in table
     */
    /** smallint(5) **/
    public static String TRACE_EVENT_ID = "trace_event_id";
    /** nvarchar(60) **/
    public static String PACKAGE_NAME = "package_name";
    /** nvarchar(60) **/
    public static String XE_EVENT_NAME = "xe_event_name";

    public TraceXeEventMapMap(){
        super();
        this.primaryKey = "";
    }
}
