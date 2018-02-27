import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;

public class ChatServer {

    public static final int DEFAULT_PORT = 1518;
    public int port;
    public boolean done;
    public HashSet<Connection> connection;

    public ChatServer(int port) {
    	this.port = port;
    	this.done = false;
    	this.connection = new HashSet<Connection>();
    }

    public void addConnection(Socket clientSocket) {
    	String name = clientSocket.getInetAddress().toString();
    	System.out.println("chat server connecting to: " + name);
    	Connection c = new Connection(clientSocket, name);
    	connection.add(c);
    	c.start();
    }

    public void run() {
    	System.out.println("Chat server running");
    	try {
    		ServerSocket serverSocket = new ServerSocket(port);

    		while (!done) {
    			System.out.println("in loop");
    			Socket clientSocket = serverSocket.accept();
    			System.out.println("accepted");
    			addConnection(clientSocket);
    			System.out.println("added");
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
                out.println("running...");

                while (!done) {
                    String line = in.readLine();
                    processLine(line);
                }

                } catch (IOException e) {
                    System.out.println("Error connecting, Terminating. " + e.getMessage());

            }
            try {
                System.out.println("shutting down");
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                System.out.println("error trying to close socket." + e.getMessage());
            }
            }

        public void processLine(String line) {
            if (line != null) {
                out.print("gotcha buddy, your message: " + line);
            }
        }
    }


    public static void main(String[] args) {
        System.out.println("kk");
        ChatServer chatServer = new ChatServer(DEFAULT_PORT);
        chatServer.run();
    }
}