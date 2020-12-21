package application;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import org.jasypt.util.password.StrongPasswordEncryptor;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController implements Initializable {

	@FXML
	private TextField txtUsername;
	
	@FXML
	private PasswordField txtPassword;
	 
	@FXML
	private Button btnRegister;
	
	@FXML
	private TextField txtLoginUsername;
	
	@FXML
	private PasswordField txtLoginPassword;
	 
	@FXML
	private Button btnLogin;
	
	Connection connection = null;
	PreparedStatement preparedStatement = null;
	ResultSet resultSet = null;
	
	public LoginController() {
		connection = ConnectionConfiguration.getConnection();
	}
	
	private String encryptPassword(String password) {
		StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
		return passwordEncryptor.encryptPassword(password);
	}
	
	public void register(ActionEvent event)
	{
		String username = txtUsername.getText().toString();
		String password = encryptPassword(txtPassword.getText().toString());		
						
		if(username.length() > 0 && password.length() > 0)
		{
			try {
				String sql = "INSERT INTO user (name, password) VALUES (?, ?)";
				PreparedStatement prepStmt = connection.prepareStatement(sql);
				prepStmt.setString(1, username);
				prepStmt.setString(2, password);
				
				prepStmt.execute();
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
			infoBox("Your account has been created", null, "Success!");
			txtUsername.clear();
			txtPassword.clear();
			
		}
		else 
		{
			infoBox("Fill in the fields", null, "Try again!");
		}
	}
	
	private boolean isMatch(User entryUser, User dbUser) {
		StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();		
		if(entryUser.getUserName().equals(dbUser.getUserName()) 
				&& passwordEncryptor.checkPassword(entryUser.getPassword(), dbUser.getPassword())) {
			return true;
		} else
			return false;
	}
	
	public void login(ActionEvent event) throws IOException, SQLException
	{
		User entryUser, dbUser;
		String username = txtLoginUsername.getText().toString();
		String password = txtLoginPassword.getText().toString();
		entryUser = new User(username, password);

		String sql = "SELECT * FROM user WHERE name = ?";
		PreparedStatement prepStmt = connection.prepareStatement(sql);
		prepStmt.setString(1, username);

		ResultSet resultSet = prepStmt.executeQuery();
		
		if (!resultSet.next()) {
			infoBox("Please enter correct username and password", null, "Failed!");
		} else {
			dbUser = new User(resultSet.getInt("user_id"), resultSet.getString("name"), resultSet.getString("password"));
//			System.out.println("entryUser: " + entryUser);
//			System.out.println("dbUser: " + dbUser);
			if(isMatch(entryUser, dbUser)) {
				FXMLLoader loader = new FXMLLoader();
				loader.setLocation(getClass().getResource("/gui/main.fxml"));
				Parent parent = loader.load();
	            
				Scene scene = new Scene(parent);
				
				MainScreenController mainScreenController = loader.getController();
				mainScreenController.initUser(username);
				mainScreenController.initUser(dbUser);
				mainScreenController.initConnention();
				
				Stage newStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
				newStage.setScene(scene);
				newStage.show();
			}
			else
			{
				infoBox("Please enter correct username and password", null, "Failed!");
			}
		}

	}
	
	
	public static void infoBox(String infoMessagem, String headerText, String title)
	{
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setContentText(infoMessagem);
		alert.setTitle(title);
		alert.setHeaderText(headerText);
		alert.showAndWait();
	}
	
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

	}

}