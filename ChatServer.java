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

    public static final int DEFAULT_PORT = 1518;
    public int port;
    public boolean done;
    public List<PrintWriter> clients = new ArrayList<>();
    public List<String> usernames = new ArrayList<>();
    static JTextArea textArea = new JTextArea();


    public ChatServer(int port) {
    	this.port = port;
    	this.done = false;
    }

    public void addConnection(Socket clientSocket) {
    	String name = clientSocket.getInetAddress().toString();
    	System.out.println("chat server connecting to: " + name);
    	Connection c = new Connection(clientSocket, name);
    	c.start();
    }

    public void run() {
        textArea.append("Server Running...\n");
        try {
          ServerSocket serverSocket = new ServerSocket(port);

          while (!done) {
             Socket clientSocket = serverSocket.accept();
             String name = clientSocket.getInetAddress().toString();
             System.out.println("chat server connecting to: " + name);
             Connection c = new Connection(clientSocket, name);
             c.start();
         }
     } catch (Exception e) {
      System.err.println("Error occured creating server socket: " + e.getMessage());
    		//System.exit(1);
  }
}

class Connection extends Thread {
    Socket socket;
    PrintWriter out;
    BufferedReader in;
    boolean done;
    String name;

    public Connection(Socket socket, String name) {
        this.socket = socket;
        this.name = name;
        done = false;
    }

    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));


            
            synchronized(clients){
                clients.add(out);
            }

            while (!done) {
                String line = in.readLine();
                if(line == null) break;
                textArea.append(line + "\n");
                processLine(line);
            }

        } catch (IOException e) {
            System.out.println("Error connecting, Terminating. " + e.getMessage());
        }finally{
            try{
                System.out.println("shutting down");
                if (in != null) in.close();
                if (out != null){
                    synchronized(clients){
                        clients.remove(out);
                    }
                    out.close();
                }
                if (socket != null) socket.close();
            }catch(IOException e){
                System.out.println(e);
            }
        }
    }

    public void processProtocol(String protocol) {

        protocol = protocol.substring(0, protocol.indexOf(' '));
        System.out.println(protocol);
        switch (protocol) {
            case "ENTER": ;
                break;
            case "EXIT": ;
                break;
            case "JOIN": ;
                break;
            case "TRANSMIT": ;
                break;
        }
    }

    public void processLine(String line) {
        int splitIndex = line.indexOf(' ');
        String protocol = line.substring(0, splitIndex);
        String message = line.substring(splitIndex, line.length());
        System.out.println(line);
        switch (protocol) {
            case "ENTER": ;
                break;
            case "EXIT": ;
                break;
            case "JOIN": ;
                break;
            case "TRANSMIT": ;
                break;
        }

        for(PrintWriter client : clients){
            client.println(message);
        }

    }
}


public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
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
        panel.setOpaque(true); 

        JFrame frame = new JFrame("Chat Server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(panel);
        frame.pack();
        frame.setVisible(true);
        frame.setSize(300, 600);



    });

    ChatServer chatServer = new ChatServer(DEFAULT_PORT);
    chatServer.run();
}
}