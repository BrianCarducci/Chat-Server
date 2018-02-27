import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.net.*;

public class ChatClient {
    private String hostname;
    private int port;
    private String clientName;
    private Socket socket = null;
    private PrintWriter out = null;
    private BufferedReader in = null;

    public ChatClient(String hostname, int port, String clientName) {
        this.hostname = hostname;
        this.port = port;
        this.clientName = clientName;
    }

    public void run() {
        try {

            socket = new Socket(hostname, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        } catch (UnknownHostException e) {
            System.out.println("Unkown host: " + hostname);
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println("IO Error: Error establishing communication with server.");
            System.out.println(e.getMessage());
        }

        try {
            if (out != null) out.close();
	        if (in != null) in.close();
	        if (socket != null) socket.close();
	    } catch (IOException e) {
	        System.out.println("Error closing the streams.");
	    }
    }

    public static void main(String[] args) {
        String hostname = args[0];
        int port = Integer.parseInt(args[1]);
        String clientName = args[2];

        ChatClient chatClient = new ChatClient(hostname, port, clientName);
        chatClient.run();
    }
}

