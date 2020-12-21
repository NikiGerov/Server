package application;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class MessagesController implements Initializable {
	
	private User currentUser;
	private User senderUser;
	
	private UserData userData;
	
	private List<User> msgs;
	
	@FXML
	private ListView<User> listUsers;
	@FXML
	private Label label;
	@FXML
	private Button btnBack;
	@FXML
	private TextArea txtMsgs; 
	
	Connection connection;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		listUsers.setOnMouseClicked(event -> {
			senderUser = listUsers.getSelectionModel().getSelectedItem();
			
			if(senderUser!=null) {
				
				String chatContent = "";
				List<String> listOfMsgs = new ArrayList<>();
				userData = new UserData(connection);
				
				try {
					listOfMsgs = userData.getUnreadMessagesContent(currentUser, senderUser);
				} catch (SQLException e) {
					//log the ex
					e.printStackTrace();
				}
				for(String msg : listOfMsgs)
				{
					chatContent = chatContent + senderUser.getUserName() + ": " + msg + "\n";
				}
				
				txtMsgs.setText(chatContent);
				
				
			}
		});
	}
	
	public void initUsersSentMsgs(User user1, Connection pConnection) {
		connection = pConnection;
		this.currentUser=user1;
		try {
			userData = new UserData(connection);
			msgs = userData.getUnreadMessagesSender(currentUser);
			for(User user : msgs) {
				listUsers.getItems().add(user);
			}
		} catch (SQLException e) {
			e.printStackTrace();
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
	
	public void goBack(ActionEvent event) throws IOException{
		((Stage)(((Button)event.getSource()).getScene().getWindow())).close();
	}
}