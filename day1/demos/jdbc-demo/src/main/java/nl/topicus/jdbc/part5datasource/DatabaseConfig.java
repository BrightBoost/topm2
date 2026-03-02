package nl.topicus.jdbc.part5datasource;

import org.h2.jdbcx.JdbcDataSource;
import javax.sql.DataSource;

/**
 * In a real application, this would be replaced by a connection pool like HikariCP.
 * Here we use H2's built-in DataSource for simplicity.
 */
public class DatabaseConfig {
    public static DataSource createDataSource() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:file:./data/jdbcdemo");
        ds.setUser("sa");
        ds.setPassword("");
        return ds;
    }
}
