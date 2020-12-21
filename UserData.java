package application;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserData {
	private Connection connection;
	private String GET_USER = "SELECT * FROM user WHERE name = ?";
	private String ADD_FRIEND = "INSERT INTO friends_list(sender_id,receiver_id,accepted) VALUES (?,?,0)";
	private String REQ_QUERY = "SELECT user.* FROM friends_list JOIN user ON user.user_id = friends_list.sender_id WHERE friends_list.receiver_id=? AND accepted=0";
	
//	private String UNREAD_MSGS_QUERY = "SELECT user.* FROM messages JOIN user ON user.user_id = messages.receiver_id WHERE messages.receiver_id=? AND delivered=0";
	
	private String UNREAD_MSGS_SENDER_ID_QUERY = "SELECT sender_id FROM messages WHERE messages.receiver_id=? AND delivered=0";
	private String UNREAD_MSGS_CONTENT_QUERY = "SELECT content FROM messages WHERE messages.sender_id=? AND messages.receiver_id=? AND delivered=0";
	
//	private String GET_FRIENDS = "SELECT user.* FROM friends_list JOIN user ON user.user_id = friends_list.sender_id WHERE (friends_list.sender_id=? OR friends_list.receiver_id=?) AND accepted=1";
	private String GET_FRIENDS = "SELECT sender_id, receiver_id FROM friends_list WHERE (friends_list.sender_id=? OR friends_list.receiver_id=?) AND accepted=1";
	private String GET_USER_BY_ID = "SELECT user_id, name FROM user WHERE user_id = ?";
	
	private String ACC_FRIEND = "UPDATE friends_list SET accepted=1 WHERE sender_id=? AND receiver_id=?";
	private String DEC_FRIEND = "DELETE FROM friends_list WHERE sender_id=? AND receiver_id=?";
	
	private String MARK_DELIVERED_MSGS = "UPDATE messages SET delivered=1 WHERE sender_id=? AND receiver_id=?";
	
	private PreparedStatement prepStmt;
	private ResultSet resultSet;
	User user = null;
	
	public UserData() {
		connection = ConnectionConfiguration.getConnection();
	}
	
	public UserData(Connection conn) {
		connection = conn;
	}
	
	public User getUser(String username) throws SQLException {
		prepStmt = connection.prepareStatement(GET_USER);
		prepStmt.setString(1, username);
		resultSet = prepStmt.executeQuery();
		if(resultSet.next()) {
			user = new User(resultSet.getInt("user_id"), 
					resultSet.getString("name"), resultSet.getString("password"));
		}
		return user;
	}
	
	public User getUserById(Integer id) throws SQLException {
		prepStmt = connection.prepareStatement(GET_USER_BY_ID);
		prepStmt.setInt(1, id);
		resultSet = prepStmt.executeQuery();
		if(resultSet.next()) {
			user = new User(resultSet.getInt("user_id"), 
					resultSet.getString("name"));
		}
		return user;
	}
	
	public void sendFriendRequest(User sender, User receiver) throws SQLException {
		prepStmt = connection.prepareStatement(ADD_FRIEND);
		prepStmt.setInt(1, sender.getId());
		prepStmt.setInt(2, receiver.getId());
		prepStmt.execute();
	}
	
	public List<User> getPendingRequests(User user) throws SQLException{
		List<User> pendingRequests = new ArrayList<User>();
		prepStmt = connection.prepareStatement(REQ_QUERY);
		prepStmt.setInt(1, user.getId());
		resultSet = prepStmt.executeQuery();
		
		while(resultSet.next()) {
			pendingRequests.add(new User(resultSet.getInt("user_id"), 
					resultSet.getString("name")));
		}
		
		return pendingRequests;
	}
	
	
	public List<User> getUnreadMessagesSender(User user) throws SQLException{
		List<User> unreadMessages = new ArrayList<User>();
		prepStmt = connection.prepareStatement(UNREAD_MSGS_SENDER_ID_QUERY);
		prepStmt.setInt(1, user.getId());
		resultSet = prepStmt.executeQuery();
		
		Set<Integer> userIdsList = new HashSet<>();
		
		while(resultSet.next()) {
			userIdsList.add(resultSet.getInt("sender_id"));
		}
		
		for (Integer id : userIdsList)
		{
			User result = getUserById(id);
			unreadMessages.add(result);
		}
		
		return unreadMessages;
	}
	
	public List<String> getUnreadMessagesContent(User receiver, User sender) throws SQLException{
		List<String> unreadMessages = new ArrayList<>();
		prepStmt = connection.prepareStatement(UNREAD_MSGS_CONTENT_QUERY);
		prepStmt.setInt(1, sender.getId());
		prepStmt.setInt(2, receiver.getId());
		resultSet = prepStmt.executeQuery();
		
		while(resultSet.next()) {
			unreadMessages.add(resultSet.getString("content"));
		}
		
		prepStmt = connection.prepareStatement(MARK_DELIVERED_MSGS);
		prepStmt.setInt(1, sender.getId());
		prepStmt.setInt(2, receiver.getId());
		prepStmt.execute();
		
		return unreadMessages;
	}
	
//	public List<User> getFriends(User user) throws SQLException{
//		List<User> friendsList = new ArrayList<User>();
//		prepStmt = connection.prepareStatement(GET_FRIENDS);
//		prepStmt.setInt(1, user.getId());
//		prepStmt.setInt(2, user.getId());
//		resultSet = prepStmt.executeQuery();
//		
//		while(resultSet.next()) {
//			friendsList.add(new User(resultSet.getInt("user_id"), 
//					resultSet.getString("name"), resultSet.getString("password")));
//		}
//		
//		return friendsList;
//	}
	
	public List<User> getFriends(User user) throws SQLException{
		List<User> friendsList = new ArrayList<User>();
		prepStmt = connection.prepareStatement(GET_FRIENDS);
		int userId = user.getId();
		prepStmt.setInt(1, userId);
		prepStmt.setInt(2, userId);
		resultSet = prepStmt.executeQuery();
		
		Set<Integer> userIdsList = new HashSet<>();
		
		while(resultSet.next()) {
			
			if(resultSet.getInt("sender_id") == userId)
			{
				userIdsList.add(resultSet.getInt("receiver_id"));
			}
			else
			{
				userIdsList.add(resultSet.getInt("sender_id"));
			}
		}
		
		for (Integer id : userIdsList)
		{
			User result = getUserById(id);
			friendsList.add(result);
		}
		
		
		return friendsList;
	}
	
	public void replyRequest(User sender, User receiver, boolean status) throws SQLException {
		if(status) {
			prepStmt = connection.prepareStatement(ACC_FRIEND);			
		} else {
			prepStmt = connection.prepareStatement(DEC_FRIEND);
		}
		prepStmt.setInt(1, sender.getId());
		prepStmt.setInt(2, receiver.getId());
		prepStmt.execute();
	}
	
}
