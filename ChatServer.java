import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;

public class ChatServer {

    private static final int DEFAULT_PORT = 1518;
    private final List<Connection> clients = new ArrayList<>();
    private final List<String> usernames = new ArrayList<>();
    private final JTextArea textArea = new JTextArea();
    private final JLabel clientsLabel = new JLabel("Clients connected: 0");

     public static void main(String[] args) {
        ChatServer chatServer = new ChatServer();
        chatServer.initGUI();
        chatServer.run();
    }

    private void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(DEFAULT_PORT);
            textArea.append("Server Running at localhost:" + DEFAULT_PORT + "\n");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                String name = clientSocket.getInetAddress().toString();
                Connection c = new Connection(clientSocket, name);
                synchronized(clients){
                    clients.add(c);
                    clientsLabel.setText("Clients connected: " + clients.size());
                }
                c.start();
            }
        } catch (Exception e) {
            System.err.println("Error occured creating server socket: " + e.getMessage());
        }
    }

    private class Connection extends Thread {
        Socket socket;
        public PrintWriter out;
        BufferedReader in;
        String name = "";
        public String username = "";
        public String roomId = "0";

        public Connection(Socket socket, String name) {
            this.socket = socket;
            this.name = name;
        }

        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                while (true) {
                    String line = in.readLine();
                    if(line == null) {
                        processLine("EXIT");
                        break;   
                    }
                    processLine(line);
                    textArea.append(username + ": " + line + "\n");
                }

            } catch (IOException e) {
                System.out.println("Error connecting, Terminating. " + e.getMessage());
            }finally{
                closeResources();
            }
        }

        private void closeResources(){
            try{
                synchronized(clients){
                    clients.remove(this);
                    clientsLabel.setText("Clients connected: " + clients.size());
                }
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null) socket.close();
            }catch(IOException e){
                System.out.println(e);
            }
        }

        private void processLine(String line) {
            if(line.startsWith("ENTER")){
                this.username = line.substring(line.indexOf(' ') + 1, line.length()).toUpperCase();
                messageClientsInvlusive(this.username + " has entered the room ");
            }else if(line.startsWith("EXIT")){
                closeResources();
                messageClientsExclusive(this.username + " has left the room");
            }else if(line.startsWith("JOIN")){
                messageClientsExclusive(username + " has left the room");
                roomId = line.substring(line.indexOf(' ') + 1, line.length());
                messageClientsExclusive(username + " has entered the room");
                out.println("\nYou have switched to room " + roomId);
            }else if(line.startsWith("TRANSMIT")){
                messageClientsInvlusive(this.username + ": " + line.substring(line.indexOf(' ') + 1, line.length()));
            }
        }

        private void messageClientsInvlusive(String message){
            for(Connection client : clients){
                if(client != null && client.out != null && roomId.equals(client.roomId)){
                    client.out.println(message);
                }
            }
        }

        private void messageClientsExclusive(String message){
            for(Connection client : clients){
                if(client != null && client.out != null && client.out != out && roomId.equals(client.roomId)){
                    client.out.println(message);
                }
            }
        }
    }

    private void initGUI(){
        textArea.setLineWrap(true);
        textArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(textArea);

        BorderLayout layout = new BorderLayout();
        layout.setHgap(10);
        layout.setVgap(10);

        JPanel panel = new JPanel();
        panel.setLayout(layout);  
        panel.setSize(300,600);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(clientsLabel, BorderLayout.NORTH);
        panel.setOpaque(true); 

        JFrame frame = new JFrame("Chat Server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(panel);
        frame.pack();
        frame.setVisible(true);
        frame.setSize(300, 600);
    }
}