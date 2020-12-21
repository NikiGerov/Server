package application;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
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
import javafx.stage.Stage;

public class FriendsController implements Initializable {
	
	private User currentUser;
	private User senderUser;
	
	private UserData userData;
	
	private List<User> requests;
	
	@FXML
	private ListView<User> listRequests;
	@FXML
	private Button btnAccept;
	@FXML
	private Button btnDecline;
	@FXML
	private Label label;
	@FXML
	private Button btnBack;
	
	private int selectedIndex;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		listRequests.setOnMouseClicked(event -> {
			senderUser = listRequests.getSelectionModel().getSelectedItem();
			if(senderUser!=null) {
				label.setText(senderUser.getUserName() + " has sent you a friend request.");
				selectedIndex = listRequests.getSelectionModel().getSelectedIndex();
			}
		});
	}
	
	public void initUser(User user1, Connection pConnection) {
		this.currentUser=user1;
		try {
			userData = new UserData(pConnection);
			requests = userData.getPendingRequests(currentUser);
			for(User user : requests) {
				listRequests.getItems().add(user);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void acceptFriend(ActionEvent event) throws IOException, SQLException{
		userData.replyRequest(senderUser, currentUser, true);
		infoBox("Accepted " + senderUser.getUserName() + "'s friend request.", null, "Accepted");
		listRequests.getItems().remove(selectedIndex);
	}
	
	public void declineFriend(ActionEvent event) throws IOException, SQLException{
		userData.replyRequest(senderUser, currentUser, false);
		infoBox("Declined " + senderUser.getUserName() + "'s friend request.", null, "Declined");
		listRequests.getItems().remove(selectedIndex);
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