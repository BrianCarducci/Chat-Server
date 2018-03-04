import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ChatClient {
    private String hostname;
    private int port;
    private String clientName;
    private Socket socket = null;
    private PrintWriter out = null;
    private BufferedReader in = null;
    private static JTextArea textArea = new JTextArea();


    public ChatClient(String hostname, int port, String clientName) {
        this.hostname = hostname;
        this.port = port;
        this.clientName = clientName;
    }

    public void run() {
        try {
            socket = new Socket(hostname, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            textArea.append(clientName + " has just connected to the server\n");
            out.println(clientName + " has just connected to the server");
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            while(!socket.isClosed()){
                String line = in.readLine();
                if(line == null) break;
                System.out.println(line);
            }

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
            
            JFrame frame = new JFrame("Chat Client");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(panel);
            frame.pack();
            frame.setVisible(true);
            frame.setSize(300, 600);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
            Rectangle rect = defaultScreen.getDefaultConfiguration().getBounds();
            int x = (int) rect.getMaxX() - frame.getWidth();
            int y = 0;
            frame.setLocation(x, y);
        });

        ChatClient chatClient = new ChatClient(hostname, port, clientName);
        chatClient.run();
    }
}

