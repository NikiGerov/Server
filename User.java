package application;

public class User {
	private int id;
	private String userName;
	private String password;
	
	public User() {}
	
	public User(int id, String userName, String password) {
		this.id=id;
		this.userName=userName;
		this.password=password;
	}
	
	public User(int id, String userName) {
		this.id=id;
		this.userName=userName;
	}
	
	public User(String userName, String password) {
		this.userName=userName;
		this.password=password;
	}
	
	public int getId() {
		return id;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	@Override
	public String toString() {
		return this.userName;
	}
}
