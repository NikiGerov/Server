package application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class MainScreenController implements Initializable {

	//private static final String SERVER_HOST = "25.61.184.100";
	private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 6543;
    private static final Logger LOGGER = Logger.getLogger(MainScreenController.class.getName());
	
    private Socket socket;
	private BufferedReader inFromServer;
	private PrintWriter outToServer;
	private Scanner scanner;
	
	private String user;
	private User userObj;
	
	private String serverReply;
	
	private String chatText = "";
	
	@FXML
	private Text txtLoggedInAs;
	
	@FXML
	private Text txtChattingWith;
	
	//@FXML
	//private ListView<String> listUsers;
	
	@FXML
	private ListView<User> listUsers;
	
	@FXML
	private Button btnOpenChat;
	
	@FXML
	private Button btnLoadUsers;
	
	@FXML
	private TextArea txtChat; 
	
	@FXML 
	private TextField txtMsg;
	
	@FXML
	private Label userNameLabel;
	
	@FXML
	private TextField txtAddFriend;
	
	@FXML
	private Button btnAddFriend;	
	
	@FXML
	private Button btnCheckRequests;
	
	@FXML
	private Button btnCheckMsgs;
	
	private String selectedUser;	
	
	Connection connection = null;
//	PreparedStatement preparedStatement = null;
//	ResultSet resultSet = null;
	
	public MainScreenController() throws UnknownHostException, IOException {
		connection = ConnectionConfiguration.getConnection();
	}
	
	public void connect(ActionEvent event) throws IOException
	{
		initData(selectedUser, socket, inFromServer, outToServer, scanner);
		txtChattingWith.setText("You are chatting with: " + selectedUser);
		outToServer.println("CONNECTTO" + " " + selectedUser);
		
	}
	
	public void exit(ActionEvent event) throws IOException
	{
		outToServer.println("CLOSE" + " " + user);
		Platform.exit();
		
	}
	
	public void send(ActionEvent event) throws IOException, SQLException
	{
		
		String msg = txtMsg.getText().toString();
		
		if(msg.length()>0)
		{
			UserData userData = new UserData(connection);
			
			outToServer.println(Command.MSG + " " + userData.getUser(selectedUser).getId() + " " + user + ": " + msg);
			
			
			chatText = chatText + "You: " + msg + "\n";
			txtChat.setText(chatText);
			txtMsg.clear();
		}
		
	}
	
	private void initData(String chatWith, Socket socket, BufferedReader inFromServer, PrintWriter outToServer, Scanner scanner)
	{
		this.socket = socket;
		this.inFromServer = inFromServer;
		this.outToServer = outToServer;
		this.scanner = scanner;
		
		selectedUser = chatWith;
	}
	
	public void loadUsers(ActionEvent event) throws IOException
	{
		/*outToServer.println(Command.LOADUSERS);
		List<String> users = new ArrayList<>();
		if(serverReply != null)
		{
			char[] chars = serverReply.toCharArray();
			if(chars.length != 0 && chars[0] == 'L')
			{
				String[] msg = serverReply.split("/");
				for( int i = 1; i < msg.length; i++)
				{
					users.add(msg[i]);
				}
				
			}
			
			ObservableList<String> items = FXCollections.observableArrayList();
			items.addAll(users);
			listUsers.setItems(items);*/
		
		try {
			listUsers.getItems().clear();
			UserData userData = new UserData(connection);
			List<User> friendsList = new ArrayList<User>();
			friendsList = userData.getFriends(userObj);
			for(User user : friendsList) {
				listUsers.getItems().add(user);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
			
		
	}
	
	public void initUser(String user)
	{
		this.user = user;
		//userNameLabel.setText(user);
	}
	
	public void initUser(User user) {
		this.userObj = user;
		userNameLabel.setText(userObj.getUserName());
	}
	
	public void initConnention() throws UnknownHostException, IOException, SQLException
	{
		socket = new Socket(SERVER_HOST, SERVER_PORT);
		inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		outToServer = new PrintWriter(socket.getOutputStream(), true);

		LOGGER.log(Level.INFO, "Connected to the server.");
		
		UserData u = new UserData(connection);
		User userObj = u.getUser(user);

		outToServer.println(user + " " + userObj.getId());

		
//		new ReaderThread2(inFromServer, serverReply, txtChat, chatText).run();
		
		Thread th = new Thread(new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				while (true) {
					try {
						String reply = inFromServer.readLine();
						serverReply = reply;
						
						if(reply.startsWith("MSG"))
						{
							String[] arr = reply.split(" ");
							
							String command = arr[0];
							String id = arr[1];
							
							reply = reply.substring(command.length() + id.length() + 2, reply.length());
							
							chatText = chatText + reply + "\n";
							txtChat.setText(chatText);
						}
						
					} catch (final IOException e) {
						LOGGER.log(Level.INFO, "Error occured while reading server response");
						LOGGER.log(Level.INFO, e.getMessage(), e);
						return null;
					}
				}
			}

		});
		th.setDaemon(true);
		th.start();

	}
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		
		listUsers.setOnMouseClicked(event -> {
			User senderUser = listUsers.getSelectionModel().getSelectedItem();
			if (senderUser != null)
			{
				selectedUser = senderUser.getUserName();
				btnOpenChat.setDisable(false);
			}
		});

	}
	
	public void addFriend(ActionEvent event) throws IOException, SQLException{
		String username = txtAddFriend.getText();
		
		UserData userData = new UserData(connection);
		User receiver = userData.getUser(username);
		
		if (receiver!=null) {
			userData.sendFriendRequest(userObj, receiver);
			infoBox("Friend request sent to " + username, null, "Success!");
		} else {
			infoBox("No user under the name " + username + " could be found.", null, "Failed!");
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
	
	public void checkRequests(ActionEvent event) throws IOException, SQLException{
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getResource("/gui/friends.fxml"));
		
		Scene scene = new Scene(loader.load());

		FriendsController friendsController = loader.getController();
		friendsController.initUser(userObj, connection);
		
		Stage newStage = new Stage();
		newStage.setScene(scene);
		newStage.show();
	}
	
	public void checkMsgs(ActionEvent event) throws IOException, SQLException{
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getResource("/gui/messages.fxml"));
		
		Scene scene = new Scene(loader.load());

		MessagesController controller = loader.getController();
		controller.initUsersSentMsgs(userObj, connection);
		
		Stage newStage = new Stage();
		newStage.setScene(scene);
		newStage.show();
	}
	
}