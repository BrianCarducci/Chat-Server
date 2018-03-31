import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;

public class ChatServer {
    // default port for the server
    private static final int DEFAULT_PORT = 1518;
    // list of the clients who have an established socket connection
    private final List<Connection> connectedClients = new ArrayList<>();
    // log where all in and out transmissions will be displayed
    private final JTextArea transmissionLog = new JTextArea();
    // label that displays how many clients are connected
    private final JLabel numberOfClientsLabel = new JLabel("Clients connected: 0");

    // initialize the gui and server socket
    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer();
        chatServer.initGUI();
        chatServer.initServerSocket();
    }

    private void initGUI(){
        transmissionLog.setLineWrap(true);
        transmissionLog.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(transmissionLog);
        /// use a border layout for entire gui
        BorderLayout layout = new BorderLayout();
        layout.setHgap(10);
        layout.setVgap(10);
        // default jpanel which will house our components
        JPanel panel = new JPanel();
        panel.setLayout(layout);  
        panel.setSize(300,600);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(numberOfClientsLabel, BorderLayout.NORTH);
        panel.setOpaque(true); 
        // initialize jframe
        JFrame frame = new JFrame("Chat Server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(panel);
        frame.pack();
        frame.setVisible(true);
        frame.setSize(300, 600);
    }

    // 1. Try to create a server socket on the default port
    // 2a. Start an infinite loop where the socket can accept incoming
    //    client connection requests.
    // 2b. When a new client is created on a new thread, we must synchronize our
    //     list of clients before we add the new client to the list
    private void initServerSocket() {
        try {
            ServerSocket serverSocket = new ServerSocket(DEFAULT_PORT);
            log("Server Running at localhost:" + DEFAULT_PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                Connection clientConnection = new Connection(clientSocket);
                synchronized(connectedClients){
                    connectedClients.add(clientConnection);
                    numberOfClientsLabel.setText("Clients connected: " + connectedClients.size());
                }
                clientConnection.start();
            }
        } catch (Exception e) {
            System.err.println("Error occured creating server socket: " + e.getMessage());
        }
    }

    // Private inner class so that we can process each connection on a separate thread
    private class Connection extends Thread {
        protected Socket socket;
        // used for writing to client
        protected PrintWriter serverOutput;
        // user for reading from client
        protected BufferedReader clientInput;
        // default room is 0
        protected String roomId = "0";
        protected String username = "";
        protected boolean isConnected = true;

        protected Connection(Socket socket) {
            this.socket = socket;
        }

        // While the connection is alive:
        // 1. Read client input and process the transmission protocol
        // 2. If the input was null something went wrong. Send an exit tranmission
        //    to ackowledge the client has disconnected, and close the resources
        public void run() {
            try {
                serverOutput = new PrintWriter(socket.getOutputStream(), true);
                clientInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                while (isConnected) {
                    String clientInputLine = clientInput.readLine();
                    if(clientInputLine == null) {
                        processClientInput("EXIT");
                    }else{
                        processClientInput(clientInputLine);
                    }
                }

            } catch (IOException e) {
                System.out.println("Error connecting, Terminating. " + e.getMessage());
            }finally{
                try{
                    synchronized(connectedClients){
                        connectedClients.remove(this);
                        numberOfClientsLabel.setText("Clients connected: " + connectedClients.size());
                    }
                    if (clientInput != null) clientInput.close();
                    if (serverOutput != null) serverOutput.close();
                    if (socket != null) socket.close();
                }catch(IOException e){
                    System.out.println(e);
                }
            }
        }

        // process all incoming lines from clients. Read the swtich case to understand
        // the logic. substring and indexOf are used to parse the input and separate the
        // protocol from the message.
        private void processClientInput(String clientInputLine) {
            log("in: " + clientInputLine);
            int indexOfProtocolContentSplit = clientInputLine.indexOf(' ');
            String protocol = clientInputLine.substring(0, indexOfProtocolContentSplit >= 0 ? indexOfProtocolContentSplit : clientInputLine.length());
            String content = clientInputLine.substring(indexOfProtocolContentSplit + 1, clientInputLine.length());
            switch(protocol){
                case "ENTER" : {
                    username = content;
                    serverOutput.println("ACK ENTER " + username);
                    broadcastMessage("ENTERING " + username);
                    break;
                }
                case "EXIT" : {
                    broadcastMessage("EXITING " + username);
                    isConnected = false;
                    break;
                }
                case "JOIN" : {
                    broadcastMessage("EXITING " + username);
                    roomId = content;
                    serverOutput.println("ACK JOIN " + roomId);
                    broadcastMessage("ENTERING " + username);
                    break;
                }
                case "TRANSMIT" : {
                    broadcastMessage("NEWMESSAGE " + username + " " + content);
                    break;
                }
                default: break;
            }
        }

        // method used to broadcast messages to all the clients
        private void broadcastMessage(String message){
            // log the outgoing transmissions
            log("out: " + message);
            for(Connection client : connectedClients){
                if(client != null && client.serverOutput != null && roomId.equals(client.roomId)){
                    client.serverOutput.println(message);
                }
            }
        }
    }

    // used to log all transmissions
    private void log(String transmission){
        transmissionLog.append(transmission + "\n");
    }
}