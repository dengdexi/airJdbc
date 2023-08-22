package tableMaps.sqlserver;

import sql.airJdbc.data.TableMap;

/**
 * Data from table: sqlserverdb.sysdiagrams
 * Create by sql.airJdbc.SqlService
 * At date: 2023-08-22 12:00:41
 * @see sql.airJdbc.service.SqlService#exportTableData(String, String, String, boolean)
 */
public class SysdiagramsMap extends TableMap {
    /**
     * Column names in table
     */
    /** nvarchar(128) **/
    public static String NAME = "name";
    /** integer(10) **/
    public static String PRINCIPAL_ID = "principal_id";
    /** integer(10) **/
    public static String DIAGRAM_ID = "diagram_id";
    /** integer(10) **/
    public static String VERSION = "version";
    /** varbinary(2147483647) **/
    public static String DEFINITION = "definition";

    public SysdiagramsMap(){
        super();
        this.primaryKey = "diagram_id";
    }
}
