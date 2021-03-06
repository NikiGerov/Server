import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

public class DbUtils {
	
	private PreparedStatement prepStmt;
//	private ResultSet resultSet;
	private Connection connection;
	
	private final String ADD_MSG = "INSERT INTO messages(sender_id,receiver_id,delivered, content, date) VALUES (?,?,?,?,?)";

	public DbUtils(Connection conn) {
		connection = conn;
	}
	
	public void sendMsg(Long sender, Long receiver, Integer delivered, String content) throws SQLException {
		prepStmt = connection.prepareStatement(ADD_MSG);
		prepStmt.setLong(1, sender);
		prepStmt.setLong(2, receiver);
		prepStmt.setInt(3, delivered);
		prepStmt.setString(4, content);
		prepStmt.setTimestamp(5, new Timestamp(new Date().getTime()));
		prepStmt.execute();
	}
}
