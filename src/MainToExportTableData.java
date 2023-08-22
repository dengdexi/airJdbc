import sql.airJdbc.service.SqlService;

public class MainToExportTableData {
    public static void main(String[] args){
        System.out.println("MainToExportTableData start");

        //mysql
        SqlService.getInstance().init(
                "com.mysql.jdbc.Driver",
                "jdbc:mysql://localhost:3306/opendb2?allowMultiQueries=true",
                "root",
                "root8@my");

        SqlService.getInstance().exportTableData("src/","src/tableMaps/myTables/", "Map", false);
    }
}
