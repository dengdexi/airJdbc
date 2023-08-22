package sql.airJdbc.service;

public enum DatabaseName {
    MARIADB("MariaDb"),
    MYSQL("MySql"),
    ORACLE("Oracle"),
    SQL_SERVER("SQL server"),
    POSTGRE_SQL("Postger sql");

    private final String value;
    DatabaseName(String value){
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
