


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Entry point of the server application
 *
 */
public final class Server {

    private static final int SERVER_PORT = 6543;
    private static final Map<ClientBean, ClientRequestHandler> clients = new LinkedHashMap<>();
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    public static void main(final String[] args) {
        try (final ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            LOGGER.log(Level.INFO, "Server started.");

            Socket clientSocket;
            while (true) {
                clientSocket = serverSocket.accept();
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String line =  inFromClient.readLine();
                String name = line.split(" ")[0];
                Long id = Long.parseLong(line.split(" ")[1]);
                
                final ClientBean clientBean = generateClientBean(name, id);
                final ClientRequestHandler clientHandler = new ClientRequestHandler(clients, clientSocket, clientBean.getId());
                clients.put(clientBean, clientHandler);
                clientHandler.start();
                LOGGER.log(Level.INFO, clientBean.getName() + " connected to the server.");
            }
        } catch (final IOException e) {
            LOGGER.log(Level.INFO, "Error occured while starting server. Server stopped.");
            LOGGER.log(Level.INFO, e.getMessage(), e);
        }
    }

    private static ClientBean generateClientBean(String name, Long id) {
//        Long id = 1l;
//        for(Entry<ClientBean, ClientRequestHandler> c : clients.entrySet())
//        {
//        	if(c.getKey().getId().equals(id))
//        	{
//        		id++;
//        	}
//        }

        ClientBean bean = new ClientBean(id, name);
        return bean;
    }
}