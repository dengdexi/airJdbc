package sql.airJdbc.template;

import sql.airJdbc.data.TableMap;

/**
 * Data from table: %table%
 * Create by sql.airJdbc.SqlService
 * At date: 2023-08-21 15:30:45
 * @see sql.airJdbc.service.SqlService#exportTableData(String, String, String, boolean)
 */
public class TableClassStaticTemplate extends TableMap {
    /**
     * Column names in table
     */
    /** String **/
    public static String ID = "id";

    public TableClassStaticTemplate(){
        super();
        this.primaryKey = "primaryKey";
    }
}
