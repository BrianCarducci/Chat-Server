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
    private static final int DEFAULT_PORT = 1518;
    private final List<Connection> connectedClients = new ArrayList<>();
    private final JTextArea transmissionLog = new JTextArea();
    private final JLabel numberOfClientsLabel = new JLabel("Clients connected: 0");

    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer();
        chatServer.initGUI();
        chatServer.initServerSocket();
    }

    private void initGUI(){
        transmissionLog.setLineWrap(true);
        transmissionLog.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(transmissionLog);

        BorderLayout layout = new BorderLayout();
        layout.setHgap(10);
        layout.setVgap(10);

        JPanel panel = new JPanel();
        panel.setLayout(layout);  
        panel.setSize(300,600);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(numberOfClientsLabel, BorderLayout.NORTH);
        panel.setOpaque(true); 

        JFrame frame = new JFrame("Chat Server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(panel);
        frame.pack();
        frame.setVisible(true);
        frame.setSize(300, 600);
    }

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

    private class Connection extends Thread {
        protected Socket socket;
        protected PrintWriter output;
        protected BufferedReader input;
        protected String username = "";
        protected String roomId = "0";
        protected boolean isConnected = true;

        protected Connection(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                output = new PrintWriter(socket.getOutputStream(), true);
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                while (isConnected) {
                    String inputLine = input.readLine();
                    if(inputLine == null) {
                        processLine("EXIT");
                    }else{
                        processLine(inputLine);
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
                    if (input != null) input.close();
                    if (output != null) output.close();
                    if (socket != null) socket.close();
                }catch(IOException e){
                    System.out.println(e);
                }
            }
        }

        private void processLine(String inputLine) {
            log("in: " + inputLine);
            int indexOfProtocolContentSplit = inputLine.indexOf(' ');
            String protocol = inputLine.substring(0, indexOfProtocolContentSplit >= 0 ? indexOfProtocolContentSplit : inputLine.length());
            String content = inputLine.substring(indexOfProtocolContentSplit + 1, inputLine.length());
            switch(protocol){
                case "ENTER" : {
                    username = content;
                    output.println("ACK ENTER " + username);
                    messageClients("ENTERING " + username);
                    break;
                }
                case "EXIT" : {
                    messageClients("EXITING " + username);
                    isConnected = false;
                    break;
                }
                case "JOIN" : {
                    messageClients("EXITING " + username);
                    roomId = content;
                    output.println("ACK JOIN " + roomId);
                    messageClients("ENTERING " + username);
                    break;
                }
                case "TRANSMIT" : {
                    messageClients("NEWMESSAGE " + username + " " + content);
                    break;
                }
                default: break;
            }
        }

        private void messageClients(String message){
            log("out: " + message);
            for(Connection client : connectedClients){
                if(client != null && client.output != null && roomId.equals(client.roomId)){
                    client.output.println(message);
                }
            }
        }
    }

    private void log(String transmission){
        transmissionLog.append(transmission + "\n");
    }
}