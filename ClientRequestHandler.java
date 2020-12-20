

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Class for handling requests for an individual client
 *
 */
public final class ClientRequestHandler extends Thread {

    private final Map<ClientBean, ClientRequestHandler> clients;
    private final Long id;
    private final PrintWriter outToClient;
    private final BufferedReader inFromClient;
    private static final Logger LOGGER = Logger.getLogger(ClientRequestHandler.class.getName());
    private ClientBean clientConnectedTo = null;
    
    private Connection connection = ConnectionConfiguration.getConnection();
    
    public ClientRequestHandler(final Map<ClientBean, ClientRequestHandler> clients, final Socket socket, final Long id) 
    		throws IOException {
        this.clients = clients;
        this.id = id;
        outToClient = new PrintWriter(socket.getOutputStream(), true);
        inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    @Override
    public void run() {

        String inputLine;
        try {
            while ((inputLine = inFromClient.readLine()) != null) {
            	
            	final String currentCommand = inputLine.split(" ")[0];
            	
            	switch (currentCommand) {
                case Command.CONNECTTO:
                    final String user = inputLine.split(" ", 2)[1];
                	
            		for(Entry<ClientBean, ClientRequestHandler> c : clients.entrySet())
        			{
        				if (c.getKey().getName().equals(user))
            			{
        					clientConnectedTo = c.getKey();
            			}
        			}
                    break;
                case Command.LOADUSERS:
                	sendListOfUsersToClient();
                    break;
                case Command.MSG:
                	
                	final Long msgToId = Long.parseLong(inputLine.split(" ")[1]);
                	final String msgContent = inputLine.split(" ")[3];
                	if(clientConnectedTo != null && !currentCommand.equals("CONNECTTO"))
                	{
                		ClientRequestHandler msgTo = clients.get(clientConnectedTo);
                		PrintWriter out = msgTo.outToClient;
    					out.println(inputLine);
    					
    					DbUtils utils = new DbUtils(connection);
                		utils.sendMsg(id, clientConnectedTo.getId(), 1, msgContent);
                	}
                	else
                	{
                		//offline msg
                		DbUtils utils = new DbUtils(connection);
                		utils.sendMsg(id, msgToId, 0, msgContent);
                	}
                    break;
                    
                case Command.CLOSE:
                	final String userToClose = inputLine.split(" ", 2)[1];
                	Iterator<Entry<ClientBean, ClientRequestHandler>> iter = clients.entrySet().iterator();
                	
                	while(iter.hasNext())
                	{
                		if(iter.next().getKey().getName().equals(userToClose))
                		{
//                			iter.next().getValue().outToClient.close();
//                			iter.next().getValue().inFromClient.close();
                			iter.remove();
                		}
                	}
                    break;
                default:
                    final String receivedMessage = "[" + id + "]: " + inputLine;
                    LOGGER.log(Level.INFO, receivedMessage);
                    break;
                }
            }

            outToClient.close();
            inFromClient.close();
        } catch (final IOException | SQLException e) {
            LOGGER.log(Level.INFO, "Error occured while handling request for client " + id);
            LOGGER.log(Level.INFO, e.getMessage(), e);
        }
    }
    
    private void sendListOfUsersToClient() {
    	String users = "L";
        for ( Entry<ClientBean, ClientRequestHandler> c : clients.entrySet() )
        {
        	users = users + "/" + c.getKey().getName();
        }
//        if(!users.equals("L"))
//        {
        	outToClient.println(users);
//        }
    }
    
//    private void disconnect() {
//
//        final String disconnectMessage = id + " disconnected from the server.";
//        LOGGER.log(Level.INFO, disconnectMessage);
//
//        clients.remove(id);
//    }
}