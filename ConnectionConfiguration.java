
import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectionConfiguration {

    public static Connection getConnection() {
        Connection connection = null;

        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/skypedb", "root", "root");
//            connection = DriverManager.getConnection("jdbc:mysql://25.61.184.100/skypedb", "root", "root");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return connection;
    }

}